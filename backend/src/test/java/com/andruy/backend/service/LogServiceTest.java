package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings("ResultOfMethodCallIgnored")
class LogServiceTest {
    private LogService logService;
    private File logsDir;
    private File logFile;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        logService = new LogService();
        logsDir = new File("logs");
        logsDir.mkdirs();
        logFile = new File(logsDir, "app.log");
    }

    @AfterEach
    void tearDown() {
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    @Test
    @DisplayName("Should read log file contents successfully")
    void logReader_WhenLogFileExists_ReturnsContents() throws IOException {
        String logContent = "2024-01-15 10:30:00 INFO - Application started\n" +
                           "2024-01-15 10:30:01 DEBUG - Processing request\n";
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(logContent);
        }

        Map<String, String> result = logService.logReader();

        assertThat(result).containsKey("report");
        assertThat(result.get("report")).contains("Application started");
        assertThat(result.get("report")).contains("Processing request");
    }

    @Test
    @DisplayName("Should return empty report when log file does not exist")
    void logReader_WhenLogFileNotExists_ReturnsEmptyReport() {
        if (logFile.exists()) {
            logFile.delete();
        }

        Map<String, String> result = logService.logReader();

        assertThat(result).containsKey("report");
        assertThat(result.get("report")).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty log file")
    void logReader_WhenLogFileEmpty_ReturnsEmptyReport() throws IOException {
        logFile.createNewFile();

        Map<String, String> result = logService.logReader();

        assertThat(result).containsKey("report");
        assertThat(result.get("report")).isEmpty();
    }

    @Test
    @DisplayName("Should preserve line breaks in log content")
    void logReader_PreservesLineBreaks() throws IOException {
        String line1 = "Line 1";
        String line2 = "Line 2";
        String line3 = "Line 3";
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(line1 + "\n" + line2 + "\n" + line3 + "\n");
        }

        Map<String, String> result = logService.logReader();

        String report = result.get("report");
        assertThat(report).contains(line1);
        assertThat(report).contains(line2);
        assertThat(report).contains(line3);
        assertThat(report.split("\n")).hasSize(3);
    }

    @Test
    @DisplayName("Should handle large log files")
    void logReader_HandlesLargeFiles() throws IOException {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Log line ").append(i).append(" - Some log message here\n");
        }
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(largeContent.toString());
        }

        Map<String, String> result = logService.logReader();

        assertThat(result.get("report")).isNotEmpty();
        assertThat(result.get("report").split("\n")).hasSize(1000);
    }
}
