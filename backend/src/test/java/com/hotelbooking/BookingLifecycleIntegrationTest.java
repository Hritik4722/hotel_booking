package com.hotelbooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.BookingRequestDTO;
import com.hotelbooking.dto.BookingResponseDTO;
import com.hotelbooking.dto.CancelBookingRequestDTO;
import com.hotelbooking.dto.PriceBreakdownDTO;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.exception.InvalidDateException;
import com.hotelbooking.exception.RoomUnavailableException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingLifecycleIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();

        // Create a test hotel with exactly 2 rooms for deterministic capacity testing
        testHotel = hotelRepository.save(
            Hotel.builder()
                .name("Boutique Test Resort")
                .location("Goa")
                .description("Intimate seaside boutique property with limited rooms.")
                .amenities("WiFi, Pool, Breakfast")
                .pricePerNight(new BigDecimal("4000.00"))
                .totalRooms(2)
                .build()
        );
    }

    // =========================================================================
    // 1. Correct Price Calculation (Including 10% Long-Stay Discount for 4+ Nights)
    // =========================================================================

    @Test
    @DisplayName("Price Calculation: 3 nights should calculate base amount with NO discount")
    void testPriceCalculationForShortStay() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(3); // 3 nights

        PriceBreakdownDTO breakdown = hotelService.calculatePriceBreakdown(
            testHotel.getPricePerNight(), checkIn, checkOut
        );

        assertThat(breakdown.getNights()).isEqualTo(3);
        // 3 * 4000 = 12,000.00
        assertThat(breakdown.getBaseAmount()).isEqualByComparingTo("12000.00");
        assertThat(breakdown.isDiscountApplied()).isFalse();
        assertThat(breakdown.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo("12000.00");
    }

    @Test
    @DisplayName("Price Calculation: 4 nights should automatically apply 10% Smart Stay discount")
    void testPriceCalculationForFourNightsThreshold() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(4); // Exactly 4 nights

        PriceBreakdownDTO breakdown = hotelService.calculatePriceBreakdown(
            testHotel.getPricePerNight(), checkIn, checkOut
        );

        assertThat(breakdown.getNights()).isEqualTo(4);
        // Base: 4 * 4000 = 16,000.00
        assertThat(breakdown.getBaseAmount()).isEqualByComparingTo("16000.00");
        assertThat(breakdown.isDiscountApplied()).isTrue();
        // 10% discount: 1,600.00
        assertThat(breakdown.getDiscountAmount()).isEqualByComparingTo("1600.00");
        // Total payable: 16,000 - 1,600 = 14,400.00
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo("14400.00");
    }

    @Test
    @DisplayName("Price Calculation: 7 nights should apply 10% Smart Stay discount on entire stay")
    void testPriceCalculationForSevenNights() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(7); // 7 nights

        PriceBreakdownDTO breakdown = hotelService.calculatePriceBreakdown(
            testHotel.getPricePerNight(), checkIn, checkOut
        );

        assertThat(breakdown.getNights()).isEqualTo(7);
        // Base: 7 * 4000 = 28,000.00
        assertThat(breakdown.getBaseAmount()).isEqualByComparingTo("28000.00");
        assertThat(breakdown.isDiscountApplied()).isTrue();
        // 10% discount: 2,800.00
        assertThat(breakdown.getDiscountAmount()).isEqualByComparingTo("2800.00");
        // Total payable: 28,000 - 2,800 = 25,200.00
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo("25200.00");
    }

    // =========================================================================
    // 2. Date Validation Failures (Check-in in past, Check-out before check-in)
    // =========================================================================

    @Test
    @DisplayName("Date Validation: Check-in in the past should be rejected with InvalidDateException")
    void testDateValidationPastCheckIn() {
        LocalDate pastCheckIn = LocalDate.now().minusDays(1);
        LocalDate futureCheckOut = LocalDate.now().plusDays(3);

        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(testHotel.getId())
            .guestName("Ananya Roy")
            .guestEmail("ananya@example.com")
            .checkInDate(pastCheckIn)
            .checkOutDate(futureCheckOut)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    @DisplayName("Date Validation: Check-out before check-in should be rejected with InvalidDateException")
    void testDateValidationCheckOutBeforeCheckIn() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(3); // Earlier than check-in

        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(testHotel.getId())
            .guestName("Ananya Roy")
            .guestEmail("ananya@example.com")
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-out date must be strictly after check-in date");
    }

    @Test
    @DisplayName("Date Validation: Check-out on same day as check-in (0 nights) should be rejected")
    void testDateValidationSameDayCheckInOut() {
        LocalDate sameDay = LocalDate.now().plusDays(5);

        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(testHotel.getId())
            .guestName("Ananya Roy")
            .guestEmail("ananya@example.com")
            .checkInDate(sameDay)
            .checkOutDate(sameDay)
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-out date must be strictly after check-in date");
    }

    // =========================================================================
    // 3. Overlapping Stay Availability & Blocking Bookings when Capacity is 0
    // =========================================================================

    @Test
    @DisplayName("Availability & Capacity: Blocks bookings when capacity reaches 0, allows non-overlapping")
    void testOverlappingAvailabilityAndSoldOutRejection() {
        LocalDate stayStart = LocalDate.of(2026, 12, 10);
        LocalDate stayEnd = LocalDate.of(2026, 12, 15);

        // Booking 1: Takes Room 1 of 2
        BookingResponseDTO booking1 = bookingService.createBooking(
            BookingRequestDTO.builder()
                .hotelId(testHotel.getId())
                .guestName("Guest One")
                .guestEmail("guest1@example.com")
                .checkInDate(stayStart)
                .checkOutDate(stayEnd)
                .build()
        );
        assertThat(booking1.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // Booking 2: Overlaps partially (Dec 12 to Dec 17) -> Takes Room 2 of 2
        BookingResponseDTO booking2 = bookingService.createBooking(
            BookingRequestDTO.builder()
                .hotelId(testHotel.getId())
                .guestName("Guest Two")
                .guestEmail("guest2@example.com")
                .checkInDate(LocalDate.of(2026, 12, 12))
                .checkOutDate(LocalDate.of(2026, 12, 17))
                .build()
        );
        assertThat(booking2.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // Available rooms between Dec 12 and Dec 15 is now 0 (Total: 2, Active: 2)
        long overlapping = bookingRepository.countActiveOverlappingBookings(
            testHotel.getId(), LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 14)
        );
        assertThat(overlapping).isEqualTo(2);

        // Booking 3: Attempting to book during overlapping period (Dec 13 to Dec 14) -> MUST BE BLOCKED
        BookingRequestDTO soldOutRequest = BookingRequestDTO.builder()
            .hotelId(testHotel.getId())
            .guestName("Guest Three")
            .guestEmail("guest3@example.com")
            .checkInDate(LocalDate.of(2026, 12, 13))
            .checkOutDate(LocalDate.of(2026, 12, 14))
            .build();

        assertThatThrownBy(() -> bookingService.createBooking(soldOutRequest))
            .isInstanceOf(RoomUnavailableException.class)
            .hasMessageContaining("no rooms are available");

        // Non-overlapping stay (Dec 17 onwards) -> MUST SUCCEED
        BookingResponseDTO bookingAfter = bookingService.createBooking(
            BookingRequestDTO.builder()
                .hotelId(testHotel.getId())
                .guestName("Guest Four")
                .guestEmail("guest4@example.com")
                .checkInDate(LocalDate.of(2026, 12, 17))
                .checkOutDate(LocalDate.of(2026, 12, 20))
                .build()
        );
        assertThat(bookingAfter.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // =========================================================================
    // 4. Cancellation Releasing Room Capacity for Subsequent Bookings
    // =========================================================================

    @Test
    @DisplayName("Cancellation: Cancelling a booking immediately restores capacity for another guest")
    void testCancellationReleasesRoomCapacity() throws Exception {
        // Hotel has 2 rooms. Let's saturate capacity with 2 bookings for Dec 20 to Dec 25.
        LocalDate checkIn = LocalDate.of(2026, 12, 20);
        LocalDate checkOut = LocalDate.of(2026, 12, 25);

        BookingResponseDTO bookingA = bookingService.createBooking(
            BookingRequestDTO.builder()
                .hotelId(testHotel.getId())
                .guestName("Rohan Das")
                .guestEmail("rohan@example.com")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build()
        );

        BookingResponseDTO bookingB = bookingService.createBooking(
            BookingRequestDTO.builder()
                .hotelId(testHotel.getId())
                .guestName("Simran Kaur")
                .guestEmail("simran@example.com")
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build()
        );

        // Third guest tries to book same dates -> Fails with HTTP 409 Conflict
        BookingRequestDTO requestC = BookingRequestDTO.builder()
            .hotelId(testHotel.getId())
            .guestName("Vikram Singh")
            .guestEmail("vikram@example.com")
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .build();

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestC)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));

        // Now, Rohan cancels his booking via the cancellation API endpoint
        CancelBookingRequestDTO cancelRequest = CancelBookingRequestDTO.builder()
            .email("rohan@example.com")
            .build();

        mockMvc.perform(post("/api/bookings/" + bookingA.getBookingReference() + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify active overlapping bookings dropped from 2 down to 1
        long activeOverlapping = bookingRepository.countActiveOverlappingBookings(
            testHotel.getId(), checkIn, checkOut
        );
        assertThat(activeOverlapping).isEqualTo(1);

        // Vikram now retries his booking for the exact same dates -> SUCCEEDS!
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestC)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bookingReference").exists())
            .andExpect(jsonPath("$.guestName").value("Vikram Singh"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Capacity is once again saturated (2 active bookings)
        long postBookingActive = bookingRepository.countActiveOverlappingBookings(
            testHotel.getId(), checkIn, checkOut
        );
        assertThat(postBookingActive).isEqualTo(2);
    }
}
