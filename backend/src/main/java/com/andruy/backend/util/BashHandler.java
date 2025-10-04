package com.andruy.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

import com.andruy.backend.model.Email;
import com.andruy.backend.service.EmailService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Component
public class BashHandler {
    private String line;
    private String report;
    private String[] input;
    private Process process;
    private List<String> output;
    private BufferedReader reader;
    private StringBuffer emailReport;
    private StringBuilder outputBuffer;
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
            try {
                JSch jsch = new JSch();
                Session session = jsch.getSession(System.getProperty("sshUsername"), System.getProperty("sshHost"));
                session.setPassword(System.getProperty("sshPassword"));

                // Avoid asking for key confirmation
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();

                System.out.println("Connected to the server!");

                // Execute a command
                outputBuffer = new StringBuilder();
                for (String s : input) {
                    if (s.contains(" ")) {
                        outputBuffer.append("'").append(s).append("'");
                    } else {
                        outputBuffer.append(s).append(" ");
                    }
                }

                ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
                channelExec.setCommand(outputBuffer.toString());
                channelExec.setErrStream(System.err);

                InputStream in = channelExec.getInputStream();
                channelExec.connect();

                // Read the output from the command
                outputBuffer = new StringBuilder();
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        outputBuffer.append(new String(tmp, 0, i));
                    }
                    if (channelExec.isClosed()) {
                        if (in.available() > 0) continue;
                        System.out.println("Exit status: " + channelExec.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                channelExec.disconnect();
                session.disconnect();

                // Split the output into lines
                output = Arrays.asList(outputBuffer.toString().split("\n"));
                System.out.println("Disconnected from the server!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
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
}
