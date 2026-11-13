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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookingService {

    private static final String REFERENCE_PREFIX = "TCT-";
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final HotelService hotelService;

    public BookingService(
        BookingRepository bookingRepository,
        HotelRepository hotelRepository,
        HotelService hotelService
    ) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.hotelService = hotelService;
    }

    /**
     * Creates a new booking with concurrency protection via pessimistic write lock on the hotel row.
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        validateStayDates(request.getCheckInDate(), request.getCheckOutDate());

        // 1. Acquire pessimistic write lock on Hotel row (SELECT ... FOR UPDATE)
        Hotel hotel = hotelRepository.findByIdWithLock(request.getHotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.getHotelId()));

        // 2. Count active overlapping bookings under the lock
        long overlappingCount = bookingRepository.countActiveOverlappingBookings(
            hotel.getId(),
            request.getCheckInDate(),
            request.getCheckOutDate()
        );

        int availableRooms = hotel.getTotalRooms() - (int) overlappingCount;
        if (availableRooms <= 0) {
            throw new RoomUnavailableException(
                "Sorry, no rooms are available at " + hotel.getName() + " for the selected dates."
            );
        }

        // 3. Compute pricing strictly on the backend (including 10% discount for 4+ nights)
        PriceBreakdownDTO priceBreakdown = hotelService.calculatePriceBreakdown(
            hotel.getPricePerNight(),
            request.getCheckInDate(),
            request.getCheckOutDate()
        );

        // 4. Generate unique collision-free booking reference (e.g., TCT-8K3M9P)
        String bookingReference = generateUniqueBookingReference();

        // 5. Build and persist booking entity
        Booking booking = Booking.builder()
            .bookingReference(bookingReference)
            .hotel(hotel)
            .guestName(request.getGuestName().trim())
            .guestEmail(request.getGuestEmail().trim().toLowerCase())
            .checkInDate(request.getCheckInDate())
            .checkOutDate(request.getCheckOutDate())
            .nights(priceBreakdown.getNights())
            .baseAmount(priceBreakdown.getBaseAmount())
            .discountAmount(priceBreakdown.getDiscountAmount())
            .totalAmount(priceBreakdown.getTotalAmount())
            .status(BookingStatus.CONFIRMED)
            .build();

        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponseDTO(savedBooking, priceBreakdown);
    }

    /**
     * Retrieves a booking by unique reference and email (case-insensitive).
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO lookupBooking(String reference, String email) {
        if (reference == null || reference.trim().isBlank() || email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Booking reference and guest email are required for lookup");
        }

        Booking booking = bookingRepository
            .findByBookingReferenceIgnoreCaseAndGuestEmailIgnoreCase(reference.trim(), email.trim())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No booking found matching reference '" + reference.trim() + "' and email '" + email.trim() + "'"
            ));

        PriceBreakdownDTO priceBreakdown = hotelService.calculatePriceBreakdown(
            booking.getHotel().getPricePerNight(),
            booking.getCheckInDate(),
            booking.getCheckOutDate()
        );

        return mapToResponseDTO(booking, priceBreakdown);
    }

    /**
     * Cancels an existing booking after verifying the guest's email.
     * Setting status to CANCELLED immediately releases room availability.
     */
    @Transactional
    public BookingResponseDTO cancelBooking(String reference, String email) {
        if (reference == null || reference.trim().isBlank() || email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Booking reference and guest email are required for cancellation");
        }

        Booking booking = bookingRepository.findByBookingReference(reference.trim())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No booking found with reference: " + reference.trim()
            ));

        if (!booking.getGuestEmail().equalsIgnoreCase(email.trim())) {
            throw new InvalidBookingStateException("Provided email does not match the email on the booking record.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("This booking has already been cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        PriceBreakdownDTO priceBreakdown = hotelService.calculatePriceBreakdown(
            updatedBooking.getHotel().getPricePerNight(),
            updatedBooking.getCheckInDate(),
            updatedBooking.getCheckOutDate()
        );

        return mapToResponseDTO(updatedBooking, priceBreakdown);
    }

    private void validateStayDates(LocalDate checkIn, LocalDate checkOut) {
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

    private String generateUniqueBookingReference() {
        String reference;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(REFERENCE_PREFIX);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
                sb.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
            }
            reference = sb.toString();
            attempts++;
            if (attempts > 20) {
                // Failsafe timestamp suffix if an extraordinary collision happens
                reference = REFERENCE_PREFIX + System.currentTimeMillis();
                break;
            }
        } while (bookingRepository.existsByBookingReference(reference));

        return reference;
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking, PriceBreakdownDTO priceBreakdown) {
        return BookingResponseDTO.builder()
            .id(booking.getId())
            .bookingReference(booking.getBookingReference())
            .hotelId(booking.getHotel().getId())
            .hotelName(booking.getHotel().getName())
            .hotelLocation(booking.getHotel().getLocation())
            .guestName(booking.getGuestName())
            .guestEmail(booking.getGuestEmail())
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .nights(booking.getNights())
            .pricePerNight(booking.getHotel().getPricePerNight())
            .baseAmount(booking.getBaseAmount())
            .discountAmount(booking.getDiscountAmount())
            .totalAmount(booking.getTotalAmount())
            .status(booking.getStatus())
            .createdAt(booking.getCreatedAt())
            .priceBreakdown(priceBreakdown)
            .build();
    }
}
