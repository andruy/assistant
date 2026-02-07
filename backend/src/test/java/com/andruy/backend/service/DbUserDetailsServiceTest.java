package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.andruy.backend.enity.AppUser;
import com.andruy.backend.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DbUserDetailsService dbUserDetailsService;

    private AppUser enabledUser;
    private AppUser disabledUser;

    @BeforeEach
    void setUp() {
        enabledUser = new AppUser(1L, "testuser", "hashedPassword123", true);
        disabledUser = new AppUser(2L, "disableduser", "hashedPassword456", false);
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(enabledUser));

        UserDetails userDetails = dbUserDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Should normalize username by trimming and lowercasing")
    void loadUserByUsername_WithUntrimmedUppercaseUsername_NormalizesUsername() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(enabledUser));

        UserDetails userDetails = dbUserDetailsService.loadUserByUsername("  TESTUSER  ");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_WhenUserNotFound_ThrowsException() {
        when(appUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dbUserDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should return disabled user with isEnabled false")
    void loadUserByUsername_WhenUserDisabled_ReturnsDisabledUserDetails() {
        when(appUserRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        UserDetails userDetails = dbUserDetailsService.loadUserByUsername("disableduser");

        assertThat(userDetails.getUsername()).isEqualTo("disableduser");
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should handle username with mixed case")
    void loadUserByUsername_WithMixedCaseUsername_NormalizesToLowercase() {
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(enabledUser));

        UserDetails userDetails = dbUserDetailsService.loadUserByUsername("TestUser");

        assertThat(userDetails).isNotNull();
    }
}
