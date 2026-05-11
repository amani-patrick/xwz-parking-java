package com.amnii.parking.entryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "car_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    @Column(name = "parking_code", nullable = false, length = 20)
    private String parkingCode;

    @Column(name = "entry_datetime", nullable = false)
    private LocalDateTime entryDatetime;

    @Column(name = "exit_datetime")
    private LocalDateTime exitDatetime;

    @Column(name = "charged_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal chargedAmount = BigDecimal.ZERO;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Column(name = "attendant_id")
    private UUID attendantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PARKED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status { PARKED, EXITED }
}
