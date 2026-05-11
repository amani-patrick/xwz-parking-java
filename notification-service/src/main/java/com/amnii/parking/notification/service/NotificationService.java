package com.amnii.parking.notification.service;

import com.amnii.parking.notification.event.NotificationEvents.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${notification.mail.from:noreply@xwzparking.rw}")
    private String mailFrom;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── User Registered ───────────────────────────────────────────────────────

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
        log.info("[NOTIFICATION] Subject: {}", subject);
        log.info("[NOTIFICATION] Body:\n{}", body);

        sendEmail(event.getEmail(), subject, body);
    }

    // ── Car Entered ───────────────────────────────────────────────────────────

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
        log.info("[NOTIFICATION] Ticket:\n{}", body);

        // In production send to driver's registered email / SMS
        // sendEmail(driverEmail, subject, body);
    }

    // ── Car Exited ────────────────────────────────────────────────────────────

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
        log.info("[NOTIFICATION] Bill:\n{}", body);

        // In production send to driver's registered email / SMS
        // sendEmail(driverEmail, subject, body);
    }

    // ── Email helper ──────────────────────────────────────────────────────────

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
