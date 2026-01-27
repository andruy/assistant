package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.andruy.backend.model.PushNotification;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

class PushNotificationServiceTest {
    private PushNotificationService pushNotificationService;
    private static HttpServer mockServer;
    private static int serverPort;
    private static int lastResponseCode = 200;
    private static String lastReceivedTitle;
    private static String lastReceivedBody;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        serverPort = mockServer.getAddress().getPort();

        mockServer.createContext("/test", exchange -> {
            lastReceivedTitle = exchange.getRequestHeaders().getFirst("Title");
            lastReceivedBody = new String(exchange.getRequestBody().readAllBytes());

            exchange.sendResponseHeaders(lastResponseCode, 0);
            exchange.close();
        });

        mockServer.start();
    }

    @AfterAll
    static void tearDownServer() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    @BeforeEach
    void setUp() {
        pushNotificationService = new PushNotificationService();
        ReflectionTestUtils.setField(pushNotificationService, "ntfyUrl",
                "http://localhost:" + serverPort + "/test");
        lastResponseCode = 200;
        lastReceivedTitle = null;
        lastReceivedBody = null;
    }

    @Test
    @DisplayName("Should send push notification and return 200 status")
    void push_WhenSuccessful_Returns200() {
        PushNotification notification = new PushNotification("Test Title", "Test Body");

        int statusCode = pushNotificationService.push(notification);

        assertThat(statusCode).isEqualTo(200);
    }

    @Test
    @DisplayName("Should send title in request header")
    void push_SendsTitleInHeader() {
        PushNotification notification = new PushNotification("My Title", "My Body");

        pushNotificationService.push(notification);

        assertThat(lastReceivedTitle).isEqualTo("My Title");
    }

    @Test
    @DisplayName("Should send body in request body")
    void push_SendsBodyInRequestBody() {
        PushNotification notification = new PushNotification("Title", "This is the message body");

        pushNotificationService.push(notification);

        assertThat(lastReceivedBody).isEqualTo("This is the message body");
    }

    @Test
    @DisplayName("Should handle empty title")
    void push_WithEmptyTitle_SendsEmptyHeader() {
        PushNotification notification = new PushNotification("", "Body only");

        int statusCode = pushNotificationService.push(notification);

        assertThat(statusCode).isEqualTo(200);
        assertThat(lastReceivedTitle).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty body")
    void push_WithEmptyBody_SendsEmptyBody() {
        PushNotification notification = new PushNotification("Title only", "");

        int statusCode = pushNotificationService.push(notification);

        assertThat(statusCode).isEqualTo(200);
        assertThat(lastReceivedBody).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in title and body")
    void push_WithSpecialCharacters_SendsCorrectly() {
        PushNotification notification = new PushNotification(
                "Title with special chars: !@#$%",
                "Body with unicode: \u00e9\u00e8\u00ea"
        );

        int statusCode = pushNotificationService.push(notification);

        assertThat(statusCode).isEqualTo(200);
        assertThat(lastReceivedTitle).isEqualTo("Title with special chars: !@#$%");
    }
}
