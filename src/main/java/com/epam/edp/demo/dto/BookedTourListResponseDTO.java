package com.epam.edp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedTourListResponseDTO {

    private List<BookingItem> bookings;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItem {
        private String id;
        private String state;
        private String tourImageUrl;
        private String name;
        private String destination;
        private TourDetailsDTO tourDetails;
        private TravelAgentDTO travelAgent;
        private String canceledBy;
        private String cancelReason;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourDetailsDTO {
        private String date;
        private String mealPlan;
        private String guests;
        private String totalPrice;
        private String documents;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelAgentDTO {
        private String name;
        private String email;
        private String phone;
        private String messenger;
    }
}

