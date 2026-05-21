package com.epam.edp.demo.scheduler;

import com.epam.edp.demo.service.EmailService;
import com.epam.edp.demo.service.ReportGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Triggers weekly report generation every Monday at 14:00 IST (Asia/Kolkata),
 * covering the previous Monday-to-Sunday period.
 */
@Component
public class WeeklyReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportScheduler.class);

    private final ReportGeneratorService reportGenerator;
    private final EmailService emailService;

    public WeeklyReportScheduler(ReportGeneratorService reportGenerator,
                                  EmailService emailService) {
        this.reportGenerator = reportGenerator;
        this.emailService    = emailService;
    }

    /**
     * Runs every Monday at 14:00 IST (Asia/Kolkata) = 08:30 UTC.
     * Generates the report for the previous week (Mon–Sun).
     */
    @Scheduled(cron = "0 0 14 * * MON", zone = "Asia/Kolkata")
    public void generateAndSendWeeklyReport() {
        LocalDate today     = LocalDate.now();
        LocalDate weekEnd   = today.minusDays(1);             // last Sunday
        LocalDate weekStart = weekEnd.minusDays(6);           // last Monday
        triggerReport(weekStart, weekEnd);
    }

    /**
     * Manual trigger endpoint-friendly method — callable from the controller for testing.
     */
    public void triggerReport(LocalDate weekStart, LocalDate weekEnd) {
        log.info("Generating weekly report for period {} – {}", weekStart, weekEnd);
        try {
            byte[] reportBytes = reportGenerator.generateReport(weekStart, weekEnd);
            emailService.sendReport(reportBytes, weekStart, weekEnd);
            log.info("Weekly report generated and sent for {} – {}", weekStart, weekEnd);
        } catch (IOException ex) {
            log.error("Failed to generate weekly report: {}", ex.getMessage(), ex);
        }
    }
}


