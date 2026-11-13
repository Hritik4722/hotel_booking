package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.BookingRequestDTO;
import com.hotelbooking.dto.BookingResponseDTO;
import com.hotelbooking.dto.CancelBookingRequestDTO;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.exception.RoomUnavailableException;
import com.hotelbooking.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("POST /api/bookings - should create booking and return 201 Created")
    void testCreateBookingSuccess() throws Exception {
        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(1L)
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(LocalDate.now().plusDays(2))
            .checkOutDate(LocalDate.now().plusDays(5))
            .build();

        BookingResponseDTO response = BookingResponseDTO.builder()
            .id(1L)
            .bookingReference("TCT-ABC123")
            .hotelId(1L)
            .hotelName("Taj Vista Coastal Resort")
            .hotelLocation("Goa")
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(request.getCheckInDate())
            .checkOutDate(request.getCheckOutDate())
            .nights(3)
            .pricePerNight(new BigDecimal("5000.00"))
            .baseAmount(new BigDecimal("15000.00"))
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("15000.00"))
            .status(BookingStatus.CONFIRMED)
            .build();

        when(bookingService.createBooking(any(BookingRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bookingReference").value("TCT-ABC123"))
            .andExpect(jsonPath("$.guestName").value("Rahul Sharma"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.totalAmount").value(15000.00));
    }

    @Test
    @DisplayName("POST /api/bookings - should return 400 Bad Request with fieldErrors on invalid request")
    void testCreateBookingValidationFailure() throws Exception {
        BookingRequestDTO invalidRequest = BookingRequestDTO.builder()
            .hotelId(null) // Null hotelId
            .guestName("") // Blank guestName
            .guestEmail("invalid-email") // Invalid email format
            .checkInDate(null)
            .checkOutDate(null)
            .build();

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.fieldErrors.hotelId").exists())
            .andExpect(jsonPath("$.fieldErrors.guestName").exists())
            .andExpect(jsonPath("$.fieldErrors.guestEmail").exists())
            .andExpect(jsonPath("$.fieldErrors.checkInDate").exists())
            .andExpect(jsonPath("$.fieldErrors.checkOutDate").exists());
    }

    @Test
    @DisplayName("POST /api/bookings - should return 409 Conflict when rooms are sold out")
    void testCreateBookingSoldOutConflict() throws Exception {
        BookingRequestDTO request = BookingRequestDTO.builder()
            .hotelId(1L)
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .checkInDate(LocalDate.now().plusDays(2))
            .checkOutDate(LocalDate.now().plusDays(5))
            .build();

        when(bookingService.createBooking(any(BookingRequestDTO.class)))
            .thenThrow(new RoomUnavailableException("Sorry, no rooms are available at Taj Vista Coastal Resort"));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Sorry, no rooms are available at Taj Vista Coastal Resort"));
    }

    @Test
    @DisplayName("GET /api/bookings/lookup - should return 200 OK with booking details")
    void testLookupBookingSuccess() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
            .id(1L)
            .bookingReference("TCT-ABC123")
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .status(BookingStatus.CONFIRMED)
            .build();

        when(bookingService.lookupBooking(eq("TCT-ABC123"), eq("rahul@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/bookings/lookup")
                .param("reference", "TCT-ABC123")
                .param("email", "rahul@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookingReference").value("TCT-ABC123"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/bookings/lookup - should return 404 Not Found when reference not found")
    void testLookupBookingNotFound() throws Exception {
        when(bookingService.lookupBooking(any(), any()))
            .thenThrow(new ResourceNotFoundException("No booking found with given credentials"));

        mockMvc.perform(get("/api/bookings/lookup")
                .param("reference", "TCT-UNKNOWN")
                .param("email", "none@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("No booking found with given credentials"));
    }

    @Test
    @DisplayName("POST /api/bookings/{reference}/cancel - should cancel booking and return updated DTO")
    void testCancelBookingSuccess() throws Exception {
        CancelBookingRequestDTO cancelRequest = CancelBookingRequestDTO.builder()
            .email("rahul@example.com")
            .build();

        BookingResponseDTO response = BookingResponseDTO.builder()
            .id(1L)
            .bookingReference("TCT-ABC123")
            .guestName("Rahul Sharma")
            .guestEmail("rahul@example.com")
            .status(BookingStatus.CANCELLED)
            .build();

        when(bookingService.cancelBooking(eq("TCT-ABC123"), eq("rahul@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/bookings/TCT-ABC123/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookingReference").value("TCT-ABC123"))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/bookings/{reference}/cancel - should return 400 Bad Request on invalid/blank email in request body")
    void testCancelBookingValidationFailure() throws Exception {
        CancelBookingRequestDTO invalidRequest = CancelBookingRequestDTO.builder()
            .email("not-a-valid-email")
            .build();

        mockMvc.perform(post("/api/bookings/TCT-ABC123/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
