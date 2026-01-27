package com.andruy.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.andruy.backend.config.SecurityConfig;
import com.andruy.backend.service.PolloService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PolloController.class)
@Import(SecurityConfig.class)
class PolloControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PolloService polloService;

    @Test
    @DisplayName("POST /api/pollo processes payload successfully")
    @WithMockUser
    void pollo_WhenSuccess_ReturnsProcessed() throws Exception {
        Map<String, String> payload = Map.of(
                "code", "1234567890123456",
                "meal", "Lunch",
                "visit", "Dine-in"
        );
        when(polloService.pollo(any())).thenReturn("Processed");

        mockMvc.perform(post("/api/pollo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Processed"));

        verify(polloService).pollo(any());
    }

    @Test
    @DisplayName("POST /api/pollo returns error when processing fails")
    @WithMockUser
    void pollo_WhenFails_ReturnsError() throws Exception {
        Map<String, String> payload = Map.of(
                "code", "1234567890123456",
                "meal", "Lunch",
                "visit", "Dine-in"
        );
        when(polloService.pollo(any())).thenReturn("Error");

        mockMvc.perform(post("/api/pollo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Error"));
    }

    @Test
    @DisplayName("POST /api/pollo requires authentication")
    void pollo_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
        Map<String, String> payload = Map.of(
                "code", "1234567890123456",
                "meal", "Lunch",
                "visit", "Dine-in"
        );

        mockMvc.perform(post("/api/pollo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /api/pollo passes payload to service")
    @WithMockUser
    void pollo_PassesPayloadToService() throws Exception {
        Map<String, String> payload = Map.of(
                "code", "9876543210987654",
                "meal", "Dinner",
                "visit", "Drive-through"
        );
        when(polloService.pollo(any())).thenReturn("Processed");

        mockMvc.perform(post("/api/pollo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(polloService).pollo(payload);
    }

    @Test
    @DisplayName("POST /api/pollo handles empty payload")
    @WithMockUser
    void pollo_WithEmptyPayload_CallsService() throws Exception {
        when(polloService.pollo(any())).thenReturn("Error");

        mockMvc.perform(post("/api/pollo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Error"));
    }
}
