package com.amnii.parking.entryservice.messaging;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntryEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CarEnteredEvent {
        private String entryId;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private String ticketNumber;
        private LocalDateTime entryDatetime;
        private BigDecimal feePerHour;
        private String eventType = "CAR_ENTERED";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
        private String currency = "RWF";
        private String eventType = "CAR_EXITED";
    }
}
