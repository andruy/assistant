package com.andruy.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.Directory;
import com.andruy.backend.service.ShellTaskService;
import com.andruy.backend.util.DirectoryList;

@RestController
public class ShellTaskController {
    @Autowired
    private ShellTaskService shellTaskService;

    @GetMapping("/ytd")
    public ResponseEntity<List<Directory>> printDirectories() {
        return ResponseEntity.ok().body(new DirectoryList().getDirectories());
    }

    @PostMapping("/yt")
    public ResponseEntity<Map<String, String>> gatherLinks(@RequestBody Map<Directory, List<String>> body) {
        shellTaskService.ytTask(body);

        return ResponseEntity.ok().body(Map.of("report", shellTaskService.getTaskResponse().get(1)));
    }

    @PostMapping("/yte")
    public ResponseEntity<Map<String, String>> assignDirectories(@RequestBody Map<String, List<String>> body) {
        shellTaskService.assignAndProcess(body);

        return ResponseEntity.ok().body(Map.of("report", shellTaskService.getTaskResponse().get(1)));
    }
}
