package com.andruy.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.service.EmailTaskService;

@RestController
@RequestMapping("/api/email")
public class EmailTaskController {
    private final EmailTaskService emailTaskService;

    public EmailTaskController(EmailTaskService emailTaskService) {
        this.emailTaskService = emailTaskService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<String>> getTasks() {
        return ResponseEntity.ok().body(emailTaskService.getTaskTemplate());
    }

    @DeleteMapping("/task")
    public ResponseEntity<Map<String, String>> deleteTask(@RequestBody TaskId body) {
        return ResponseEntity.ok().body(Map.of(
            "report", emailTaskService.cancelTask(body) ? "Thread " + body.id() + " killed" : "Thread " + body.id() + " not found"
        ));
    }

    @GetMapping("/running")
    public ResponseEntity<Set<TaskId>> getEmailTasks() {
        return ResponseEntity.ok().body(Set.copyOf(emailTaskService.getThreads()));
    }

    @PostMapping("/task")
    public ResponseEntity<Map<String, String>> sendTask(@RequestBody EmailTask body) {
        emailTaskService.scheduleTask(body);

        return ResponseEntity.ok().body(Map.of("report", "Task sent successfully"));
    }
}
