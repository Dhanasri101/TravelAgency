package com.epam.edp.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shared personal-detail entry used by both CreateBookingRequestDTO
 * and UpdateBookingRequestDTO to avoid class duplication.
 */
@Getter
@Setter
@NoArgsConstructor
public class PersonalDetailDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;
}

