package com.hotelbooking.service;

import com.hotelbooking.dto.HotelDetailDTO;
import com.hotelbooking.dto.HotelSummaryDTO;
import com.hotelbooking.dto.PriceBreakdownDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.exception.InvalidDateException;
import com.hotelbooking.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private HotelService hotelService;

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
    @DisplayName("Should apply 10% discount for long stays (4 or more nights)")
    void testLongStayDiscountApplied() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(5); // 5 nights

        PriceBreakdownDTO breakdown = hotelService.calculatePriceBreakdown(
            sampleHotel.getPricePerNight(), checkIn, checkOut
        );

        assertThat(breakdown.getNights()).isEqualTo(5);
        // Base: 5 * 5000 = 25000.00
        assertThat(breakdown.getBaseAmount()).isEqualByComparingTo("25000.00");
        // Discount 10%: 2500.00
        assertThat(breakdown.isDiscountApplied()).isTrue();
        assertThat(breakdown.getDiscountPercentage()).isEqualByComparingTo("10.00");
        assertThat(breakdown.getDiscountAmount()).isEqualByComparingTo("2500.00");
        // Total: 25000 - 2500 = 22500.00
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo("22500.00");
    }

    @Test
    @DisplayName("Should NOT apply discount for short stays (less than 4 nights)")
    void testShortStayNoDiscount() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3); // 3 nights

        PriceBreakdownDTO breakdown = hotelService.calculatePriceBreakdown(
            sampleHotel.getPricePerNight(), checkIn, checkOut
        );

        assertThat(breakdown.getNights()).isEqualTo(3);
        assertThat(breakdown.getBaseAmount()).isEqualByComparingTo("15000.00");
        assertThat(breakdown.isDiscountApplied()).isFalse();
        assertThat(breakdown.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("Should assign HIGH_DEMAND status when only 1 room remains")
    void testDemandStatusHighDemand() {
        when(hotelRepository.findAll()).thenReturn(List.of(sampleHotel));
        // Hotel has 3 total rooms; 2 overlapping bookings -> 1 available
        when(bookingRepository.countActiveOverlappingBookings(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(2L);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        List<HotelSummaryDTO> results = hotelService.searchHotels(null, checkIn, checkOut);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAvailableRooms()).isEqualTo(1);
        assertThat(results.get(0).getDemandStatus()).isEqualTo("HIGH_DEMAND");
    }

    @Test
    @DisplayName("Should assign SOLD_OUT status when 0 rooms remain")
    void testDemandStatusSoldOut() {
        when(hotelRepository.findAll()).thenReturn(List.of(sampleHotel));
        // Hotel has 3 total rooms; 3 overlapping bookings -> 0 available
        when(bookingRepository.countActiveOverlappingBookings(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(3L);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        List<HotelSummaryDTO> results = hotelService.searchHotels(null, checkIn, checkOut);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAvailableRooms()).isEqualTo(0);
        assertThat(results.get(0).getDemandStatus()).isEqualTo("SOLD_OUT");
    }

    @Test
    @DisplayName("Should fail date validation if check-in is in the past")
    void testPastCheckInFails() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate futureDate = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> hotelService.validateDates(pastDate, futureDate))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    @DisplayName("Should fail date validation if check-out is on or before check-in")
    void testCheckOutBeforeCheckInFails() {
        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> hotelService.validateDates(checkIn, checkOut))
            .isInstanceOf(InvalidDateException.class)
            .hasMessageContaining("Check-out date must be strictly after check-in date");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if hotel ID does not exist")
    void testHotelNotFound() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelDetails(999L, null, null))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Hotel not found with id: 999");
    }
}
