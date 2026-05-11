package com.amnii.parking.parkingservice.repository;

import com.amnii.parking.parkingservice.entity.Parking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, UUID> {

    boolean existsByCodeIgnoreCase(String code);
    Optional<Parking> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    @Query("SELECT p FROM Parking p WHERE p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Parking> findActiveBySearch(@Param("search") String search, Pageable pageable);
}
