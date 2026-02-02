package com.andruy.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Directory;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.Constants;

@Service
public class DirectoryService {
    @Value("${my.programming.directory}")
    private String base;
    @Autowired
    private BashHandler bashHandler;

    public String createFolder(Directory name) {
        List<String> output = bashHandler.startAndReturnOutput(new String[] { Constants.MAKE_DIR, base + name.name() });

        return output.isEmpty() ? "Created folder " + name.name() : output.get(0);
    }
}
