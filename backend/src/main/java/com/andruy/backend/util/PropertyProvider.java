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
    @Value("${my.ssh.host}")
    private String sshHost;
    @Value("${my.ssh.username}")
    private String sshUsername;
    @Value("${my.ssh.password}")
    private String sshPassword;
    @Value("${my.email.username}")
    private String username;
    @Value("${my.email.password}")
    private String password;
    @Value("${my.email.host}")
    private String host;
    @Value("${my.email.port}")
    private String port;
    @Value("${my.email.recipient}")
    private String recipient;
    @Value("${my.ntfy.url}")
    private String ntfyUrl;

    @PostConstruct
    public void init() {
        System.setProperty("programmingDirectory", programmingDirectory);
        System.setProperty("ytd", ytd);
        System.setProperty("bin", bin);
        System.setProperty("sshHost", sshHost);
        System.setProperty("sshUsername", sshUsername);
        System.setProperty("sshPassword", sshPassword);
        System.setProperty("emailUsername", username);
        System.setProperty("emailPassword", password);
        System.setProperty("emailHost", host);
        System.setProperty("emailPort", port);
        System.setProperty("emailRecipient", recipient);
        System.setProperty("ntfyUrl", ntfyUrl);
    }
}
