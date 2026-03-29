package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
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

import com.andruy.backend.model.PushNotification;
import com.andruy.backend.repository.InstagramRepository;
import com.andruy.backend.util.TimeTracker;

@ExtendWith(MockitoExtension.class)
class InstagramServiceTest {
    @Mock
    private InstagramRepository instagramRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private TimeTracker timeTracker;

    @InjectMocks
    private InstagramService instagramService;

    private Timestamp testDate;

    @BeforeEach
    void setUp() {
        testDate = new Timestamp(System.currentTimeMillis());
    }

    @Nested
    @DisplayName("getListOfDates")
    class GetListOfDates {

        @Test
        @DisplayName("Should return dates in reverse chronological order")
        void getListOfDates_ReturnsSortedDates() {
            Timestamp date1 = Timestamp.valueOf("2024-01-01 00:00:00");
            Timestamp date2 = Timestamp.valueOf("2024-01-15 00:00:00");
            Timestamp date3 = Timestamp.valueOf("2024-01-10 00:00:00");
            when(instagramRepository.getTimestamps()).thenReturn(new java.util.ArrayList<>(List.of(date1, date2, date3)));

            List<Timestamp> result = instagramService.getListOfDates();

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).isEqualTo(date2); // Most recent first
            assertThat(result.get(1)).isEqualTo(date3);
            assertThat(result.get(2)).isEqualTo(date1);
        }

        @Test
        @DisplayName("Should return empty list when no dates exist")
        void getListOfDates_WhenNoDates_ReturnsEmptyList() {
            when(instagramRepository.getTimestamps()).thenReturn(Collections.emptyList());

            List<Timestamp> result = instagramService.getListOfDates();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single date")
        void getListOfDates_WithSingleDate_ReturnsSingleElementList() {
            Timestamp singleDate = Timestamp.valueOf("2024-01-15 00:00:00");
            when(instagramRepository.getTimestamps()).thenReturn(new java.util.ArrayList<>(List.of(singleDate)));

            List<Timestamp> result = instagramService.getListOfDates();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(singleDate);
        }
    }

    @Nested
    @DisplayName("getListOfAccounts")
    class GetListOfAccounts {

        @Test
        @DisplayName("Should return accounts with Instagram URLs")
        void getListOfAccounts_ReturnsAccountsWithUrls() {
            List<String> accounts = List.of("user1", "user2", "user3");
            when(instagramRepository.getUsers("nmf", testDate)).thenReturn(accounts);

            Map<String, String> result = instagramService.getListOfAccounts("nmf", testDate);

            assertThat(result).hasSize(3);
            assertThat(result.get("user1")).isEqualTo("https://www.instagram.com/user1/");
            assertThat(result.get("user2")).isEqualTo("https://www.instagram.com/user2/");
            assertThat(result.get("user3")).isEqualTo("https://www.instagram.com/user3/");
        }

        @Test
        @DisplayName("Should return sorted map by username")
        void getListOfAccounts_ReturnsSortedMap() {
            List<String> accounts = List.of("zebra", "alpha", "mike");
            when(instagramRepository.getUsers("nmf", testDate)).thenReturn(accounts);

            Map<String, String> result = instagramService.getListOfAccounts("nmf", testDate);

            List<String> keys = result.keySet().stream().toList();
            assertThat(keys).containsExactly("alpha", "mike", "zebra");
        }

        @Test
        @DisplayName("Should return empty map when no accounts")
        void getListOfAccounts_WhenNoAccounts_ReturnsEmptyMap() {
            when(instagramRepository.getUsers("nmf", testDate)).thenReturn(Collections.emptyList());

            Map<String, String> result = instagramService.getListOfAccounts("nmf", testDate);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should work with different suffix types")
        void getListOfAccounts_WithDifferentSuffix_QueriesCorrectTable() {
            when(instagramRepository.getUsers("followers", testDate)).thenReturn(List.of("user1"));

            instagramService.getListOfAccounts("followers", testDate);

            verify(instagramRepository).getUsers("followers", testDate);
        }
    }

    @Nested
    @DisplayName("getComparisonBetweenDates")
    class GetComparisonBetweenDates {

        @Test
        @DisplayName("Should compare followers and following from different dates")
        void getComparisonBetweenDates_ComparesCorrectly() {
            Timestamp followersDate = Timestamp.valueOf("2024-01-01 00:00:00");
            Timestamp followingDate = Timestamp.valueOf("2024-01-15 00:00:00");
            List<String> followers = List.of("user1", "user2", "user3");
            List<String> following = List.of("user1", "user4", "user5");

            when(instagramRepository.getUsers("followers", followersDate)).thenReturn(followers);
            when(instagramRepository.getUsers("following", followingDate)).thenReturn(following);
            when(instagramRepository.saveUser(anyString(), anyString(), any(Timestamp.class))).thenReturn(1);

            Map<String, String> result = instagramService.getComparisonBetweenDates(followersDate, followingDate);

            assertThat(result).containsKey("report");
            verify(instagramRepository).getUsers("followers", followersDate);
            verify(instagramRepository).getUsers("following", followingDate);
        }

        @Test
        @DisplayName("Should save non-followers to nmf table")
        void getComparisonBetweenDates_SavesNonFollowers() {
            Timestamp followersDate = Timestamp.valueOf("2024-01-01 00:00:00");
            Timestamp followingDate = Timestamp.valueOf("2024-01-15 00:00:00");
            List<String> followers = List.of("user1");
            List<String> following = List.of("user1", "user2", "user3");

            when(instagramRepository.getUsers("followers", followersDate)).thenReturn(followers);
            when(instagramRepository.getUsers("following", followingDate)).thenReturn(following);
            when(instagramRepository.saveUser(anyString(), anyString(), any(Timestamp.class))).thenReturn(1);

            instagramService.getComparisonBetweenDates(followersDate, followingDate);

            verify(instagramRepository, times(2)).saveUser(eq("nmf"), anyString(), any(Timestamp.class));
        }
    }

    @Nested
    @DisplayName("protectAccounts")
    class ProtectAccounts {

        @Test
        @DisplayName("Should protect specified accounts")
        void protectAccounts_ProtectsAllAccounts() {
            List<String> accountsToProtect = List.of("user1", "user2", "user3");
            when(instagramRepository.protectAccount(anyString(), eq(testDate))).thenReturn(1);
            when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

            Map<String, String> result = instagramService.protectAccounts(testDate, accountsToProtect);

            assertThat(result).containsKey("report");
            assertThat(result.get("report")).contains("Protected 3 accounts");
            verify(instagramRepository, times(3)).protectAccount(anyString(), eq(testDate));
        }

        @Test
        @DisplayName("Should send push notification after protecting")
        void protectAccounts_SendsPushNotification() {
            List<String> accountsToProtect = List.of("user1");
            when(instagramRepository.protectAccount(anyString(), eq(testDate))).thenReturn(1);
            when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

            instagramService.protectAccounts(testDate, accountsToProtect);

            verify(pushNotificationService).push(any(PushNotification.class));
        }

        @Test
        @DisplayName("Should handle empty account list")
        void protectAccounts_WithEmptyList_ProtectsZeroAccounts() {
            when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

            Map<String, String> result = instagramService.protectAccounts(testDate, Collections.emptyList());

            assertThat(result.get("report")).contains("Protected 0 accounts");
        }

        @Test
        @DisplayName("Should report correct count when some updates fail")
        void protectAccounts_WhenSomeFail_ReportsCorrectCount() {
            List<String> accountsToProtect = List.of("user1", "user2", "user3");
            when(instagramRepository.protectAccount("user1", testDate)).thenReturn(1);
            when(instagramRepository.protectAccount("user2", testDate)).thenReturn(0);
            when(instagramRepository.protectAccount("user3", testDate)).thenReturn(1);
            when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

            Map<String, String> result = instagramService.protectAccounts(testDate, accountsToProtect);

            assertThat(result.get("report")).contains("Protected 2 accounts");
        }
    }
}
