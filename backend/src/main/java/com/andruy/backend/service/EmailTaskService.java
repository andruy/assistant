package com.andruy.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.repository.EmailTaskRepository;

@Service
public class EmailTaskService {
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailTaskRepository emailTaskRepository;
    private final Map<TaskId, Thread> activeThreads = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(EmailTaskService.class);

    public List<String> getTaskTemplate() {
        return emailTaskRepository.getEmailActions();
    }

    public Set<TaskId> getThreads() {
        for (Map.Entry<TaskId, Thread> entry : activeThreads.entrySet()) {
            if (!entry.getValue().isAlive()) {
                activeThreads.remove(entry.getKey());
            }
        }

        return activeThreads.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            .keySet();
    }

    public void scheduleTask(EmailTask emailTask) {
        TaskId taskId = new TaskId(UUID.randomUUID().toString(), emailTask.email().subject(), emailTask.getTime());

        Runnable callback = () -> {
            emailService.sendEmail(emailTask.email());
            logger.trace("Email sent at " + LocalDateTime.now().toString().substring(0, 16));
            pushNotificationService.push(new PushNotification(emailTask.email().subject(), "Done"));
        };

        Thread vThread = Thread.ofVirtual().start(() -> {
            try {
                if (emailTask.timeframe() < System.currentTimeMillis()) {
                    callback.run();
                } else {
                    Thread.sleep(emailTask.timeframe() - System.currentTimeMillis());

                    if (!Thread.currentThread().isInterrupted()) {
                        callback.run();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.trace("Thread interrupted: " + taskId.id());
            } catch (Exception e) {
                logger.error("Thread issue\n" + e.getMessage());
            } finally {
                activeThreads.remove(taskId);
            }
        });

        activeThreads.put(taskId, vThread);
        logger.trace("Task scheduled for " + emailTask.getTime());
    }

    public boolean cancelTask(TaskId taskId) {
        Thread vThread = activeThreads.get(taskId);
        if (vThread != null) {
            vThread.interrupt();
            activeThreads.remove(taskId);
            pushNotificationService.push(new PushNotification("Suspended", taskId.name() + " (" + taskId.time() + ")"));
            return true;
        }
        return false;
    }
}
