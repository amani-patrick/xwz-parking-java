package com.amnii.parking.entryservice.service;

import com.amnii.parking.entryservice.config.ParkingServiceClient;
import com.amnii.parking.entryservice.config.RabbitMQConfig;
import com.amnii.parking.entryservice.dto.EntryDtos.*;
import com.amnii.parking.entryservice.entity.CarEntry;
import com.amnii.parking.entryservice.exception.BadRequestException;
import com.amnii.parking.entryservice.exception.ConflictException;
import com.amnii.parking.entryservice.exception.NotFoundException;
import com.amnii.parking.entryservice.messaging.EntryEvents;
import com.amnii.parking.entryservice.repository.CarEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryService {

    private final CarEntryRepository entryRepository;
    private final ParkingServiceClient parkingClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public EntryWithTicket registerEntry(EntryRequest req, UUID attendantId) {
        String plateNumber = req.getPlateNumber().toUpperCase().trim();
        String parkingCode = req.getParkingCode().toUpperCase().trim();

        // Fetch parking details from parking-service
        ParkingServiceClient.ParkingInfo parking = parkingClient.getParkingInfo(parkingCode);
        if (parking == null) {
            throw new NotFoundException("Parking not found: " + parkingCode);
        }
        if (parking.getAvailableSpaces() <= 0) {
            throw new BadRequestException("Parking is full. No available spaces in " + parkingCode);
        }

        // Prevent duplicate entry for same plate in same parking
        boolean alreadyParked = entryRepository.existsByPlateNumberAndParkingCodeAndStatus(
                plateNumber, parkingCode, CarEntry.Status.PARKED);
        if (alreadyParked) {
            throw new ConflictException(
                    "Vehicle " + plateNumber + " is already parked in " + parkingCode);
        }

        String ticketNumber = generateTicketNumber();
        LocalDateTime entryTime = LocalDateTime.now();

        CarEntry entry = CarEntry.builder()
                .plateNumber(plateNumber)
                .parkingCode(parkingCode)
                .entryDatetime(entryTime)
                .exitDatetime(null)
                .chargedAmount(BigDecimal.ZERO)
                .ticketNumber(ticketNumber)
                .attendantId(attendantId)
                .status(CarEntry.Status.PARKED)
                .build();

        entry = entryRepository.save(entry);
        log.info("Car entered: plate={} parking={} ticket={}", plateNumber, parkingCode, ticketNumber);

        // Decrement space in parking-service
        parkingClient.decrementSpace(parkingCode);

        // Publish event to RabbitMQ
        publishEntryEvent(entry, parking);

        TicketDto ticket = TicketDto.builder()
                .ticketNumber(ticketNumber)
                .plateNumber(plateNumber)
                .parkingCode(parkingCode)
                .parkingName(parking.getName())
                .parkingLocation(parking.getLocation())
                .feePerHour(parking.getFeePerHour())
                .entryDatetime(entryTime)
                .issuedAt(LocalDateTime.now())
                .build();

        return EntryWithTicket.builder()
                .entry(CarEntryResponse.from(entry))
                .ticket(ticket)
                .remainingSpaces(parking.getAvailableSpaces() - 1)
                .build();
    }

    @Transactional
    public ExitWithBill registerExit(UUID entryId, ExitRequest req) {
        CarEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Car entry not found: " + entryId));

        if (entry.getStatus() == CarEntry.Status.EXITED) {
            throw new BadRequestException("Car has already exited. Ticket: " + entry.getTicketNumber());
        }

        LocalDateTime exitTime = (req.getExitDatetime() != null) ? req.getExitDatetime() : LocalDateTime.now();

        // Validate: exit must not be in the future
        if (exitTime.isAfter(LocalDateTime.now().plusSeconds(5))) {
            throw new BadRequestException("Exit datetime cannot be in the future");
        }

        // CRITICAL: exit must be AFTER entry — users won't be leaving earlier than they entered!
        if (!exitTime.isAfter(entry.getEntryDatetime())) {
            throw new BadRequestException(
                    "Exit time (" + exitTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    ") must be after entry time (" +
                    entry.getEntryDatetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ")");
        }

        // Fetch parking for fee
        ParkingServiceClient.ParkingInfo parking = parkingClient.getParkingInfo(entry.getParkingCode());
        BigDecimal feePerHour = (parking != null) ? parking.getFeePerHour() : BigDecimal.ZERO;
        String parkingName     = (parking != null) ? parking.getName()       : entry.getParkingCode();
        String parkingLocation = (parking != null) ? parking.getLocation()   : "";

        // Calculate duration and charge
        Duration duration      = Duration.between(entry.getEntryDatetime(), exitTime);
        double durationHours   = duration.toMinutes() / 60.0;
        BigDecimal charged     = feePerHour
                .multiply(BigDecimal.valueOf(durationHours))
                .setScale(2, RoundingMode.HALF_UP);

        entry.setExitDatetime(exitTime);
        entry.setChargedAmount(charged);
        entry.setStatus(CarEntry.Status.EXITED);
        entry = entryRepository.save(entry);

        log.info("Car exited: plate={} ticket={} charged={} RWF duration={}min",
                entry.getPlateNumber(), entry.getTicketNumber(), charged, duration.toMinutes());

        // Increment space in parking-service
        parkingClient.incrementSpace(entry.getParkingCode());

        // Format human-readable duration
        long hours   = duration.toHours();
        long minutes = duration.toMinutesPart();
        String durationStr = hours + "h " + minutes + "m";

        // Publish exit event to RabbitMQ
        publishExitEvent(entry, parkingName, charged, durationStr);

        BillDto bill = BillDto.builder()
                .ticketNumber(entry.getTicketNumber())
                .plateNumber(entry.getPlateNumber())
                .parkingCode(entry.getParkingCode())
                .parkingName(parkingName)
                .parkingLocation(parkingLocation)
                .entryDatetime(entry.getEntryDatetime())
                .exitDatetime(exitTime)
                .duration(durationStr)
                .durationHours(Math.round(durationHours * 100.0) / 100.0)
                .feePerHour(feePerHour)
                .totalCharged(charged)
                .currency("RWF")
                .generatedAt(LocalDateTime.now())
                .build();

        return ExitWithBill.builder()
                .entry(CarEntryResponse.from(entry))
                .bill(bill)
                .build();
    }

    public PagedResponse<CarEntryResponse> getAll(int page, int limit, String search,
                                                   String status, String parkingCode) {
        PageRequest pageable = PageRequest.of(page - 1, limit,
                Sort.by("entryDatetime").descending());

        CarEntry.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = CarEntry.Status.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        String code = (parkingCode != null && !parkingCode.isBlank())
                ? parkingCode.toUpperCase() : null;

        Page<CarEntry> result = entryRepository.search(
                search != null ? search : "", statusEnum, code, pageable);

        return PagedResponse.<CarEntryResponse>builder()
                .data(result.getContent().stream().map(CarEntryResponse::from).toList())
                .pagination(PaginationMeta.builder()
                        .page(page).limit(limit)
                        .total(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build())
                .build();
    }

    public CarEntryResponse getById(UUID id) {
        return CarEntryResponse.from(entryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entry not found: " + id)));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateTicketNumber() {
        String ts  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int    rnd = (int)(Math.random() * 9000) + 1000;
        return "TKT-" + ts + "-" + rnd;
    }

    private void publishEntryEvent(CarEntry entry,
                                    ParkingServiceClient.ParkingInfo parking) {
        try {
            EntryEvents.CarEnteredEvent event = EntryEvents.CarEnteredEvent.builder()
                    .entryId(entry.getId().toString())
                    .plateNumber(entry.getPlateNumber())
                    .parkingCode(entry.getParkingCode())
                    .parkingName(parking.getName())
                    .ticketNumber(entry.getTicketNumber())
                    .entryDatetime(entry.getEntryDatetime())
                    .feePerHour(parking.getFeePerHour())
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ENTRY_EXCHANGE,
                    RabbitMQConfig.CAR_ENTERED_KEY,
                    event);
            log.info("Published CAR_ENTERED event for ticket {}", entry.getTicketNumber());
        } catch (Exception e) {
            log.warn("Failed to publish CAR_ENTERED event: {}", e.getMessage());
        }
    }

    private void publishExitEvent(CarEntry entry, String parkingName,
                                   BigDecimal charged, String duration) {
        try {
            EntryEvents.CarExitedEvent event = EntryEvents.CarExitedEvent.builder()
                    .entryId(entry.getId().toString())
                    .plateNumber(entry.getPlateNumber())
                    .parkingCode(entry.getParkingCode())
                    .parkingName(parkingName)
                    .ticketNumber(entry.getTicketNumber())
                    .entryDatetime(entry.getEntryDatetime())
                    .exitDatetime(entry.getExitDatetime())
                    .duration(duration)
                    .chargedAmount(charged)
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ENTRY_EXCHANGE,
                    RabbitMQConfig.CAR_EXITED_KEY,
                    event);
            log.info("Published CAR_EXITED event for ticket {}", entry.getTicketNumber());
        } catch (Exception e) {
            log.warn("Failed to publish CAR_EXITED event: {}", e.getMessage());
        }
    }
}
