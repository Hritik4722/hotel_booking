package com.hotelbooking.controller;

import com.hotelbooking.dto.BookingRequestDTO;
import com.hotelbooking.dto.BookingResponseDTO;
import com.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking.
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve an existing booking by booking reference and guest email.
     * GET /api/bookings/lookup?reference=&email=
     */
    @GetMapping("/lookup")
    public ResponseEntity<BookingResponseDTO> lookupBooking(
        @RequestParam String reference,
        @RequestParam String email
    ) {
        BookingResponseDTO response = bookingService.lookupBooking(reference, email);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an existing booking after verifying the guest's email.
     * POST /api/bookings/{reference}/cancel?email=
     */
    @PostMapping("/{reference}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(
        @PathVariable String reference,
        @RequestParam String email
    ) {
        BookingResponseDTO response = bookingService.cancelBooking(reference, email);
        return ResponseEntity.ok(response);
    }
}
