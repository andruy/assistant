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
    private EmailTask task;
    private String deletionReport;

    public void setTask(EmailTask task) {
        this.task = task;
    }

    public List<String> getTaskTemplate() {
        return emailTaskRepository.getEmailActions();
    }

    public Set<TaskId> getThreads() {
        return Promise.getThreads().keySet();
    }

    public void sendTaskAsync() {
        new TaskHandler(task);
    }

    public void deleteThread(TaskId params) {
        Promise.killThread(params);
        deletionReport = "Thread " + params.id() + " killed";
        pushNotificationService.push(new PushNotification("Suspended", params.name() + " (" + params.time() + ")"));
    }

    public String report() {
        return task.timeframe() < System.currentTimeMillis() ? "Sending email now" :
            "Sending email on " + task.getTime();
    }

    public String getDeletionReport() {
        return deletionReport;
    }
}
