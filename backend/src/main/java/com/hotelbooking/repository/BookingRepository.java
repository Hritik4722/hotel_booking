package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Counts active overlapping bookings for a specific hotel within a stay interval.
     * Overlap condition:
     * status = 'CONFIRMED' AND checkInDate < :checkOut AND checkOutDate > :checkIn
     *
     * @param hotelId  Target hotel ID
     * @param status   Booking status filter (CONFIRMED)
     * @param checkIn  Stay start date
     * @param checkOut Stay end date
     * @return Number of rooms occupied for the given date range
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.hotel.id = :hotelId
          AND b.status = :status
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
    long countOverlappingBookings(
        @Param("hotelId") Long hotelId,
        @Param("status") BookingStatus status,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    /**
     * Helper to count active CONFIRMED overlapping bookings.
     */
    default long countActiveOverlappingBookings(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        return countOverlappingBookings(hotelId, BookingStatus.CONFIRMED, checkIn, checkOut);
    }

    /**
     * Retrieves a booking by unique booking reference and guest email (case-insensitive).
     */
    Optional<Booking> findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase(
        String bookingReference,
        String guestEmail
    );

    /**
     * Retrieves a booking by unique reference.
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Checks if a booking reference already exists (for collision avoidance during generation).
     */
    boolean existsByBookingReference(String bookingReference);
}
