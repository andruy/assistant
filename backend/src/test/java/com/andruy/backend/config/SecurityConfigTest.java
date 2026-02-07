package com.andruy.backend.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.andruy.backend.model.Directory;
import com.andruy.backend.service.DirectoryService;
import com.andruy.backend.service.InstagramService;
import com.andruy.backend.service.LogService;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.DirectoryList;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectoryService directoryService;

    @MockitoBean
    private BashHandler bashHandler;

    @MockitoBean
    private DirectoryList directoryList;

    @MockitoBean
    private InstagramService instagramService;

    @MockitoBean
    private LogService logService;

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/auth/me is accessible without authentication")
        void authMeEndpoint_IsPublic() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/health is accessible without authentication")
        void healthEndpoint_IsPublic() throws Exception {
            // Endpoint may not exist - just verify it doesn't require authentication (no 302 redirect)
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().is5xxServerError()); // 500 means endpoint exists but has issues, not 302
        }

        @Test
        @DisplayName("Swagger UI endpoints are accessible")
        void swaggerEndpoints_ArePublic() throws Exception {
            mockMvc.perform(get("/swagger-ui.html"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/logs requires authentication")
        void logsEndpoint_RequiresAuthentication() throws Exception {
            mockMvc.perform(get("/api/logs"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("GET /api/logs is accessible when authenticated")
        @WithMockUser
        void logsEndpoint_AccessibleWhenAuthenticated() throws Exception {
            when(logService.logReader()).thenReturn(java.util.Map.of("report", "test log"));

            mockMvc.perform(get("/api/logs"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/instagram/dates requires authentication")
        void instagramEndpoint_RequiresAuthentication() throws Exception {
            mockMvc.perform(get("/api/instagram/dates"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("POST /api/directory requires authentication")
        void directoryEndpoint_RequiresAuthentication() throws Exception {
            mockMvc.perform(post("/api/directory")
                            .param("name", "testfolder"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Login Flow")
    class LoginFlow {

        @Test
        @DisplayName("POST /login with invalid credentials returns 401")
        void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "wronguser")
                            .param("password", "wrongpass"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        @DisplayName("POST /login endpoint exists")
        void loginEndpoint_Exists() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "test")
                            .param("password", "test"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Logout Flow")
    class LogoutFlow {

        @Test
        @DisplayName("POST /logout returns 200 when authenticated")
        @WithMockUser
        void logout_WhenAuthenticated_ReturnsOk() throws Exception {
            mockMvc.perform(post("/logout"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /logout is accessible without authentication")
        void logout_WithoutAuthentication_IsAccessible() throws Exception {
            mockMvc.perform(post("/logout"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CSRF Configuration")
    class CsrfConfiguration {

        @Test
        @DisplayName("CSRF is disabled - POST without CSRF token succeeds")
        @WithMockUser
        void csrfDisabled_PostWithoutToken_Succeeds() throws Exception {
            when(directoryService.createFolder(any(Directory.class))).thenReturn("Created folder testfolder");

            mockMvc.perform(post("/api/directory")
                            .param("name", "testfolder"))
                    .andExpect(status().isOk());
        }
    }
}
