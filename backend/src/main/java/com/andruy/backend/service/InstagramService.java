package com.andruy.backend.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.andruy.backend.repository.InstagramRepository;
import com.andruy.backend.model.PushNotification;
import com.andruy.backend.util.TimeTracker;

@Service
public class InstagramService {
    @Autowired
    private InstagramRepository instagramRepository;
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private TimeTracker timeTracker;
    Logger logger = LoggerFactory.getLogger(InstagramService.class);
    private boolean secondIteration = false;
    private List<String> followersList;
    private List<String> followingList;
    private final String ADDRESS = "https://www.instagram.com/";
    private final int LONG_HALT = 15000;
    private final int SHORT_HALT = 3000;
    private final int MID_HALT = 7000;
    private List<WebElement> elements;
    private String response = "";
    private WebElement element;
    private WebDriver driver;
    private double totalTime;
    private Actions actions;
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
        logger.warn("Starting comparison process");
        startTime = System.currentTimeMillis();
        date = new Date(startTime);
        getList("followers", true);
        if (secondIteration) {
            getList("following", false);
        }
        compareThem(followersList, followingList);
        return CompletableFuture.completedFuture(null);
    }

    public void getList(String target, boolean comparison) {
        try {
            if (secondIteration) {
                element = driver.findElement(By.className("_abm0"));
                printClasses(element);
                actions = new Actions(driver);
                actions.moveToElement(element).click().perform();
                Thread.sleep(MID_HALT);
            } else {
                accountLogin();
                element = driver.findElement(By.cssSelector("a[href='/andruy/?next=%2F']"));
                actions = new Actions(driver);
                actions.moveToElement(element).click().perform();
                Thread.sleep(MID_HALT);
            }

            element = driver.findElement(By.cssSelector("a[href='/andruy/" + target + "/?next=%2F"));
            int followInt = Integer.parseInt(element.findElement(By.xpath("./*[1]"))
                                                    .findElement(By.xpath("./*[1]"))
                                                    .getText().replaceFirst(",", ""));

            String filler = target.equals("followers") ? followInt + " " + target : target + " " + followInt;
            logger.trace("Instagram's original counter shows " + filler);
            List<String> resultList = new ArrayList<>(followInt + 100);
            actions = new Actions(driver);
            actions.moveToElement(element).click().perform();
            Thread.sleep(MID_HALT);

            WebElement scrollingElement = driver.findElement(By.cssSelector("input[placeholder='Search']"))
                                                .findElement(By.xpath(".."))
                                                .findElement(By.xpath(".."))
                                                .findElement(By.xpath("./following-sibling::div[1]"));

            WebElement listingElement = scrollingElement.findElement(By.xpath("./*[1]"))
                                                        .findElement(By.xpath("./*[1]"));

            // Scroll down to load lazy-loaded content
            JavascriptExecutor js = (JavascriptExecutor) driver;
            int initialChildCount = listingElement.findElements(By.xpath("./*")).size();
            long lastHeight = (long) js.executeScript("return arguments[0].scrollHeight;", scrollingElement);
            long newHeight = 0;
            int scrollingIterations = 0;
            boolean contentLoaded = true;
            int i = 0;

            while (contentLoaded) {
                // Process the loaded content
                elements = listingElement.findElements(By.xpath("./div"));
                while (i < initialChildCount) {
                    resultList.add(
                        elements.get(i).findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./following-sibling::div[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .findElement(By.xpath("./*[1]"))
                                        .getText()
                    );
                    i++;
                }

                // Scroll to the bottom of the div
                js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollingElement);
                Thread.sleep(SHORT_HALT); // Wait for content to load

                // Check if new elements are loaded by counting the child elements
                int newChildCount = listingElement.findElements(By.xpath("./*")).size();

                // Get the current scroll height of the div
                newHeight = (long) js.executeScript("return arguments[0].scrollHeight;", scrollingElement);

                // Check if new content is loaded or if we've reached the end of the div
                if (newChildCount > initialChildCount) {
                    initialChildCount = newChildCount; // Update the count of loaded elements
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
            Thread.sleep(SHORT_HALT);

            // Store the list to database
            int updatedRecords = 0;
            for (String s : resultList) {
                updatedRecords += instagramRepository.saveUser(target.toUpperCase(), s, date);
            }
            logger.trace("Inserted " + updatedRecords + " records to IG_" + target.toUpperCase() + " table");

            if (target.equals("followers")) {
                followersList = resultList;
            } else {
                followingList = resultList;
            }

            response = target.equals("followers") ? "You have " + resultList.size() + " " + target : resultList.size() + " are " + target + " you";
            logger.trace(response);

            if (comparison) {
                secondIteration = true;
            } else {
                driver.close();
                driver.quit();
            }
        } catch (Exception e) {
            if (secondIteration) {
                secondIteration = false;
            }

            response = e.getMessage();
            logger.error(response);
            driver.close();
            driver.quit();
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
                updatedRecords += instagramRepository.saveUser(target.toUpperCase(), s, date);
            }
            logger.trace("Inserted " + updatedRecords + " records to IG_" + target.toUpperCase() + " table");
            totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
            logger.trace("Total elapsed time: " + totalTime + " minutes");
        }
    }

    public List<Date> getListOfDates() {
        List <Date> dateList = instagramRepository.getDates();
        Collections.sort(dateList, Collections.reverseOrder());

        return dateList;
    }

    public Map<String, String> getListOfAccounts(String suffix, Date date) {
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
                driver.get(map.get(s));
                Thread.sleep(MID_HALT);
                element = driver.findElement(By.cssSelector("._ap3a._aaco._aacw._aad6._aade"));

                if (element.getText().equals("Following")) {
                    actions = new Actions(driver);
                    actions.moveToElement(element).click().perform();
                    Thread.sleep(SHORT_HALT);
                    elements = driver.findElements(By.cssSelector(".x1i10hfl.x1qjc9v5.xjbqb8w.xjqpnuy.xa49m3k.xqeqjp1.x2hbi6w.x13fuv20.xu3j5b3.x1q0q8m5.x26u7qi.x972fbf.xcfux6l.x1qhh985.xm0m39n.x9f619.x1ypdohk.xdl72j9.x2lah0s.xe8uvvx.xdj266r.x11i5rnm.xat24cr.x1mh8g0r.x2lwn1j.xeuugli.xexx8yu.x4uap5.x18d9i69.xkhd6sd.x1n2onr6.x16tdsg8.x1hl2dhg.xggy1nq.x1ja2u2z.x1t137rt.x1q0g3np.x87ps6o.x1lku1pv.x1a2a7pz.x1dm5mii.x16mil14.xiojian.x1yutycm.x1lliihq.x193iq5w.xh8yej3"));
                    actions = new Actions(driver);
                    actions.moveToElement(elements.get(elements.size() - 1)).click().perform();
                    Thread.sleep(SHORT_HALT);
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                    logger.trace("Deleted " + s);
                } else if (element.getText().equals("Follow")) {
                    logger.trace("You were not following " + s + " anymore");
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                } else if (element.getText().equals("Requested")) {
                    actions = new Actions(driver);
                    actions.moveToElement(element).click().perform();
                    Thread.sleep(SHORT_HALT);
                    logger.trace("Had requested to follow " + s + " and it has been reverted");
                    map.remove(s);
                    listOfDeletedAccounts.add(s);
                }
            } catch (Exception e) {
                logger.error("Error deleting " + s + "\n" + e.getMessage());
                driver.close();
                driver.quit();
            }
        }

        driver.close();
        driver.quit();

        int updatedRecords = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            updatedRecords += instagramRepository.saveUser(suffix, entry.getKey(), date);
        }

        logger.trace("Deleted " + listOfDeletedAccounts.size() + " accounts and now there are " + updatedRecords + " records left in IG_" + suffix);
        totalTime = timeTracker.getTotalMinutes(System.currentTimeMillis(), startTime);
        logger.trace("Total elapsed time: " + totalTime + " minutes");
        int status = pushNotificationService.push(new PushNotification("Completed deletion", "Deleted " + listOfDeletedAccounts.size() + " accounts"));
        logger.trace("Push notification status: " + status);

        return CompletableFuture.completedFuture(null);
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
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            driver = new ChromeDriver(options);
            driver.get(ADDRESS);
            Thread.sleep(MID_HALT);

            driver.findElement(By.name("username")).sendKeys(username);
            driver.findElement(By.name("password")).sendKeys(password);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(LONG_HALT);
        } catch (Exception e) {
            logger.error("Error logging in\n" + e.getMessage());
        }
    }

    private void printClasses(WebElement e) {
        String classAttribute = e.getAttribute("class");
        String[] classes = classAttribute.split("\\s+");
        System.out.println("Classes for child " + e);
        for (String className : classes) {
            System.out.println(className);
        }
    }
}
