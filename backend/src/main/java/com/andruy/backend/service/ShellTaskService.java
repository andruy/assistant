package com.andruy.backend.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Directory;
import com.andruy.backend.model.Email;
import com.andruy.backend.model.ShellTask;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.DirectoryList;
import com.andruy.backend.util.ShellScriptBuilder;

@Service
public class ShellTaskService {
    @Value("${my.email.recipient}")
    private String receiver;
    @Value("${dir.corrections}")
    private String dataFile;
    @Autowired
    private EmailService emailService;
    private Map<Directory, List<String>> doNotExist;
    private ShellScriptBuilder scriptBuilder;
    private List<Directory> directories;
    private List<String> taskResponse;
    private StringBuilder sb;
    private Scanner scanner;
    private ShellTask task;
    private URL url;

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

        Map<Directory, List<String>> map = new HashMap<>();
        JSONObject correction = new JSONObject(renameDirectory()).getJSONObject("RenameDirectory");

        for (String url : list) {
            Directory directory = new Directory(getDirectory(url));

            if (correction.has(directory.getName())) {
                directory.setName(correction.getString(directory.getName()));
            }

            if (map.containsKey(directory)) {
                map.get(directory).add(url);
            } else {
                map.put(directory, new ArrayList<>(List.of(url)));
            }
        }

        ytTask(map);
    }

    private String getDirectory(String address) {
        String response = "";
        StringBuilder content = new StringBuilder();

        try {
            url = new URL(address);
            URLConnection conn = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }

            bufferedReader.close();
            response = content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.substring(
            response.indexOf("\"name\": \"") + 9, response.indexOf("\"}}]}</script>")
        );
    }

    private String renameDirectory() {
        sb = new StringBuilder();

        try {
            scanner = new Scanner(new File(dataFile));
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException  e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public List<String> getTaskResponse () {
        return List.of(
            taskResponse.get(0), taskResponse.get(1)
        );
    }
}
