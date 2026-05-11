package com.amnii.parking.reportservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parkings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parking {

    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "total_spaces")
    private int totalSpaces;

    @Column(name = "available_spaces")
    private int availableSpaces;

    @Column(name = "location")
    private String location;

    @Column(name = "fee_per_hour")
    private BigDecimal feePerHour;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
