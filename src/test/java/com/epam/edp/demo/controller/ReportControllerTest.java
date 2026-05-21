package com.epam.edp.demo.controller;

import com.epam.edp.demo.model.AgentReportRecord;
import com.epam.edp.demo.model.TourReportRecord;
import com.epam.edp.demo.scheduler.WeeklyReportScheduler;
import com.epam.edp.demo.service.StatsAggregationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock WeeklyReportScheduler   scheduler;
    @Mock StatsAggregationService statsService;
    @InjectMocks ReportController controller;

    private static final LocalDate START = LocalDate.of(2026, 5, 11);
    private static final LocalDate END   = LocalDate.of(2026, 5, 17);

    @Test
    void trigger_withExplicitDates_delegatesToScheduler() {
        ResponseEntity<Map<String, String>> resp = controller.trigger(START, END);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsEntry("status", "triggered");
        assertThat(resp.getBody()).containsEntry("weekStart", "2026-05-11");
        assertThat(resp.getBody()).containsEntry("weekEnd",   "2026-05-17");
        verify(scheduler).triggerReport(START, END);
    }

    @Test
    void trigger_withNullDates_defaultsToLastWeek() {
        LocalDate today   = LocalDate.now();
        LocalDate expEnd   = today.minusDays(1);
        LocalDate expStart = expEnd.minusDays(6);

        ResponseEntity<Map<String, String>> resp = controller.trigger(null, null);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(scheduler).triggerReport(expStart, expEnd);
    }

    @Test
    void getAgentReport_returnsRowsFromService() {
        AgentReportRecord row = new AgentReportRecord();
        when(statsService.getStoredAgentRows(START, END)).thenReturn(List.of(row));

        ResponseEntity<List<AgentReportRecord>> resp = controller.getAgentReport(START, END);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(row);
    }

    @Test
    void getTourReport_returnsRowsFromService() {
        TourReportRecord row = new TourReportRecord();
        when(statsService.getStoredTourRows(START, END)).thenReturn(List.of(row));

        ResponseEntity<List<TourReportRecord>> resp = controller.getTourReport(START, END);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(row);
    }

    @Test
    void getAgentReport_emptyPeriod_returnsEmptyList() {
        when(statsService.getStoredAgentRows(START, END)).thenReturn(List.of());

        ResponseEntity<List<AgentReportRecord>> resp = controller.getAgentReport(START, END);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }
}

