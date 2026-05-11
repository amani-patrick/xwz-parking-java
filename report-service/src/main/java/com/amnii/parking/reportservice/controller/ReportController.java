package com.amnii.parking.reportservice.controller;

import com.amnii.parking.reportservice.dto.ReportDtos.*;
import com.amnii.parking.reportservice.exception.ForbiddenException;
import com.amnii.parking.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytics and reporting endpoints (Admin only)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Real-time dashboard stats (Admin only)")
    public ResponseEntity<ApiResponse<DashboardDto>> dashboard(
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDashboard()));
    }

    @GetMapping("/outgoing")
    @Operation(summary = "Outgoing cars with total revenue between two datetimes (Admin only)")
    public ResponseEntity<ApiResponse<PagedReportResponse<CarEntryDto>>> outgoing(
            @RequestHeader("X-User-Role") String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String parkingCode,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getOutgoing(startDate, endDate, parkingCode, page, limit)));
    }

    @GetMapping("/entered")
    @Operation(summary = "All entered cars between two datetimes (Admin only)")
    public ResponseEntity<ApiResponse<PagedReportResponse<CarEntryDto>>> entered(
            @RequestHeader("X-User-Role") String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String parkingCode,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getEntered(startDate, endDate, parkingCode, page, limit)));
    }

    @GetMapping("/parking/{code}/cars")
    @Operation(summary = "Cars currently parked at a specific parking")
    public ResponseEntity<ApiResponse<PagedReportResponse<CarEntryDto>>> currentlyParked(
            @PathVariable String code,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getCurrentlyParked(code, page, limit)));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Admin access required");
        }
    }
}
