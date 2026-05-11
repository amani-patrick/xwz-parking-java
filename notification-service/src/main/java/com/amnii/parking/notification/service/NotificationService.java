package com.amnii.parking.notification.service;

import com.amnii.parking.notification.entity.Notification;
import com.amnii.parking.notification.entity.NotificationPreference;
import com.amnii.parking.notification.event.NotificationEvents.*;
import com.amnii.parking.notification.repository.NotificationPreferenceRepository;
import com.amnii.parking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @Value("${notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${notification.mail.from:noreply@xwzparking.rw}")
    private String mailFrom;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── User Registered ───────────────────────────────────────────────────────

    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        String subject = "Welcome to XWZ Parking, " + event.getFirstName() + "!";
        String body = String.format("""
                Dear %s %s,
                
                Your account has been successfully created on the XWZ Parking Management System.
                
                Account Details:
                  Email : %s
                  Role  : %s
                
                You can now log in and start using the system.
                
                Best regards,
                XWZ Parking Team — Kigali, Rwanda
                """,
                event.getFirstName(), event.getLastName(),
                event.getEmail(), event.getRole());

        log.info("[NOTIFICATION] USER_REGISTERED | email={} role={}", event.getEmail(), event.getRole());
        
        processNotification(event.getEmail(), subject, body, "USER_REGISTERED");
    }

    // ── Car Entered ───────────────────────────────────────────────────────────

    @Transactional
    public void handleCarEntered(CarEnteredEvent event) {
        String subject = "Parking Ticket — " + event.getTicketNumber();
        String body = String.format("""
                ╔══════════════════════════════════════╗
                ║      XWZ PARKING — ENTRY TICKET      ║
                ╚══════════════════════════════════════╝
                
                Ticket No  : %s
                Plate      : %s
                Parking    : %s (%s)
                Entry Time : %s
                Rate       : %s RWF/hr
                
                Please keep this ticket. You will need it on exit.
                
                XWZ Parking — Kigali, Rwanda
                """,
                event.getTicketNumber(),
                event.getPlateNumber(),
                event.getParkingCode(), event.getParkingName(),
                event.getEntryDatetime() != null ? event.getEntryDatetime().format(FMT) : "N/A",
                event.getFeePerHour());

        log.info("[NOTIFICATION] CAR_ENTERED | plate={} parking={} ticket={}",
                event.getPlateNumber(), event.getParkingCode(), event.getTicketNumber());

        processNotification("plate:" + event.getPlateNumber(), subject, body, "CAR_ENTERED");
    }

    // ── Car Exited ────────────────────────────────────────────────────────────

    @Transactional
    public void handleCarExited(CarExitedEvent event) {
        String subject = "Parking Bill — " + event.getTicketNumber();
        String body = String.format("""
                ╔══════════════════════════════════════╗
                ║       XWZ PARKING — PARKING BILL     ║
                ╚══════════════════════════════════════╝
                
                Ticket No  : %s
                Plate      : %s
                Parking    : %s (%s)
                Entry Time : %s
                Exit Time  : %s
                Duration   : %s
                Amount Due : %s %s
                
                Thank you for using XWZ Parking!
                
                XWZ Parking — Kigali, Rwanda
                """,
                event.getTicketNumber(),
                event.getPlateNumber(),
                event.getParkingCode(), event.getParkingName(),
                event.getEntryDatetime() != null ? event.getEntryDatetime().format(FMT) : "N/A",
                event.getExitDatetime()  != null ? event.getExitDatetime().format(FMT)  : "N/A",
                event.getDuration(),
                event.getChargedAmount(), event.getCurrency());

        log.info("[NOTIFICATION] CAR_EXITED | plate={} ticket={} charged={} {}",
                event.getPlateNumber(), event.getTicketNumber(),
                event.getChargedAmount(), event.getCurrency());

        processNotification("plate:" + event.getPlateNumber(), subject, body, "CAR_EXITED");
    }

    // ── Core Processing Logic ────────────────────────────────────────────────

    private void processNotification(String recipient, String title, String message, String type) {
        NotificationPreference prefs = preferenceRepository.findById(recipient)
                .orElse(NotificationPreference.builder().email(recipient).build());

        if (prefs.isInAppEnabled()) {
            saveInAppNotification(recipient, title, message, type);
        }

        if (prefs.isEmailEnabled() && !recipient.startsWith("plate:")) {
            sendEmail(recipient, title, message);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void saveInAppNotification(String recipient, String title, String message, String type) {
        try {
            Notification notification = Notification.builder()
                    .recipientEmail(recipient)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.info("[NOTIFICATION] Saved in-app notification for {}", recipient);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to save in-app notification: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[NOTIFICATION] Mail disabled — would have sent to: {}", to);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[NOTIFICATION] Email sent to {}", to);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
