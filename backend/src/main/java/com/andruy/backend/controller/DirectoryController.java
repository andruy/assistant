package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.Directory;
import com.andruy.backend.service.DirectoryService;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createFolder(@RequestParam Directory name) {
        String result = directoryService.createFolder(name);

        return ResponseEntity.ok(Map.of("report", result));
    }
}
