package com.hotelbooking.dto;

import com.hotelbooking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long id;
    private String bookingReference;
    private Long hotelId;
    private String hotelName;
    private String hotelLocation;
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private BigDecimal pricePerNight;
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private PriceBreakdownDTO priceBreakdown;
}
