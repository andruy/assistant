package com.andruy.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.andruy.backend.config.SecurityConfig;
import com.andruy.backend.service.LogService;

@WebMvcTest(LogController.class)
@Import(SecurityConfig.class)
class LogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    @Test
    @DisplayName("GET /api/logs returns log contents")
    @WithMockUser
    void logReader_ReturnsLogContents() throws Exception {
        String logContent = "2024-01-15 10:00:00 INFO - Application started\n" +
                           "2024-01-15 10:00:01 DEBUG - Processing request";
        when(logService.logReader()).thenReturn(Map.of("report", logContent));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value(logContent));
    }

    @Test
    @DisplayName("GET /api/logs returns empty report when no logs")
    @WithMockUser
    void logReader_WhenNoLogs_ReturnsEmptyReport() throws Exception {
        when(logService.logReader()).thenReturn(Map.of("report", ""));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value(""));
    }

    @Test
    @DisplayName("GET /api/logs requires authentication")
    void logReader_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/logs"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /api/logs handles large log files")
    @WithMockUser
    void logReader_WithLargeLogs_ReturnsContent() throws Exception {
        StringBuilder largeLog = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeLog.append("Log line ").append(i).append("\n");
        }
        when(logService.logReader()).thenReturn(Map.of("report", largeLog.toString()));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").exists());
    }

    @Test
    @DisplayName("GET /api/logs handles special characters in logs")
    @WithMockUser
    void logReader_WithSpecialCharacters_ReturnsContent() throws Exception {
        String logWithSpecialChars = "Error: Invalid JSON { \"key\": \"value\" }";
        when(logService.logReader()).thenReturn(Map.of("report", logWithSpecialChars));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value(logWithSpecialChars));
    }
}
