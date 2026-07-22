package org.example.rwandasupplychain.DTOs.InvertoryManagementDtos;

import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.AlertType;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationChannel;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationStatus;

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