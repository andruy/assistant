package com.andruy.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.service.InstagramService;

@RestController
@RequestMapping("/api/instagram")
public class InstagramController {
    private final InstagramService instagramService;

    public InstagramController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    @GetMapping("/followers")
    public ResponseEntity<Map<String, String>> getFollowers() {
        instagramService.getFollowers();
        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }

    @GetMapping("/following")
    public ResponseEntity<Map<String, String>> getFollowing() {
        instagramService.getFollowing();
        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, String>> getFollowersAndFollowingComparison() {
        instagramService.getComparison();

        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }

    @GetMapping("/dates")
    public ResponseEntity<List<Long>> getListOfDates() {
        return ResponseEntity.ok(instagramService.getListOfDates().stream()
                .map(Timestamp::getTime).toList());
    }

    @GetMapping("/dates/{type}")
    public ResponseEntity<List<Long>> getListOfDatesByType(@PathVariable String type) {
        return ResponseEntity.ok(instagramService.getListOfDates(type).stream()
                .map(Timestamp::getTime).toList());
    }

    @GetMapping("/accounts")
    public ResponseEntity<Map<String, String>> getListOfAccounts(@RequestParam Long date) {
        return ResponseEntity.ok(instagramService.getListOfAccounts("nmf", new Timestamp(date)));
    }

    @GetMapping("/compare-dates")
    public ResponseEntity<Map<String, String>> getComparisonBetweenDates(
            @RequestParam Long dateFollowers,
            @RequestParam Long dateFollowing) {
        return ResponseEntity.ok(instagramService.getComparisonBetweenDates(
                new Timestamp(dateFollowers), new Timestamp(dateFollowing)));
    }

    @DeleteMapping("/accounts")
    public ResponseEntity<Map<String, String>> deleteAccounts(
            @RequestParam Long date,
            @RequestBody List<String> accounts) {
        instagramService.deleteAccounts("nmf", new Timestamp(date), accounts);

        return ResponseEntity.accepted().body(Map.of("message", "Deletion started. You will be notified when complete."));
    }

    @PutMapping("/accounts/protect")
    public ResponseEntity<Map<String, String>> protectAccounts(
            @RequestParam Long date,
            @RequestBody List<String> accounts) {
        return ResponseEntity.ok(instagramService.protectAccounts(new Timestamp(date), accounts));
    }

    @GetMapping("/screenshots")
    public ResponseEntity<List<String>> listRuns() throws IOException {
        Path dir = Paths.get("screenshots");
        if (!Files.exists(dir)) {
            return ResponseEntity.ok(List.of());
        }
        try (Stream<Path> dirs = Files.list(dir)) {
            List<String> runs = dirs
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted(java.util.Comparator.reverseOrder())
                    .toList();
            return ResponseEntity.ok(runs);
        }
    }

    @GetMapping("/screenshots/{run}")
    public ResponseEntity<List<String>> listScreenshots(@PathVariable String run) throws IOException {
        Path dir = Paths.get("screenshots", run);
        if (!Files.exists(dir)) {
            return ResponseEntity.notFound().build();
        }
        try (Stream<Path> files = Files.list(dir)) {
            List<String> names = files
                    .filter(p -> p.toString().endsWith(".png"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toList();
            return ResponseEntity.ok(names);
        }
    }

    @GetMapping("/screenshots/{run}/{filename}")
    public ResponseEntity<byte[]> getScreenshot(@PathVariable String run, @PathVariable String filename) throws IOException {
        Path file = Paths.get("screenshots", run, filename);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/png"))
                .body(Files.readAllBytes(file));
    }
}
