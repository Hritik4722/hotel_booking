package com.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Guest name is required")
    private String guestName;

    @NotBlank(message = "Guest email is required")
    @Email(message = "Please provide a valid email address")
    private String guestEmail;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;
}
