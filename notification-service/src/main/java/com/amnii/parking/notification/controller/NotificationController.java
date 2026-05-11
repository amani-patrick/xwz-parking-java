package com.amnii.parking.notification.controller;

import com.amnii.parking.notification.entity.Notification;
import com.amnii.parking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public Page<Notification> getMyNotifications(
            @RequestHeader("X-User-Email") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email, PageRequest.of(page, size));
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestHeader("X-User-Email") String email) {
        return notificationRepository.countByRecipientEmailAndIsReadFalse(email);
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
