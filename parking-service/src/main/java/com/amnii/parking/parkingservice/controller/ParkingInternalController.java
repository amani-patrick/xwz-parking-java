package com.amnii.parking.parkingservice.controller;

import com.amnii.parking.parkingservice.dto.ParkingDtos.*;
import com.amnii.parking.parkingservice.exception.BadRequestException;
import com.amnii.parking.parkingservice.exception.NotFoundException;
import com.amnii.parking.parkingservice.repository.ParkingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking Internal", description = "Internal space management endpoints (called by entry-service)")
public class ParkingInternalController {

    private final ParkingRepository parkingRepository;

    @PatchMapping("/{code}/decrement")
    @Transactional
    @Operation(summary = "Decrement available spaces by 1 (internal)")
    public ResponseEntity<ApiResponse<Void>> decrement(@PathVariable String code) {
        var parking = parkingRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Parking not found: " + code));
        if (parking.getAvailableSpaces() <= 0) {
            throw new BadRequestException("No available spaces in " + code);
        }
        parking.setAvailableSpaces(parking.getAvailableSpaces() - 1);
        parkingRepository.save(parking);
        log.info("Decremented space in {}: {} remaining", code, parking.getAvailableSpaces());
        return ResponseEntity.ok(ApiResponse.ok("Space decremented", null));
    }

    @PatchMapping("/{code}/increment")
    @Transactional
    @Operation(summary = "Increment available spaces by 1 (internal)")
    public ResponseEntity<ApiResponse<Void>> increment(@PathVariable String code) {
        var parking = parkingRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Parking not found: " + code));
        if (parking.getAvailableSpaces() >= parking.getTotalSpaces()) {
            throw new BadRequestException("Spaces already at maximum in " + code);
        }
        parking.setAvailableSpaces(parking.getAvailableSpaces() + 1);
        parkingRepository.save(parking);
        log.info("Incremented space in {}: {} available", code, parking.getAvailableSpaces());
        return ResponseEntity.ok(ApiResponse.ok("Space incremented", null));
    }
}
