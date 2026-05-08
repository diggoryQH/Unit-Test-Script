package com.nongsan.selenium.base;

import com.nongsan.selenium.utils.DriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseTest {

    protected static WebDriver driver;

    private static final String REPORT_PATH = "target/selenium-reports/";
    private static final String SCREENSHOT_PATH = REPORT_PATH + "screenshots/";
    private static int testCounter = 0;
    private static String currentTestName = "";

    @BeforeAll
    public static void beforeAll() {
        createDirectories();
        System.out.println("=".repeat(80));
        System.out.println("SELENIUM TEST SUITE - Starting");
        System.out.println("=".repeat(80));
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("=".repeat(80));
        System.out.println("SELENIUM TEST SUITE - Completed");
        System.out.println("=".repeat(80));
    }

    @BeforeEach
    public void setup() {
        driver = DriverManager.getDriver();
        driver.manage().deleteAllCookies();
        driver.get(DriverManager.getBaseUrl());
        testCounter++;
        System.out.println("\n" + "=".repeat(60));
        System.out.println("[TEST #" + testCounter + "] Starting test...");
        System.out.println("=".repeat(60));
    }

    @AfterEach
    public void tearDown() {
        // Không quit driver để giữ browser mở giữa các tests
        System.out.println("-".repeat(60));
    }

    protected void startTest(String testName) {
        currentTestName = testName;
        System.out.println("\n>>> " + testName + " <<<");
    }

    protected void startTest(DisplayName displayName) {
        currentTestName = displayName.value();
        System.out.println("\n>>> " + currentTestName + " <<<");
    }

    protected void endTest() {
        System.out.println(">>> END TEST: " + currentTestName + " <<<\n");
    }

    protected void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    protected void logPass(String message) {
        System.out.println("[PASS] " + message);
    }

    protected void logFail(String message) {
        System.err.println("[FAIL] " + message);
    }

    protected void logError(String message, Throwable throwable) {
        System.err.println("[ERROR] " + message);
        throwable.printStackTrace();
    }

    protected String captureScreenshot(String screenshotName) {
        try {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File source = takesScreenshot.getScreenshotAs(OutputType.FILE);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = screenshotName + "_" + timestamp + ".png";
            Path destination = Paths.get(SCREENSHOT_PATH + fileName);

            Files.createDirectories(destination.getParent());
            Files.copy(source.toPath(), destination);

            System.out.println("[SCREENSHOT] Saved: " + destination.toString());
            return destination.toString();
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }

    protected void assertTrue(boolean condition, String message) {
        if (!condition) {
            captureScreenshot("AssertionFailed_" + currentTestName);
            throw new AssertionError(message);
        }
    }

    protected void assertFalse(boolean condition, String message) {
        if (condition) {
            captureScreenshot("AssertionFailed_" + currentTestName);
            throw new AssertionError(message);
        }
    }

    protected void assertEquals(Object actual, Object expected, String message) {
        if (!actual.equals(expected)) {
            captureScreenshot("AssertionFailed_" + currentTestName);
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(REPORT_PATH));
            Files.createDirectories(Paths.get(SCREENSHOT_PATH));
        } catch (IOException e) {
            System.err.println("Failed to create directories: " + e.getMessage());
        }
    }

    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void waitForPageReady() {
        waitFor(1);
    }

    protected void waitForAngular() {
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

    protected WebDriver getDriver() {
        return driver;
    }
}
