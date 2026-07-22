package org.example.rwandasupplychain.Repositories.InvetoryRepositories;

import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.AlertType;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Notification;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationChannel;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    boolean existsByAlertTypeAndReferenceIdAndChannelAndStatusAndCreatedAtAfter(
            AlertType alertType,
            UUID referenceId,
            NotificationChannel channel,
            NotificationStatus status,
            LocalDateTime createdAtAfter
    );

    List<Notification> findByChannelOrderByCreatedAtDesc(NotificationChannel channel);

    List<Notification> findByChannelAndReadFalseOrderByCreatedAtDesc(NotificationChannel channel);

    long countByChannelAndReadFalse(NotificationChannel channel);
}