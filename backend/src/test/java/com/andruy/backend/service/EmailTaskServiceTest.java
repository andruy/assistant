package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.andruy.backend.model.Email;
import com.andruy.backend.model.EmailTask;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.model.TaskId;
import com.andruy.backend.repository.EmailTaskRepository;

@ExtendWith(MockitoExtension.class)
class EmailTaskServiceTest {
    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTaskRepository emailTaskRepository;

    @InjectMocks
    private EmailTaskService emailTaskService;

    private Email testEmail;
    private EmailTask testEmailTask;

    @BeforeEach
    void setUp() {
        System.setProperty("appTimezone", "America/New_York");
        testEmail = new Email("test@example.com", "Test Subject", "Test Body");
    }

    @Test
    @DisplayName("Should return email action templates from repository")
    void getTaskTemplate_ReturnsTemplatesFromRepository() {
        List<String> templates = List.of("Template 1", "Template 2", "Template 3");
        when(emailTaskRepository.getEmailActions()).thenReturn(templates);

        List<String> result = emailTaskService.getTaskTemplate();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Template 1", "Template 2", "Template 3");
        verify(emailTaskRepository).getEmailActions();
    }

    @Test
    @DisplayName("Should return empty set when no active threads")
    void getThreads_WhenNoActiveThreads_ReturnsEmptySet() {
        Set<TaskId> threads = emailTaskService.getThreads();

        assertThat(threads).isEmpty();
    }

    @Test
    @DisplayName("Should schedule task and execute immediately when timeframe is in the past")
    void scheduleTask_WhenTimeframeInPast_ExecutesImmediately() throws InterruptedException {
        long pastTime = System.currentTimeMillis() - 10000;
        testEmailTask = new EmailTask(pastTime, testEmail);
        doNothing().when(emailService).sendEmail(any(Email.class));
        when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

        emailTaskService.scheduleTask(testEmailTask);

        verify(emailService, timeout(2000)).sendEmail(testEmail);
        verify(pushNotificationService, timeout(2000)).push(any(PushNotification.class));
    }

    @Test
    @DisplayName("Should add task to active threads when scheduled")
    void scheduleTask_AddsTaskToActiveThreads() throws InterruptedException {
        long futureTime = System.currentTimeMillis() + 60000;
        testEmailTask = new EmailTask(futureTime, testEmail);

        emailTaskService.scheduleTask(testEmailTask);
        Thread.sleep(100);

        Set<TaskId> threads = emailTaskService.getThreads();
        assertThat(threads).hasSize(1);
        assertThat(threads.iterator().next().name()).isEqualTo("Test Subject");
    }

    @Test
    @DisplayName("Should cancel scheduled task successfully")
    void cancelTask_WhenTaskExists_ReturnsTrue() throws InterruptedException {
        long futureTime = System.currentTimeMillis() + 60000;
        testEmailTask = new EmailTask(futureTime, testEmail);
        when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

        emailTaskService.scheduleTask(testEmailTask);
        Thread.sleep(100);

        Set<TaskId> threads = emailTaskService.getThreads();
        TaskId taskId = threads.iterator().next();

        boolean cancelled = emailTaskService.cancelTask(taskId);

        assertThat(cancelled).isTrue();
        assertThat(emailTaskService.getThreads()).isEmpty();
        verify(pushNotificationService).push(any(PushNotification.class));
    }

    @Test
    @DisplayName("Should return false when cancelling non-existent task")
    void cancelTask_WhenTaskNotExists_ReturnsFalse() {
        TaskId nonExistentTask = new TaskId("non-existent-id", "Test", "2024-01-01 at 10:00");

        boolean cancelled = emailTaskService.cancelTask(nonExistentTask);

        assertThat(cancelled).isFalse();
    }

    @Test
    @DisplayName("Should remove task from active threads after execution")
    void scheduleTask_RemovesTaskAfterExecution() throws InterruptedException {
        long pastTime = System.currentTimeMillis() - 1000;
        testEmailTask = new EmailTask(pastTime, testEmail);
        doNothing().when(emailService).sendEmail(any(Email.class));
        when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

        emailTaskService.scheduleTask(testEmailTask);
        Thread.sleep(500);

        assertThat(emailTaskService.getThreads()).isEmpty();
    }

    @Test
    @DisplayName("Should not send email when task is cancelled before execution")
    void cancelTask_PreventsEmailFromBeingSent() throws InterruptedException {
        long futureTime = System.currentTimeMillis() + 5000;
        testEmailTask = new EmailTask(futureTime, testEmail);
        when(pushNotificationService.push(any(PushNotification.class))).thenReturn(200);

        emailTaskService.scheduleTask(testEmailTask);
        Thread.sleep(100);

        Set<TaskId> threads = emailTaskService.getThreads();
        TaskId taskId = threads.iterator().next();
        emailTaskService.cancelTask(taskId);

        Thread.sleep(6000);

        verify(emailService, never()).sendEmail(any(Email.class));
    }
}
