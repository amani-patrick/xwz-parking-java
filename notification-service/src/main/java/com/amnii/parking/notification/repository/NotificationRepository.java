package com.amnii.parking.notification.repository;

import com.amnii.parking.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email, Pageable pageable);
    long countByRecipientEmailAndIsReadFalse(String email);
}
