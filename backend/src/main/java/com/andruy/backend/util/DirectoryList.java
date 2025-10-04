package com.andruy.backend.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.andruy.backend.model.Directory;

@Component
public class DirectoryList {
    @Autowired
    private BashHandler bashHandler;
    
    public List<Directory> getDirectories() {
        return bashHandler.startAndReturnOutput(
            new String[] { Constants.LIST, System.getProperty("programmingDirectory") }
        ).stream().map(s -> new Directory(s)).toList();
    }
}
