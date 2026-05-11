package com.amnii.parking.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification service status")
public class NotificationController {

    @GetMapping("/status")
    @Operation(summary = "Get notification service status and subscribed queues")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "service", "notification-service",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "subscribedQueues", new String[]{
                        "xwz.notif.user.registered",
                        "xwz.notif.car.entered",
                        "xwz.notif.car.exited"
                },
                "description", "Listens to RabbitMQ events and sends notifications"
        ));
    }
}
