package com.andruy.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.Directory;
import com.andruy.backend.service.ShellTaskService;
import com.andruy.backend.util.DirectoryList;

@RestController
@RequestMapping("/api/shell")
public class ShellTaskController {
    private final ShellTaskService shellTaskService;
    private final DirectoryList directoryList;

    public ShellTaskController(ShellTaskService shellTaskService, DirectoryList directoryList) {
        this.shellTaskService = shellTaskService;
        this.directoryList = directoryList;
    }

    @GetMapping("/directories")
    public ResponseEntity<List<Directory>> getDirectories() {
        return ResponseEntity.ok(directoryList.getDirectories());
    }

    @PostMapping("/youtube")
    public ResponseEntity<Map<String, String>> processYoutubeLinks(@RequestBody Map<Directory, List<String>> body) {
        shellTaskService.ytTask(body);

        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }

    @PostMapping("/youtube/auto")
    public ResponseEntity<Map<String, String>> processYoutubeLinksAuto(@RequestBody Map<String, List<String>> body) {
        Map<Directory, List<String>> map = shellTaskService.assignDirectories(body);
        shellTaskService.ytTask(map);

        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }
}
