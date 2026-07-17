package org.example.rwandasupplychain.DTOs;

import org.example.rwandasupplychain.Enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDtos {

    public record NotificationResponse(
            UUID id,
            UUID recipientId,
            NotificationType type,
            String title,
            String message,
            UUID referenceId,
            boolean read,
            LocalDateTime createdAt
    ) {}
}
