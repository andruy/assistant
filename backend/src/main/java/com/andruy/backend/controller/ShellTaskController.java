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
    @Autowired
    private DirectoryList directoryList;

    @GetMapping("/ytd")
    public ResponseEntity<List<Directory>> printDirectories() {
        return ResponseEntity.ok().body(directoryList.getDirectories());
    }

    @PostMapping("/yt")
    public ResponseEntity<Map<String, String>> gatherLinks(@RequestBody Map<Directory, List<String>> body) {
        shellTaskService.ytTask(body);

        return ResponseEntity.ok(Map.of("report", "You will be notified when the task is done"));
    }

    @PostMapping("/yte")
    public ResponseEntity<Map<String, String>> assignDirectories(@RequestBody Map<String, List<String>> body) {
        Map<Directory, List<String>> map = shellTaskService.assignDirectories(body);
        shellTaskService.ytTask(map);

        return ResponseEntity.ok(Map.of("report", "You will be notified when the task is done"));
    }
}
