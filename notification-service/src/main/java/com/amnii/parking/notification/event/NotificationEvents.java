package com.amnii.parking.notification.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NotificationEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public static class UserRegisteredEvent {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private String eventType;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public static class CarEnteredEvent {
        private String entryId;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private String ticketNumber;
        private LocalDateTime entryDatetime;
        private BigDecimal feePerHour;
        private String eventType;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public static class CarExitedEvent {
        private String entryId;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private String ticketNumber;
        private LocalDateTime entryDatetime;
        private LocalDateTime exitDatetime;
        private String duration;
        private BigDecimal chargedAmount;
        private String currency;
        private String eventType;
    }
}
