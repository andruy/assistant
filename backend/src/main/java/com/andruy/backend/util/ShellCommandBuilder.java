package com.andruy.backend.util;

import com.andruy.backend.model.ShellTask;
import java.util.ArrayList;
import java.util.List;

public final class ShellCommandBuilder {
    private List<String> command = new ArrayList<>();

    public ShellCommandBuilder(ShellTask task) {
        command.add("{");

        if (task == ShellTask.YOUTUBE) {
            command.add(Constants.CD);
            command.add(System.getProperty("programmingDirectory"));
            command.add(Constants.SEPARATOR);
            command.add(Constants.PWD);
        }
    }

    public void moveTo(String dir) {
        command.add(Constants.CD);
        command.add(dir);
        command.add(Constants.SEPARATOR);
        command.add(Constants.PWD);
    }

    public void downloadVideo(String downloadLink) {
        command.add(System.getProperty("ytd"));
        command.add(downloadLink);
    }

    public void moveUp() {
        command.add(Constants.CD);
        command.add(Constants.PREVIOUS_DIR);
        command.add(Constants.SEPARATOR);
        command.add(Constants.PWD);
    }

    public String[] build() {
        command.add(Constants.DATE);
        command.add("}");
        return command.toArray(new String[0]);
    }
}
