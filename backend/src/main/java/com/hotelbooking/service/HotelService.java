package com.hotelbooking.service;

import com.hotelbooking.dto.HotelDetailDTO;
import com.hotelbooking.dto.HotelSummaryDTO;
import com.hotelbooking.dto.PriceBreakdownDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.exception.InvalidDateException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public HotelService(HotelRepository hotelRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Searches hotels with optional location filter and dynamic date-based room availability.
     */
    public List<HotelSummaryDTO> searchHotels(String location, LocalDate checkIn, LocalDate checkOut) {
        boolean hasDates = (checkIn != null || checkOut != null);
        if (hasDates) {
            validateDates(checkIn, checkOut);
        }

        List<Hotel> hotels;
        if (location != null && !location.trim().isBlank()) {
            hotels = hotelRepository.findByLocationContainingIgnoreCase(location.trim());
        } else {
            hotels = hotelRepository.findAll();
        }

        return hotels.stream()
            .map(hotel -> mapToSummaryDTO(hotel, checkIn, checkOut, hasDates))
            .toList();
    }

    /**
     * Retrieves full details for a single hotel, including real-time availability and pricing breakdown.
     */
    public HotelDetailDTO getHotelDetails(Long id, LocalDate checkIn, LocalDate checkOut) {
        Hotel hotel = hotelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        boolean hasDates = (checkIn != null || checkOut != null);
        if (hasDates) {
            validateDates(checkIn, checkOut);
        }

        int availableRooms;
        PriceBreakdownDTO priceBreakdown = null;

        if (hasDates) {
            long overlappingBookings = bookingRepository.countActiveOverlappingBookings(hotel.getId(), checkIn, checkOut);
            availableRooms = Math.max(0, hotel.getTotalRooms() - (int) overlappingBookings);
            priceBreakdown = calculatePriceBreakdown(hotel.getPricePerNight(), checkIn, checkOut);
        } else {
            availableRooms = hotel.getTotalRooms();
        }

        String demandStatus = determineDemandStatus(availableRooms);

        return HotelDetailDTO.builder()
            .id(hotel.getId())
            .name(hotel.getName())
            .location(hotel.getLocation())
            .description(hotel.getDescription())
            .amenities(hotel.getAmenities())
            .pricePerNight(hotel.getPricePerNight())
            .totalRooms(hotel.getTotalRooms())
            .availableRooms(availableRooms)
            .demandStatus(demandStatus)
            .priceBreakdown(priceBreakdown)
            .build();
    }

    /**
     * Surprise Feature Implementation: "Smart Stay Pricing & Demand Insights"
     * Calculates nightly stay pricing and automatically applies a 10% Long-Stay Discount
     * if the guest stays for 4 or more nights.
     */
    public PriceBreakdownDTO calculatePriceBreakdown(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal baseAmount = pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountPercentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean discountApplied = false;
        String discountDescription = null;

        if (nights >= 4) {
            discountPercentage = new BigDecimal("10.00");
            discountAmount = baseAmount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            discountApplied = true;
            discountDescription = "Smart Stay: 10% Long-Stay Discount applied for 4+ nights";
        }

        BigDecimal totalAmount = baseAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdownDTO.builder()
            .nights(nights)
            .pricePerNight(pricePerNight)
            .baseAmount(baseAmount)
            .discountPercentage(discountPercentage)
            .discountAmount(discountAmount)
            .totalAmount(totalAmount)
            .discountApplied(discountApplied)
            .discountDescription(discountDescription)
            .build();
    }

    /**
     * Determines demand status insight based on remaining room count.
     */
    public String determineDemandStatus(int availableRooms) {
        if (availableRooms <= 0) {
            return "SOLD_OUT";
        } else if (availableRooms == 1) {
            return "HIGH_DEMAND";
        } else {
            return "AVAILABLE";
        }
    }

    /**
     * Validates date constraints:
     * 1. Both dates must be present
     * 2. Check-in cannot be in the past
     * 3. Check-out must be after check-in
     */
    public void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDateException("Both check-in and check-out dates must be provided");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidDateException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateException("Check-out date must be strictly after check-in date");
        }
    }

    private HotelSummaryDTO mapToSummaryDTO(Hotel hotel, LocalDate checkIn, LocalDate checkOut, boolean hasDates) {
        int availableRooms;
        PriceBreakdownDTO priceBreakdown = null;

        if (hasDates) {
            long overlappingBookings = bookingRepository.countActiveOverlappingBookings(hotel.getId(), checkIn, checkOut);
            availableRooms = Math.max(0, hotel.getTotalRooms() - (int) overlappingBookings);
            priceBreakdown = calculatePriceBreakdown(hotel.getPricePerNight(), checkIn, checkOut);
        } else {
            availableRooms = hotel.getTotalRooms();
        }

        String demandStatus = determineDemandStatus(availableRooms);

        return HotelSummaryDTO.builder()
            .id(hotel.getId())
            .name(hotel.getName())
            .location(hotel.getLocation())
            .description(hotel.getDescription())
            .amenities(hotel.getAmenities())
            .pricePerNight(hotel.getPricePerNight())
            .totalRooms(hotel.getTotalRooms())
            .availableRooms(availableRooms)
            .demandStatus(demandStatus)
            .priceBreakdown(priceBreakdown)
            .build();
    }
}
