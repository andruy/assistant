package com.andruy.backend.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Map;

import com.andruy.backend.model.TaskId;

public class Promise {
    private static final Map<TaskId, Thread> threads = new ConcurrentHashMap<>();

    private Promise() {}

    public static Map<TaskId, Thread> getThreads() {
        for (Map.Entry<TaskId, Thread> entry : threads.entrySet()) {
            if (!entry.getValue().isAlive()) {
                threads.remove(entry.getKey());
            }
        }

        return threads.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void add(TaskId params, Thread thread) {
        threads.put(params, thread);
    }

    public static void killThread(TaskId params) {
        if (threads.containsKey(params)) {
            threads.get(params).interrupt();
            threads.remove(params);
        }
    }
}
