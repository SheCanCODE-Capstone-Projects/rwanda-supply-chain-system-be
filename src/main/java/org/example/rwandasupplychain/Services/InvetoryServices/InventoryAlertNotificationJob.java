package org.example.rwandasupplychain.Services.InvetoryServices;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.InventoryAlertDtos.ExpiringBatchAlert;
import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.InventoryAlertDtos.LowStockAlert;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.AlertType;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Notification;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationChannel;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationStatus;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
public class InventoryAlertNotificationJob {

    private static final Logger log = LoggerFactory.getLogger(InventoryAlertNotificationJob.class);

    private final InventoryAlertService inventoryAlertService;
    private final BrevoEmailService emailService;
    private final NotificationRepository notificationRepository;

    private final List<String> recipientEmails;
    private final int expiryDaysAhead;

    public InventoryAlertNotificationJob(InventoryAlertService inventoryAlertService,
                                         BrevoEmailService emailService,
                                         NotificationRepository notificationRepository,
                                         @Value("${app.alerts.recipient-emails:}") String recipientEmailsCsv,
                                         @Value("${app.alerts.expiry-days-ahead:14}") int expiryDaysAhead) {
        this.inventoryAlertService = inventoryAlertService;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.recipientEmails = Arrays.stream(recipientEmailsCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        this.expiryDaysAhead = expiryDaysAhead;
    }

    @Scheduled(cron = "${app.alerts.low-stock-cron:0 0 7 * * *}")
    @Transactional
    public void runLowStockCheck() {
        List<LowStockAlert> alerts = inventoryAlertService.getLowStockAlerts();
        log.info("Low-stock sweep found {} SKU(s) at or below threshold", alerts.size());
        for (LowStockAlert alert : alerts) {
            processLowStockAlert(alert);
        }
    }

    /**
     * Near-expiry / expired sweep. Default: every day at 07:30 server time.
     * Override with app.alerts.expiry-cron.
     */
    @Scheduled(cron = "${app.alerts.expiry-cron:0 30 7 * * *}")
    @Transactional
    public void runExpiryCheck() {
        List<ExpiringBatchAlert> expiring = inventoryAlertService.getExpiringBatches(expiryDaysAhead);
        List<ExpiringBatchAlert> expired = inventoryAlertService.getExpiredBatches();
        log.info("Expiry sweep found {} batch(es) expiring within {} day(s) and {} already expired",
                expiring.size(), expiryDaysAhead, expired.size());

        for (ExpiringBatchAlert alert : expiring) {
            processExpiryAlert(alert, AlertType.EXPIRING_BATCH);
        }
        for (ExpiringBatchAlert alert : expired) {
            processExpiryAlert(alert, AlertType.EXPIRED_BATCH);
        }
    }

    /**
     * Runs both sweeps immediately. Exposed for manual/on-demand triggering
     * (e.g. an admin "Check now" button) instead of waiting for the cron schedule.
     */
    @Transactional
    public void runAllChecksNow() {
        runLowStockCheck();
        runExpiryCheck();
    }

    private void processLowStockAlert(LowStockAlert alert) {
        if (alreadyNotifiedToday(AlertType.LOW_STOCK, alert.skuId(), NotificationChannel.PUSH)) {
            return;
        }

        String title = "Low stock: " + alert.productName() + " (" + alert.skuCode() + ")";
        String message = "Current stock is " + alert.currentStock() + ", at or below the threshold of "
                + alert.lowStockThreshold() + ".";
        String html = "<p>SKU <strong>" + alert.skuCode() + "</strong> (" + alert.productName() + ") is low on stock.</p>"
                + "<p>Current quantity: <strong>" + alert.currentStock() + "</strong><br/>"
                + "Threshold: " + alert.lowStockThreshold() + "</p>"
                + "<p>Please arrange replenishment.</p>";

        dispatch(AlertType.LOW_STOCK, alert.skuId(), title, message, html);
    }

    private void processExpiryAlert(ExpiringBatchAlert alert, AlertType alertType) {
        if (alreadyNotifiedToday(alertType, alert.batchId(), NotificationChannel.PUSH)) {
            return;
        }

        boolean expired = alertType == AlertType.EXPIRED_BATCH;
        String title = (expired ? "Expired batch: " : "Expiring soon: ")
                + alert.skuCode() + " / batch " + alert.batchNo();
        String message = expired
                ? "Batch " + alert.batchNo() + " (" + alert.skuCode() + ") expired on " + alert.expiryDate()
                  + ". Quantity: " + alert.quantity() + "."
                : "Batch " + alert.batchNo() + " (" + alert.skuCode() + ") expires in "
                  + alert.daysUntilExpiry() + " day(s), on " + alert.expiryDate()
                  + ". Quantity: " + alert.quantity() + ".";
        String html = "<p>" + message + "</p><p>Please review and prioritize this batch for distribution or disposal.</p>";

        dispatch(alertType, alert.batchId(), title, message, html);
    }

    private void dispatch(AlertType alertType, UUID referenceId, String title, String message, String htmlBody) {
        boolean emailSent = emailService.sendEmail(recipientEmails, title, htmlBody);
        saveNotification(alertType, referenceId, NotificationChannel.EMAIL,
                emailSent ? NotificationStatus.SENT : (emailService.isConfigured() ? NotificationStatus.FAILED : NotificationStatus.SKIPPED),
                title, message, String.join(",", recipientEmails));

        // In-app / push notification: always recorded so the dashboard can show it,
        // regardless of whether the email provider is configured.
        saveNotification(alertType, referenceId, NotificationChannel.PUSH,
                NotificationStatus.SENT, title, message, "INVENTORY_MANAGERS");
    }

    private boolean alreadyNotifiedToday(AlertType alertType, UUID referenceId, NotificationChannel channel) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return notificationRepository.existsByAlertTypeAndReferenceIdAndChannelAndStatusAndCreatedAtAfter(
                alertType, referenceId, channel, NotificationStatus.SENT, startOfDay);
    }

    private void saveNotification(AlertType alertType, UUID referenceId, NotificationChannel channel,
                                  NotificationStatus status, String title, String message, String recipient) {
        Notification notification = new Notification();
        notification.setAlertType(alertType);
        notification.setReferenceId(referenceId);
        notification.setChannel(channel);
        notification.setStatus(status);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notificationRepository.save(notification);
    }
}