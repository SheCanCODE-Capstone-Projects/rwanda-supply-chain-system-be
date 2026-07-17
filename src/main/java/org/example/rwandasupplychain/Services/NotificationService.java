package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.NotificationDtos.NotificationResponse;
import org.example.rwandasupplychain.Entities.Notification;
import org.example.rwandasupplychain.Enums.NotificationType;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public NotificationResponse notify(UUID recipientId, NotificationType type, String title, String message, UUID referenceId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);

        NotificationResponse response = toResponse(notificationRepository.save(notification));
        messagingTemplate.convertAndSend("/topic/notifications." + recipientId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(UUID recipientId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId)
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        return notifications.stream().map(this::toResponse).toList();
    }

    public NotificationResponse markRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
