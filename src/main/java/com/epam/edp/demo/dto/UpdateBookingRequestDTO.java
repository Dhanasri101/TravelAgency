package com.epam.edp.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateBookingRequestDTO {

    private LocalDate date;

    private String duration;

    private String mealPlan;

    @Valid
    private GuestsDTO guests;

    @Valid
    @Size(min = 1, message = "At least one person's details are required")
    private List<PersonalDetailDTO> personalDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GuestsDTO {
        private int adult = 1;
        private int children = 0;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PersonalDetailDTO {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;
    }
}

