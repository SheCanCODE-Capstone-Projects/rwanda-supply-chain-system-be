package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId);
}
