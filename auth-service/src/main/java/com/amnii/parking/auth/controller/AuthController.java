package com.amnii.parking.auth.controller;

import com.amnii.parking.auth.dto.ApiResponse;
import com.amnii.parking.auth.dto.AuthDtos.*;
import com.amnii.parking.auth.exception.ForbiddenException;
import com.amnii.parking.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login and management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserDto>> me(@RequestHeader("X-User-Id") String userId) {
        UserDto user = authService.getProfile(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedUsersResponse>> getUsers(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "")   String search) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Admin access required");
        return ResponseEntity.ok(ApiResponse.ok(authService.getAllUsers(page, limit, search)));
    }

    @PatchMapping("/users/{id}/toggle")
    @Operation(summary = "Toggle user active status (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserDto>> toggleUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID id) {
        if (!"ADMIN".equals(role)) throw new ForbiddenException("Admin access required");
        UserDto user = authService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("User status updated", user));
    }
}
