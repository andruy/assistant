package com.andruy.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.repository.EmailTaskRepository;
import com.andruy.backend.util.Promise;
import com.andruy.backend.util.TaskHandler;

@Service
public class EmailTaskService {
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private EmailTaskRepository emailTaskRepository;
    @Autowired
    private TaskHandler taskHandler;

    public List<String> getTaskTemplate() {
        return emailTaskRepository.getEmailActions();
    }

    public Set<TaskId> getThreads() {
        return Promise.getThreads().keySet();
    }

    public void sendTaskAsync(EmailTask task) {
        taskHandler.init(task);
    }

    public String deleteThread(TaskId params) {
        Promise.killThread(params);
        pushNotificationService.push(new PushNotification("Suspended", params.name() + " (" + params.time() + ")"));
        return "Thread " + params.id() + " killed";
    }
}
