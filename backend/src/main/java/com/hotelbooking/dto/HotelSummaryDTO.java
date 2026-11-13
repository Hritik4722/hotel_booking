package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Summary DTO for hotel search results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelSummaryDTO {

    private Long id;
    private String name;
    private String location;
    private String description;
    private String amenities;
    private BigDecimal pricePerNight;
    private int totalRooms;
    private int availableRooms;
    private String demandStatus;
    private PriceBreakdownDTO priceBreakdown;
}
