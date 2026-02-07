package com.andruy.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Directory;
import com.andruy.backend.model.DirectoryCorrection;
import com.andruy.backend.model.Email;
import com.andruy.backend.model.ShellTask;
import com.andruy.backend.repository.ShellTaskRepository;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.DirectoryList;
import com.andruy.backend.util.ShellScriptBuilder;

@Service
public class ShellTaskService {
    private Logger logger = LoggerFactory.getLogger(ShellTaskService.class);
    @Value("${my.email.recipient}")
    private String receiver;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShellTaskRepository shellTaskRepository;
    @Autowired
    private DirectoryList directoryList;
    @Autowired
    private BashHandler bashHandler;
    private Map<Directory, List<String>> doNotExist;
    private ShellScriptBuilder scriptBuilder;
    private List<Directory> directories;
    private List<String> taskResponse;
    private ShellTask task;

    @Async
    public CompletableFuture<Void> ytTask(Map<Directory, List<String>> map) {
        task = ShellTask.YOUTUBE;
        scriptBuilder = new ShellScriptBuilder(task);
        directories = directoryList.getDirectories();
        doNotExist = new HashMap<>();

        for (Entry<Directory, List<String>> entry : map.entrySet()) {
            if (directories.contains(entry.getKey())) {
                scriptBuilder.moveTo(entry.getKey().name());

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
                    receiver,
                    "The following directories do not exist today " + LocalDateTime.now().toString().substring(0, 16),
                    doNotExist.toString()
                )
            );
        }

        scriptBuilder.build();
        taskResponse = scriptBuilder.getReport();
        bashHandler.init(taskResponse.toString());
        return CompletableFuture.completedFuture(null);
    }

    public Map<Directory, List<String>> assignDirectories(Map<String, List<String>> body) {
        List<String> list = body.get("links");

        Map<Directory, List<String>> mapForTask = new HashMap<>();
        List<DirectoryCorrection> corrections = shellTaskRepository.getDirectories();

        try {
            for (String url : list) {
                Directory directory = new Directory(getDirectory(url));

                if (directory != null && !directory.name().isEmpty()) {
                    for (DirectoryCorrection dc : corrections) {
                        if (directory.name().equals(dc.name())) {
                            directory = new Directory(dc.alias());
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
        }

        return mapForTask;
    }

    private String getDirectory(String address) {
        String response = "";

        try {
            Document document = Jsoup.connect(address).get();
            Element element = document.selectFirst("link[itemprop=name]");

            if (element != null) {
                response = element.attr("content");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return response;
    }
}
