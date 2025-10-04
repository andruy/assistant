package com.andruy.backend.util;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.service.EmailService;
import com.andruy.backend.service.PushNotificationService;

@Component
public class TaskHandler extends Thread {
    private EmailTask task;
    private Thread thread;
    @Autowired
    private EmailService emailService;

    public void init(EmailTask task) {
        this.task = task;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            if (task.timeframe() < System.currentTimeMillis()) {
                execute();
            } else {
                TaskId params = new TaskId(UUID.randomUUID().toString(), task.email().subject(), task.getTime());

                Promise.add(params, thread);

                Thread.sleep(task.timeframe() - System.currentTimeMillis());

                execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute() {
        emailService.sendEmail(task.email());
        System.out.println("Email sent at " + LocalDateTime.now().toString().substring(0, 16));
        new PushNotificationService().push(new PushNotification(task.email().subject(), "Done"));
    }
}
