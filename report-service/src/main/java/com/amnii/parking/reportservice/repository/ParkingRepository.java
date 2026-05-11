package com.amnii.parking.reportservice.repository;

import com.amnii.parking.reportservice.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, UUID> {

    List<Parking> findByIsActiveTrue();

    @Query("SELECT COALESCE(SUM(p.totalSpaces), 0) FROM Parking p WHERE p.isActive = true")
    int sumTotalSpaces();

    @Query("SELECT COALESCE(SUM(p.availableSpaces), 0) FROM Parking p WHERE p.isActive = true")
    int sumAvailableSpaces();

    long countByIsActiveTrue();
}
