package h2.orm.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Professional export service with mature file handling
 * Automatically creates directories, handles file conflicts, and provides robust error handling
 */
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private final ObjectMapper objectMapper;
    private boolean overwriteExisting = true;
    private boolean addTimestampIfExists = false;

    public ExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Configure Jackson to handle Hibernate proxy objects
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Add Hibernate module for proper proxy handling
        try {
            // Try to register Hibernate5Module if available
            Class<?> hibernateModuleClass = Class.forName("com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module");
            Object hibernateModule = hibernateModuleClass.getDeclaredConstructor().newInstance();
            this.objectMapper.registerModule((com.fasterxml.jackson.databind.Module) hibernateModule);
            logger.debug("Hibernate5Module registered for JSON serialization");
        } catch (Exception e) {
            // Hibernate module not available, use manual configuration
            logger.debug("Hibernate5Module not available, using manual proxy handling");
            this.objectMapper.addMixIn(Object.class, HibernateProxyMixin.class);
        }
    }

    /**
     * Set file handling behavior
     */
    public ExportService setOverwriteExisting(boolean overwrite) {
        this.overwriteExisting = overwrite;
        return this;
    }

    public ExportService setAddTimestampIfExists(boolean addTimestamp) {
        this.addTimestampIfExists = addTimestamp;
        return this;
    }

    /**
     * Export data to CSV file with professional file handling
     */
    public <T> void toCsv(List<T> data, String filePath) {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to export to CSV - data is null or empty");
            return;
        }

        String resolvedPath = null;
        try {
            // Resolve file path with professional handling
            resolvedPath = resolveFilePath(filePath, "csv");

            // Ensure parent directory exists
            createDirectoriesIfNeeded(resolvedPath);

            // Export with buffered writer for better performance
            try (FileOutputStream fos = new FileOutputStream(resolvedPath);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                 BufferedWriter writer = new BufferedWriter(osw)) {

                Class<?> clazz = data.get(0).getClass();
                Field[] fields = getAllFields(clazz);

                // Write CSV header
                writeCsvHeader(writer, fields);

                // Write CSV data
                writeCsvData(writer, data, fields);

                writer.flush();
                logger.info("Successfully exported {} records to CSV: {}", data.size(), resolvedPath);
            }

        } catch (IOException e) {
            logger.error("IO error during CSV export to: {}", resolvedPath, e);
            throw new RuntimeException("CSV export failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during CSV export", e);
            throw new RuntimeException("CSV export failed: " + e.getMessage(), e);
        }
    }

    /**
     * Export data to Excel file with professional file handling
     */
    public <T> void toExcel(List<T> data, String filePath) {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to export to Excel - data is null or empty");
            return;
        }

        String resolvedPath = null;
        try {
            // Resolve file path with professional handling
            resolvedPath = resolveFilePath(filePath, "xlsx");

            // Ensure parent directory exists
            createDirectoriesIfNeeded(resolvedPath);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Data");

                Class<?> clazz = data.get(0).getClass();
                Field[] fields = getAllFields(clazz);

                // Create header row with styling
                createExcelHeader(workbook, sheet, fields);

                // Create data rows
                createExcelData(sheet, data, fields);

                // Auto-size columns for better readability
                autoSizeColumns(sheet, fields.length);

                // Write to file with proper buffering
                try (FileOutputStream fileOut = new FileOutputStream(resolvedPath);
                     BufferedOutputStream bos = new BufferedOutputStream(fileOut)) {
                    workbook.write(bos);
                    bos.flush();
                }

                logger.info("Successfully exported {} records to Excel: {}", data.size(), resolvedPath);
            }

        } catch (IOException e) {
            logger.error("IO error during Excel export to: {}", resolvedPath, e);
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during Excel export", e);
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        }
    }

    /**
     * Export data to JSON file with professional file handling
     */
    public <T> void toJson(List<T> data, String filePath) {
        String resolvedPath = null;
        try {
            // Resolve file path with professional handling
            resolvedPath = resolveFilePath(filePath, "json");

            // Ensure parent directory exists
            createDirectoriesIfNeeded(resolvedPath);

            // Write JSON directly to file to avoid stream closing issues
            File outputFile = new File(resolvedPath);
            objectMapper.writeValue(outputFile, data);

            logger.info("Successfully exported {} records to JSON: {}",
                       data != null ? data.size() : 0, resolvedPath);

        } catch (IOException e) {
            logger.error("IO error during JSON export to: {}", resolvedPath, e);
            throw new RuntimeException("JSON export failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during JSON export", e);
            throw new RuntimeException("JSON export failed: " + e.getMessage(), e);
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Professional file path resolution with conflict handling
     */
    private String resolveFilePath(String originalPath, String expectedExtension) {
        if (originalPath == null || originalPath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        String path = originalPath.trim();

        // Add extension if missing
        if (!path.toLowerCase().endsWith("." + expectedExtension.toLowerCase())) {
            path = path + "." + expectedExtension;
        }

        Path filePath = Paths.get(path);

        // Handle file conflicts professionally
        if (Files.exists(filePath)) {
            if (overwriteExisting && !addTimestampIfExists) {
                logger.debug("File exists, will overwrite: {}", path);
                return path;
            } else if (addTimestampIfExists) {
                return createTimestampedPath(path);
            } else {
                // Default behavior: add timestamp to avoid conflicts
                return createTimestampedPath(path);
            }
        }

        return path;
    }

    /**
     * Create timestamped file path to avoid conflicts
     */
    private String createTimestampedPath(String originalPath) {
        Path path = Paths.get(originalPath);
        String fileName = path.getFileName().toString();
        String directory = path.getParent() != null ? path.getParent().toString() : "";

        int lastDot = fileName.lastIndexOf('.');
        String baseName = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        String extension = lastDot > 0 ? fileName.substring(lastDot) : "";

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String newFileName = baseName + "_" + timestamp + extension;

        String newPath = directory.isEmpty() ? newFileName :
                        Paths.get(directory, newFileName).toString();

        logger.debug("Created timestamped path to avoid conflict: {}", newPath);
        return newPath;
    }

    /**
     * Create directories if they don't exist
     */
    private void createDirectoriesIfNeeded(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                logger.debug("Created directories: {}", parentDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories for: " + filePath, e);
        }
    }

    /**
     * Get all fields including inherited ones
     */
    private Field[] getAllFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    /**
     * Write CSV header row
     */
    private void writeCsvHeader(BufferedWriter writer, Field[] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) writer.append(",");
            writer.append(escapeCsvValue(fields[i].getName()));
        }
        writer.append("\n");
    }

    /**
     * Write CSV data rows with proper Hibernate proxy handling
     */
    private <T> void writeCsvData(BufferedWriter writer, List<T> data, Field[] fields) throws IOException {
        for (T item : data) {
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) writer.append(",");

                try {
                    fields[i].setAccessible(true);
                    Object value = fields[i].get(item);
                    String stringValue = "";

                    if (value != null) {
                        try {
                            // Try to get the string representation
                            stringValue = value.toString();
                        } catch (org.hibernate.LazyInitializationException e) {
                            // Handle Hibernate proxy objects that are not initialized
                            logger.warn("LazyInitializationException for field: {} - using class name instead", fields[i].getName());
                            stringValue = value.getClass().getSimpleName() + "@" + System.identityHashCode(value);
                        }
                    }

                    writer.append(escapeCsvValue(stringValue));
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access field: {}", fields[i].getName());
                    writer.append("");
                }
            }
            writer.append("\n");
        }
    }

    /**
     * Properly escape CSV values
     */
    private String escapeCsvValue(String value) {
        if (value == null) return "";

        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Create Excel header with styling
     */
    private void createExcelHeader(Workbook workbook, Sheet sheet, Field[] fields) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields[i].getName());
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Create Excel data rows
     */
    private <T> void createExcelData(Sheet sheet, List<T> data, Field[] fields) {
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            T item = data.get(rowIndex);

            for (int colIndex = 0; colIndex < fields.length; colIndex++) {
                Cell cell = row.createCell(colIndex);

                try {
                    fields[colIndex].setAccessible(true);
                    Object value = fields[colIndex].get(item);
                    setCellValue(cell, value);
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access field: {}", fields[colIndex].getName());
                    cell.setCellValue("");
                }
            }
        }
    }

    /**
     * Set Excel cell value with proper type handling
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
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

    /**
     * Auto-size Excel columns
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Set minimum width to avoid too narrow columns
            if (sheet.getColumnWidth(i) < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
        }
    }

    /**
     * Mixin class to ignore Hibernate proxy properties during JSON serialization
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private static abstract class HibernateProxyMixin {
    }
}
