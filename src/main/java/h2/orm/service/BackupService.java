package h2.orm.service;

import h2.orm.core.TransactionManager;
import h2.orm.core.EntityManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Database backup and restore service
 * Now uses core TransactionManager and QueryExecutor for better reliability
 */
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    /**
     * Create database backup using core components
     */
    public void createBackup(String backupPath) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFile = backupPath + "_" + timestamp + ".sql";

            // Use TransactionManager for safe backup operation
            TransactionManager.executeInTransaction(em -> {
                em.createNativeQuery("SCRIPT TO '" + backupFile + "'").executeUpdate();
                logger.info("Database backup created: {}", backupFile);
                return null;
            });

        } catch (Exception e) {
            logger.error("Failed to create backup", e);
            throw new RuntimeException("Backup creation failed", e);
        }
    }

    /**
     * Restore database from backup using core components
     */
    public void restoreBackup(String backupPath) {
        try {
            if (!Files.exists(Paths.get(backupPath))) {
                throw new FileNotFoundException("Backup file not found: " + backupPath);
            }

            // Use TransactionManager for safe restore operation
            TransactionManager.executeInTransaction(em -> {
                em.createNativeQuery("RUNSCRIPT FROM '" + backupPath + "'").executeUpdate();
                logger.info("Database restored from backup: {}", backupPath);
                return null;
            });

        } catch (Exception e) {
            logger.error("Failed to restore backup", e);
            throw new RuntimeException("Backup restoration failed", e);
        }
    }

    /**
     * Create compressed backup with improved performance
     */
    public void createCompressedBackup(String backupPath) {
        try {
            String tempFile = backupPath + ".tmp";
            createBackup(tempFile);

            // Compress the backup file
            try (FileInputStream fis = new FileInputStream(tempFile);
                 FileOutputStream fos = new FileOutputStream(backupPath + ".gz");
                 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {

                byte[] buffer = new byte[8192]; // Increased buffer size for better performance
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    gzos.write(buffer, 0, len);
                }
            }

            // Delete temporary file
            Files.deleteIfExists(Paths.get(tempFile));
            logger.info("Compressed backup created: {}.gz", backupPath);

        } catch (Exception e) {
            logger.error("Failed to create compressed backup", e);
            throw new RuntimeException("Compressed backup creation failed", e);
        }
    }

    /**
     * Restore from compressed backup
     */
    public void restoreCompressedBackup(String compressedBackupPath) {
        try {
            String tempFile = compressedBackupPath.replace(".gz", ".tmp");

            // Decompress the backup file
            try (FileInputStream fis = new FileInputStream(compressedBackupPath);
                 GZIPInputStream gzis = new GZIPInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192]; // Increased buffer size
                int len;
                while ((len = gzis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }

            // Restore from decompressed file
            restoreBackup(tempFile);

            // Clean up
            Files.deleteIfExists(Paths.get(tempFile));

        } catch (Exception e) {
            logger.error("Failed to restore compressed backup", e);
            throw new RuntimeException("Compressed backup restoration failed", e);
        }
    }

    /**
     * Get backup file information
     */
    public BackupInfo getBackupInfo(String backupPath) {
        try {
            Path path = Paths.get(backupPath);
            if (!Files.exists(path)) {
                return null;
            }

            BackupInfo info = new BackupInfo();
            info.setFilePath(backupPath);
            info.setFileSize(Files.size(path));
            info.setCreationTime(Files.getLastModifiedTime(path).toInstant());
            info.setCompressed(backupPath.endsWith(".gz"));

            return info;

        } catch (Exception e) {
            logger.warn("Failed to get backup info for: {}", backupPath, e);
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
