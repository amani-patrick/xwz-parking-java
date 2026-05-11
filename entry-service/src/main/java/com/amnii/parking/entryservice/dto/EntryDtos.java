package com.amnii.parking.entryservice.dto;

import com.amnii.parking.entryservice.entity.CarEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EntryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EntryRequest {
        @NotBlank(message = "Plate number is required")
        @Pattern(regexp = "^[A-Z0-9\\s\\-]{2,20}$",
                 message = "Plate number must contain only uppercase letters, numbers, spaces or hyphens")
        private String plateNumber;

        @NotBlank(message = "Parking code is required")
        private String parkingCode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExitRequest {
        // Optional: if null, uses current time
        private LocalDateTime exitDatetime;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CarEntryResponse {
        private UUID id;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private LocalDateTime entryDatetime;
        private LocalDateTime exitDatetime;
        private BigDecimal chargedAmount;
        private String ticketNumber;
        private CarEntry.Status status;
        private LocalDateTime createdAt;

        public static CarEntryResponse from(CarEntry e) {
            return CarEntryResponse.builder()
                    .id(e.getId()).plateNumber(e.getPlateNumber())
                    .parkingCode(e.getParkingCode())
                    .entryDatetime(e.getEntryDatetime()).exitDatetime(e.getExitDatetime())
                    .chargedAmount(e.getChargedAmount())
                    .ticketNumber(e.getTicketNumber()).status(e.getStatus())
                    .createdAt(e.getCreatedAt()).build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TicketDto {
        private String ticketNumber;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private String parkingLocation;
        private BigDecimal feePerHour;
        private LocalDateTime entryDatetime;
        private LocalDateTime issuedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BillDto {
        private String ticketNumber;
        private String plateNumber;
        private String parkingCode;
        private String parkingName;
        private String parkingLocation;
        private LocalDateTime entryDatetime;
        private LocalDateTime exitDatetime;
        private String duration;
        private double durationHours;
        private BigDecimal feePerHour;
        private BigDecimal totalCharged;
        private String currency;
        private LocalDateTime generatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EntryWithTicket {
        private CarEntryResponse entry;
        private TicketDto ticket;
        private int remainingSpaces;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExitWithBill {
        private CarEntryResponse entry;
        private BillDto bill;
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
        public static <T> ApiResponse<T> error(String msg, List<String> errors) {
            return ApiResponse.<T>builder().success(false).message(msg).errors(errors).build();
        }
    }
}
