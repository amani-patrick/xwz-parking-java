package com.amnii.parking.parkingservice.service;

import com.amnii.parking.parkingservice.dto.ParkingDtos.*;
import com.amnii.parking.parkingservice.entity.Parking;
import com.amnii.parking.parkingservice.exception.ConflictException;
import com.amnii.parking.parkingservice.exception.NotFoundException;
import com.amnii.parking.parkingservice.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingService {

    private final ParkingRepository parkingRepository;

    @Transactional
    public ParkingResponse create(ParkingRequest req, UUID createdBy) {
        if (parkingRepository.existsByCodeIgnoreCase(req.getCode())) {
            throw new ConflictException("Parking code already exists: " + req.getCode());
        }
        Parking parking = Parking.builder()
                .code(req.getCode().toUpperCase())
                .name(req.getName())
                .totalSpaces(req.getTotalSpaces())
                .availableSpaces(req.getTotalSpaces())
                .location(req.getLocation())
                .feePerHour(req.getFeePerHour())
                .createdBy(createdBy)
                .isActive(true)
                .build();
        parking = parkingRepository.save(parking);
        log.info("Parking created: {} ({})", parking.getCode(), parking.getName());
        return ParkingResponse.from(parking);
    }

    public PagedResponse<ParkingResponse> getAll(int page, int limit, String search) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Parking> result = parkingRepository.findActiveBySearch(search != null ? search : "", pageable);
        return PagedResponse.<ParkingResponse>builder()
                .data(result.getContent().stream().map(ParkingResponse::from).toList())
                .pagination(PaginationMeta.builder()
                        .page(page).limit(limit)
                        .total(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    public ParkingResponse getByCode(String code) {
        return ParkingResponse.from(findActiveParking(code));
    }

    @Transactional
    public ParkingResponse update(String code, ParkingRequest req, UUID updatedBy) {
        Parking existing = findActiveParking(code);
        int spaceDiff = req.getTotalSpaces() - existing.getTotalSpaces();
        int newAvailable = Math.max(0, existing.getAvailableSpaces() + spaceDiff);

        existing.setCode(req.getCode().toUpperCase());
        existing.setName(req.getName());
        existing.setTotalSpaces(req.getTotalSpaces());
        existing.setAvailableSpaces(newAvailable);
        existing.setLocation(req.getLocation());
        existing.setFeePerHour(req.getFeePerHour());

        return ParkingResponse.from(parkingRepository.save(existing));
    }

    @Transactional
    public void deactivate(String code) {
        Parking parking = findActiveParking(code);
        parking.setActive(false);
        parkingRepository.save(parking);
        log.info("Parking deactivated: {}", code);
    }

    private Parking findActiveParking(String code) {
        return parkingRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Parking not found: " + code));
    }
}
