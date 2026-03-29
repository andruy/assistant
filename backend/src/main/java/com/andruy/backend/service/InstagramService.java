package com.andruy.backend.service;

import java.sql.Timestamp;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private Timestamp date;
    private Path screenshotRunDir;
    @Value("${my.ig.username}")
    private String username;
    @Value("${my.ig.password}")
    private String password;

    @Async
    public CompletableFuture<Void> getFollowers() {
        logger.trace("Getting followers only");
        startTime = System.currentTimeMillis();
        date = new Timestamp(startTime);
        initScreenshotDir();
        getList("followers", false);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> getFollowing() {
        logger.trace("Getting following only");
        startTime = System.currentTimeMillis();
        date = new Timestamp(startTime);
        initScreenshotDir();
        getList("following", false);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> getComparison() {
        logger.trace("Starting comparison process");
        startTime = System.currentTimeMillis();
        date = new Timestamp(startTime);
        initScreenshotDir();
        try {
            getList("followers", true);
        } catch (Exception e) {
            logger.error("Followers retrieval failed", e);
        }

        try {
            if (secondIteration) {
                getList("following", false);
            } else {
                // Followers failed or browser was closed — start fresh for following
                getList("following", false);
            }
        } catch (Exception e) {
            logger.error("Following retrieval failed", e);
        }

        logger.trace("Calling compareThem with followersList={}, followingList={}",
                followersList == null ? "null" : followersList.size(),
                followingList == null ? "null" : followingList.size());
        compareThem(followersList, followingList);
        double elapsed = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
        logger.trace("Comparison process completed in {} minutes", elapsed);
        return CompletableFuture.completedFuture(null);
    }

    private void getList(String target, boolean comparison) {
        try {
            if (secondIteration) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
                takeScreenshot(target + "-after-close");
                // Already on profile page after closing dialog, no need to navigate again
            } else {
                accountLogin();
                takeScreenshot(target + "-after-login");
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(username + "'s profile picture")).first().click();
                takeScreenshot(target + "-profile");
            }

            String filler = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(target)).innerText();

            logger.trace("Starting " + target + " retrieval");
            logger.trace("Instagram's original counter shows " + filler);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(target)).click();
            Thread.sleep(SHORT_HALT);

            // Wait for actual content to load (not skeleton placeholders)
            page.locator("div[role='dialog'] a[href]").first().waitFor(
                    new Locator.WaitForOptions().setTimeout(15000));
            Thread.sleep(1000); // Let React finish rendering
            takeScreenshot(target + "-dialog-open");

            // Dynamically find the scrollable container and list inside the dialog
            page.evaluate("() => {"
                    + "const dialog = document.querySelector('div[role=\"dialog\"]');"
                    + "if (!dialog) return;"
                    + "const divs = dialog.querySelectorAll('div');"
                    + "for (const div of divs) {"
                    + "  if (div.scrollHeight > div.clientHeight + 10) {"
                    + "    div.setAttribute('data-ig-scrollable', 'true');"
                    + "    const findList = (el) => {"
                    + "      if (el.children.length > 5) return el;"
                    + "      for (const c of el.children) { const r = findList(c); if (r) return r; }"
                    + "      return null;"
                    + "    };"
                    + "    const list = findList(div);"
                    + "    if (list) list.setAttribute('data-ig-list', 'true');"
                    + "    break;"
                    + "  }"
                    + "}"
                    + "}");
            Locator scrollingElement = page.locator("[data-ig-scrollable='true']");
            Locator listingElement = page.locator("[data-ig-list='true']");

            // Scroll down to load lazy-loaded content
            int initialChildCount = listingElement.locator("xpath=./*").count();
            logger.trace("Initial child count: " + initialChildCount);
            int lastHeight = (int) scrollingElement.evaluate("element => element.scrollHeight");
            logger.trace("Initial scroll height: " + lastHeight);
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

                    if (scrollingIterations >= 15) {
                        contentLoaded = false; // No more content is being loaded
                        logger.warn("Reached the end of lazy-loaded content.");
                    }
                }

                // Update last height for comparison in the next iteration
                lastHeight = newHeight;
            }

            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
            logger.trace("Total elapsed time retrieving accounts: " + totalTime + " minutes");
            takeScreenshot(target + "-after-scroll");

            // Process the loaded content
            long newStartTime = System.currentTimeMillis();

            List<Locator> elements = listingElement.locator("xpath=./div").all();
            logger.trace("Found " + elements.size() + " div elements to parse");

            Set<String> resultList = new HashSet<>();

            try {
                for (Locator element : elements) {
                    String href = element.locator("a").first().getAttribute("href");
                    // href format: /username/ — strip leading and trailing slashes
                    String username = href.replaceAll("^/|/$", "");
                    if (!username.isEmpty()) {
                        resultList.add(username);
                    }
                }
            } catch (Exception e) {
                logger.error("Name parsing failed — saving raw HTML fallback", e);
                saveFallbackFile(target, listingElement.innerHTML());
            }

            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), newStartTime);
            logger.trace("Total elapsed time adding the names to the list: " + totalTime + " minutes");

            // Store the list to database
            int updatedRecords = instagramRepository.saveUsers(target, resultList, date);
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
            takeScreenshot(target + "-error");
            logger.error("Error in getList for target '{}': {}", target, response, e);
            browser.close();
            playwright.close();
        }
    }

    private void compareThem(List<String> followers, List<String> following) {
        logger.trace("compareThem called — followers: {}, following: {}",
                followers == null ? "null" : followers.size(),
                following == null ? "null" : following.size());

        if (secondIteration) {
            secondIteration = false;
        }

        if (followers == null || following == null || followers.isEmpty() || following.isEmpty()) {
            logger.warn("compareThem skipped — one or both lists are null/empty");
            return;
        }

        if (followers.size() > 0 && following.size() > 0) {
            List<String> result = following.stream()
                           .filter(e -> !followers.contains(e))
                           .collect(Collectors.toList());

            response = result.size() + " are not your followers";
            logger.trace(response);

            String target = "nmf";

            // Store the list to database
            Timestamp saveDate = date == null ? new Timestamp(System.currentTimeMillis()) : date;
            int updatedRecords = instagramRepository.saveUsers(target, result, saveDate);
            logger.trace("Inserted " + updatedRecords + " records to ig_" + target + " table");
            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
            logger.trace("Total elapsed time: " + totalTime + " minutes");
        }
    }

    public List<Timestamp> getListOfDates() {
        logger.trace("Called to retrieve list of dates");
        List<Timestamp> dateList = instagramRepository.getTimestamps();
        Collections.sort(dateList, Collections.reverseOrder());

        return dateList;
    }

    public List<Timestamp> getListOfDates(String suffix) {
        logger.trace("Called to retrieve list of dates for ig_{}", suffix);
        return instagramRepository.getTimestamps(suffix);
    }

    public Map<String, String> getListOfAccounts(String suffix, Timestamp date) {
        logger.trace("Called to retrieve list of accounts with suffix [" + suffix + "] and date " + date.toString());
        List <String> list = instagramRepository.getUsers(suffix, date);

        Map<String, String> map = new HashMap<>();
        for (String s : list) {
            map.put(s, convertToLink(s));
        }

        return new TreeMap<>(map);
    }

    @Async
    public CompletableFuture<Void> deleteAccounts(String suffix, Timestamp oldDate, List<String> list) {
        logger.trace("Starting to delete accounts dating back to " + oldDate.toString());
        startTime = System.currentTimeMillis();
        date = new Timestamp(startTime);
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

    public Map<String, String> getComparisonBetweenDates(Timestamp dateFollowers, Timestamp dateFollowing) {
        startTime = System.currentTimeMillis();
        date = new Timestamp(startTime);
        List<String> followers = instagramRepository.getUsers("followers", dateFollowers);
        List<String> following = instagramRepository.getUsers("following", dateFollowing);

        compareThem(followers, following);

        return Map.of("report", "You may now check the list of accounts that do not follow you back");
    }

    public Map<String, String> protectAccounts(Timestamp date, List<String> list) {
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

    private void takeScreenshot(String label) {
        try {
            Files.createDirectories(screenshotRunDir);
            String filename = label + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshotRunDir.resolve(filename)).setFullPage(true));
            logger.trace("Screenshot saved: {}/{}", screenshotRunDir.getFileName(), filename);
        } catch (Exception e) {
            logger.warn("Failed to take screenshot '{}': {}", label, e.getMessage());
        }
    }

    private String convertToLink(String str) {
        return ADDRESS + str + "/";
    }

    private void initScreenshotDir() {
        String runName = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(startTime));
        screenshotRunDir = Paths.get("screenshots", runName);
    }

    private void saveFallbackFile(String target, String rawHtml) {
        try {
            Path fallbackDir = Paths.get("fallback");
            Files.createDirectories(fallbackDir);
            String filename = target + "_" + date.getTime() + ".html";
            Files.writeString(fallbackDir.resolve(filename), rawHtml);
            logger.trace("Fallback file saved: fallback/{}", filename);
        } catch (Exception e) {
            logger.error("Failed to write fallback file for {}", target, e);
        }
    }

    private void accountLogin() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of("--disable-blink-features=AutomationControlled")));

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"));
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false });");
            page = context.newPage();
            page.navigate(ADDRESS);
            takeScreenshot("after-navigate");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Mobile number, username or email")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Mobile number, username or email")).fill(username);
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
            takeScreenshot("after-fill");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).first().click();
            takeScreenshot("after-login");

            // Dismiss "Save your login info?" dialog
            try {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Not now")).click(new Locator.ClickOptions().setTimeout(10000));
                takeScreenshot("after-save-info-dismiss");
            } catch (Exception e) {
                logger.trace("No 'Save login info' dialog appeared");
            }

            // Dismiss "Turn on Notifications" dialog
            try {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Not Now")).click(new Locator.ClickOptions().setTimeout(5000));
                takeScreenshot("after-notifications-dismiss");
            } catch (Exception e) {
                logger.trace("No 'Turn on Notifications' dialog appeared");
            }

            logger.trace("Successfully logged into Instagram");
        } catch (Exception e) {
            takeScreenshot("error");
            logger.error("Error logging in", e);
            browser.close();
            playwright.close();
            throw e;
        }
    }
}
