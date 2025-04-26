package com.andruy.backend.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.util.Promise;
import com.andruy.backend.util.TaskHandler;

@Service
public class EmailTaskService {
    @Autowired
    private PushNotificationService pushNotificationService;
    @Value("${dir.corrections}")
    private String dataFile;
    private Scanner scanner;
    private StringBuilder sb;
    private EmailTask task;
    private String deletionReport;

    public void setTask(EmailTask task) {
        this.task = task;
    }

    public List<String> getTaskTemplate() {
        List<String> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(getTaskList()).getJSONObject("EmailActions");

        jsonObject.keys().forEachRemaining(key -> list.add(jsonObject.get(key).toString()));

        return list;
    }

    public Set<TaskId> getThreads() {
        return Promise.getThreads().keySet();
    }

    public void sendTaskAsync() {
        new TaskHandler(task);
    }

    public void deleteThread(TaskId params) {
        Promise.killThread(params);
        deletionReport = "Thread " + params.getId() + " killed";
        pushNotificationService.push(new PushNotification("Suspended", params.getName() + " (" + params.getTime() + ")"));
    }

    private String getTaskList() {
        sb = new StringBuilder();

        try {
            scanner = new Scanner(new File(dataFile));
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String report() {
        return task.getTimeframe() < System.currentTimeMillis() ? "Sending email now" :
            "Sending email on " + task.getTime();
    }

    public String getDeletionReport() {
        return deletionReport;
    }
}
