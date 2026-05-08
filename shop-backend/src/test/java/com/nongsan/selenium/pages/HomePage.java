package com.nongsan.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:4200";

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(500));
        PageFactory.initElements(driver, this);
    }

    public void navigateToHome() {
        driver.get(BASE_URL + "/home");
        waitForPageReady();
        waitForAngular();
        log("Navigated to home page");
    }

    public void enterSearchKeyword(String keyword) {
        By searchInputLocator = By.id("input-search");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        log("Entered search keyword: " + keyword);
    }

    public void submitSearch() {
        By searchButtonLocator = By.xpath(
            "//form[contains(@class,'ps-form--quick-search')]//button"
        );
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(searchButtonLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
        log("Clicked search button");
        waitForPageReady();
        waitForAngular();
    }

    public void searchAndNavigate(String keyword) {
        enterSearchKeyword(keyword);
        submitSearch();
    }

    private void waitForPageReady() {
        wait.until(webDriver -> {
            Boolean isReady = (Boolean) ((JavascriptExecutor) webDriver)
                .executeScript("return window.ng && document.readyState === 'complete'");
            return isReady != null && isReady;
        });
    }

    private void waitForAngular() {
        try {
            for (int i = 0; i < 30; i++) {
                Boolean isAngularLoaded = (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return typeof angular !== 'undefined' && document.querySelector('app-root[_nghost') !== null");
                if (isAngularLoaded != null && isAngularLoaded) {
                    Thread.sleep(1000);
                    return;
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not wait for Angular: " + e.getMessage());
        }
    }

    private void log(String message) {
        System.out.println("[HomePage] " + message);
    }
}
