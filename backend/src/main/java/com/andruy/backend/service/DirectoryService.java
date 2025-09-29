package com.andruy.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Directory;
import com.andruy.backend.util.BashHandler;
import com.andruy.backend.util.Constants;

@Service
public class DirectoryService {
    @Value("${my.programming.directory}")
    private String base;

    public String createFolder(Directory name) {
        List<String> output = new BashHandler(new String[] { Constants.MAKE_DIR, base + name.name() }).startAndReturnOutput();

        // if (!output.isEmpty()) {
        // }
        return output.isEmpty() ? "Created folder " + name.name() : output.get(0);
    }
}
