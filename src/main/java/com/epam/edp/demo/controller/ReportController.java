package com.epam.edp.demo.controller;

import com.epam.edp.demo.model.AgentReportRecord;
import com.epam.edp.demo.model.TourReportRecord;
import com.epam.edp.demo.scheduler.WeeklyReportScheduler;
import com.epam.edp.demo.service.StatsAggregationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report management endpoints.
 *
 * <ul>
 *   <li>POST /api/reports/trigger – compute stats, save to DB, generate Excel, email it</li>
 *   <li>GET  /api/reports/agents  – read stored agent-performance rows from DB</li>
 *   <li>GET  /api/reports/tours   – read stored tour-statistics rows from DB</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final WeeklyReportScheduler scheduler;
    private final StatsAggregationService statsService;

    public ReportController(WeeklyReportScheduler scheduler,
                             StatsAggregationService statsService) {
        this.scheduler    = scheduler;
        this.statsService = statsService;
    }

    /**
     * Triggers a full report cycle: compute stats → save to MongoDB → generate Excel → email.
     * Example: POST /api/reports/trigger?weekStart=2026-05-11&weekEnd=2026-05-17
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd
    ) {
        LocalDate end   = weekEnd   != null ? weekEnd   : LocalDate.now().minusDays(1);
        LocalDate start = weekStart != null ? weekStart : end.minusDays(6);

        scheduler.triggerReport(start, end);
        return ResponseEntity.ok(Map.of(
                "status",    "triggered",
                "weekStart", start.toString(),
                "weekEnd",   end.toString()
        ));
    }

    /**
     * Returns the stored agent-performance rows from MongoDB for the given period.
     * Example: GET /api/reports/agents?periodStart=2026-05-11&periodEnd=2026-05-17
     */
    @GetMapping("/agents")
    public ResponseEntity<List<AgentReportRecord>> getAgentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        List<AgentReportRecord> rows = statsService.getStoredAgentRows(periodStart, periodEnd);
        return ResponseEntity.ok(rows);
    }

    /**
     * Returns the stored tour-statistics rows from MongoDB for the given period.
     * Example: GET /api/reports/tours?periodStart=2026-05-11&periodEnd=2026-05-17
     */
    @GetMapping("/tours")
    public ResponseEntity<List<TourReportRecord>> getTourReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        List<TourReportRecord> rows = statsService.getStoredTourRows(periodStart, periodEnd);
        return ResponseEntity.ok(rows);
    }
}
