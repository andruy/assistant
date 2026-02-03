package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.Email;
import com.andruy.backend.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> emailAgent(@RequestBody Email email) {
        emailService.sendEmail(email);

        return ResponseEntity.ok().body(Map.of("report", emailService.getFeedback()));
    }
}
