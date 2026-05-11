package com.amnii.parking.reportservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "car_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarEntry {

    @Id
    private UUID id;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "parking_code")
    private String parkingCode;

    @Column(name = "entry_datetime")
    private LocalDateTime entryDatetime;

    @Column(name = "exit_datetime")
    private LocalDateTime exitDatetime;

    @Column(name = "charged_amount")
    private BigDecimal chargedAmount;

    @Column(name = "ticket_number")
    private String ticketNumber;

    @Column(name = "attendant_id")
    private UUID attendantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Status { PARKED, EXITED }
}
