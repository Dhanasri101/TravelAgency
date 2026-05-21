package com.epam.edp.demo.service;

import com.epam.edp.demo.model.AgentReportRecord;
import com.epam.edp.demo.model.BookingRecord;
import com.epam.edp.demo.model.TourReportRecord;
import com.epam.edp.demo.repository.AgentReportRecordRepository;
import com.epam.edp.demo.repository.BookingRecordRepository;
import com.epam.edp.demo.repository.TourReportRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsAggregationServiceTest {

    @Mock BookingRecordRepository     bookingRepo;
    @Mock AgentReportRecordRepository agentReportRepo;
    @Mock TourReportRecordRepository  tourReportRepo;

    @InjectMocks StatsAggregationService service;

    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 11);
    private static final LocalDate WEEK_END   = LocalDate.of(2026, 5, 17);

    // ── computeStats — no mocks needed, pure logic ────────────────────────────

    @Test
    void computeStats_noBookings_returnsZeroStats() {
        StatsAggregationService.PeriodStats s = service.computeStats(List.of(), List.of());

        assertThat(s.toursSold()).isZero();
        assertThat(s.avgFeedback()).isZero();
        assertThat(s.minFeedback()).isZero();
        assertThat(s.revenue()).isZero();
        assertThat(s.deltaToursSold()).isEqualTo("0%");
    }

    @Test
    void computeStats_increasedBookings_positiveDelta() {
        List<BookingRecord> cur  = List.of(booking(4.5, 1000), booking(4.0, 2000));
        List<BookingRecord> prev = List.of(booking(3.5, 1500));

        StatsAggregationService.PeriodStats s = service.computeStats(cur, prev);

        assertThat(s.toursSold()).isEqualTo(2);
        assertThat(s.deltaToursSold()).isEqualTo("+100%");
        assertThat(s.revenue()).isEqualTo(3000L);
        assertThat(s.avgFeedback()).isEqualTo(4.25);
        assertThat(s.minFeedback()).isEqualTo(4.0);
    }

    @Test
    void computeStats_decreasedBookings_negativeDelta() {
        List<BookingRecord> cur  = List.of(booking(3.0, 800));
        List<BookingRecord> prev = List.of(booking(4.0, 1000), booking(4.0, 1000));

        StatsAggregationService.PeriodStats s = service.computeStats(cur, prev);

        assertThat(s.deltaToursSold()).isEqualTo("-50%");
        assertThat(s.deltaRevenue()).isEqualTo("-60%");
    }

    @Test
    void computeStats_zeroPrevious_newBookings_returns100Pct() {
        StatsAggregationService.PeriodStats s = service.computeStats(
                List.of(booking(5.0, 1000)), List.of());

        assertThat(s.deltaToursSold()).isEqualTo("+100%");
        assertThat(s.deltaRevenue()).isEqualTo("+100%");
    }

    @Test
    void computeStats_bothZero_returns0Pct() {
        StatsAggregationService.PeriodStats s = service.computeStats(List.of(), List.of());

        assertThat(s.deltaToursSold()).isEqualTo("0%");
        assertThat(s.deltaRevenue()).isEqualTo("0%");
    }

    // ── getAgentPerformance ───────────────────────────────────────────────────

    @Test
    void getAgentPerformance_groupsAndSavesPerAgent() {
        BookingRecord b1 = agentBooking("agent1", "Alice", "alice@test.com", 4.5, 1000);
        BookingRecord b2 = agentBooking("agent1", "Alice", "alice@test.com", 4.0, 2000);
        BookingRecord b3 = agentBooking("agent2", "Bob",   "bob@test.com",   3.5, 1500);

        stubRepo(WEEK_START, WEEK_END, List.of(b1, b2, b3), List.of());
        stubAgentUpsert();

        List<AgentReportRecord> result = service.getAgentPerformance(WEEK_START, WEEK_END);

        assertThat(result).hasSize(2);
        AgentReportRecord alice = result.stream()
                .filter(r -> "agent1".equals(r.getAgentId())).findFirst().orElseThrow();
        assertThat(alice.getToursSold()).isEqualTo(2);
        assertThat(alice.getAgentName()).isEqualTo("Alice");
        assertThat(alice.getRevenueUsd()).isEqualTo(3000L);
        verify(agentReportRepo).save(alice);
    }

    @Test
    void getAgentPerformance_emptyPeriod_returnsEmptyList() {
        stubRepo(WEEK_START, WEEK_END, List.of(), List.of());

        List<AgentReportRecord> result = service.getAgentPerformance(WEEK_START, WEEK_END);

        assertThat(result).isEmpty();
    }

    // ── getTourStatistics ─────────────────────────────────────────────────────

    @Test
    void getTourStatistics_groupsAndSavesPerTour() {
        BookingRecord t1 = tourBooking("tour1", "Tropical Caribe", "Dominican Republic", 4.8, 2000);
        BookingRecord t2 = tourBooking("tour1", "Tropical Caribe", "Dominican Republic", 4.0, 3000);
        BookingRecord t3 = tourBooking("tour2", "Garden Resort",   "Dominican Republic", 3.9, 1500);

        stubRepo(WEEK_START, WEEK_END, List.of(t1, t2, t3), List.of());
        stubTourUpsert();

        List<TourReportRecord> result = service.getTourStatistics(WEEK_START, WEEK_END);

        assertThat(result).hasSize(2);
        TourReportRecord tropical = result.stream()
                .filter(r -> "tour1".equals(r.getTourId())).findFirst().orElseThrow();
        assertThat(tropical.getToursSold()).isEqualTo(2);
        assertThat(tropical.getRevenueUsd()).isEqualTo(5000L);
        assertThat(tropical.getTourName()).isEqualTo("Tropical Caribe");
        verify(tourReportRepo).save(tropical);
    }

    @Test
    void getTourStatistics_sortedByRevenueDesc() {
        BookingRecord low  = tourBooking("tourLow",  "Low",  "Place", 4.0, 500);
        BookingRecord high = tourBooking("tourHigh", "High", "Place", 4.0, 5000);

        stubRepo(WEEK_START, WEEK_END, List.of(low, high), List.of());
        stubTourUpsert();

        List<TourReportRecord> result = service.getTourStatistics(WEEK_START, WEEK_END);

        assertThat(result.get(0).getTourId()).isEqualTo("tourHigh");
        assertThat(result.get(1).getTourId()).isEqualTo("tourLow");
    }

    // ── getStoredRows ──────────────────────────────────────────────────────────

    @Test
    void getStoredAgentRows_delegatesToRepo() {
        AgentReportRecord row = new AgentReportRecord();
        when(agentReportRepo.findByPeriodStartAndPeriodEnd(WEEK_START, WEEK_END))
                .thenReturn(List.of(row));

        assertThat(service.getStoredAgentRows(WEEK_START, WEEK_END)).containsExactly(row);
    }

    @Test
    void getStoredTourRows_delegatesToRepo() {
        TourReportRecord row = new TourReportRecord();
        when(tourReportRepo.findByPeriodStartAndPeriodEndOrderByRevenueUsdDesc(WEEK_START, WEEK_END))
                .thenReturn(List.of(row));

        assertThat(service.getStoredTourRows(WEEK_START, WEEK_END)).containsExactly(row);
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    /** Stubs bookingRepo to return current-week records for the given window, empty for prev week. */
    private void stubRepo(LocalDate weekStart, LocalDate weekEnd,
                          List<BookingRecord> current, List<BookingRecord> previous) {
        Instant curStart  = weekStart.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant prevStart = weekStart.minusWeeks(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        when(bookingRepo.findByEventTypeAndEventTimestampBetween(
                eq("BOOKING_CREATED"), any(Instant.class), any(Instant.class)))
                .thenAnswer(inv -> {
                    Instant s = inv.getArgument(1);
                    if (s.equals(curStart))  return current;
                    if (s.equals(prevStart)) return previous;
                    return List.of();
                });
    }

    private void stubAgentUpsert() {
        lenient().when(agentReportRepo.findByAgentIdAndPeriodStartAndPeriodEnd(any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(agentReportRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void stubTourUpsert() {
        lenient().when(tourReportRepo.findByTourIdAndPeriodStartAndPeriodEnd(any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(tourReportRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ── Record factories ──────────────────────────────────────────────────────

    private BookingRecord booking(double rating, long revenue) {
        BookingRecord r = new BookingRecord();
        r.setTourRating(rating);
        r.setRevenueAmount(revenue);
        r.setEventTimestamp(Instant.now());
        return r;
    }

    private BookingRecord agentBooking(String agentId, String name, String email,
                                       double rating, long revenue) {
        BookingRecord r = booking(rating, revenue);
        r.setAgentId(agentId);
        r.setAgentName(name);
        r.setAgentEmail(email);
        return r;
    }

    private BookingRecord tourBooking(String tourId, String name, String dest,
                                      double rating, long revenue) {
        BookingRecord r = booking(rating, revenue);
        r.setTourId(tourId);
        r.setTourName(name);
        r.setDestination(dest);
        return r;
    }
}

