package com.andruy.backend.service;

import com.andruy.backend.config.WebAuthnProperties;
import com.andruy.backend.enity.AppUser;
import com.andruy.backend.enity.PasskeyCredential;
import com.andruy.backend.repository.PasskeyCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PasskeyService {
    private final PasskeyCredentialRepository credentialRepo;
    private final WebAuthnManager webAuthnManager;
    private final WebAuthnProperties props;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasskeyService(PasskeyCredentialRepository credentialRepo,
                          WebAuthnManager webAuthnManager,
                          WebAuthnProperties props,
                          ObjectMapper objectMapper) {
        this.credentialRepo = credentialRepo;
        this.webAuthnManager = webAuthnManager;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    // ---- Registration ----

    public RegistrationOptions generateRegistrationOptions(AppUser user) {
        byte[] challenge = new byte[32];
        secureRandom.nextBytes(challenge);

        byte[] userHandle = ByteBuffer.allocate(8).putLong(user.getId()).array();

        List<PasskeyCredential> existing = credentialRepo.findByUserId(user.getId());
        List<Map<String, Object>> excludeCredentials = existing.stream()
                .map(c -> {
                    Map<String, Object> cred = new LinkedHashMap<>();
                    cred.put("id", c.getCredentialId());
                    cred.put("type", "public-key");
                    if (c.getTransports() != null && !c.getTransports().isEmpty()) {
                        cred.put("transports", Arrays.asList(c.getTransports().split(",")));
                    }
                    return cred;
                }).toList();

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64UrlUtil.encodeToString(challenge));
        options.put("rp", Map.of("id", props.getRpId(), "name", props.getRpName()));
        options.put("user", Map.of(
                "id", Base64UrlUtil.encodeToString(userHandle),
                "name", user.getUsername(),
                "displayName", user.getUsername()
        ));
        options.put("pubKeyCredParams", List.of(
                Map.of("type", "public-key", "alg", -7),
                Map.of("type", "public-key", "alg", -257)
        ));
        options.put("timeout", 300000);
        options.put("excludeCredentials", excludeCredentials);
        options.put("authenticatorSelection", Map.of(
                "residentKey", "required",
                "userVerification", "required"
        ));
        options.put("attestation", "none");

        return new RegistrationOptions(options, challenge);
    }

    public PasskeyCredential verifyAndSaveRegistration(AppUser user, Map<String, Object> credential,
                                                        byte[] challenge, String label) {
        String registrationJson;
        try {
            registrationJson = objectMapper.writeValueAsString(credential);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid credential data", e);
        }

        RegistrationData registrationData = webAuthnManager.parseRegistrationResponseJSON(registrationJson);

        ServerProperty serverProperty = ServerProperty.builder()
                .origins(java.util.Set.of(new Origin(props.getOrigin())))
                .rpId(props.getRpId())
                .challenge(new DefaultChallenge(challenge))
                .build();

        @SuppressWarnings("deprecation")
        RegistrationParameters registrationParameters = new RegistrationParameters(
                serverProperty, true, false
        );

        webAuthnManager.verify(registrationData, registrationParameters);

        AttestedCredentialData attestedCredData = registrationData.getAttestationObject()
                .getAuthenticatorData().getAttestedCredentialData();
        byte[] credIdBytes = attestedCredData.getCredentialId();
        long signCount = registrationData.getAttestationObject()
                .getAuthenticatorData().getSignCount();

        String credentialId = Base64UrlUtil.encodeToString(credIdBytes);
        if (credentialRepo.existsByCredentialId(credentialId)) {
            throw new IllegalArgumentException("This passkey is already registered");
        }

        List<String> transports = registrationData.getTransports() != null
                ? registrationData.getTransports().stream()
                    .map(t -> t.getValue())
                    .toList()
                : List.of();

        PasskeyCredential cred = new PasskeyCredential();
        cred.setUser(user);
        cred.setCredentialId(credentialId);
        cred.setRegistrationJson(registrationJson);
        cred.setSignCount(signCount);
        cred.setLabel(label != null && !label.isBlank() ? label : "Passkey");
        cred.setTransports(String.join(",", transports));

        return credentialRepo.save(cred);
    }

    // ---- Authentication ----

    public AuthenticationOptions generateAuthenticationOptions() {
        byte[] challenge = new byte[32];
        secureRandom.nextBytes(challenge);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64UrlUtil.encodeToString(challenge));
        options.put("rpId", props.getRpId());
        options.put("timeout", 300000);
        options.put("userVerification", "required");

        return new AuthenticationOptions(options, challenge);
    }

    public AppUser verifyAuthentication(Map<String, Object> credential, byte[] challenge) {
        String authenticationJson;
        try {
            authenticationJson = objectMapper.writeValueAsString(credential);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid credential data", e);
        }

        AuthenticationData authenticationData = webAuthnManager.parseAuthenticationResponseJSON(authenticationJson);

        byte[] credIdBytes = authenticationData.getCredentialId();
        String credentialId = Base64UrlUtil.encodeToString(credIdBytes);

        PasskeyCredential stored = credentialRepo.findByCredentialId(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown passkey"));

        AppUser user = stored.getUser();
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("User account is disabled");
        }

        // Reconstruct CredentialRecord from stored registration JSON
        RegistrationData regData = webAuthnManager.parseRegistrationResponseJSON(stored.getRegistrationJson());
        CredentialRecordImpl credentialRecord = new CredentialRecordImpl(
                regData.getAttestationObject(),
                regData.getCollectedClientData(),
                regData.getClientExtensions(),
                regData.getTransports()
        );
        credentialRecord.setCounter(stored.getSignCount());

        ServerProperty serverProperty = ServerProperty.builder()
                .origins(java.util.Set.of(new Origin(props.getOrigin())))
                .rpId(props.getRpId())
                .challenge(new DefaultChallenge(challenge))
                .build();

        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
                serverProperty, credentialRecord, null, true, false
        );

        webAuthnManager.verify(authenticationData, authenticationParameters);

        // Update sign count and last used
        stored.setSignCount(authenticationData.getAuthenticatorData().getSignCount());
        stored.setLastUsedAt(Instant.now());
        credentialRepo.save(stored);

        return user;
    }

    // ---- Management ----

    public List<PasskeyCredential> getPasskeysForUser(Long userId) {
        return credentialRepo.findByUserId(userId);
    }

    public void deletePasskey(Long passkeyId, Long userId) {
        PasskeyCredential cred = credentialRepo.findByIdAndUserId(passkeyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Passkey not found"));
        credentialRepo.delete(cred);
    }

    // ---- DTOs ----

    public record RegistrationOptions(Map<String, Object> options, byte[] challenge) {}
    public record AuthenticationOptions(Map<String, Object> options, byte[] challenge) {}
}
