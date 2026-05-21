package com.epam.edp.demo.service;

import com.epam.edp.demo.model.AgentReportRecord;
import com.epam.edp.demo.model.BookingRecord;
import com.epam.edp.demo.model.TourReportRecord;
import com.epam.edp.demo.repository.AgentReportRecordRepository;
import com.epam.edp.demo.repository.BookingRecordRepository;
import com.epam.edp.demo.repository.TourReportRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates booking records into weekly statistics, persists them to
 * {@code agent_report_records} / {@code tour_report_records} and returns
 * the saved documents for Excel report generation.
 *
 * <p>The MongoDB models are used directly as the data model — no intermediate
 * row DTOs exist.
 */
@Service
public class StatsAggregationService {

    private static final Logger log = LoggerFactory.getLogger(StatsAggregationService.class);

    private final BookingRecordRepository    bookingRepo;
    private final AgentReportRecordRepository agentReportRepo;
    private final TourReportRecordRepository  tourReportRepo;

    public StatsAggregationService(BookingRecordRepository bookingRepo,
                                   AgentReportRecordRepository agentReportRepo,
                                   TourReportRecordRepository tourReportRepo) {
        this.bookingRepo     = bookingRepo;
        this.agentReportRepo = agentReportRepo;
        this.tourReportRepo  = tourReportRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Compute, persist and return
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes agent-performance stats for the given week, compares with the
     * previous week to calculate deltas, upserts results into MongoDB and
     * returns the persisted rows ready for Excel generation.
     */
    public List<AgentReportRecord> getAgentPerformance(LocalDate weekStart, LocalDate weekEnd) {
        List<BookingRecord> current  = fetchCreated(weekStart, weekEnd);
        List<BookingRecord> previous = fetchCreated(weekStart.minusWeeks(1), weekEnd.minusWeeks(1));

        Map<String, List<BookingRecord>> byAgent     = groupBy(current,  BookingRecord::getAgentId);
        Map<String, List<BookingRecord>> prevByAgent = groupBy(previous, BookingRecord::getAgentId);

        List<AgentReportRecord> rows = byAgent.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> buildAgentRecord(
                        e.getKey(), e.getValue(),
                        prevByAgent.getOrDefault(e.getKey(), List.of()),
                        weekStart, weekEnd))
                .collect(Collectors.toList());

        rows.forEach(r -> upsertAgent(r, weekStart, weekEnd));
        log.info("Saved {} agent report rows for {} – {}", rows.size(), weekStart, weekEnd);
        return rows;
    }

    /**
     * Computes tour sales stats for the given week, compares with the previous
     * week, upserts results into MongoDB and returns the persisted rows.
     */
    public List<TourReportRecord> getTourStatistics(LocalDate weekStart, LocalDate weekEnd) {
        List<BookingRecord> current  = fetchCreated(weekStart, weekEnd);
        List<BookingRecord> previous = fetchCreated(weekStart.minusWeeks(1), weekEnd.minusWeeks(1));

        Map<String, List<BookingRecord>> byTour     = groupBy(current,  BookingRecord::getTourId);
        Map<String, List<BookingRecord>> prevByTour = groupBy(previous, BookingRecord::getTourId);

        List<TourReportRecord> rows = byTour.entrySet().stream()
                .sorted(Comparator.comparingLong(
                        (Map.Entry<String, List<BookingRecord>> e) -> totalRevenue(e.getValue()))
                        .reversed())
                .map(e -> buildTourRecord(
                        e.getKey(), e.getValue(),
                        prevByTour.getOrDefault(e.getKey(), List.of()),
                        weekStart, weekEnd))
                .collect(Collectors.toList());

        rows.forEach(r -> upsertTour(r, weekStart, weekEnd));
        log.info("Saved {} tour report rows for {} – {}", rows.size(), weekStart, weekEnd);
        return rows;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read stored results
    // ─────────────────────────────────────────────────────────────────────────

    public List<AgentReportRecord> getStoredAgentRows(LocalDate start, LocalDate end) {
        return agentReportRepo.findByPeriodStartAndPeriodEnd(start, end);
    }

    public List<TourReportRecord> getStoredTourRows(LocalDate start, LocalDate end) {
        return tourReportRepo.findByPeriodStartAndPeriodEndOrderByRevenueUsdDesc(start, end);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builders
    // ─────────────────────────────────────────────────────────────────────────

    /** Shared computed stats for a period — eliminates duplication between agent and tour builders. */
    record PeriodStats(long toursSold, String deltaToursSold,
                       double avgFeedback, double minFeedback, String deltaAvgFeedback,
                       long revenue, String deltaRevenue) {}

    PeriodStats computeStats(List<BookingRecord> cur, List<BookingRecord> prev) {
        return new PeriodStats(
                cur.size(),
                changePct(cur.size(), prev.size()),
                avgRating(cur),
                minRating(cur),
                changePct(avgRating(cur), avgRating(prev)),
                totalRevenue(cur),
                changePct(totalRevenue(cur), totalRevenue(prev))
        );
    }

    private AgentReportRecord buildAgentRecord(String agentId,
                                               List<BookingRecord> cur,
                                               List<BookingRecord> prev,
                                               LocalDate start, LocalDate end) {
        AgentReportRecord r = agentReportRepo
                .findByAgentIdAndPeriodStartAndPeriodEnd(agentId, start, end)
                .orElse(new AgentReportRecord());

        PeriodStats s = computeStats(cur, prev);
        r.setAgentId(agentId);
        r.setAgentName(firstNonBlank(cur,  BookingRecord::getAgentName,  "Unknown"));
        r.setAgentEmail(firstNonBlank(cur, BookingRecord::getAgentEmail, "Unknown"));
        r.setPeriodStart(start);
        r.setPeriodEnd(end);
        r.setToursSold(s.toursSold());
        r.setDeltaOfToursSoldPct(s.deltaToursSold());
        r.setAvgFeedbackRate(s.avgFeedback());
        r.setMinFeedbackRate(s.minFeedback());
        r.setDeltaOfAvgFeedbackPct(s.deltaAvgFeedback());
        r.setRevenueUsd(s.revenue());
        r.setDeltaOfRevenuePct(s.deltaRevenue());
        r.setGeneratedAt(Instant.now());
        return r;
    }

    private TourReportRecord buildTourRecord(String tourId,
                                             List<BookingRecord> cur,
                                             List<BookingRecord> prev,
                                             LocalDate start, LocalDate end) {
        TourReportRecord r = tourReportRepo
                .findByTourIdAndPeriodStartAndPeriodEnd(tourId, start, end)
                .orElse(new TourReportRecord());

        PeriodStats s = computeStats(cur, prev);
        r.setTourId(tourId);
        r.setTourName(firstNonBlank(cur,    BookingRecord::getTourName,    "Unknown"));
        r.setDestination(firstNonBlank(cur, BookingRecord::getDestination, "Unknown"));
        r.setPeriodStart(start);
        r.setPeriodEnd(end);
        r.setToursSold(s.toursSold());
        r.setDeltaOfToursSoldPct(s.deltaToursSold());
        r.setAvgFeedbackRate(s.avgFeedback());
        r.setMinFeedbackRate(s.minFeedback());
        r.setDeltaOfAvgFeedbackPct(s.deltaAvgFeedback());
        r.setRevenueUsd(s.revenue());
        r.setDeltaOfRevenuePct(s.deltaRevenue());
        r.setGeneratedAt(Instant.now());
        return r;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Upsert helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void upsertAgent(AgentReportRecord r, LocalDate start, LocalDate end) {
        agentReportRepo.findByAgentIdAndPeriodStartAndPeriodEnd(r.getAgentId(), start, end)
                .ifPresent(existing -> r.setId(existing.getId()));
        agentReportRepo.save(r);
    }

    private void upsertTour(TourReportRecord r, LocalDate start, LocalDate end) {
        tourReportRepo.findByTourIdAndPeriodStartAndPeriodEnd(r.getTourId(), start, end)
                .ifPresent(existing -> r.setId(existing.getId()));
        tourReportRepo.save(r);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Aggregation helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<BookingRecord> fetchCreated(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end   = to.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        return bookingRepo.findByEventTypeAndEventTimestampBetween("BOOKING_CREATED", start, end);
    }

    private Map<String, List<BookingRecord>> groupBy(List<BookingRecord> records,
                                                      java.util.function.Function<BookingRecord, String> keyFn) {
        return records.stream()
                .filter(r -> keyFn.apply(r) != null)
                .collect(Collectors.groupingBy(keyFn, LinkedHashMap::new, Collectors.toList()));
    }

    private double avgRating(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getTourRating() != null)
                .mapToDouble(BookingRecord::getTourRating)
                .average().orElse(0.0);
    }

    private double minRating(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getTourRating() != null)
                .mapToDouble(BookingRecord::getTourRating)
                .min().orElse(0.0);
    }

    private long totalRevenue(List<BookingRecord> records) {
        return records.stream().mapToLong(BookingRecord::getRevenueAmount).sum();
    }

    private String firstNonBlank(List<BookingRecord> records,
                                  java.util.function.Function<BookingRecord, String> fn,
                                  String fallback) {
        return records.stream().map(fn)
                .filter(s -> s != null && !s.isBlank())
                .findFirst().orElse(fallback);
    }

    private String changePct(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : "0%";
        long pct = Math.round((double)(current - previous) / previous * 100);
        return (pct >= 0 ? "+" : "") + pct + "%";
    }

    private String changePct(double current, double previous) {
        return changePct(Math.round(current), Math.round(previous));
    }
}

