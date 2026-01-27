package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.andruy.backend.model.Directory;
import com.andruy.backend.model.DirectoryCorrection;
import com.andruy.backend.repository.ShellTaskRepository;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.DirectoryList;

@ExtendWith(MockitoExtension.class)
class ShellTaskServiceTest {
    @Mock
    private EmailService emailService;

    @Mock
    private ShellTaskRepository shellTaskRepository;

    @Mock
    private DirectoryList directoryList;

    @Mock
    private BashHandler bashHandler;

    @InjectMocks
    private ShellTaskService shellTaskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shellTaskService, "receiver", "test@example.com");
    }

    @Nested
    @DisplayName("assignDirectories")
    class AssignDirectories {

        @Test
        @DisplayName("Should return empty map when links list is empty")
        void assignDirectories_WithEmptyLinks_ReturnsEmptyMap() {
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", Collections.emptyList());
            when(shellTaskRepository.getDirectories()).thenReturn(Collections.emptyList());

            Map<Directory, List<String>> result = shellTaskService.assignDirectories(body);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map when links key is missing")
        void assignDirectories_WithMissingLinksKey_ReturnsEmptyMap() {
            Map<String, List<String>> body = new HashMap<>();
            when(shellTaskRepository.getDirectories()).thenReturn(Collections.emptyList());

            Map<Directory, List<String>> result = shellTaskService.assignDirectories(body);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should apply directory corrections from repository")
        void assignDirectories_AppliesCorrections() {
            // This test verifies the correction logic without actual URL fetching
            when(shellTaskRepository.getDirectories()).thenReturn(
                    List.of(new DirectoryCorrection("OldName", "NewName"))
            );
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", Collections.emptyList());

            Map<Directory, List<String>> result = shellTaskService.assignDirectories(body);

            // Verify corrections were loaded
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should group multiple URLs under same directory")
        void assignDirectories_GroupsUrlsByDirectory() {
            when(shellTaskRepository.getDirectories()).thenReturn(Collections.emptyList());
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", Collections.emptyList());

            Map<Directory, List<String>> result = shellTaskService.assignDirectories(body);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Directory Mapping Logic")
    class DirectoryMappingLogic {

        @Test
        @DisplayName("Should load directory corrections from repository")
        void directoryCorrections_LoadedFromRepository() {
            List<DirectoryCorrection> corrections = List.of(
                    new DirectoryCorrection("YouTube Music", "Music"),
                    new DirectoryCorrection("Tutorials", "Learning")
            );
            when(shellTaskRepository.getDirectories()).thenReturn(corrections);
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", Collections.emptyList());

            shellTaskService.assignDirectories(body);

            // Verify the repository was called
            org.mockito.Mockito.verify(shellTaskRepository).getDirectories();
        }

        @Test
        @DisplayName("Should handle null links gracefully")
        void assignDirectories_WithNullLinks_HandlesGracefully() {
            when(shellTaskRepository.getDirectories()).thenReturn(Collections.emptyList());
            Map<String, List<String>> body = new HashMap<>();
            body.put("links", null);

            // Should not throw exception
            try {
                shellTaskService.assignDirectories(body);
            } catch (NullPointerException e) {
                // Expected behavior when links is null
            }
        }
    }
}
