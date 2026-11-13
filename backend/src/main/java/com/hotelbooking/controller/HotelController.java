package com.hotelbooking.controller;

import com.hotelbooking.dto.HotelDetailDTO;
import com.hotelbooking.dto.HotelSummaryDTO;
import com.hotelbooking.service.HotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * Search hotels with optional location filtering and dynamic date-based availability.
     * GET /api/hotels?location=&checkIn=&checkOut=
     */
    @GetMapping
    public ResponseEntity<List<HotelSummaryDTO>> getHotels(
        @RequestParam(required = false) String location,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        List<HotelSummaryDTO> hotels = hotelService.searchHotels(location, checkIn, checkOut);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Retrieve full details for a single hotel, including availability and pricing breakdown for the selected dates.
     * GET /api/hotels/{id}?checkIn=&checkOut=
     */
    @GetMapping("/{id}")
    public ResponseEntity<HotelDetailDTO> getHotelById(
        @PathVariable Long id,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        HotelDetailDTO hotel = hotelService.getHotelDetails(id, checkIn, checkOut);
        return ResponseEntity.ok(hotel);
    }
}
