package com.andruy.backend.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.PushNotification;
import com.andruy.backend.repository.InstagramRepository;
import com.andruy.backend.util.TimeTracker;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

@Service
public class InstagramService {
    @Autowired
    private InstagramRepository instagramRepository;
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private TimeTracker timeTracker;
    private Logger logger = LoggerFactory.getLogger(InstagramService.class);
    private boolean secondIteration = false;
    private List<String> followersList;
    private List<String> followingList;
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private final String ADDRESS = "https://www.instagram.com/";
    private final int SHORT_HALT = 3000;
    private String response = "";
    private double totalTime;
    private long startTime;
    private Date date;
    @Value("${my.ig.username}")
    private String username;
    @Value("${my.ig.password}")
    private String password;

    public Map<String, String> getFollowers() {
        logger.trace("Getting followers only");
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        getList("followers", false);
        return Map.of("message", response);
    }

    public Map<String, String> getFollowing() {
        logger.trace("Getting following only");
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        getList("following", false);
        return Map.of("message", response);
    }

    @Async
    public CompletableFuture<Void> getComparison() {
        logger.trace("New comparison process");
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        getList("followers", true);
        if (secondIteration) {
            getList("following", false);
        }
        compareThem(followersList, followingList);
        return CompletableFuture.completedFuture(null);
    }

    private void getList(String target, boolean comparison) {
        try {
            if (secondIteration) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
            } else {
                accountLogin();
            }

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(username + "'s profile picture Profile")).click();

            String filler = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(target)).innerText();

            logger.trace("Starting " + target + " retrieval");
            logger.trace("Instagram's original counter shows " + filler);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(target)).click();
            Thread.sleep(SHORT_HALT);

            Locator scrollingElement = page.locator("xpath=/html/body/div[4]/div[2]/div/div/div[1]/div/div[2]/div/div/div/div/div[2]/div/div/div[3]");
            Locator listingElement = page.locator("xpath=/html/body/div[4]/div[2]/div/div/div[1]/div/div[2]/div/div/div/div/div[2]/div/div/div[3]/div[1]/div");

            // Scroll down to load lazy-loaded content
            int initialChildCount = listingElement.locator("xpath=./*").count();
            int lastHeight = (int) scrollingElement.evaluate("element => element.scrollHeight");
            int newHeight = 0;
            int scrollingIterations = 0;
            boolean contentLoaded = true;

            while (contentLoaded) {
                // Scroll to the bottom of the div
                scrollingElement.evaluate("e => e.scrollTop = e.scrollHeight");
                Thread.sleep(SHORT_HALT); // Wait for content to load

                // Check if new elements are loaded by counting the child elements
                int newChildCount = listingElement.locator("xpath=./*").count();

                // Get the current scroll height of the div
                newHeight = (int) scrollingElement.evaluate("element => element.scrollHeight");

                // Check if new content is loaded or if we've reached the end of the div
                if (newChildCount > initialChildCount) {
                    // Update the count of loaded elements
                    initialChildCount = newChildCount;
                    logger.trace("New content loaded, child count: " + newChildCount);

                    if (scrollingIterations > 0) {
                        scrollingIterations = 0;
                    }
                } else if (newHeight == lastHeight) {
                    scrollingIterations++;
                    logger.warn("Iteration number " + scrollingIterations);

                    if (scrollingIterations >= 10) {
                        contentLoaded = false; // No more content is being loaded
                        logger.warn("Reached the end of lazy-loaded content.");
                    }
                }

                // Update last height for comparison in the next iteration
                lastHeight = newHeight;
            }

            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
            logger.trace("Total elapsed time retrieving accounts: " + totalTime + " minutes");

            // Process the loaded content
            long newStartTime = System.currentTimeMillis();

            List<Locator> elements = listingElement.locator("xpath=./div").all();

            Set<String> resultList = new HashSet<>();

            for (Locator element : elements) {
                try {
                    String alt = element.locator("a").first().getAttribute("href");
                    int idx = alt.indexOf('?');
                    resultList.add(alt.substring(1, idx - 1));
                } catch (Exception e) {
                    logger.warn("Encountered an element with no name?\n" + element.innerHTML() + "\n" + e.getMessage());
                }
            }

            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), newStartTime);
            logger.trace("Total elapsed time adding the names to the list: " + totalTime + " minutes");

            // Store the list to database
            int updatedRecords = 0;
            for (String s : resultList) {
                updatedRecords += instagramRepository.saveUser(target, s, date);
            }

            logger.trace("Inserted " + updatedRecords + " records to ig_" + target + " table");

            if (target.equals("followers")) {
                followersList = resultList.stream().toList();
            } else {
                followingList = resultList.stream().toList();
            }

            response = target.equals("followers") ? "You have " + resultList.size() + " " + target : resultList.size() + " are " + target + " you";
            logger.trace(response);
            logger.trace("Expected: " + filler);
            logger.trace("Completed " + target + " retrieval");
            logger.trace("###");

            if (comparison) {
                secondIteration = true;
            } else {
                browser.close();
                playwright.close();
            }
        } catch (Exception e) {
            if (secondIteration) {
                secondIteration = false;
            }

            response = e.getMessage();
            logger.error(response);
            browser.close();
            playwright.close();
        }
    }

    private void compareThem(List<String> followers, List<String> following) {
        if (secondIteration) {
            secondIteration = false;
        }

        if (followers.size() > 0 && following.size() > 0) {
            List<String> result = following.stream()
                           .filter(e -> !followers.contains(e))
                           .collect(Collectors.toList());

            response = result.size() + " are not your followers";
            logger.trace(response);

            String target = "nmf";

            // Store the list to database
            int updatedRecords = 0;
            for (String s : result) {
                updatedRecords += instagramRepository.saveUser(target, s, date == null ? new Date(System.currentTimeMillis()) : date);
            }
            logger.trace("Inserted " + updatedRecords + " records to ig_" + target + " table");
            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
            logger.trace("Total elapsed time: " + totalTime + " minutes");
        }
    }

    public List<Date> getListOfDates() {
        logger.trace("Called to retrieve list of dates");
        List <Date> dateList = instagramRepository.getDates();
        Collections.sort(dateList, Collections.reverseOrder());

        return dateList;
    }

    public Map<String, String> getListOfAccounts(String suffix, Date date) {
        logger.trace("Called to retrieve list of accounts with suffix [" + suffix + "] and date " + date.toString());
        List <String> list = instagramRepository.getUsers(suffix, date);

        Map<String, String> map = new HashMap<>();
        for (String s : list) {
            map.put(s, convertToLink(s));
        }

        return new TreeMap<>(map);
    }

    @Async
    public CompletableFuture<Void> deleteAccounts(String suffix, Date oldDate, List<String> list) {
        logger.trace("Starting to delete accounts dating back to " + oldDate.toString());
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        List<String> listOfDeletedAccounts = new ArrayList<>();
        Map<String, String> map = getListOfAccounts(suffix, oldDate);

        accountLogin();

        for (String s : list) {
            try {
                page.navigate(s);

                if (page.locator("header").innerText().contains("Following")) {
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Following Down chevron icon")).click();
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Unfollow")).click();
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                    logger.trace("Deleted " + s);
                } else if (page.locator("header").innerText().contains("Requested")) {
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Requested")).click();
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                    logger.trace("Had requested to follow " + s + " and it has been reverted");
                } else {
                    logger.trace("You were not following " + s + " anymore");
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                }
            } catch (Exception e) {
                logger.error("Error deleting " + s + "\n" + e.getMessage());
                browser.close();
                playwright.close();
            }
        }

        browser.close();
        playwright.close();

        int updatedRecords = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            updatedRecords += instagramRepository.saveUser(suffix, entry.getKey(), date);
        }

        logger.trace("Deleted " + listOfDeletedAccounts.size() + " accounts and now there are " + updatedRecords + " records left in ig_" + suffix);
        totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
        logger.trace("Total elapsed time: " + totalTime + " minutes");
        int status = pushNotificationService.push(new PushNotification("Completed deletion", "Deleted " + listOfDeletedAccounts.size() + " accounts"));
        logger.trace("Push notification status: " + status);

        return CompletableFuture.completedFuture(null);
    }

    public Map<String, String> getComparisonBetweenDates(Date dateFollowers, Date dateFollowing) {
        List<String> followers = instagramRepository.getUsers("followers", dateFollowers);
        List<String> following = instagramRepository.getUsers("following", dateFollowing);

        compareThem(followers, following);

        return Map.of("report", "You may now check the list of accounts that do not follow you back");
    }

    public Map<String, String> protectAccounts(Date date, List<String> list) {
        logger.trace("Will protect accounts dating back to " + date.toString());
        int updatedRecords = 0;
        for (String s : list) {
            updatedRecords += instagramRepository.protectAccount(s, date);
        }
        response = "Protected " + updatedRecords + " accounts";
        int status = pushNotificationService.push(new PushNotification("Process completed", response));
        logger.trace(response);
        logger.trace("Push notification status: " + status);

        return Map.of("report", response);
    }

    private String convertToLink(String str) {
        return ADDRESS + str + "/";
    }

    private void accountLogin() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext context = browser.newContext();
            page = context.newPage();
            page.navigate(ADDRESS);
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Phone number, username, or")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Phone number, username, or")).fill(username);
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in").setExact(true)).click();
        } catch (Exception e) {
            logger.error("Error logging in\n" + e.getMessage());
            browser.close();
            playwright.close();
        }
    }
}
