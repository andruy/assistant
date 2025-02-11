package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.andruy.backend.service.PolloService;

@Controller
public class PolloController {
    @Autowired
    private PolloService polloService;
    @PostMapping("/pollo")
    public ResponseEntity<Map<String, String>> pollo(@RequestBody Map<String, String> payload) {
        String response = polloService.pollo(payload);
        return ResponseEntity.ok(Map.of("report", response));
    }
}
