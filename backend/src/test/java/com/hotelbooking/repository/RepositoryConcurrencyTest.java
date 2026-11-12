package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryConcurrencyTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Hotel sampleHotel;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();

        sampleHotel = hotelRepository.save(
            Hotel.builder()
                .name("Taj Vista Coastal Resort")
                .location("Goa")
                .description("Luxury beachfront resort in South Goa.")
                .amenities("Beach Access, Pool, Spa, Free WiFi")
                .pricePerNight(new BigDecimal("5000.00"))
                .totalRooms(3)
                .build()
        );
    }

    @Test
    @DisplayName("Should successfully acquire pessimistic write lock on hotel")
    void testFindByIdWithLock() {
        Optional<Hotel> lockedHotel = hotelRepository.findByIdWithLock(sampleHotel.getId());
        assertThat(lockedHotel).isPresent();
        assertThat(lockedHotel.get().getName()).isEqualTo("Taj Vista Coastal Resort");
        assertThat(lockedHotel.get().getTotalRooms()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should correctly count active overlapping bookings and ignore cancelled bookings")
    void testCountOverlappingBookings() {
        LocalDate stayStart = LocalDate.of(2026, 12, 10);
        LocalDate stayEnd = LocalDate.of(2026, 12, 15);

        // Booking 1: Overlaps (Dec 12 - Dec 17), CONFIRMED
        bookingRepository.save(
            Booking.builder()
                .bookingReference("TCT-TEST01")
                .hotel(sampleHotel)
                .guestName("Rahul Sharma")
                .guestEmail("rahul@example.com")
                .checkInDate(LocalDate.of(2026, 12, 12))
                .checkOutDate(LocalDate.of(2026, 12, 17))
                .nights(5)
                .baseAmount(new BigDecimal("25000.00"))
                .discountAmount(new BigDecimal("2500.00"))
                .totalAmount(new BigDecimal("22500.00"))
                .status(BookingStatus.CONFIRMED)
                .build()
        );

        // Booking 2: Overlaps (Dec 8 - Dec 11), CONFIRMED
        bookingRepository.save(
            Booking.builder()
                .bookingReference("TCT-TEST02")
                .hotel(sampleHotel)
                .guestName("Priya Patel")
                .guestEmail("priya@example.com")
                .checkInDate(LocalDate.of(2026, 12, 8))
                .checkOutDate(LocalDate.of(2026, 12, 11))
                .nights(3)
                .baseAmount(new BigDecimal("15000.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("15000.00"))
                .status(BookingStatus.CONFIRMED)
                .build()
        );

        // Booking 3: Overlaps dates (Dec 10 - Dec 14), but CANCELLED -> Should NOT be counted
        bookingRepository.save(
            Booking.builder()
                .bookingReference("TCT-TEST03")
                .hotel(sampleHotel)
                .guestName("Amit Kumar")
                .guestEmail("amit@example.com")
                .checkInDate(LocalDate.of(2026, 12, 10))
                .checkOutDate(LocalDate.of(2026, 12, 14))
                .nights(4)
                .baseAmount(new BigDecimal("20000.00"))
                .discountAmount(new BigDecimal("2000.00"))
                .totalAmount(new BigDecimal("18000.00"))
                .status(BookingStatus.CANCELLED)
                .build()
        );

        // Booking 4: Non-overlapping (Dec 15 - Dec 20), check-in matches requested check-out -> No overlap in standard hotel night semantics
        bookingRepository.save(
            Booking.builder()
                .bookingReference("TCT-TEST04")
                .hotel(sampleHotel)
                .guestName("Neha Verma")
                .guestEmail("neha@example.com")
                .checkInDate(LocalDate.of(2026, 12, 15))
                .checkOutDate(LocalDate.of(2026, 12, 20))
                .nights(5)
                .baseAmount(new BigDecimal("25000.00"))
                .discountAmount(new BigDecimal("2500.00"))
                .totalAmount(new BigDecimal("22500.00"))
                .status(BookingStatus.CONFIRMED)
                .build()
        );

        // Query overlapping active bookings for Dec 10 to Dec 15
        long overlappingCount = bookingRepository.countActiveOverlappingBookings(
            sampleHotel.getId(),
            stayStart,
            stayEnd
        );

        // Only Booking 1 and Booking 2 should be counted (2 active overlapping bookings)
        assertThat(overlappingCount).isEqualTo(2);

        // Available rooms = totalRooms (3) - overlappingCount (2) = 1 room left
        int availableRooms = sampleHotel.getTotalRooms() - (int) overlappingCount;
        assertThat(availableRooms).isEqualTo(1);
    }
}
