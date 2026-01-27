package com.andruy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
class EmailTaskRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private EmailTaskRepository emailTaskRepository;

    @BeforeEach
    void setUp() {
        emailTaskRepository = new EmailTaskRepository();
        org.springframework.test.util.ReflectionTestUtils.setField(emailTaskRepository, "jdbcTemplate", jdbcTemplate);

        // Create test table
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS public");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.email_actions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                subject VARCHAR(255)
            )
        """);

        // Clear table
        jdbcTemplate.execute("DELETE FROM public.email_actions");
    }

    @Test
    @DisplayName("Should return all email action subjects")
    void getEmailActions_ReturnsAllSubjects() {
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Morning Reminder");
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "End of Day Report");

        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("Morning Reminder", "End of Day Report");
    }

    @Test
    @DisplayName("Should return empty list when no email actions exist")
    void getEmailActions_WhenNoActions_ReturnsEmptyList() {
        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle single email action")
    void getEmailActions_WithSingleAction_ReturnsSingleElementList() {
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Single Action");

        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("Single Action");
    }

    @Test
    @DisplayName("Should handle subjects with special characters")
    void getEmailActions_WithSpecialCharacters_ReturnsCorrectly() {
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)",
                "Subject with 'quotes' and \"double quotes\"");
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)",
                "Subject with @#$% symbols");

        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).hasSize(2);
        assertThat(result).contains("Subject with 'quotes' and \"double quotes\"");
        assertThat(result).contains("Subject with @#$% symbols");
    }

    @Test
    @DisplayName("Should handle unicode subjects")
    void getEmailActions_WithUnicode_ReturnsCorrectly() {
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)",
                "Subject with émojis 🎉");

        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).contains("émojis");
    }

    @Test
    @DisplayName("Should return subjects in order")
    void getEmailActions_ReturnsInInsertionOrder() {
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Alpha");
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Beta");
        jdbcTemplate.update("INSERT INTO public.email_actions (subject) VALUES (?)", "Gamma");

        List<String> result = emailTaskRepository.getEmailActions();

        assertThat(result).containsExactly("Alpha", "Beta", "Gamma");
    }
}
