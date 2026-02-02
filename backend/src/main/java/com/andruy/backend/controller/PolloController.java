package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.service.PolloService;

@RestController
@RequestMapping("/api/pollo")
public class PolloController {
    private final PolloService polloService;

    public PolloController(PolloService polloService) {
        this.polloService = polloService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> pollo(@RequestBody Map<String, String> payload) {
        String response = polloService.pollo(payload);
        return ResponseEntity.ok(Map.of("report", response));
    }
}
