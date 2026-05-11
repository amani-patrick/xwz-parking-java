package com.amnii.parking.parkingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parkings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "total_spaces", nullable = false)
    private int totalSpaces;

    @Column(name = "available_spaces", nullable = false)
    private int availableSpaces;

    @Column(nullable = false, length = 500)
    private String location;

    @Column(name = "fee_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal feePerHour;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
