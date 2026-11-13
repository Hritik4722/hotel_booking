package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Breakdown of stay pricing including the Smart Stay long-stay discount.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceBreakdownDTO {

    private int nights;
    private BigDecimal pricePerNight;
    private BigDecimal baseAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private boolean discountApplied;
    private String discountDescription;
}
