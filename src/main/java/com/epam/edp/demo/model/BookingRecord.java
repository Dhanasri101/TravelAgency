package com.epam.edp.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Persisted record of every booking event received from sprint1 via RabbitMQ.
 * Used to aggregate weekly reports for agents and tours.
 */
@Document(collection = "booking_records")
@Getter
@Setter
@NoArgsConstructor
public class BookingRecord {

    @Id
    private String id;

    /** BOOKING_CREATED | BOOKING_CANCELLED | BOOKING_FINISHED */
    @Indexed
    private String eventType;

    private String bookingId;

    @Indexed
    private String tourId;
    private String tourName;
    private String destination;

    @Indexed
    private String agentId;
    private String agentName;
    private String agentEmail;

    private String userId;
    private int adults;
    private int children;

    /** Revenue in USD */
    private long revenueAmount;

    private Double tourRating;
    private Integer tourReviewCount;

    private LocalDate bookingDate;
    private String duration;

    /** When the event was originally emitted by sprint1 */
    @Indexed
    private Instant eventTimestamp;

    /** When this record was stored in report-app DB */
    @CreatedDate
    private Instant recordedAt;
}

