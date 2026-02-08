package com.andruy.backend.repository;

import com.andruy.backend.enity.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    Optional<PasskeyCredential> findByCredentialId(String credentialId);

    boolean existsByCredentialId(String credentialId);

    List<PasskeyCredential> findByUserId(Long userId);

    Optional<PasskeyCredential> findByIdAndUserId(Long id, Long userId);
}
