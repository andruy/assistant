package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.service.LogService;

@RestController
public class LogController {
    @Autowired
    private LogService logService;

    @GetMapping("/logReader")
    public ResponseEntity<Map<String, String>> logReader() {
        return ResponseEntity.ok(logService.logReader());
    }
}
