package com.amnii.parking.parkingservice.dto;

import com.amnii.parking.parkingservice.entity.Parking;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ParkingDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ParkingRequest {
        @NotBlank(message = "Parking code is required")
        @Pattern(regexp = "^[A-Z0-9]{2,20}$", message = "Code must be 2-20 uppercase alphanumeric characters")
        private String code;

        @NotBlank(message = "Parking name is required")
        @Size(min = 3, max = 255, message = "Name must be 3-255 characters")
        private String name;

        @NotNull(message = "Total spaces is required")
        @Min(value = 1, message = "Total spaces must be at least 1")
        private Integer totalSpaces;

        @NotBlank(message = "Location is required")
        @Size(min = 3, max = 500, message = "Location must be 3-500 characters")
        private String location;

        @NotNull(message = "Fee per hour is required")
        @DecimalMin(value = "0.0", message = "Fee per hour cannot be negative")
        private BigDecimal feePerHour;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ParkingResponse {
        private UUID id;
        private String code;
        private String name;
        private int totalSpaces;
        private int availableSpaces;
        private int occupiedSpaces;
        private String occupancyRate;
        private String location;
        private BigDecimal feePerHour;
        private boolean isActive;
        private LocalDateTime createdAt;

        public static ParkingResponse from(Parking p) {
            int occupied = p.getTotalSpaces() - p.getAvailableSpaces();
            double pct = p.getTotalSpaces() > 0 ? (occupied * 100.0 / p.getTotalSpaces()) : 0;
            return ParkingResponse.builder()
                    .id(p.getId()).code(p.getCode()).name(p.getName())
                    .totalSpaces(p.getTotalSpaces()).availableSpaces(p.getAvailableSpaces())
                    .occupiedSpaces(occupied)
                    .occupancyRate(String.format("%.1f", pct))
                    .location(p.getLocation()).feePerHour(p.getFeePerHour())
                    .isActive(p.isActive()).createdAt(p.getCreatedAt())
                    .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagedResponse<T> {
        private List<T> data;
        private PaginationMeta pagination;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaginationMeta {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private List<String> errors;

        public static <T> ApiResponse<T> ok(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).build();
        }
        public static <T> ApiResponse<T> ok(T data) {
            return ApiResponse.<T>builder().success(true).data(data).build();
        }
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).build();
        }
        public static <T> ApiResponse<T> error(String message, List<String> errors) {
            return ApiResponse.<T>builder().success(false).message(message).errors(errors).build();
        }
    }
}
