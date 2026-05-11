package com.amnii.parking.reportservice.service;

import com.amnii.parking.reportservice.dto.ReportDtos.*;
import com.amnii.parking.reportservice.entity.CarEntry;
import com.amnii.parking.reportservice.entity.Parking;
import com.amnii.parking.reportservice.repository.CarEntryRepository;
import com.amnii.parking.reportservice.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final CarEntryRepository entryRepository;
    private final ParkingRepository  parkingRepository;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    public DashboardDto getDashboard() {
        long   totalParkings   = parkingRepository.countByIsActiveTrue();
        int    totalSpaces     = parkingRepository.sumTotalSpaces();
        int    availableSpaces = parkingRepository.sumAvailableSpaces();
        long   totalCars       = entryRepository.count();
        long   currentlyParked = entryRepository.countByStatus(CarEntry.Status.PARKED);
        BigDecimal todayRev    = entryRepository.sumTodayRevenue(
                LocalDate.now().atStartOfDay());

        List<Parking> parkings = parkingRepository.findByIsActiveTrue();
        List<ParkingBreakdown> breakdown = parkings.stream().map(p -> {
            long parked = entryRepository.countByStatusAndParkingCode(
                    CarEntry.Status.PARKED, p.getCode());
            return ParkingBreakdown.builder()
                    .code(p.getCode()).name(p.getName())
                    .totalSpaces(p.getTotalSpaces())
                    .availableSpaces(p.getAvailableSpaces())
                    .currentlyParked((int) parked)
                    .feePerHour(p.getFeePerHour())
                    .build();
        }).toList();

        return DashboardDto.builder()
                .totalParkings(totalParkings)
                .totalSpaces(totalSpaces)
                .availableSpaces(availableSpaces)
                .totalCarsRegistered(totalCars)
                .currentlyParked(currentlyParked)
                .todayRevenue(todayRev != null ? todayRev : BigDecimal.ZERO)
                .currency("RWF")
                .parkingBreakdown(breakdown)
                .build();
    }

    // ── Outgoing Report ───────────────────────────────────────────────────────

    public PagedReportResponse<CarEntryDto> getOutgoing(
            LocalDateTime start, LocalDateTime end,
            String parkingCode, int page, int limit) {

        validateDateRange(start, end);
        String code = resolveCode(parkingCode);
        PageRequest pageable = PageRequest.of(page - 1, limit,
                Sort.by("exitDatetime").descending());

        Page<CarEntry> result = entryRepository.findOutgoing(start, end, code, pageable);
        BigDecimal totalRevenue = entryRepository.sumOutgoingRevenue(start, end, code);
        long totalCars = entryRepository.countOutgoing(start, end, code);

        OutgoingReportSummary summary = OutgoingReportSummary.builder()
                .totalCars(totalCars)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .currency("RWF")
                .periodStart(start)
                .periodEnd(end)
                .build();

        return PagedReportResponse.<CarEntryDto>builder()
                .data(result.getContent().stream().map(CarEntryDto::from).toList())
                .summary(summary)
                .pagination(PaginationMeta.builder()
                        .page(page).limit(limit)
                        .total(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    // ── Entered Report ────────────────────────────────────────────────────────

    public PagedReportResponse<CarEntryDto> getEntered(
            LocalDateTime start, LocalDateTime end,
            String parkingCode, int page, int limit) {

        validateDateRange(start, end);
        String code = resolveCode(parkingCode);
        PageRequest pageable = PageRequest.of(page - 1, limit,
                Sort.by("entryDatetime").descending());

        Page<CarEntry> result = entryRepository.findEntered(start, end, code, pageable);

        EnteredReportSummary summary = EnteredReportSummary.builder()
                .totalCars(result.getTotalElements())
                .periodStart(start)
                .periodEnd(end)
                .build();

        return PagedReportResponse.<CarEntryDto>builder()
                .data(result.getContent().stream().map(CarEntryDto::from).toList())
                .summary(summary)
                .pagination(PaginationMeta.builder()
                        .page(page).limit(limit)
                        .total(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    // ── Currently parked at a parking ─────────────────────────────────────────

    public PagedReportResponse<CarEntryDto> getCurrentlyParked(
            String code, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit,
                Sort.by("entryDatetime").descending());
        Page<CarEntry> result = entryRepository.findByParkingCodeAndStatus(
                code.toUpperCase(), CarEntry.Status.PARKED, pageable);

        return PagedReportResponse.<CarEntryDto>builder()
                .data(result.getContent().stream().map(CarEntryDto::from).toList())
                .pagination(PaginationMeta.builder()
                        .page(page).limit(limit)
                        .total(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    private String resolveCode(String code) {
        return (code != null && !code.isBlank()) ? code.toUpperCase() : null;
    }
}
