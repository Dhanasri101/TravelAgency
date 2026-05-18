package com.epam.edp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Mirror of the BookingEvent published by sprint1.
 * Must match the JSON structure sent by the producer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String eventType;       // BOOKING_CREATED | BOOKING_CANCELLED | BOOKING_FINISHED
    private String bookingId;
    private String tourId;
    private String tourName;
    private String destination;
    private String agentId;
    private String agentName;
    private String agentEmail;
    private String userId;
    private int adults;
    private int children;
    private long revenueAmount;     // numeric USD value
    private Double tourRating;
    private Integer tourReviewCount;
    private String bookingDate;     // yyyy-MM-dd
    private String duration;
    private Instant timestamp;
}

