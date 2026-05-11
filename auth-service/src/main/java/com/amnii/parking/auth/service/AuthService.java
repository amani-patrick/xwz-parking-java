package com.amnii.parking.auth.service;

import com.amnii.parking.auth.config.JwtUtil;
import com.amnii.parking.auth.config.RabbitMQConfig;
import com.amnii.parking.auth.dto.AuthDtos.*;
import com.amnii.parking.auth.entity.User;
import com.amnii.parking.auth.exception.BadRequestException;
import com.amnii.parking.auth.exception.ConflictException;
import com.amnii.parking.auth.exception.NotFoundException;
import com.amnii.parking.auth.messaging.UserRegisteredEvent;
import com.amnii.parking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : User.Role.PARKING_TENANT)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} ({})", user.getEmail(), user.getRole());

        // Publish event to RabbitMQ
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(user.getId().toString())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.USER_REGISTERED_KEY,
                    event
            );
            log.info("Published USER_REGISTERED event for {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to publish USER_REGISTERED event: {}", e.getMessage());
            // Don't fail registration just because of messaging
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder().user(UserDto.fromUser(user)).token(token).build();
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadRequestException("Account is deactivated. Please contact admin.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder().user(UserDto.fromUser(user)).token(token).build();
    }

    public UserDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserDto.fromUser(user);
    }

    public PagedUsersResponse getAllUsers(int page, int limit, String search) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findBySearchTerm(search != null ? search : "", pageable);

        return PagedUsersResponse.builder()
                .data(users.getContent().stream().map(UserDto::fromUser).toList())
                .pagination(PaginationMeta.builder()
                        .page(page)
                        .limit(limit)
                        .total(users.getTotalElements())
                        .totalPages(users.getTotalPages())
                        .build())
                .build();
    }

    @Transactional
    public UserDto toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(!user.isActive());
        return UserDto.fromUser(userRepository.save(user));
    }
}
