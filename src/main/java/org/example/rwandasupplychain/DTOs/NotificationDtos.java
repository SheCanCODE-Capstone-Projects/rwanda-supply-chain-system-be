package org.example.rwandasupplychain.DTOs;

import org.example.rwandasupplychain.Entities.AlertType;
import org.example.rwandasupplychain.Entities.NotificationChannel;
import org.example.rwandasupplychain.Entities.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDtos {

    public record NotificationResponse(
            UUID id,
            AlertType alertType,
            UUID referenceId,
            NotificationChannel channel,
            NotificationStatus status,
            String title,
            String message,
            String recipient,
            boolean read,
            LocalDateTime createdAt
    ) {}
}