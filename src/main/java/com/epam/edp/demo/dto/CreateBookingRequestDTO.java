package com.epam.edp.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateBookingRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Tour ID is required")
    private String tourId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Duration is required")
    private String duration;

    @NotBlank(message = "Meal plan is required")
    private String mealPlan;

    @NotNull(message = "Guests are required")
    @Valid
    private GuestsDTO guests;

    @NotNull(message = "Personal details are required")
    @Size(min = 1, message = "At least one person's details are required")
    @Valid
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

