package com.andruy.backend.service;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    Logger logger = LoggerFactory.getLogger(LogService.class);

    public Map<String, String> logReader() {
        StringBuilder sb = new StringBuilder();
        File log = new File("logs/app.log");

        try {
            Scanner scanner = new Scanner(log);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return Map.of("report", sb.toString());
    }
}
