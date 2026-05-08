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
        private String tourId;
        private String state;
        private String tourImageUrl;
        private String name;
        private String destination;
        private double rating;
        private TourDetailsDTO tourDetails;
        private TravelAgentDTO travelAgent;
        private String canceledBy;
        private String cancelReason;
        // Raw fields for edit form
        private String rawDate;
        private String rawDuration;
        private String rawMealPlan;
        private int rawAdults;
        private int rawChildren;
        private String freeCancellationDate;
        private java.util.List<PersonalDetailItem> personalDetails;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalDetailItem {
        private String firstName;
        private String lastName;
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

