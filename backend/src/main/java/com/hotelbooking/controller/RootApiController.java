package com.hotelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root controller providing API discovery and server health status.
 * Prevents NoResourceFoundException when visiting http://localhost:8080/ in a browser.
 */
@RestController
@CrossOrigin(origins = "*")
public class RootApiController {

    @GetMapping({"/", "/api"})
    public ResponseEntity<Map<String, Object>> getApiStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "StayHaven Hotel Booking Portal API");
        response.put("version", "1.0.0");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("getHotels", "GET /api/hotels");
        endpoints.put("searchHotels", "GET /api/hotels?location={location}&checkIn={checkIn}&checkOut={checkOut}");
        endpoints.put("getHotelDetails", "GET /api/hotels/{id}?checkIn={checkIn}&checkOut={checkOut}");
        endpoints.put("createBooking", "POST /api/bookings");
        endpoints.put("lookupBooking", "GET /api/bookings/lookup?reference={reference}&email={email}");
        endpoints.put("cancelBooking", "POST /api/bookings/{reference}/cancel");

        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
}
