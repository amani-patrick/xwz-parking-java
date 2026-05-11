package com.amnii.parking.entryservice.controller;

import com.amnii.parking.entryservice.dto.EntryDtos.*;
import com.amnii.parking.entryservice.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
@Tag(name = "Car Entries", description = "Car entry, exit, ticket and bill management")
public class EntryController {

    private final EntryService entryService;

    @PostMapping
    @Operation(summary = "Register car entry and generate ticket")
    public ResponseEntity<ApiResponse<EntryWithTicket>> registerEntry(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody EntryRequest req) {
        EntryWithTicket result = entryService.registerEntry(req, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Car entry registered successfully", result));
    }

    @PatchMapping("/{id}/exit")
    @Operation(summary = "Register car exit and generate bill")
    public ResponseEntity<ApiResponse<ExitWithBill>> registerExit(
            @PathVariable UUID id,
            @RequestBody(required = false) ExitRequest req) {
        ExitWithBill result = entryService.registerExit(id, req != null ? req : new ExitRequest());
        return ResponseEntity.ok(ApiResponse.ok("Car exit registered successfully", result));
    }

    @GetMapping
    @Operation(summary = "List all car entries with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<CarEntryResponse>>> getAll(
            @RequestParam(defaultValue = "1")  int    page,
            @RequestParam(defaultValue = "10") int    limit,
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "")   String status,
            @RequestParam(defaultValue = "")   String parkingCode) {
        return ResponseEntity.ok(
                ApiResponse.ok(entryService.getAll(page, limit, search, status, parkingCode)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single car entry by ID")
    public ResponseEntity<ApiResponse<CarEntryResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(entryService.getById(id)));
    }
}
