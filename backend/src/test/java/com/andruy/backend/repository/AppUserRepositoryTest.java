package com.andruy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.andruy.backend.enity.AppUser;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setEnabled(true);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername_WhenUserExists_ReturnsUser() {
        Optional<AppUser> found = appUserRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void findByUsername_WhenUserNotExists_ReturnsEmpty() {
        Optional<AppUser> found = appUserRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when username exists")
    void existsByUsername_WhenUserExists_ReturnsTrue() {
        boolean exists = appUserRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void existsByUsername_WhenUserNotExists_ReturnsFalse() {
        boolean exists = appUserRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save and retrieve user with all fields")
    void save_WithValidUser_PersistsCorrectly() {
        AppUser newUser = new AppUser();
        newUser.setUsername("newuser");
        newUser.setPasswordHash("$2a$10$newhashedpassword");
        newUser.setEnabled(false);

        AppUser saved = appUserRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        Optional<AppUser> retrieved = appUserRepository.findById(saved.getId());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUsername()).isEqualTo("newuser");
        assertThat(retrieved.get().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Username search is case-sensitive")
    void findByUsername_IsCaseSensitive() {
        Optional<AppUser> found = appUserRepository.findByUsername("TESTUSER");

        assertThat(found).isEmpty();
    }
}
