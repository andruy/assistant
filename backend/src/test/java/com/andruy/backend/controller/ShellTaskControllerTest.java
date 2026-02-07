package com.andruy.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.andruy.backend.model.Directory;
import com.andruy.backend.service.ShellTaskService;
import com.andruy.backend.util.DirectoryList;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ShellTaskController.class)
@Import(SecurityConfig.class)
class ShellTaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShellTaskService shellTaskService;

    @MockitoBean
    private DirectoryList directoryList;

    @Nested
    @DisplayName("GET /api/shell/directories")
    class GetDirectories {

        @Test
        @DisplayName("Returns list of directories")
        @WithMockUser
        void getDirectories_ReturnsDirectoryList() throws Exception {
            List<Directory> directories = List.of(
                    new Directory("Music"),
                    new Directory("Videos"),
                    new Directory("Documents")
            );
            when(directoryList.getDirectories()).thenReturn(directories);

            mockMvc.perform(get("/api/shell/directories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].name").value("Music"))
                    .andExpect(jsonPath("$[1].name").value("Videos"))
                    .andExpect(jsonPath("$[2].name").value("Documents"));
        }

        @Test
        @DisplayName("Returns empty list when no directories")
        @WithMockUser
        void getDirectories_WhenEmpty_ReturnsEmptyArray() throws Exception {
            when(directoryList.getDirectories()).thenReturn(List.of());

            mockMvc.perform(get("/api/shell/directories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Requires authentication")
        void getDirectories_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(get("/api/shell/directories"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("POST /api/shell/youtube")
    class ProcessYoutubeLinks {

        @Test
        @DisplayName("Starts task and returns accepted status")
        @WithMockUser
        void processYoutubeLinks_ReturnsAccepted() throws Exception {
            Map<String, List<String>> body = new HashMap<>();
            body.put("Music", List.of("https://youtube.com/watch?v=123"));

            mockMvc.perform(post("/api/shell/youtube")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Task started. You will be notified when complete."));
        }

        @Test
        @DisplayName("Calls service with request body")
        @WithMockUser
        void processYoutubeLinks_CallsService() throws Exception {
            Map<String, List<String>> body = new HashMap<>();
            body.put("Music", List.of("https://youtube.com/watch?v=123"));

            mockMvc.perform(post("/api/shell/youtube")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isAccepted());

            verify(shellTaskService).ytTask(any());
        }

        @Test
        @DisplayName("Requires authentication")
        void processYoutubeLinks_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(post("/api/shell/youtube")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("POST /api/shell/youtube/auto")
    class ProcessYoutubeLinksAuto {

        @Test
        @DisplayName("Auto-assigns directories and starts task")
        @WithMockUser
        void processYoutubeLinksAuto_ReturnsAccepted() throws Exception {
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", List.of("https://youtube.com/watch?v=123"));

            Map<Directory, List<String>> assigned = new HashMap<>();
            assigned.put(new Directory("Music"), List.of("https://youtube.com/watch?v=123"));
            when(shellTaskService.assignDirectories(any())).thenReturn(assigned);

            mockMvc.perform(post("/api/shell/youtube/auto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Task started. You will be notified when complete."));
        }

        @Test
        @DisplayName("Calls assignDirectories then ytTask")
        @WithMockUser
        void processYoutubeLinksAuto_CallsServicesInOrder() throws Exception {
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", List.of("https://youtube.com/watch?v=123"));

            Map<Directory, List<String>> assigned = new HashMap<>();
            when(shellTaskService.assignDirectories(any())).thenReturn(assigned);

            mockMvc.perform(post("/api/shell/youtube/auto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isAccepted());

            verify(shellTaskService).assignDirectories(any());
            verify(shellTaskService).ytTask(assigned);
        }

        @Test
        @DisplayName("Requires authentication")
        void processYoutubeLinksAuto_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
            mockMvc.perform(post("/api/shell/youtube/auto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
