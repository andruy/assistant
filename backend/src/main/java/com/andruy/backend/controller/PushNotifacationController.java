package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.PushNotification;
import com.andruy.backend.service.PushNotificationService;

@RestController
@RequestMapping("/api/push")
public class PushNotifacationController {
    private final PushNotificationService pushNotificationService;

    public PushNotifacationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> push(@RequestBody PushNotification msg) {
        return ResponseEntity.ok().body(Map.of("report", pushNotificationService.push(msg)));
    }
}
