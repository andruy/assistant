package com.andruy.backend.util;

import java.time.LocalDateTime;
import java.util.UUID;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.service.EmailService;
import com.andruy.backend.service.PushNotificationService;

public class TaskHandler extends Thread {
    private EmailTask task;
    private Thread thread;

    public TaskHandler(EmailTask task) {
        this.task = task;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            if (task.getTimeframe() < System.currentTimeMillis()) {
                execute();
            } else {
                TaskId params = new TaskId(UUID.randomUUID().toString(), task.getEmail().getSubject(), task.getTime());

                Promise.add(params, thread);

                Thread.sleep(task.getTimeframe() - System.currentTimeMillis());

                execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute() {
        new EmailService().sendEmail(task.getEmail());
        System.out.println("Email sent at " + LocalDateTime.now().toString().substring(0, 16));
        new PushNotificationService().push(new PushNotification(task.getEmail().getSubject(), "Done"));
    }
}
