package com.hotelbooking.repository;

import com.hotelbooking.entity.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Acquires a pessimistic write lock (SELECT ... FOR UPDATE) on the Hotel record.
     * Prevents race conditions and overbooking when multiple concurrent requests attempt
     * to reserve the last available room for the same hotel at the exact same moment.
     *
     * @param id Hotel ID to lock and retrieve
     * @return Locked Hotel entity if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hotel h WHERE h.id = :id")
    Optional<Hotel> findByIdWithLock(@Param("id") Long id);

    /**
     * Finds hotels where location contains the search term (case-insensitive).
     *
     * @param location City or destination name
     * @return List of matching hotels
     */
    List<Hotel> findByLocationContainingIgnoreCase(String location);
}
