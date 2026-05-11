package com.epam.edp.demo.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shared guests payload used by both CreateBookingRequestDTO
 * and UpdateBookingRequestDTO to avoid class duplication.
 */
@Getter
@Setter
@NoArgsConstructor
public class BookingGuestsDTO {
    @Min(value = 1, message = "At least 1 adult is required")
    private int adult = 1;

    @Min(value = 0, message = "Children count cannot be negative")
    private int children = 0;
}

