package com.andruy.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PropertyProvider {
    @Value("${my.programming.directory}")
    private String programmingDirectory;
    @Value("${my.yt.downloader}")
    private String ytd;
    @Value("${my.bin.bash}")
    private String bin;
    @Value("${my.email.recipient}")
    private String recipient;
    @Value("${my.app.timezone}")
    private String timezone;

    @PostConstruct
    public void init() {
        System.setProperty("programmingDirectory", programmingDirectory);
        System.setProperty("ytd", ytd);
        System.setProperty("bin", bin);
        System.setProperty("emailRecipient", recipient);
        System.setProperty("appTimezone", timezone);
    }
}
