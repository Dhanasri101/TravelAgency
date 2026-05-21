package com.epam.edp.demo.service;

import com.epam.edp.demo.model.AgentReportRecord;
import com.epam.edp.demo.model.TourReportRecord;
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
 * <ol>
 *   <li>Agent Performance</li>
 *   <li>Sales Statistics</li>
 * </ol>
 * Reads data directly from {@link AgentReportRecord} and {@link TourReportRecord}
 * — no intermediate DTO conversion.
 */
@Service
public class ReportGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final StatsAggregationService stats;

    public ReportGeneratorService(StatsAggregationService stats) {
        this.stats = stats;
    }

    public byte[] generateReport(LocalDate weekStart, LocalDate weekEnd) throws IOException {
        List<AgentReportRecord> agentRows = stats.getAgentPerformance(weekStart, weekEnd);
        List<TourReportRecord>  tourRows  = stats.getTourStatistics(weekStart, weekEnd);

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

    private void buildAgentSheet(Workbook wb, List<AgentReportRecord> rows) {
        Sheet sheet = wb.createSheet("Agent Performance");
        CellStyle header = createHeaderStyle(wb);
        CellStyle alt    = createAltRowStyle(wb);

        String[] headers = {
            "Travel Agent", "TA E-mail",
            "Report Period Start", "Report Period End",
            "Tours Sold", "Delta of Tours Sold %",
            "Avg Feedback Rate (1-5)", "Min Feedback Rate (1-5)",
            "Delta of Avg Feedback %", "Revenue (USD)"
        };
        writeHeaderRow(sheet, headers, header);

        for (int i = 0; i < rows.size(); i++) {
            AgentReportRecord r = rows.get(i);
            CellStyle style = (i % 2 == 1) ? alt : null;
            Row row = sheet.createRow(i + 1);
            int c = 0;
            writeCell(row, c++, r.getAgentName(),                              style);
            writeCell(row, c++, r.getAgentEmail(),                             style);
            writeCell(row, c++, r.getPeriodStart().format(DATE_FMT),           style);
            writeCell(row, c++, r.getPeriodEnd().format(DATE_FMT),             style);
            writeCell(row, c++, r.getToursSold(),                              style);
            writeCell(row, c++, r.getDeltaOfToursSoldPct(),                    style);
            writeCell(row, c++, fmt(r.getAvgFeedbackRate()),                   style);
            writeCell(row, c++, fmt(r.getMinFeedbackRate()),                   style);
            writeCell(row, c++, r.getDeltaOfAvgFeedbackPct(),                  style);
            writeCell(row, c,   formatRevenue(r.getRevenueUsd()),              style);
        }
        autoSize(sheet, headers.length);
    }

    // ─────────────────────────────────────────────
    // Sheet 2 – Sales Statistics
    // ─────────────────────────────────────────────

    private void buildTourSheet(Workbook wb, List<TourReportRecord> rows) {
        Sheet sheet = wb.createSheet("Sales Statistics");
        CellStyle header = createHeaderStyle(wb);
        CellStyle alt    = createAltRowStyle(wb);

        String[] headers = {
            "Tour Name", "Destination",
            "Report Period Start", "Report Period End",
            "Tours Sold", "Delta of Tours Sold %",
            "Avg Feedback Rate (1-5)", "Min Feedback Rate (1-5)",
            "Delta of Avg Feedback %", "Revenue (USD)"
        };
        writeHeaderRow(sheet, headers, header);

        for (int i = 0; i < rows.size(); i++) {
            TourReportRecord r = rows.get(i);
            CellStyle style = (i % 2 == 1) ? alt : null;
            Row row = sheet.createRow(i + 1);
            int c = 0;
            writeCell(row, c++, r.getTourName(),                               style);
            writeCell(row, c++, r.getDestination(),                            style);
            writeCell(row, c++, r.getPeriodStart().format(DATE_FMT),           style);
            writeCell(row, c++, r.getPeriodEnd().format(DATE_FMT),             style);
            writeCell(row, c++, r.getToursSold(),                              style);
            writeCell(row, c++, r.getDeltaOfToursSoldPct(),                    style);
            writeCell(row, c++, fmt(r.getAvgFeedbackRate()),                   style);
            writeCell(row, c++, fmt(r.getMinFeedbackRate()),                   style);
            writeCell(row, c++, r.getDeltaOfAvgFeedbackPct(),                  style);
            writeCell(row, c,   formatRevenue(r.getRevenueUsd()),              style);
        }
        autoSize(sheet, headers.length);
    }

    // ─────────────────────────────────────────────
    // Shared helpers
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

    private void autoSize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) sheet.autoSizeColumn(i);
    }

    private String fmt(double value)       { return String.format("%.1f", value); }
    private String formatRevenue(long amt) { return String.format("%,d", amt); }
}
