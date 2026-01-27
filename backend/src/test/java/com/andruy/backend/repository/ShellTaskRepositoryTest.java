package com.andruy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.andruy.backend.model.DirectoryCorrection;

@JdbcTest
@ActiveProfiles("test")
class ShellTaskRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ShellTaskRepository shellTaskRepository;

    @BeforeEach
    void setUp() {
        shellTaskRepository = new ShellTaskRepository();
        org.springframework.test.util.ReflectionTestUtils.setField(shellTaskRepository, "jdbcTemplate", jdbcTemplate);

        // Create test tables
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS public");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.directories (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255),
                alias VARCHAR(255)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.email_actions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                subject VARCHAR(255)
            )
        """);

        // Clear tables
        jdbcTemplate.execute("DELETE FROM public.directories");
        jdbcTemplate.execute("DELETE FROM public.email_actions");
    }

    @Nested
    @DisplayName("getDirectories")
    class GetDirectories {

        @Test
        @DisplayName("Should return all directory corrections")
        void getDirectories_ReturnsAllCorrections() {
            jdbcTemplate.update("INSERT INTO public.directories (name, alias) VALUES (?, ?)",
                    "YouTube Music", "Music");
            jdbcTemplate.update("INSERT INTO public.directories (name, alias) VALUES (?, ?)",
                    "Tech Tutorials", "Learning");

            List<DirectoryCorrection> result = shellTaskRepository.getDirectories();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(DirectoryCorrection::name)
                    .containsExactlyInAnyOrder("YouTube Music", "Tech Tutorials");
            assertThat(result).extracting(DirectoryCorrection::alias)
                    .containsExactlyInAnyOrder("Music", "Learning");
        }

        @Test
        @DisplayName("Should return empty list when no directories")
        void getDirectories_WhenNoDirectories_ReturnsEmptyList() {
            List<DirectoryCorrection> result = shellTaskRepository.getDirectories();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should map name and alias correctly")
        void getDirectories_MapsFieldsCorrectly() {
            jdbcTemplate.update("INSERT INTO public.directories (name, alias) VALUES (?, ?)",
                    "Original Name", "Corrected Alias");

            List<DirectoryCorrection> result = shellTaskRepository.getDirectories();

            assertThat(result).hasSize(1);
            DirectoryCorrection correction = result.get(0);
            assertThat(correction.name()).isEqualTo("Original Name");
            assertThat(correction.alias()).isEqualTo("Corrected Alias");
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void getDirectories_HandlesSpecialCharacters() {
            jdbcTemplate.update("INSERT INTO public.directories (name, alias) VALUES (?, ?)",
                    "Name with 'quotes'", "Alias-with_special.chars");

            List<DirectoryCorrection> result = shellTaskRepository.getDirectories();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Name with 'quotes'");
            assertThat(result.get(0).alias()).isEqualTo("Alias-with_special.chars");
        }
    }

    @Nested
    @DisplayName("getEmailActions")
    class GetEmailActions {

        @Test
        @DisplayName("Should return all email action subjects")
        void getEmailActions_ReturnsAllSubjects() {
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Daily Report");
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Weekly Summary");
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Alert Notification");

            List<String> result = shellTaskRepository.getEmailActions();

            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder("Daily Report", "Weekly Summary", "Alert Notification");
        }

        @Test
        @DisplayName("Should return empty list when no email actions")
        void getEmailActions_WhenNoActions_ReturnsEmptyList() {
            List<String> result = shellTaskRepository.getEmailActions();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return subjects in insertion order")
        void getEmailActions_ReturnsInOrder() {
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "First");
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Second");
            jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Third");

            List<String> result = shellTaskRepository.getEmailActions();

            assertThat(result).containsExactly("First", "Second", "Third");
        }
    }
}
