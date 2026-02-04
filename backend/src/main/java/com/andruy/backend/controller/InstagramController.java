package com.andruy.backend.controller;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
        return ResponseEntity.ok(instagramService.getFollowers());
    }

    @GetMapping("/following")
    public ResponseEntity<Map<String, String>> getFollowing() {
        return ResponseEntity.ok(instagramService.getFollowing());
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, String>> getFollowersAndFollowingComparison() {
        instagramService.getComparison();

        return ResponseEntity.accepted().body(Map.of("message", "Task started. You will be notified when complete."));
    }

    @GetMapping("/dates")
    public ResponseEntity<List<Date>> getListOfDates() {
        return ResponseEntity.ok(instagramService.getListOfDates());
    }

    @GetMapping("/accounts")
    public ResponseEntity<Map<String, String>> getListOfAccounts(@RequestParam Date date) {
        return ResponseEntity.ok(instagramService.getListOfAccounts("nmf", date));
    }

    @GetMapping("/compare-dates")
    public ResponseEntity<Map<String, String>> getComparisonBetweenDates(
            @RequestParam("dateFollowers") Date dateFollowers,
            @RequestParam("dateFollowing") Date dateFollowing) {
        return ResponseEntity.ok(instagramService.getComparisonBetweenDates(dateFollowers, dateFollowing));
    }

    @DeleteMapping("/accounts")
    public ResponseEntity<Map<String, String>> deleteAccounts(
            @RequestParam("date") Date date,
            @RequestBody List<String> accounts) {
        instagramService.deleteAccounts("nmf", date, accounts);

        return ResponseEntity.accepted().body(Map.of("message", "Deletion started. You will be notified when complete."));
    }

    @PutMapping("/accounts/protect")
    public ResponseEntity<Map<String, String>> protectAccounts(
            @RequestParam("date") Date date,
            @RequestBody List<String> accounts) {
        return ResponseEntity.ok(instagramService.protectAccounts(date, accounts));
    }
}
