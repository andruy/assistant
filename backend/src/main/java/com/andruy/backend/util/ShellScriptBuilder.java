package com.andruy.backend.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.andruy.backend.model.ShellTask;

public final class ShellScriptBuilder {
    private String date;
    private String logReport;
    private String scriptReport;
    private StringBuilder content = new StringBuilder();

    public ShellScriptBuilder(ShellTask type) {
        content.append(System.getProperty("bin") + "\n");

        if (type == ShellTask.YOUTUBE) {
            content.append("\n%s '%s'\n".formatted(Constants.CD, System.getProperty("programmingDirectory")));
            content.append("%s\n".formatted(Constants.PWD));
        }
    }

    public void moveTo(String dir) {
        content.append("\n%s '%s/'\n".formatted(Constants.CD, dir));
        content.append("%s\n".formatted(Constants.PWD));
    }

    public void downloadVideo(String downloadLink) {
        content.append("%s %s\n".formatted(System.getProperty("ytd"), downloadLink));
    }

    public void moveUp() {
        content.append("%s %s\n".formatted(Constants.CD, Constants.PREVIOUS_DIR));
        content.append("%s\n".formatted(Constants.PWD));
    }

    public void build() {
        try {
            content.append("\n%s\n".formatted(Constants.DATE));
            BufferedWriter writer = new BufferedWriter(new FileWriter("script.sh"));
            writer.write(content.toString());
            writer.close();
            scriptReport = "Script was built successfully";
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            date = LocalDateTime.now().toString().replace(":", "").substring(0, 15);
            BufferedWriter writer = new BufferedWriter(new FileWriter("logs/" + date + ".log"));
            writer.write(content.toString());
            writer.close();
            logReport = "Created file " + date + ".log";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getReport() {
        return List.of(scriptReport, logReport);
    }
}
