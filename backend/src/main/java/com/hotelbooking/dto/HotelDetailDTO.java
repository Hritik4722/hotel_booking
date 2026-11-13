package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Detailed DTO for a specific hotel view.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDetailDTO {

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
