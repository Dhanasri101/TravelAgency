package com.epam.edp.demo.service;

import com.epam.edp.demo.service.StatsAggregationService.AgentRow;
import com.epam.edp.demo.service.StatsAggregationService.TourRow;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates an Excel workbook (.xlsx) with two sheets:
 * 1. Agent Performance
 * 2. Sales Statistics
 */
@Service
public class ReportGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final StatsAggregationService stats;

    public ReportGeneratorService(StatsAggregationService stats) {
        this.stats = stats;
    }

    /**
     * Builds and returns the Excel file bytes for the given week.
     */
    public byte[] generateReport(LocalDate weekStart, LocalDate weekEnd) throws IOException {
        List<AgentRow> agentRows = stats.getAgentPerformance(weekStart, weekEnd);
        List<TourRow>  tourRows  = stats.getTourStatistics(weekStart, weekEnd);

        try (Workbook wb = new XSSFWorkbook()) {
            buildAgentSheet(wb, agentRows);
            buildTourSheet(wb, tourRows);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─────────────────────────────────────────────
    // Sheet 1 – Agent Performance
    // ─────────────────────────────────────────────

    private void buildAgentSheet(Workbook wb, List<AgentRow> rows) {
        Sheet sheet = wb.createSheet("Agent Performance");
        CellStyle headerStyle = createHeaderStyle(wb);

        String[] headers = {
            "Travel Agent", "TA E-mail",
            "Report Period Start", "Report Period End",
            "Tours Sold", "Delta of Tours Sold %",
            "Avg Feedback Rate (1-5)", "Min Feedback Rate (1-5)",
            "Delta of Avg Feedback %", "Revenue (USD)"
        };
        writeHeaderRow(sheet, headers, headerStyle);

        int rowIdx = 1;
        CellStyle altStyle = createAltRowStyle(wb);
        for (AgentRow r : rows) {
            Row row = sheet.createRow(rowIdx);
            CellStyle style = (rowIdx % 2 == 0) ? altStyle : null;
            int c = 0;
            writeCell(row, c++, r.name(), style);
            writeCell(row, c++, r.email(), style);
            writeCell(row, c++, r.periodStart().format(DATE_FMT), style);
            writeCell(row, c++, r.periodEnd().format(DATE_FMT), style);
            writeCell(row, c++, r.toursSold(), style);
            writeCell(row, c++, r.deltaOfToursSoldPct(), style);
            writeCell(row, c++, String.format("%.1f", r.avgFeedbackRate()), style);
            writeCell(row, c++, String.format("%.1f", r.minFeedbackRate()), style);
            writeCell(row, c++, r.deltaOfAvgFeedbackPct(), style);
            writeCell(row, c,   formatRevenue(r.revenueUsd()), style);
            rowIdx++;
        }
        autoSizeColumns(sheet, headers.length);
    }

    // ─────────────────────────────────────────────
    // Sheet 2 – Sales Statistics
    // ─────────────────────────────────────────────

    private void buildTourSheet(Workbook wb, List<TourRow> rows) {
        Sheet sheet = wb.createSheet("Sales Statistics");
        CellStyle headerStyle = createHeaderStyle(wb);

        String[] headers = {
            "Tour Name", "Destination",
            "Report Period Start", "Report Period End",
            "Tours Sold", "Delta of Tours Sold %",
            "Avg Feedback Rate (1-5)", "Min Feedback Rate (1-5)",
            "Delta of Avg Feedback %", "Revenue (USD)"
        };
        writeHeaderRow(sheet, headers, headerStyle);

        int rowIdx = 1;
        CellStyle altStyle = createAltRowStyle(wb);
        for (TourRow r : rows) {
            Row row = sheet.createRow(rowIdx);
            CellStyle style = (rowIdx % 2 == 0) ? altStyle : null;
            int c = 0;
            writeCell(row, c++, r.tourName(), style);
            writeCell(row, c++, r.destination(), style);
            writeCell(row, c++, r.periodStart().format(DATE_FMT), style);
            writeCell(row, c++, r.periodEnd().format(DATE_FMT), style);
            writeCell(row, c++, r.toursSold(), style);
            writeCell(row, c++, r.deltaOfToursSoldPct(), style);
            writeCell(row, c++, String.format("%.1f", r.avgFeedbackRate()), style);
            writeCell(row, c++, String.format("%.1f", r.minFeedbackRate()), style);
            writeCell(row, c++, r.deltaOfAvgFeedbackPct(), style);
            writeCell(row, c,   formatRevenue(r.revenueUsd()), style);
            rowIdx++;
        }
        autoSizeColumns(sheet, headers.length);
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private void writeHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void writeCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        if (style != null) cell.setCellStyle(style);
    }

    private void writeCell(Row row, int col, long value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createAltRowStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String formatRevenue(long amount) {
        // Format as 246,500
        return String.format("%,d", amount);
    }
}
