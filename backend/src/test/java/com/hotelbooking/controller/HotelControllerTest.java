package com.hotelbooking.controller;

import com.hotelbooking.dto.HotelDetailDTO;
import com.hotelbooking.dto.HotelSummaryDTO;
import com.hotelbooking.dto.PriceBreakdownDTO;
import com.hotelbooking.service.HotelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Test
    @DisplayName("GET /api/hotels - should return list of hotel summaries")
    void testGetHotels() throws Exception {
        HotelSummaryDTO summary = HotelSummaryDTO.builder()
            .id(1L)
            .name("The Grand Heritage Palace")
            .location("Udaipur")
            .description("Royal retreat by Lake Pichola")
            .amenities("WiFi, Pool, Spa")
            .pricePerNight(new BigDecimal("7500.00"))
            .totalRooms(4)
            .availableRooms(3)
            .demandStatus("AVAILABLE")
            .build();

        when(hotelService.searchHotels(any(), any(), any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("The Grand Heritage Palace"))
            .andExpect(jsonPath("$[0].location").value("Udaipur"))
            .andExpect(jsonPath("$[0].availableRooms").value(3))
            .andExpect(jsonPath("$[0].demandStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/hotels/{id} - should return hotel details with price breakdown")
    void testGetHotelByIdWithBreakdown() throws Exception {
        PriceBreakdownDTO priceBreakdown = PriceBreakdownDTO.builder()
            .nights(5)
            .pricePerNight(new BigDecimal("7500.00"))
            .baseAmount(new BigDecimal("37500.00"))
            .discountPercentage(new BigDecimal("10.00"))
            .discountAmount(new BigDecimal("3750.00"))
            .totalAmount(new BigDecimal("33750.00"))
            .discountApplied(true)
            .discountDescription("Smart Stay: 10% Long-Stay Discount applied for 4+ nights")
            .build();

        HotelDetailDTO detail = HotelDetailDTO.builder()
            .id(1L)
            .name("The Grand Heritage Palace")
            .location("Udaipur")
            .description("Royal retreat by Lake Pichola")
            .amenities("WiFi, Pool, Spa")
            .pricePerNight(new BigDecimal("7500.00"))
            .totalRooms(4)
            .availableRooms(1)
            .demandStatus("HIGH_DEMAND")
            .priceBreakdown(priceBreakdown)
            .build();

        when(hotelService.getHotelDetails(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(detail);

        mockMvc.perform(get("/api/hotels/1")
                .param("checkIn", "2026-12-10")
                .param("checkOut", "2026-12-15")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("The Grand Heritage Palace"))
            .andExpect(jsonPath("$.availableRooms").value(1))
            .andExpect(jsonPath("$.demandStatus").value("HIGH_DEMAND"))
            .andExpect(jsonPath("$.priceBreakdown.discountApplied").value(true))
            .andExpect(jsonPath("$.priceBreakdown.totalAmount").value(33750.00));
    }
}
