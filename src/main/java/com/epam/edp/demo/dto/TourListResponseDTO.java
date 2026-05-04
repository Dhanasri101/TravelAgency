package com.epam.edp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourListResponseDTO {

    private List<TourItem> tours;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Integer totalItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TourItem {
        private String id;
        private String name;
        private String destination;
        private LocalDate startDate;
        private List<String> durations;
        private List<String> mealPlans;
        private String price;
        private Double rating;
        private Integer reviews;
        private LocalDate freeCancellation;
    }
}
