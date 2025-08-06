package h2.orm.core.service;

import h2.orm.core.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Database backup and restore service
 * Now uses direct JDBC for H2-specific commands that don't work with JPA
 */
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    /**
     * Create database backup using alternative approach for H2 SCRIPT command
     */
    public void backup(String backupPath) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFile = backupPath + "_" + timestamp + ".sql";

            // Create backup directory if it doesn't exist
            Path backupDir = Paths.get(backupFile).getParent();
            if (backupDir != null && !Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // Use native query approach instead of direct JDBC unwrapping
            TransactionManager.executeInTransaction(em -> {
                try {
                    // Try to execute SCRIPT command using native query with execute
                    var query = em.createNativeQuery("SCRIPT TO '" + backupFile + "'");

                    // For H2 SCRIPT command, we need to use a different approach
                    // Since executeUpdate() doesn't work, we'll use the session to get connection
                    var session = em.unwrap(org.hibernate.Session.class);
                    session.doWork(connection -> {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("SCRIPT TO '" + backupFile + "'");
                            logger.info("Database backup created: {}", backupFile);
                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException("Failed to execute SCRIPT command", e);
                }
                return null;
            });

        } catch (Exception e) {
            logger.error("Failed to create backup", e);
            throw new RuntimeException("Backup creation failed", e);
        }
    }

    /**
     * Restore database from backup using alternative approach for H2 RUNSCRIPT command
     */
    public void restore(String backupPath) {
        try {
            if (!Files.exists(Paths.get(backupPath))) {
                throw new FileNotFoundException("Backup file not found: " + backupPath);
            }

            // Use native query approach instead of direct JDBC unwrapping
            TransactionManager.executeInTransaction(em -> {
                try {
                    // Use session.doWork to get proper connection access
                    var session = em.unwrap(org.hibernate.Session.class);
                    session.doWork(connection -> {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("RUNSCRIPT FROM '" + backupPath + "'");
                            logger.info("Database restored from backup: {}", backupPath);
                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException("Failed to execute RUNSCRIPT command", e);
                }
                return null;
            });

        } catch (Exception e) {
            logger.error("Failed to restore backup", e);
            throw new RuntimeException("Backup restoration failed", e);
        }
    }

    /**
     * Create compressed backup with improved error handling and file management
     */
    public void compressed(String backupPath) {
        String tempFile = null;
        try {
            // Create backup directory if it doesn't exist
            Path backupDir = Paths.get(backupPath).getParent();
            if (backupDir != null && !Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                logger.debug("Created backup directory: {}", backupDir);
            }

            // Create regular backup first - this will generate a file with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            tempFile = backupPath + "_" + timestamp + ".sql";

            // Create backup using the same logic as createBackup but without extra timestamp
            toFile(tempFile);

            // Verify temp file exists before compression
            if (!Files.exists(Paths.get(tempFile))) {
                throw new IOException("Temporary backup file was not created: " + tempFile);
            }

            String compressedFile = backupPath + "_" + timestamp + ".gz";

            // Compress the backup file with proper error handling
            try (FileInputStream fis = new FileInputStream(tempFile);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(compressedFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 GZIPOutputStream gzos = new GZIPOutputStream(bos)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    gzos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                gzos.finish(); // Important: ensure all data is written
                logger.debug("Compressed {} bytes from backup file", totalBytes);
            }

            // Verify compressed file was created successfully
            if (!Files.exists(Paths.get(compressedFile))) {
                throw new IOException("Compressed backup file was not created: " + compressedFile);
            }

            logger.info("Compressed backup created: {}", compressedFile);

        } catch (IOException e) {
            logger.error("IO error during compressed backup creation", e);
            throw new RuntimeException("Compressed backup creation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during compressed backup creation", e);
            throw new RuntimeException("Compressed backup creation failed", e);
        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(Paths.get(tempFile));
                    logger.debug("Cleaned up temporary file: {}", tempFile);
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary backup file: {}", tempFile, e);
                }
            }
        }
    }

    /**
     * Create backup to a specific file path (helper method)
     */
    private void toFile(String backupFile) {
        try {
            // Create backup directory if it doesn't exist
            Path backupDir = Paths.get(backupFile).getParent();
            if (backupDir != null && !Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // Use Hibernate Session.doWork for safe connection access
            TransactionManager.executeInTransaction(em -> {
                try {
                    var session = em.unwrap(org.hibernate.Session.class);
                    session.doWork(connection -> {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("SCRIPT TO '" + backupFile + "'");
                            logger.debug("Database backup created to specific file: {}", backupFile);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to execute SCRIPT command to: " + backupFile, e);
                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException("Failed to create backup to specific file", e);
                }
                return null;
            });

        } catch (Exception e) {
            logger.error("Failed to create backup to specific file: {}", backupFile, e);
            throw new RuntimeException("Backup creation failed to: " + backupFile, e);
        }
    }

    /**
     * Restore from compressed backup with improved error handling
     */
    public void restoreCompressed(String compressedBackupPath) {
        String tempFile = null;
        try {
            // Verify compressed backup file exists
            if (!Files.exists(Paths.get(compressedBackupPath))) {
                throw new FileNotFoundException("Compressed backup file not found: " + compressedBackupPath);
            }

            // Create unique temporary file name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            tempFile = compressedBackupPath.replace(".gz", "_temp_" + timestamp + ".sql");

            // Create temp directory if needed
            Path tempDir = Paths.get(tempFile).getParent();
            if (tempDir != null && !Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // Decompress the backup file with proper error handling
            try (FileInputStream fis = new FileInputStream(compressedBackupPath);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 GZIPInputStream gzis = new GZIPInputStream(bis);
                 FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = gzis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                bos.flush(); // Ensure all data is written
                logger.debug("Decompressed {} bytes to temporary file", totalBytes);
            }

            // Verify decompressed file exists and has content
            if (!Files.exists(Paths.get(tempFile))) {
                throw new IOException("Failed to create decompressed temporary file: " + tempFile);
            }

            if (Files.size(Paths.get(tempFile)) == 0) {
                throw new IOException("Decompressed file is empty: " + tempFile);
            }

            // Restore from decompressed file
            restore(tempFile);

            logger.info("Successfully restored from compressed backup: {}", compressedBackupPath);

        } catch (IOException e) {
            logger.error("IO error during compressed backup restoration", e);
            throw new RuntimeException("Compressed backup restoration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during compressed backup restoration", e);
            throw new RuntimeException("Compressed backup restoration failed", e);
        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(Paths.get(tempFile));
                    logger.debug("Cleaned up temporary restore file: {}", tempFile);
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary restore file: {}", tempFile, e);
                }
            }
        }
    }

    /**
     * Get backup file information with comprehensive error handling
     */
    public BackupInfo getBackupInfo(String backupPath) {
        try {
            if (backupPath == null || backupPath.trim().isEmpty()) {
                logger.warn("Backup path is null or empty");
                return null;
            }

            Path path = Paths.get(backupPath);
            if (!Files.exists(path)) {
                logger.debug("Backup file does not exist: {}", backupPath);
                return null;
            }

            if (!Files.isReadable(path)) {
                logger.warn("Backup file is not readable: {}", backupPath);
                return null;
            }

            BackupInfo info = new BackupInfo();
            info.setFilePath(backupPath);
            info.setFileSize(Files.size(path));
            info.setCreationTime(Files.getLastModifiedTime(path).toInstant());
            info.setCompressed(backupPath.toLowerCase().endsWith(".gz"));

            logger.debug("Retrieved backup info for: {} (size: {} bytes)", backupPath, info.getFileSize());
            return info;

        } catch (IOException e) {
            logger.error("IO error getting backup info for: {}", backupPath, e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error getting backup info for: {}", backupPath, e);
            return null;
        }
    }

    /**
     * Backup information holder
     */
    public static class BackupInfo {
        private String filePath;
        private long fileSize;
        private java.time.Instant creationTime;
        private boolean compressed;

        // Getters and setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public java.time.Instant getCreationTime() { return creationTime; }
        public void setCreationTime(java.time.Instant creationTime) { this.creationTime = creationTime; }

        public boolean isCompressed() { return compressed; }
        public void setCompressed(boolean compressed) { this.compressed = compressed; }

        @Override
        public String toString() {
            return "BackupInfo{" +
                    "filePath='" + filePath + '\'' +
                    ", fileSize=" + fileSize +
                    ", creationTime=" + creationTime +
                    ", compressed=" + compressed +
                    '}';
        }
    }
}
