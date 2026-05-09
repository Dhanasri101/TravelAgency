package com.epam.edp.demo.dto;

import jakarta.validation.Valid;
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
    private BookingGuestsDTO guests;

    @Valid
    @Size(min = 1, message = "At least one person's details are required")
    private List<PersonalDetailDTO> personalDetails;
}
