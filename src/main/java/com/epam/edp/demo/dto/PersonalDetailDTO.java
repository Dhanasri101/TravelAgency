package com.epam.edp.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "First name must contain only letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Last name must contain only letters, spaces, hyphens, and apostrophes")
    private String lastName;
}

