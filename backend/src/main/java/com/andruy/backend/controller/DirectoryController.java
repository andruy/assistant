package com.andruy.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.Directory;
import com.andruy.backend.service.DirectoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class DirectoryController {
    @Autowired
    private DirectoryService directoryService;

    @PostMapping("newDirectory")
    public ResponseEntity<Map<String, String>> createFolder(@RequestParam Directory name) {
        return ResponseEntity.ok().body(Map.of("report", directoryService.createFolder(name)));
    }
}
