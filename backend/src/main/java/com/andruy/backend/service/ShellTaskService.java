package com.andruy.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Directory;
import com.andruy.backend.model.DirectoryCorrection;
import com.andruy.backend.model.Email;
import com.andruy.backend.model.ShellTask;
import com.andruy.backend.repository.ShellTaskRepository;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.DirectoryList;
import com.andruy.backend.util.ShellScriptBuilder;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class ShellTaskService {
    private Logger logger = LoggerFactory.getLogger(ShellTaskService.class);
    @Value("${my.email.recipient}")
    private String receiver;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShellTaskRepository shellTaskRepository;
    private Map<Directory, List<String>> doNotExist;
    private ShellScriptBuilder scriptBuilder;
    private List<Directory> directories;
    private List<String> taskResponse;
    private Playwright playwright;
    private Browser browser;
    private ShellTask task;

    public void ytTask(Map<Directory, List<String>> map) {
        task = ShellTask.YOUTUBE;
        scriptBuilder = new ShellScriptBuilder(task);
        directories = new DirectoryList().getDirectories();
        doNotExist = new HashMap<>();

        for (Entry<Directory, List<String>> entry : map.entrySet()) {
            if (directories.contains(entry.getKey())) {
                scriptBuilder.moveTo(entry.getKey().getName());

                for (String s : entry.getValue()) {
                    scriptBuilder.downloadVideo(s);
                }

                scriptBuilder.moveUp();
            } else {
                doNotExist.put(entry.getKey(), entry.getValue());
            }
        }

        if (!doNotExist.isEmpty()) {
            emailService.sendEmail(
                new Email(
                    receiver == null ? System.getProperty("emailRecipient") : receiver,
                    "The following directories do not exist today " + LocalDateTime.now().toString().substring(0, 16),
                    doNotExist.toString()
                )
            );
        }

        scriptBuilder.build();
        taskResponse = scriptBuilder.getReport();
        new BashHandler(taskResponse.toString()).start();
    }

    public void assignAndProcess(Map<String, List<String>> body) {
        List<String> list = body.get("links");

        Map<Directory, List<String>> mapForTask = new HashMap<>();
        List<DirectoryCorrection> corrections = shellTaskRepository.getDirectories();

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            for (String url : list) {
                Directory directory = new Directory(getDirectory(url, page));

                if (directory != null && !directory.getName().isEmpty()) {
                    for (DirectoryCorrection dc : corrections) {
                        if (directory.getName().equals(dc.name())) {
                            directory.setName(dc.alias());
                            break;
                        }
                    }

                    if (mapForTask.containsKey(directory)) {
                        mapForTask.get(directory).add(url);
                    } else {
                        mapForTask.put(directory, new ArrayList<>(List.of(url)));
                    }
                } else {
                    logger.warn("Directory is null or empty for URL: " + url);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            browser.close();
            playwright.close();
        }

        ytTask(mapForTask);
    }

    private String getDirectory(String address, Page page) {
        String response = "";

        try {
            page.navigate(address);
            response = page.locator("#text-container").locator("a").innerText();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return response;
    }

    public List<String> getTaskResponse () {
        return List.of(
            taskResponse.get(0), taskResponse.get(1)
        );
    }
}
