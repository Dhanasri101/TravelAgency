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
 * Aggregates booking records into weekly statistics rows, persists the results
 * to dedicated MongoDB collections (agent_report_records / tour_report_records),
 * and returns them for Excel report generation.
 *
 * <p><b>Columns produced (matching the spec template):</b>
 * <ul>
 *   <li>Agent Performance: Name, E-mail, Period Start, Period End, Tours Sold,
 *       Delta Tours Sold %, Avg Feedback (1-5), Min Feedback (1-5),
 *       Delta Avg Feedback %, Revenue (USD)</li>
 *   <li>Sales Statistics: Tour Name, Destination, Period Start, Period End, Tours Sold,
 *       Delta Tours Sold %, Avg Feedback (1-5), Min Feedback (1-5),
 *       Delta Avg Feedback %, Revenue (USD)</li>
 * </ul>
 */
@Service
public class StatsAggregationService {

    private static final Logger log = LoggerFactory.getLogger(StatsAggregationService.class);

    private final BookingRecordRepository bookingRepo;
    private final AgentReportRecordRepository agentReportRepo;
    private final TourReportRecordRepository tourReportRepo;

    public StatsAggregationService(BookingRecordRepository bookingRepo,
                                   AgentReportRecordRepository agentReportRepo,
                                   TourReportRecordRepository tourReportRepo) {
        this.bookingRepo    = bookingRepo;
        this.agentReportRepo = agentReportRepo;
        this.tourReportRepo  = tourReportRepo;
    }

    // ─────────────────────────────────────────────
    // Agent performance rows
    // ─────────────────────────────────────────────

    /**
     * Computes agent-performance stats for {@code weekStart..weekEnd},
     * compares with the previous week, upserts results into MongoDB,
     * and returns the rows for Excel generation.
     */
    public List<AgentRow> getAgentPerformance(LocalDate weekStart, LocalDate weekEnd) {
        List<BookingRecord> current  = fetchCreated(weekStart, weekEnd);
        List<BookingRecord> previous = fetchCreated(weekStart.minusWeeks(1), weekEnd.minusWeeks(1));

        Map<String, List<BookingRecord>> byAgent     = groupByAgent(current);
        Map<String, List<BookingRecord>> prevByAgent = groupByAgent(previous);

        List<AgentRow> rows = byAgent.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String agentId = e.getKey();
                    List<BookingRecord> records     = e.getValue();
                    List<BookingRecord> prevRecords = prevByAgent.getOrDefault(agentId, List.of());

                    long   toursSold        = records.size();
                    long   prevToursSold    = prevRecords.size();
                    double avgRating        = avgRating(records);
                    double prevAvgRating    = avgRating(prevRecords);
                    double minRating        = minRating(records);
                    long   revenue          = totalRevenue(records);
                    long   prevRevenue      = totalRevenue(prevRecords);

                    return new AgentRow(
                            agentName(records),
                            agentEmail(records),
                            weekStart,
                            weekEnd,
                            toursSold,
                            changePct(toursSold, prevToursSold),
                            avgRating,
                            minRating,
                            changePct(avgRating, prevAvgRating),
                            revenue,
                            changePct(revenue, prevRevenue)
                    );
                })
                .collect(Collectors.toList());

        // Persist / upsert into agent_report_records
        rows.forEach(r -> saveAgentRow(r, weekStart, weekEnd, byAgent));
        log.info("Saved {} agent report rows for period {} – {}", rows.size(), weekStart, weekEnd);
        return rows;
    }

    // ─────────────────────────────────────────────
    // Sales statistics rows
    // ─────────────────────────────────────────────

    /**
     * Computes tour sales stats for {@code weekStart..weekEnd},
     * compares with the previous week, upserts results into MongoDB,
     * and returns the rows for Excel generation.
     */
    public List<TourRow> getTourStatistics(LocalDate weekStart, LocalDate weekEnd) {
        List<BookingRecord> current  = fetchCreated(weekStart, weekEnd);
        List<BookingRecord> previous = fetchCreated(weekStart.minusWeeks(1), weekEnd.minusWeeks(1));

        Map<String, List<BookingRecord>> byTour     = groupByTour(current);
        Map<String, List<BookingRecord>> prevByTour = groupByTour(previous);

        List<TourRow> rows = byTour.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, List<BookingRecord>> e)
                        -> totalRevenue(e.getValue())).reversed())
                .map(e -> {
                    String tourId = e.getKey();
                    List<BookingRecord> records     = e.getValue();
                    List<BookingRecord> prevRecords = prevByTour.getOrDefault(tourId, List.of());

                    long   toursSold        = records.size();
                    long   prevToursSold    = prevRecords.size();
                    double avgRating        = avgRating(records);
                    double prevAvgRating    = avgRating(prevRecords);
                    double minRating        = minRating(records);
                    long   revenue          = totalRevenue(records);
                    long   prevRevenue      = totalRevenue(prevRecords);

                    return new TourRow(
                            tourName(records),
                            destination(records),
                            weekStart,
                            weekEnd,
                            toursSold,
                            changePct(toursSold, prevToursSold),
                            avgRating,
                            minRating,
                            changePct(avgRating, prevAvgRating),
                            revenue,
                            changePct(revenue, prevRevenue)
                    );
                })
                .collect(Collectors.toList());

        // Persist / upsert into tour_report_records
        rows.forEach(r -> saveTourRow(r, weekStart, weekEnd, byTour));
        log.info("Saved {} tour report rows for period {} – {}", rows.size(), weekStart, weekEnd);
        return rows;
    }

    // ─────────────────────────────────────────────
    // Read stored results from MongoDB
    // ─────────────────────────────────────────────

    /** Returns previously stored agent-performance rows for the given period. */
    public List<AgentReportRecord> getStoredAgentRows(LocalDate periodStart, LocalDate periodEnd) {
        return agentReportRepo.findByPeriodStartAndPeriodEnd(periodStart, periodEnd);
    }

    /** Returns previously stored tour-statistics rows for the given period. */
    public List<TourReportRecord> getStoredTourRows(LocalDate periodStart, LocalDate periodEnd) {
        return tourReportRepo.findByPeriodStartAndPeriodEndOrderByRevenueUsdDesc(periodStart, periodEnd);
    }

    // ─────────────────────────────────────────────
    // Persist helpers
    // ─────────────────────────────────────────────

    private void saveAgentRow(AgentRow r, LocalDate start, LocalDate end,
                               Map<String, List<BookingRecord>> byAgent) {
        // Determine agentId from the booking records
        String agentId = byAgent.entrySet().stream()
                .filter(e -> agentName(e.getValue()).equals(r.name()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(r.email()); // fall back to email as key

        AgentReportRecord record = agentReportRepo
                .findByAgentIdAndPeriodStartAndPeriodEnd(agentId, start, end)
                .orElse(new AgentReportRecord());

        record.setAgentId(agentId);
        record.setAgentName(r.name());
        record.setAgentEmail(r.email());
        record.setPeriodStart(start);
        record.setPeriodEnd(end);
        record.setToursSold(r.toursSold());
        record.setDeltaOfToursSoldPct(r.deltaOfToursSoldPct());
        record.setAvgFeedbackRate(r.avgFeedbackRate());
        record.setMinFeedbackRate(r.minFeedbackRate());
        record.setDeltaOfAvgFeedbackPct(r.deltaOfAvgFeedbackPct());
        record.setRevenueUsd(r.revenueUsd());
        record.setDeltaOfRevenuePct(r.deltaOfRevenuePct());
        record.setGeneratedAt(Instant.now());

        agentReportRepo.save(record);
    }

    private void saveTourRow(TourRow r, LocalDate start, LocalDate end,
                              Map<String, List<BookingRecord>> byTour) {
        String tourId = byTour.entrySet().stream()
                .filter(e -> tourName(e.getValue()).equals(r.tourName()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(r.tourName());

        TourReportRecord record = tourReportRepo
                .findByTourIdAndPeriodStartAndPeriodEnd(tourId, start, end)
                .orElse(new TourReportRecord());

        record.setTourId(tourId);
        record.setTourName(r.tourName());
        record.setDestination(r.destination());
        record.setPeriodStart(start);
        record.setPeriodEnd(end);
        record.setToursSold(r.toursSold());
        record.setDeltaOfToursSoldPct(r.deltaOfToursSoldPct());
        record.setAvgFeedbackRate(r.avgFeedbackRate());
        record.setMinFeedbackRate(r.minFeedbackRate());
        record.setDeltaOfAvgFeedbackPct(r.deltaOfAvgFeedbackPct());
        record.setRevenueUsd(r.revenueUsd());
        record.setDeltaOfRevenuePct(r.deltaOfRevenuePct());
        record.setGeneratedAt(Instant.now());

        tourReportRepo.save(record);
    }

    // ─────────────────────────────────────────────
    // Query helpers
    // ─────────────────────────────────────────────

    private List<BookingRecord> fetchCreated(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end   = to.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        return bookingRepo.findByEventTypeAndEventTimestampBetween("BOOKING_CREATED", start, end);
    }

    private Map<String, List<BookingRecord>> groupByAgent(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getAgentId() != null)
                .collect(Collectors.groupingBy(BookingRecord::getAgentId,
                        LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, List<BookingRecord>> groupByTour(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getTourId() != null)
                .collect(Collectors.groupingBy(BookingRecord::getTourId,
                        LinkedHashMap::new, Collectors.toList()));
    }

    private double avgRating(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getTourRating() != null)
                .mapToDouble(BookingRecord::getTourRating)
                .average()
                .orElse(0.0);
    }

    private double minRating(List<BookingRecord> records) {
        return records.stream()
                .filter(r -> r.getTourRating() != null)
                .mapToDouble(BookingRecord::getTourRating)
                .min()
                .orElse(0.0);
    }

    private long totalRevenue(List<BookingRecord> records) {
        return records.stream().mapToLong(BookingRecord::getRevenueAmount).sum();
    }

    private String agentName(List<BookingRecord> records) {
        return records.stream().map(BookingRecord::getAgentName)
                .filter(n -> n != null && !n.isBlank()).findFirst().orElse("Unknown");
    }

    private String agentEmail(List<BookingRecord> records) {
        return records.stream().map(BookingRecord::getAgentEmail)
                .filter(e -> e != null && !e.isBlank()).findFirst().orElse("Unknown");
    }

    private String tourName(List<BookingRecord> records) {
        return records.stream().map(BookingRecord::getTourName)
                .filter(n -> n != null && !n.isBlank()).findFirst().orElse("Unknown");
    }

    private String destination(List<BookingRecord> records) {
        return records.stream().map(BookingRecord::getDestination)
                .filter(d -> d != null && !d.isBlank()).findFirst().orElse("Unknown");
    }

    private String changePct(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : "0%";
        long diff = current - previous;
        long pct  = Math.round((double) diff / previous * 100);
        return (pct >= 0 ? "+" : "") + pct + "%";
    }

    private String changePct(double current, double previous) {
        return changePct(Math.round(current), Math.round(previous));
    }

    // ─────────────────────────────────────────────
    // Row DTOs (returned to ReportGeneratorService)
    // ─────────────────────────────────────────────

    /**
     * Agent Performance row — columns match the spec template exactly.
     */
    public record AgentRow(
            String name,
            String email,
            LocalDate periodStart,
            LocalDate periodEnd,
            long   toursSold,
            String deltaOfToursSoldPct,
            double avgFeedbackRate,
            double minFeedbackRate,
            String deltaOfAvgFeedbackPct,
            long   revenueUsd,
            String deltaOfRevenuePct
    ) {}

    /**
     * Sales Statistics row — columns match the spec template exactly.
     */
    public record TourRow(
            String tourName,
            String destination,
            LocalDate periodStart,
            LocalDate periodEnd,
            long   toursSold,
            String deltaOfToursSoldPct,
            double avgFeedbackRate,
            double minFeedbackRate,
            String deltaOfAvgFeedbackPct,
            long   revenueUsd,
            String deltaOfRevenuePct
    ) {}
}

