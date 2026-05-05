package com.epam.edp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDetailResponseDTO {

    private String id;
    private String name;
    private String destination;
    private String summary;
    private List<String> imageUrls;
    private Double rating;
    private Integer reviewCount;
    private List<LocalDate> startDates;
    private List<String> durations;
    private Map<String, String> pricePerDuration;
    private List<String> mealPlans;
    private Map<String, String> mealSupplementsPerDay;
    private String hotelName;
    private String hotelDescription;
    private String accommodation;
    private String tourType;
    private Map<String, String> customDetails;
    private GuestQuantityDTO guestQuantity;
    private Integer freeCancellationDaysBefore;
    private LocalDate freeCancellationDeadline;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestQuantityDTO {
        private Integer adultsMaxValue;
        private Integer childrenMaxValue;
        private Integer totalMaxValue;
    }
}
