package com.epam.edp.demo.scheduler;

import com.epam.edp.demo.service.EmailService;
import com.epam.edp.demo.service.ReportGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportSchedulerTest {

    @Mock ReportGeneratorService reportGenerator;
    @Mock EmailService           emailService;
    @InjectMocks WeeklyReportScheduler scheduler;

    private static final LocalDate START = LocalDate.of(2026, 5, 11);
    private static final LocalDate END   = LocalDate.of(2026, 5, 17);

    @Test
    void triggerReport_generatesAndSendsReport() throws Exception {
        byte[] bytes = new byte[]{1, 2, 3};
        when(reportGenerator.generateReport(START, END)).thenReturn(bytes);

        scheduler.triggerReport(START, END);

        verify(reportGenerator).generateReport(START, END);
        verify(emailService).sendReport(bytes, START, END);
    }

    @Test
    void triggerReport_ioException_doesNotPropagateAndSkipsSend() throws Exception {
        doThrow(new IOException("disk full")).when(reportGenerator).generateReport(START, END);

        // must not throw
        scheduler.triggerReport(START, END);

        verify(emailService, never()).sendReport(any(), any(), any());
    }

    @Test
    void generateAndSendWeeklyReport_usesLastWeekDates() throws Exception {
        LocalDate today    = LocalDate.now();
        LocalDate expEnd   = today.minusDays(1);
        LocalDate expStart = expEnd.minusDays(6);

        when(reportGenerator.generateReport(expStart, expEnd)).thenReturn(new byte[0]);

        scheduler.generateAndSendWeeklyReport();

        verify(reportGenerator).generateReport(expStart, expEnd);
        verify(emailService).sendReport(any(), eq(expStart), eq(expEnd));
    }
}

