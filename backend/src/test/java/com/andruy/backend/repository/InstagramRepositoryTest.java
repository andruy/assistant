package com.andruy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
class InstagramRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private InstagramRepository instagramRepository;

    private Date testDate;
    private Date testDate2;

    @BeforeEach
    void setUp() {
        instagramRepository = new InstagramRepository();
        // Use reflection to inject JdbcTemplate since @Autowired won't work in this context
        org.springframework.test.util.ReflectionTestUtils.setField(instagramRepository, "jdbcTemplate", jdbcTemplate);

        testDate = Date.valueOf("2024-01-15");
        testDate2 = Date.valueOf("2024-01-20");

        // Create test tables
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS public");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.ig_followers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                account_name VARCHAR(255),
                created_on DATE
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.ig_following (
                id INT AUTO_INCREMENT PRIMARY KEY,
                account_name VARCHAR(255),
                created_on DATE
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS public.ig_nmf (
                id INT AUTO_INCREMENT PRIMARY KEY,
                account_name VARCHAR(255),
                created_on DATE,
                protected INT DEFAULT 0
            )
        """);

        // Clear tables
        jdbcTemplate.execute("DELETE FROM public.ig_followers");
        jdbcTemplate.execute("DELETE FROM public.ig_following");
        jdbcTemplate.execute("DELETE FROM public.ig_nmf");
    }

    @Nested
    @DisplayName("saveUser")
    class SaveUser {

        @Test
        @DisplayName("Should save user to followers table")
        void saveUser_ToFollowers_ReturnsOne() {
            int result = instagramRepository.saveUser("followers", "testuser", testDate);

            assertThat(result).isEqualTo(1);

            List<String> users = jdbcTemplate.queryForList(
                    "SELECT account_name FROM public.ig_followers WHERE created_on = ?",
                    String.class, testDate);
            assertThat(users).containsExactly("testuser");
        }

        @Test
        @DisplayName("Should save user to following table")
        void saveUser_ToFollowing_ReturnsOne() {
            int result = instagramRepository.saveUser("following", "followinguser", testDate);

            assertThat(result).isEqualTo(1);

            List<String> users = jdbcTemplate.queryForList(
                    "SELECT account_name FROM public.ig_following WHERE created_on = ?",
                    String.class, testDate);
            assertThat(users).containsExactly("followinguser");
        }

        @Test
        @DisplayName("Should save user to nmf table")
        void saveUser_ToNmf_ReturnsOne() {
            int result = instagramRepository.saveUser("nmf", "nmfuser", testDate);

            assertThat(result).isEqualTo(1);

            List<String> users = jdbcTemplate.queryForList(
                    "SELECT account_name FROM public.ig_nmf WHERE created_on = ?",
                    String.class, testDate);
            assertThat(users).containsExactly("nmfuser");
        }

        @Test
        @DisplayName("Should save multiple users with same date")
        void saveUser_MultipleUsersWithSameDate_SavesAll() {
            instagramRepository.saveUser("followers", "user1", testDate);
            instagramRepository.saveUser("followers", "user2", testDate);
            instagramRepository.saveUser("followers", "user3", testDate);

            List<String> users = jdbcTemplate.queryForList(
                    "SELECT account_name FROM public.ig_followers WHERE created_on = ?",
                    String.class, testDate);
            assertThat(users).hasSize(3);
            assertThat(users).containsExactlyInAnyOrder("user1", "user2", "user3");
        }
    }

    @Nested
    @DisplayName("getDates")
    class GetDates {

        @Test
        @DisplayName("Should return distinct dates from nmf table")
        void getDates_ReturnsDistinctDates() {
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user1", testDate);
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user2", testDate);
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user3", testDate2);

            List<Date> dates = instagramRepository.getDates();

            assertThat(dates).hasSize(2);
            assertThat(dates).containsExactlyInAnyOrder(testDate, testDate2);
        }

        @Test
        @DisplayName("Should return empty list when no data")
        void getDates_WhenNoData_ReturnsEmptyList() {
            List<Date> dates = instagramRepository.getDates();

            assertThat(dates).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("Should return users for specific date and suffix")
        void getUsers_ReturnsUsersForDateAndSuffix() {
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user1", testDate);
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user2", testDate);
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on) VALUES (?, ?)",
                    "user3", testDate2);

            List<String> users = instagramRepository.getUsers("nmf", testDate);

            assertThat(users).hasSize(2);
            assertThat(users).containsExactlyInAnyOrder("user1", "user2");
        }

        @Test
        @DisplayName("Should return empty list when no users for date")
        void getUsers_WhenNoUsersForDate_ReturnsEmptyList() {
            List<String> users = instagramRepository.getUsers("nmf", testDate);

            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("Should work with different suffixes")
        void getUsers_WithDifferentSuffixes_ReturnsCorrectUsers() {
            jdbcTemplate.update("INSERT INTO public.ig_followers (account_name, created_on) VALUES (?, ?)",
                    "follower1", testDate);
            jdbcTemplate.update("INSERT INTO public.ig_following (account_name, created_on) VALUES (?, ?)",
                    "following1", testDate);

            List<String> followers = instagramRepository.getUsers("followers", testDate);
            List<String> following = instagramRepository.getUsers("following", testDate);

            assertThat(followers).containsExactly("follower1");
            assertThat(following).containsExactly("following1");
        }
    }

    @Nested
    @DisplayName("protectAccount")
    class ProtectAccount {

        @Test
        @DisplayName("Should update protected flag for user")
        void protectAccount_UpdatesProtectedFlag() {
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on, protected) VALUES (?, ?, ?)",
                    "user1", testDate, 0);

            int result = instagramRepository.protectAccount("user1", testDate);

            assertThat(result).isEqualTo(1);

            Integer protectedFlag = jdbcTemplate.queryForObject(
                    "SELECT protected FROM public.ig_nmf WHERE account_name = ? AND created_on = ?",
                    Integer.class, "user1", testDate);
            assertThat(protectedFlag).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return 0 when user not found")
        void protectAccount_WhenUserNotFound_ReturnsZero() {
            int result = instagramRepository.protectAccount("nonexistent", testDate);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should only update matching user and date")
        void protectAccount_OnlyUpdatesMatchingUserAndDate() {
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on, protected) VALUES (?, ?, ?)",
                    "user1", testDate, 0);
            jdbcTemplate.update("INSERT INTO public.ig_nmf (account_name, created_on, protected) VALUES (?, ?, ?)",
                    "user1", testDate2, 0);

            instagramRepository.protectAccount("user1", testDate);

            Integer protectedFlag1 = jdbcTemplate.queryForObject(
                    "SELECT protected FROM public.ig_nmf WHERE account_name = ? AND created_on = ?",
                    Integer.class, "user1", testDate);
            Integer protectedFlag2 = jdbcTemplate.queryForObject(
                    "SELECT protected FROM public.ig_nmf WHERE account_name = ? AND created_on = ?",
                    Integer.class, "user1", testDate2);

            assertThat(protectedFlag1).isEqualTo(1);
            assertThat(protectedFlag2).isEqualTo(0);
        }
    }
}
