package com.andruy.backend.controller;

import com.andruy.backend.enity.AppUser;
import com.andruy.backend.enity.PasskeyCredential;
import com.andruy.backend.repository.AppUserRepository;
import com.andruy.backend.service.PasskeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passkey")
public class PasskeyController {
    private final PasskeyService passkeyService;
    private final AppUserRepository userRepo;

    private static final String SESSION_CHALLENGE_KEY = "webauthn_challenge";

    public PasskeyController(PasskeyService passkeyService, AppUserRepository userRepo) {
        this.passkeyService = passkeyService;
        this.userRepo = userRepo;
    }

    // ---- Registration (requires authentication) ----

    @PostMapping("/register/options")
    public ResponseEntity<Map<String, Object>> registrationOptions(Authentication auth, HttpSession session) {
        AppUser user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PasskeyService.RegistrationOptions regOptions = passkeyService.generateRegistrationOptions(user);

        session.setAttribute(SESSION_CHALLENGE_KEY, regOptions.challenge());

        return ResponseEntity.ok(regOptions.options());
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/register/verify")
    public ResponseEntity<Map<String, Object>> registrationVerify(
            @RequestBody Map<String, Object> body,
            Authentication auth, HttpSession session) {

        byte[] challenge = (byte[]) session.getAttribute(SESSION_CHALLENGE_KEY);
        if (challenge == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No challenge found. Please try again."));
        }
        session.removeAttribute(SESSION_CHALLENGE_KEY);

        AppUser user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String, Object> credential = (Map<String, Object>) body.get("credential");
        String label = (String) body.get("label");

        PasskeyCredential saved = passkeyService.verifyAndSaveRegistration(user, credential, challenge, label);

        return ResponseEntity.ok(Map.of(
                "message", "Passkey registered successfully",
                "id", saved.getId()
        ));
    }

    // ---- Authentication (public, no auth required) ----

    @PostMapping("/authenticate/options")
    public ResponseEntity<Map<String, Object>> authenticationOptions(HttpSession session) {
        PasskeyService.AuthenticationOptions authOptions = passkeyService.generateAuthenticationOptions();

        session.setAttribute(SESSION_CHALLENGE_KEY, authOptions.challenge());

        return ResponseEntity.ok(authOptions.options());
    }

    @PostMapping("/authenticate/verify")
    public ResponseEntity<Map<String, Object>> authenticationVerify(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        byte[] challenge = session != null
                ? (byte[]) session.getAttribute(SESSION_CHALLENGE_KEY)
                : null;

        if (session == null || challenge == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No challenge found. Please try again."));
        }
        session.removeAttribute(SESSION_CHALLENGE_KEY);

        @SuppressWarnings("unchecked")
        Map<String, Object> credential = (Map<String, Object>) body.get("credential");
        AppUser user = passkeyService.verifyAuthentication(credential, challenge);

        // Create Spring Security session
        session.invalidate();
        HttpSession newSession = request.getSession(true);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);

        newSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        return ResponseEntity.ok(Map.of("username", user.getUsername()));
    }

    // ---- Management (requires authentication) ----

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listPasskeys(Authentication auth) {
        AppUser user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Map<String, Object>> passkeys = passkeyService.getPasskeysForUser(user.getId())
                .stream()
                .map(p -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", p.getId());
                    map.put("label", p.getLabel());
                    map.put("createdAt", p.getCreatedAt().toString());
                    map.put("lastUsedAt", p.getLastUsedAt() != null ? p.getLastUsedAt().toString() : null);
                    return map;
                })
                .toList();

        return ResponseEntity.ok(passkeys);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePasskey(@PathVariable Long id, Authentication auth) {
        AppUser user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        passkeyService.deletePasskey(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}
