package com.andruy.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import com.andruy.backend.model.Email;
import com.andruy.backend.service.EmailService;

@Component
public class BashHandler {
    private String line;
    private String report;
    private String[] input;
    private Process process;
    private List<String> output;
    private BufferedReader reader;
    private StringBuffer emailReport;
    @Autowired
    private EmailService emailService;
    private final String OS = System.getProperty("os.name").toLowerCase();

    public void init(String report) {
        this.report = report;
        emailReport = new StringBuffer();
        input = new String[] { "./script.sh" };
        output = new CopyOnWriteArrayList<>();
        runWithNoOutput();
    }

    public void runWithNoOutput() {
        execute();
        emailReport.append("Running script...\n");

        if (output.size() == 0 && input[0].equals("./script.sh")) {
            emailReport.append("No output\n");
            System.out.println("Something went wrong");
            emailOutput();
        } else if (output.size() > 0 && input[0].equals("./script.sh")) {
            for (int i = 0; i < output.size() - 1; i++) {
                if (output.get(i).startsWith("[d") && output.get(i).contains("ETA")) {
                    continue;
                }
                emailReport.append(output.get(i) + "\n");
            }
            emailReport.append("Completed: " + output.get(output.size() - 1));
            System.out.println("Task complete");
            emailOutput();
        } else if (output.size() > 0) {
            for (String s : output) {
                emailReport.append(s + "\n");
            }
            emailOutput();
        }
    }

    public List<String> startAndReturnOutput(String[] input) {
        this.input = input;
        output = new ArrayList<>();
        execute();
        return output;
    }

    private void emailOutput() {
        emailService.sendEmail(
            new Email(
                System.getProperty("emailRecipient"),
                "Script report for " + LocalDateTime.now().toString().substring(0, 16),
                report + "\n" + emailReport.toString()
            )
        );
    }

    private void execute() {
        if (OS.contains("mac") || OS.contains("win")) {
            throw new UnsupportedOperationException("This operation is only supported on Linux systems.");
        }

        try {
            process = Runtime.getRuntime().exec(input);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
