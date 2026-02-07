package com.andruy.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.andruy.backend.config.SecurityConfig;
import com.andruy.backend.model.Directory;
import com.andruy.backend.service.DirectoryService;

@WebMvcTest(DirectoryController.class)
@Import(SecurityConfig.class)
class DirectoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectoryService directoryService;

    @Test
    @DisplayName("POST /api/directory creates folder successfully")
    @WithMockUser
    void createFolder_WhenSuccess_ReturnsOkWithReport() throws Exception {
        when(directoryService.createFolder(any(Directory.class))).thenReturn("Created folder testfolder");

        mockMvc.perform(post("/api/directory")
                        .param("name", "testfolder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Created folder testfolder"));
    }

    @Test
    @DisplayName("POST /api/directory returns error when folder exists")
    @WithMockUser
    void createFolder_WhenFolderExists_ReturnsErrorMessage() throws Exception {
        when(directoryService.createFolder(any(Directory.class)))
                .thenReturn("mkdir: cannot create directory: File exists");

        mockMvc.perform(post("/api/directory")
                        .param("name", "existingfolder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("mkdir: cannot create directory: File exists"));
    }

    @Test
    @DisplayName("POST /api/directory requires authentication")
    void createFolder_WhenNotAuthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("name", "testfolder"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /api/directory handles special characters in name")
    @WithMockUser
    void createFolder_WithSpecialCharacters_PassesToService() throws Exception {
        when(directoryService.createFolder(any(Directory.class)))
                .thenReturn("Created folder my-project_v2");

        mockMvc.perform(post("/api/directory")
                        .param("name", "my-project_v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Created folder my-project_v2"));
    }

    @Test
    @DisplayName("POST /api/directory without name parameter returns error")
    @WithMockUser
    void createFolder_WithoutName_ReturnsError() throws Exception {
        // Missing required param results in server error due to conversion failure
        mockMvc.perform(post("/api/directory"))
                .andExpect(status().is5xxServerError());
    }
}
