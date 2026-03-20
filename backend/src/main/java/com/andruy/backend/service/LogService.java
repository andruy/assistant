package com.andruy.backend.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class LogService {
    private final Logger logger = LoggerFactory.getLogger(LogService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final File logFile;
    private volatile long lastPosition = 0;

    public LogService(@Value("${logging.file.name:logs/app.log}") String logFilePath) {
        this.logFile = new File(logFilePath);
    }

    public Map<String, String> logReader() {
        StringBuilder sb = new StringBuilder();

        try {
            Scanner scanner = new Scanner(logFile);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return Map.of("report", sb.toString());
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);

        try {
            String fullLog = readFile(0, logFile.length());
            lastPosition = logFile.length();
            emitter.send(SseEmitter.event().name("init").data(fullLog));
        } catch (IOException e) {
            logger.error("Failed to send initial log content: {}", e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }

        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        return emitter;
    }

    @Scheduled(fixedRate = 1000)
    public void pushNewLogLines() {
        if (emitters.isEmpty() || !logFile.exists()) return;

        long fileLength = logFile.length();

        if (fileLength < lastPosition) {
            lastPosition = 0;
        }
        if (fileLength <= lastPosition) return;

        String newContent = readFile(lastPosition, fileLength);
        lastPosition = fileLength;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("log").data(newContent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @NonNull
    private String readFile(long from, long to) {
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            raf.seek(from);
            byte[] bytes = new byte[(int) (to - from)];
            raf.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read log file: {}", e.getMessage());
            return "";
        }
    }
}
