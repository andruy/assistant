package com.andruy.backend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.PushNotification;

@Service
public class PushNotificationService {
    @Value("${my.ntfy.url}")
    private String ntfyUrl;
    private int statusCode;
    private HttpClient httpClient;
    private HttpRequest httpRequest;

    public int push(PushNotification msg) {
        httpClient = HttpClient.newHttpClient();
        httpRequest = HttpRequest.newBuilder()
            .header("Title", msg.title())
            .POST(HttpRequest.BodyPublishers.ofString(msg.body()))
            .uri(URI.create(ntfyUrl))
            .build();

        statusCode = httpClient.sendAsync(httpRequest, BodyHandlers.ofString())
        .thenApply(HttpResponse::statusCode)
        .join();

        return statusCode;
    }
}
