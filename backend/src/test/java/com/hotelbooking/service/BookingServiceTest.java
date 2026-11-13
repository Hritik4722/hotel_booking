package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequestDTO;
import com.hotelbooking.dto.BookingResponseDTO;
import com.hotelbooking.dto.PriceBreakdownDTO;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.exception.InvalidBookingStateException;
import com.hotelbooking.exception.InvalidDateException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.exception.RoomUnavailableException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private BookingService bookingService;

    private Hotel sampleHotel;

    @BeforeEach
    void setUp() {
        sampleHotel = Hotel.builder()
            .id(1L)
            .name("Taj Vista Coastal Resort")
            .location("Goa")
            .description("Luxury beachfront resort in South Goa.")
            .amenities("Beach Access, Pool, Spa, Free WiFi")
            .pricePerNight(new BigDecimal("5000.00"))
            .totalRooms(3)
            .build();
    }

    @Test
    @DisplayName("Should create booking successfully when rooms are available")
    void testCreateBookingSuccess() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(4); // 4 nights -> 10% discount

        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(1L)
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .build();

        PriceBreakdownDTO breakdown = PriceBreakdownDTO.builder()
            .nights(4)
            .pricePerNight(new BigDecimal("5000.00"))
            .baseAmount(new BigDecimal("20000.00"))
            .discountPercentage(new BigDecimal("10.00"))
            .discountAmount(new BigDecimal("2000.00"))
            .totalAmount(new BigDecimal("18000.00"))
            .discountApplied(true)
            .build();

        when(hotelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sampleHotel));
        when(bookingRepository.countActiveOverlappingBookings(1L, checkIn, checkOut)).thenReturn(1L);
        when(hotelService.calculatePriceBreakdown(sampleHotel.getPricePerNight(), checkIn, checkOut))
            .thenReturn(breakdown);
        when(bookingRepository.existsByBookingReference(any())).thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponseDTO response = bookingService.createBooking(request);

        assertThat(response).isNotNull();
        assertThat(response.getBookingReference()).startsWith("TCT-");
        assertThat(response.getGuestName()).isEqualTo("Rahul Sharma");
        assertThat(response.getGuestEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("18000.00");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw RoomUnavailableException when all rooms are booked")
    void testCreateBookingSoldOutThrowsException() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(4);

        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(1L)
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .build();

        when(hotelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sampleHotel));
        // Hotel has 3 rooms; 3 overlapping bookings
        when(bookingRepository.countActiveOverlappingBookings(1L, checkIn, checkOut)).thenReturn(3L);

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(RoomUnavailableException.class)
            .hasMessageContaining("no rooms are available");
    }

    @Test
    @DisplayName("Should throw InvalidDateException when check-in is in the past")
    void testCreateBookingPastDateThrowsException() {
        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(1L)
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(LocalDate.now().minusDays(1))
            .checkOutDate(LocalDate.now().plusDays(2))
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    @DisplayName("Should lookup booking by reference and email case-insensitively")
    void testLookupBookingSuccess() {
        Booking booking = Booking.builder()
            .id(101L)
            .bookingReference("TCT-XYZ123")
            .hotel(sampleHotel)
            .guestName("Aarav Mehta")
            .guestEmail("aarav@example.com")
            .checkInDate(LocalDate.now().plusDays(5))
            .checkOutDate(LocalDate.now().plusDays(7))
            .nights(2)
            .baseAmount(new BigDecimal("10000.00"))
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("10000.00"))
            .status(BookingStatus.CONFIRMED)
            .build();

        when(bookingRepository.findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase("tct-xyz123", "AARAV@EXAMPLE.COM"))
            .thenReturn(Optional.of(booking));
        when(hotelService.calculatePriceBreakdown(any(), any(), any()))
            .thenReturn(PriceBreakdownDTO.builder().nights(2).totalAmount(new BigDecimal("10000.00")).build());

        BookingResponseDTO response = bookingService.lookupBooking("tct-xyz123", "AARAV@EXAMPLE.COM");

        assertThat(response.getBookingReference()).isEqualTo("TCT-XYZ123");
        assertThat(response.getGuestName()).isEqualTo("Aarav Mehta");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should successfully cancel confirmed booking and verify state change")
    void testCancelBookingSuccess() {
        Booking booking = Booking.builder()
            .id(101L)
            .bookingReference("TCT-XYZ123")
            .hotel(sampleHotel)
            .guestName("Aarav Mehta")
            .guestEmail("aarav@example.com")
            .checkInDate(LocalDate.now().plusDays(5))
            .checkOutDate(LocalDate.now().plusDays(7))
            .nights(2)
            .baseAmount(new BigDecimal("10000.00"))
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("10000.00"))
            .status(BookingStatus.CONFIRMED)
            .build();

        when(bookingRepository.findByBookingReference("TCT-XYZ123")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(hotelService.calculatePriceBreakdown(any(), any(), any()))
            .thenReturn(PriceBreakdownDTO.builder().nights(2).totalAmount(new BigDecimal("10000.00")).build());

        BookingResponseDTO response = bookingService.cancelBooking("TCT-XYZ123", "aarav@example.com");

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("Should reject cancellation if email does not match")
    void testCancelBookingEmailMismatchThrowsException() {
        Booking booking = Booking.builder()
            .id(101L)
            .bookingReference("TCT-XYZ123")
            .hotel(sampleHotel)
            .guestName("Aarav Mehta")
            .guestEmail("aarav@example.com")
            .status(BookingStatus.CONFIRMED)
            .build();

        when(bookingRepository.findByBookingReference("TCT-XYZ123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("TCT-XYZ123", "wrong@example.com"))
            .isInstanceOf(InvalidBookingStateException.class)
            .hasMessageContaining("Provided email does not match");
    }

    @Test
    @DisplayName("Should reject cancellation if booking is already cancelled")
    void testCancelAlreadyCancelledBookingThrowsException() {
        Booking booking = Booking.builder()
            .id(101L)
            .bookingReference("TCT-XYZ123")
            .hotel(sampleHotel)
            .guestName("Aarav Mehta")
            .guestEmail("aarav@example.com")
            .status(BookingStatus.CANCELLED)
            .build();

        when(bookingRepository.findByBookingReference("TCT-XYZ123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("TCT-XYZ123", "aarav@example.com"))
            .isInstanceOf(InvalidBookingStateException.class)
            .hasMessageContaining("already been cancelled");
    }
}
