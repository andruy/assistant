package com.andruy.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.andruy.backend.config.SecurityConfig;
import com.andruy.backend.service.InstagramService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InstagramController.class)
@Import(SecurityConfig.class)
class InstagramControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstagramService instagramService;

    @Nested
    @DisplayName("GET /api/instagram/dates")
    class GetDates {

        @Test
        @DisplayName("Returns list of dates")
        @WithMockUser
        void getDates_ReturnsDates() throws Exception {
            List<Date> dates = List.of(
                    Date.valueOf("2024-01-15"),
                    Date.valueOf("2024-01-10")
            );
            when(instagramService.getListOfDates()).thenReturn(dates);

            mockMvc.perform(get("/api/instagram/dates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Requires authentication")
        void getDates_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/api/instagram/dates"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("GET /api/instagram/accounts")
    class GetAccounts {

        @Test
        @DisplayName("Returns accounts for specified date")
        @WithMockUser
        void getAccounts_ReturnsAccounts() throws Exception {
            Map<String, String> accounts = new TreeMap<>();
            accounts.put("user1", "https://instagram.com/user1/");
            accounts.put("user2", "https://instagram.com/user2/");
            when(instagramService.getListOfAccounts(eq("nmf"), any(Date.class))).thenReturn(accounts);

            mockMvc.perform(get("/api/instagram/accounts")
                            .param("date", "2024-01-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user1").value("https://instagram.com/user1/"))
                    .andExpect(jsonPath("$.user2").value("https://instagram.com/user2/"));
        }

        @Test
        @DisplayName("Requires date parameter")
        @WithMockUser
        void getAccounts_WithoutDate_ReturnsError() throws Exception {
            // Missing required param results in server error due to conversion failure
            mockMvc.perform(get("/api/instagram/accounts"))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Requires authentication")
        void getAccounts_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/api/instagram/accounts")
                            .param("date", "2024-01-15"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("GET /api/instagram/compare")
    class GetComparison {

        @Test
        @DisplayName("Starts comparison and returns accepted")
        @WithMockUser
        void getComparison_ReturnsAccepted() throws Exception {
            when(instagramService.getComparison()).thenReturn(CompletableFuture.completedFuture(null));

            mockMvc.perform(get("/api/instagram/compare"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Task started. You will be notified when complete."));
        }

        @Test
        @DisplayName("Requires authentication")
        void getComparison_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/api/instagram/compare"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("GET /api/instagram/compare-dates")
    class GetComparisonBetweenDates {

        @Test
        @DisplayName("Compares dates and returns report")
        @WithMockUser
        void compareDates_ReturnsReport() throws Exception {
            when(instagramService.getComparisonBetweenDates(any(Date.class), any(Date.class)))
                    .thenReturn(Map.of("report", "Comparison complete"));

            mockMvc.perform(get("/api/instagram/compare-dates")
                            .param("dateFollowers", "2024-01-01")
                            .param("dateFollowing", "2024-01-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.report").value("Comparison complete"));
        }

        @Test
        @DisplayName("Requires both date parameters")
        @WithMockUser
        void compareDates_WithoutParams_ReturnsError() throws Exception {
            // Missing required params results in server error due to conversion failure
            mockMvc.perform(get("/api/instagram/compare-dates"))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Requires authentication")
        void compareDates_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/api/instagram/compare-dates")
                            .param("dateFollowers", "2024-01-01")
                            .param("dateFollowing", "2024-01-15"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("DELETE /api/instagram/accounts")
    class DeleteAccounts {

        @Test
        @DisplayName("Starts deletion and returns accepted")
        @WithMockUser
        void deleteAccounts_ReturnsAccepted() throws Exception {
            when(instagramService.deleteAccounts(anyString(), any(Date.class), anyList()))
                    .thenReturn(CompletableFuture.completedFuture(null));
            List<String> accounts = List.of("user1", "user2");

            mockMvc.perform(delete("/api/instagram/accounts")
                            .param("date", "2024-01-15")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(accounts)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Deletion started. You will be notified when complete."));
        }

        @Test
        @DisplayName("Requires authentication")
        void deleteAccounts_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(delete("/api/instagram/accounts")
                            .param("date", "2024-01-15")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"user1\"]"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("PUT /api/instagram/accounts/protect")
    class ProtectAccounts {

        @Test
        @DisplayName("Protects accounts and returns report")
        @WithMockUser
        void protectAccounts_ReturnsReport() throws Exception {
            when(instagramService.protectAccounts(any(Date.class), anyList()))
                    .thenReturn(Map.of("report", "Protected 2 accounts"));
            List<String> accounts = List.of("user1", "user2");

            mockMvc.perform(put("/api/instagram/accounts/protect")
                            .param("date", "2024-01-15")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(accounts)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.report").value("Protected 2 accounts"));

            verify(instagramService).protectAccounts(any(Date.class), anyList());
        }

        @Test
        @DisplayName("Requires authentication")
        void protectAccounts_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(put("/api/instagram/accounts/protect")
                            .param("date", "2024-01-15")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"user1\"]"))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
