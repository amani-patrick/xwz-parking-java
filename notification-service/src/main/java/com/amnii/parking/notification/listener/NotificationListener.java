package com.amnii.parking.notification.listener;

import com.amnii.parking.notification.config.RabbitMQConfig;
import com.amnii.parking.notification.event.NotificationEvents.*;
import com.amnii.parking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    /**
     * Listens for user registration events from auth-service.
     * Sends a welcome notification.
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_USER_REGISTERED)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("[LISTENER] Received USER_REGISTERED event for {}", event.getEmail());
        try {
            notificationService.handleUserRegistered(event);
        } catch (Exception e) {
            log.error("[LISTENER] Error handling USER_REGISTERED for {}: {}",
                    event.getEmail(), e.getMessage());
            // Message acknowledged — don't re-queue to avoid poison pill
        }
    }

    /**
     * Listens for car entry events from entry-service.
     * Logs ticket info and can send SMS/email to driver.
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_CAR_ENTERED)
    public void onCarEntered(CarEnteredEvent event) {
        log.info("[LISTENER] Received CAR_ENTERED event: plate={} ticket={}",
                event.getPlateNumber(), event.getTicketNumber());
        try {
            notificationService.handleCarEntered(event);
        } catch (Exception e) {
            log.error("[LISTENER] Error handling CAR_ENTERED for ticket {}: {}",
                    event.getTicketNumber(), e.getMessage());
        }
    }

    /**
     * Listens for car exit events from entry-service.
     * Logs bill info and can send payment receipt.
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIF_CAR_EXITED)
    public void onCarExited(CarExitedEvent event) {
        log.info("[LISTENER] Received CAR_EXITED event: plate={} ticket={} charged={} {}",
                event.getPlateNumber(), event.getTicketNumber(),
                event.getChargedAmount(), event.getCurrency());
        try {
            notificationService.handleCarExited(event);
        } catch (Exception e) {
            log.error("[LISTENER] Error handling CAR_EXITED for ticket {}: {}",
                    event.getTicketNumber(), e.getMessage());
        }
    }
}
