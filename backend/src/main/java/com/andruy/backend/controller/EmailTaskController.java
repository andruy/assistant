package com.andruy.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.service.EmailTaskService;

@Controller
public class EmailTaskController {
    @Autowired
    private EmailTaskService emailTaskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<String>> getTasks() {
        return ResponseEntity.ok().body(emailTaskService.getTaskTemplate());
    }

    @DeleteMapping("/deletetask")
    public ResponseEntity<Map<String, String>> deleteTask(@RequestBody TaskId body) {
        emailTaskService.deleteThread(body);
        return ResponseEntity.ok().body(Map.of("report", emailTaskService.getDeletionReport()));
    }

    @GetMapping("/emailtasks")
    public ResponseEntity<Set<TaskId>> getEmailTasks() {
        return ResponseEntity.ok().body(Set.copyOf(emailTaskService.getThreads()));
    }

    @PostMapping("/emailtask")
    public ResponseEntity<Map<String, String>> sendTask(@RequestBody List<EmailTask> body) {
        for (EmailTask task : body) {
            emailTaskService.setTask(task);
            emailTaskService.sendTaskAsync();
        }
        return ResponseEntity.ok().body(Map.of("report", "Tasks sent successfully"));
    }
}
