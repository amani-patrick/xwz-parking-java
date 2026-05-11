package com.amnii.parking.parkingservice.controller;

import com.amnii.parking.parkingservice.dto.ParkingDtos.*;
import com.amnii.parking.parkingservice.exception.ForbiddenException;
import com.amnii.parking.parkingservice.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
@Tag(name = "Parkings", description = "Parking lot management")
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping
    @Operation(summary = "Register new parking (Admin only)")
    public ResponseEntity<ApiResponse<ParkingResponse>> create(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id")   String userId,
            @Valid @RequestBody ParkingRequest req) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Admin access required");
        ParkingResponse response = parkingService.create(req, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Parking registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all active parkings")
    public ResponseEntity<ApiResponse<PagedResponse<ParkingResponse>>> getAll(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "")   String search) {
        return ResponseEntity.ok(ApiResponse.ok(parkingService.getAll(page, limit, search)));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get parking by code")
    public ResponseEntity<ApiResponse<ParkingResponse>> getOne(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(parkingService.getByCode(code)));
    }

    @PutMapping("/{code}")
    @Operation(summary = "Update parking details (Admin only)")
    public ResponseEntity<ApiResponse<ParkingResponse>> update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id")   String userId,
            @PathVariable String code,
            @Valid @RequestBody ParkingRequest req) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Admin access required");
        return ResponseEntity.ok(ApiResponse.ok("Parking updated", parkingService.update(code, req, UUID.fromString(userId))));
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Deactivate parking (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String code) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Admin access required");
        parkingService.deactivate(code);
        return ResponseEntity.ok(ApiResponse.ok("Parking deactivated", null));
    }
}
