package com.amnii.parking.reportservice.dto;

import com.amnii.parking.reportservice.entity.CarEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReportDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CarEntryDto {
        private UUID id;
        private String plateNumber;
        private String parkingCode;
        private LocalDateTime entryDatetime;
        private LocalDateTime exitDatetime;
        private BigDecimal chargedAmount;
        private String ticketNumber;
        private CarEntry.Status status;

        public static CarEntryDto from(CarEntry e) {
            return CarEntryDto.builder()
                    .id(e.getId()).plateNumber(e.getPlateNumber())
                    .parkingCode(e.getParkingCode())
                    .entryDatetime(e.getEntryDatetime())
                    .exitDatetime(e.getExitDatetime())
                    .chargedAmount(e.getChargedAmount())
                    .ticketNumber(e.getTicketNumber())
                    .status(e.getStatus()).build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ParkingBreakdown {
        private String code;
        private String name;
        private int totalSpaces;
        private int availableSpaces;
        private int currentlyParked;
        private BigDecimal feePerHour;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DashboardDto {
        private long totalParkings;
        private int totalSpaces;
        private int availableSpaces;
        private long totalCarsRegistered;
        private long currentlyParked;
        private BigDecimal todayRevenue;
        private String currency;
        private List<ParkingBreakdown> parkingBreakdown;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OutgoingReportSummary {
        private long totalCars;
        private BigDecimal totalRevenue;
        private String currency;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EnteredReportSummary {
        private long totalCars;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagedReportResponse<T> {
        private List<T> data;
        private Object summary;
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

        public static <T> ApiResponse<T> ok(T data) {
            return ApiResponse.<T>builder().success(true).data(data).build();
        }
        public static <T> ApiResponse<T> ok(String msg, T data) {
            return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
        }
        public static <T> ApiResponse<T> error(String msg) {
            return ApiResponse.<T>builder().success(false).message(msg).build();
        }
    }
}
