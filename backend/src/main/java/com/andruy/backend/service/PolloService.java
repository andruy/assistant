package com.andruy.backend.service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.PushNotification;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class PolloService {
    Logger logger = LoggerFactory.getLogger(PolloService.class);
    @Autowired
    private PushNotificationService pushNotificationService;
    private final String EMAIL = "andruydev@outlook.com";
    private final String FEEDBACK = "Great service!";
    private final String ADDRESS = "https://www.pollolistens.com/";
    private final String MOVING_ON = "Leaving page ";
    private final String NEXT = "#nextPageLink";

    public String pollo(Map<String, String> payload) {
        String serverResponse = "None";
        List<String> body = List.of(
            payload.get("code").substring(0, 4),
            payload.get("code").substring(4, 8),
            payload.get("code").substring(8, 12),
            payload.get("code").substring(12, 16),
            payload.get("meal"),
            payload.get("visit")
        );

        logger.trace("Pollo code: " + payload.get("code"));
        logger.trace("Pollo meal: " + payload.get("meal"));
        logger.trace("Pollo visit: " + payload.get("visit"));

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        Page page = browser.newPage();

        try {
            String response = "None";
            int pageNumber = 0;
            int iterator = 0;

            page.navigate(ADDRESS);

            // Select language
            page.click("//label[@for='option_1568944_667416']");
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Enter code
            page.fill("#promptInput_665451_0", body.get(iterator++));
            page.fill("#promptInput_665451_1", body.get(iterator++));
            page.fill("#promptInput_665451_2", body.get(iterator++));
            page.fill("#promptInput_665451_3", body.get(iterator++));
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Enter email
            page.fill("#promptInput_909664", EMAIL);
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Rate visit
            page.locator(".rating").last().click();
            page.fill("#commentArea_658912", FEEDBACK);
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Select meal
            page.click("//*[@id=\"prompt_670287\"]/label/div");
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // More ratings
            page.click("//label[@for='option_1576789_670275']");
            page.click("//label[@for='option_1576799_670277']");
            page.click("//label[@for='option_1576784_670274']");
            page.click("//label[@for='option_1576794_670276']");
            page.click("//label[@for='option_1576804_670278']");
            page.click("//label[@for='option_1576809_670280']");
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Visit type
            page.click(body.get(iterator).contains("Dine") ? "//label[@for='option_1547526_658917']" : "//label[@for='option_1547528_658917']");
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Age and gender
            page.click("//label[@for='option_1576812_670283']");
            page.click("//label[@for='option_1576818_670286']");
            page.click(NEXT);
            logger.trace(MOVING_ON + pageNumber++);

            // Redeem
            response = page.locator("//*[@id=\"promptText_658911\"]/div/span/span/label/div/div/div[4]").textContent();
            if (pushNotificationService.push(new PushNotification("Pollo reward code", response)) == 200) {
                logger.trace("Returned reward code: " + response);
            }

            serverResponse = "Processed";
        } catch (Exception e) {
            String fileName = LocalDateTime.now().toString().replace(":", "").substring(0, 15) + ".png";
            page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(Paths.get("screenshots/" + fileName))
                        .setFullPage(true)
            );

            logger.error(e.getMessage());
            logger.debug("Saved screenshot " + fileName);
            serverResponse = "Error";
        } finally {
            browser.close();
            playwright.close();
        }

        return serverResponse;
    }
}
