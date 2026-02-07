package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.andruy.backend.model.Directory;
import com.andruy.backend.util.BashHandler;

@ExtendWith(MockitoExtension.class)
class DirectoryServiceTest {
    @Mock
    private BashHandler bashHandler;

    @InjectMocks
    private DirectoryService directoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(directoryService, "base", "/home/user/programming/");
    }

    @Test
    @DisplayName("Should create folder successfully when bash command succeeds")
    void createFolder_WhenSuccess_ReturnsSuccessMessage() {
        Directory directory = new Directory("newproject");
        when(bashHandler.startAndReturnOutput(any(String[].class))).thenReturn(Collections.emptyList());

        String result = directoryService.createFolder(directory);

        assertThat(result).isEqualTo("Created folder newproject");
        verify(bashHandler).startAndReturnOutput(new String[]{"mkdir", "/home/user/programming/newproject"});
    }

    @Test
    @DisplayName("Should return error message when folder creation fails")
    void createFolder_WhenFails_ReturnsErrorMessage() {
        Directory directory = new Directory("existingfolder");
        String errorMessage = "mkdir: cannot create directory '/home/user/programming/existingfolder': File exists";
        when(bashHandler.startAndReturnOutput(any(String[].class))).thenReturn(List.of(errorMessage));

        String result = directoryService.createFolder(directory);

        assertThat(result).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("Should use correct base path from configuration")
    void createFolder_UsesConfiguredBasePath() {
        ReflectionTestUtils.setField(directoryService, "base", "/custom/path/");
        Directory directory = new Directory("testdir");
        when(bashHandler.startAndReturnOutput(any(String[].class))).thenReturn(Collections.emptyList());

        directoryService.createFolder(directory);

        verify(bashHandler).startAndReturnOutput(new String[]{"mkdir", "/custom/path/testdir"});
    }

    @Test
    @DisplayName("Should handle directory names with special characters")
    void createFolder_WithSpecialCharacters_PassesToBashHandler() {
        Directory directory = new Directory("my-project_v2");
        when(bashHandler.startAndReturnOutput(any(String[].class))).thenReturn(Collections.emptyList());

        String result = directoryService.createFolder(directory);

        assertThat(result).isEqualTo("Created folder my-project_v2");
        verify(bashHandler).startAndReturnOutput(new String[]{"mkdir", "/home/user/programming/my-project_v2"});
    }

    @Test
    @DisplayName("Should return first error when multiple error lines returned")
    void createFolder_WhenMultipleErrors_ReturnsFirstError() {
        Directory directory = new Directory("baddir");
        List<String> errors = List.of("Error line 1", "Error line 2");
        when(bashHandler.startAndReturnOutput(any(String[].class))).thenReturn(errors);

        String result = directoryService.createFolder(directory);

        assertThat(result).isEqualTo("Error line 1");
    }
}
