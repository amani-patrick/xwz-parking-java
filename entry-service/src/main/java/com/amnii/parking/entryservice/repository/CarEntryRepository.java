package com.amnii.parking.entryservice.repository;

import com.amnii.parking.entryservice.entity.CarEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarEntryRepository extends JpaRepository<CarEntry, UUID> {

    boolean existsByPlateNumberAndParkingCodeAndStatus(
            String plateNumber, String parkingCode, CarEntry.Status status);

    @Query("SELECT e FROM CarEntry e WHERE " +
           "(LOWER(e.plateNumber)  LIKE LOWER(CONCAT('%',:s,'%')) OR " +
           " LOWER(e.parkingCode)  LIKE LOWER(CONCAT('%',:s,'%')) OR " +
           " LOWER(e.ticketNumber) LIKE LOWER(CONCAT('%',:s,'%'))) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:code   IS NULL OR e.parkingCode = :code)")
    Page<CarEntry> search(@Param("s") String search,
                          @Param("status") CarEntry.Status status,
                          @Param("code") String parkingCode,
                          Pageable pageable);
}
