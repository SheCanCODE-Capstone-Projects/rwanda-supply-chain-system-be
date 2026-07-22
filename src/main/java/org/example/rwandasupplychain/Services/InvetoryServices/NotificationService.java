package org.example.rwandasupplychain.Services.InvetoryServices;

import org.example.rwandasupplychain.DTOs.InvertoryManagementDtos.NotificationDtos.NotificationResponse;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Notification;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationChannel;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.InvetoryRepositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final InventoryAlertNotificationJob alertNotificationJob;

    public NotificationService(NotificationRepository notificationRepository,
                               InventoryAlertNotificationJob alertNotificationJob) {
        this.notificationRepository = notificationRepository;
        this.alertNotificationJob = alertNotificationJob;
    }

    public List<NotificationResponse> getInbox(boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByChannelAndReadFalseOrderByCreatedAtDesc(NotificationChannel.PUSH)
                : notificationRepository.findByChannelOrderByCreatedAtDesc(NotificationChannel.PUSH);
        return notifications.stream().map(this::toResponse).toList();
    }

    public long countUnread() {
        return notificationRepository.countByChannelAndReadFalse(NotificationChannel.PUSH);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void runAlertChecksNow() {
        alertNotificationJob.runAllChecksNow();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getAlertType(),
                n.getReferenceId(),
                n.getChannel(),
                n.getStatus(),
                n.getTitle(),
                n.getMessage(),
                n.getRecipient(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}