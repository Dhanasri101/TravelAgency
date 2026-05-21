package com.epam.edp.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Persisted result of the agent-performance aggregation for a specific reporting period.
 * Stored in the {@code agent_report_records} MongoDB collection.
 */
@Document(collection = "agent_report_records")
@CompoundIndex(name = "agent_period_idx", def = "{'agentId': 1, 'periodStart': 1, 'periodEnd': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
public class AgentReportRecord {

    @Id
    private String id;

    private String agentId;
    private String agentName;
    private String agentEmail;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    /** Number of tours sold (BOOKING_CREATED events) in the period */
    private long toursSold;

    /** % change in tours sold vs previous week, e.g. "+10%" */
    private String deltaOfToursSoldPct;

    /** Average feedback/rating (1-5) across all bookings in the period */
    private double avgFeedbackRate;

    /** Minimum feedback/rating (1-5) seen in the period */
    private double minFeedbackRate;

    /** % change in average feedback rate vs previous week, e.g. "+5%" */
    private String deltaOfAvgFeedbackPct;

    /** Total revenue in USD for the period */
    private long revenueUsd;

    /** % change in revenue vs previous week */
    private String deltaOfRevenuePct;

    /** When this record was computed/saved */
    private Instant generatedAt;
}

