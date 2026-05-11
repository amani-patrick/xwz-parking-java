package com.amnii.parking.reportservice.repository;

import com.amnii.parking.reportservice.entity.CarEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CarEntryRepository extends JpaRepository<CarEntry, UUID> {

    // Outgoing cars between two datetimes (optionally filtered by parking)
    @Query("SELECT e FROM CarEntry e WHERE e.status = 'EXITED' " +
           "AND e.exitDatetime BETWEEN :start AND :end " +
           "AND (:code IS NULL OR e.parkingCode = :code) " +
           "ORDER BY e.exitDatetime DESC")
    Page<CarEntry> findOutgoing(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            @Param("code")  String code,
            Pageable pageable);

    // Sum of charged amounts for outgoing report
    @Query("SELECT COALESCE(SUM(e.chargedAmount), 0) FROM CarEntry e WHERE e.status = 'EXITED' " +
           "AND e.exitDatetime BETWEEN :start AND :end " +
           "AND (:code IS NULL OR e.parkingCode = :code)")
    BigDecimal sumOutgoingRevenue(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            @Param("code")  String code);

    // Count outgoing for summary
    @Query("SELECT COUNT(e) FROM CarEntry e WHERE e.status = 'EXITED' " +
           "AND e.exitDatetime BETWEEN :start AND :end " +
           "AND (:code IS NULL OR e.parkingCode = :code)")
    long countOutgoing(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            @Param("code")  String code);

    // All entered cars between two datetimes
    @Query("SELECT e FROM CarEntry e WHERE e.entryDatetime BETWEEN :start AND :end " +
           "AND (:code IS NULL OR e.parkingCode = :code) " +
           "ORDER BY e.entryDatetime DESC")
    Page<CarEntry> findEntered(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            @Param("code")  String code,
            Pageable pageable);

    long countByEntryDatetimeBetweenAndParkingCodeContaining(
            LocalDateTime start, LocalDateTime end, String code);

    // Currently parked at a specific parking
    Page<CarEntry> findByParkingCodeAndStatus(String code, CarEntry.Status status, Pageable pageable);

    // Count currently parked
    long countByStatusAndParkingCode(CarEntry.Status status, String parkingCode);

    // Today revenue
    @Query("SELECT COALESCE(SUM(e.chargedAmount), 0) FROM CarEntry e " +
           "WHERE e.status = 'EXITED' AND e.exitDatetime >= :todayStart")
    BigDecimal sumTodayRevenue(@Param("todayStart") LocalDateTime todayStart);

    long countByStatus(CarEntry.Status status);
}
