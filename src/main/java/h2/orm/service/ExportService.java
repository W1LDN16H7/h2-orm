package h2.orm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Export service for converting data to various formats (CSV, Excel, JSON)
 */
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private final ObjectMapper objectMapper;

    public ExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Export data to CSV file
     */
    public <T> void exportToCsv(List<T> data, String filePath) {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to export to CSV");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            Class<?> clazz = data.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();

            // Write header
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) writer.append(",");
                writer.append(fields[i].getName());
            }
            writer.append("\n");

            // Write data
            for (T item : data) {
                for (int i = 0; i < fields.length; i++) {
                    if (i > 0) writer.append(",");

                    fields[i].setAccessible(true);
                    Object value = fields[i].get(item);
                    String stringValue = value != null ? value.toString() : "";

                    // Escape commas and quotes in CSV
                    if (stringValue.contains(",") || stringValue.contains("\"")) {
                        stringValue = "\"" + stringValue.replace("\"", "\"\"") + "\"";
                    }

                    writer.append(stringValue);
                }
                writer.append("\n");
            }

            logger.info("Data exported to CSV: {} ({} records)", filePath, data.size());

        } catch (Exception e) {
            logger.error("Failed to export to CSV", e);
            throw new RuntimeException("CSV export failed", e);
        }
    }

    /**
     * Export data to Excel file
     */
    public <T> void exportToExcel(List<T> data, String filePath) {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to export to Excel");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            Class<?> clazz = data.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < fields.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields[i].getName());
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                T item = data.get(rowIndex);

                for (int colIndex = 0; colIndex < fields.length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    fields[colIndex].setAccessible(true);
                    Object value = fields[colIndex].get(item);

                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else if (value instanceof LocalDate) {
                            cell.setCellValue(value.toString());
                        } else if (value instanceof LocalDateTime) {
                            cell.setCellValue(value.toString());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < fields.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            logger.info("Data exported to Excel: {} ({} records)", filePath, data.size());

        } catch (Exception e) {
            logger.error("Failed to export to Excel", e);
            throw new RuntimeException("Excel export failed", e);
        }
    }

    /**
     * Export data to JSON file
     */
    public <T> void exportToJson(List<T> data, String filePath) {
        try {
            objectMapper.writeValue(new java.io.File(filePath), data);
            logger.info("Data exported to JSON: {} ({} records)", filePath, data != null ? data.size() : 0);
        } catch (IOException e) {
            logger.error("Failed to export to JSON", e);
            throw new RuntimeException("JSON export failed", e);
        }
    }

    /**
     * Export single object to JSON file
     */
    public <T> void exportToJson(T object, String filePath) {
        try {
            objectMapper.writeValue(new java.io.File(filePath), object);
            logger.info("Object exported to JSON: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to export to JSON", e);
            throw new RuntimeException("JSON export failed", e);
        }
    }
}
