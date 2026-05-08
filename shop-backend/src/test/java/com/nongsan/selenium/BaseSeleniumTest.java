package com.nongsan.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.fail;

public class BaseSeleniumTest {

    protected WebDriver driver;
    protected Connection dbConnection;

    // Default local URLs based on standard Angular / Spring Boot setup
    protected final String FRONTEND_URL = "http://localhost:4200"; // Client Shop


    @BeforeEach
    public void setUp() {
        // Setup Chrome Driver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, java.util.concurrent.TimeUnit.SECONDS);

        // Setup JDBC Connection
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/nongsan", "root", "123456");
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database: " + e.getMessage());
            // Throw exception or handle depending on requirement
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Utility Methods for Rollback ---

    /**
     * Deletes an order and its details directly via JDBC to rollback test data
     */
    protected void deleteOrderById(Long orderId) {
        if (dbConnection == null || orderId == null)
            return;
        try {
            // Delete OrderDetails first due to foreign key constraints
            String deleteDetailsQuery = "DELETE FROM order_details WHERE order_id = ?";
            try (PreparedStatement pstmt1 = dbConnection.prepareStatement(deleteDetailsQuery)) {
                pstmt1.setLong(1, orderId);
                pstmt1.executeUpdate();
            }

            // Then delete the Order
            String deleteOrderQuery = "DELETE FROM orders WHERE orders_id = ?";
            try (PreparedStatement pstmt2 = dbConnection.prepareStatement(deleteOrderQuery)) {
                pstmt2.setLong(1, orderId);
                pstmt2.executeUpdate();
            }

            // Also clean up any returns associated with the order if they exist
            String deleteReturnQuery = "DELETE FROM order_returns WHERE order_id = ?";
            try (PreparedStatement pstmt3 = dbConnection.prepareStatement(deleteReturnQuery)) {
                pstmt3.setLong(1, orderId);
                pstmt3.executeUpdate();
            }

            System.out.println("Rollback complete for order ID: " + orderId);
        } catch (SQLException e) {
            System.err.println("Failed to rollback order " + orderId + ": " + e.getMessage());
        }
    }

    /**
     * Reverts order status for tests that update it (DH_ group, XL_ group)
     */
    protected void revertOrderStatus(Long orderId, int originalStatus) {
        if (dbConnection == null || orderId == null)
            return;
        try {
            String updateQuery = "UPDATE orders SET status = ? WHERE orders_id = ?";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(updateQuery)) {
                pstmt.setInt(1, originalStatus);
                pstmt.setLong(2, orderId);
                pstmt.executeUpdate();
            }
            System.out.println("Rollback status complete for order ID: " + orderId);
        } catch (SQLException e) {
            System.err.println("Failed to revert status for order " + orderId + ": " + e.getMessage());
        }
    }
    // --- Common Login Methods ---

    public void loginClient() {
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver,
                java.time.Duration.ofSeconds(15));

        try {
            // Start from Home page
            System.out.println("DEBUG: Navigating to HOME page: " + FRONTEND_URL);
            driver.get(FRONTEND_URL);
            Thread.sleep(2000);

            // Find and click 'Đăng nhập & Đăng ký' link in the navigation bar specifically
            // Using a more specific selector to avoid the hidden dropdown link
            org.openqa.selenium.WebElement loginLink = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                    org.openqa.selenium.By.cssSelector(".navigation a[href='/sign-form']")));
            loginLink.click();
            
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/sign-form"));
            Thread.sleep(2000);

            // Use a more flexible XPath to find the input fields that works with Angular's case sensitivity
            org.openqa.selenium.WebElement emailInput = wait
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                            org.openqa.selenium.By.xpath("//input[contains(@formcontrolname,'email') or contains(@formControlName,'email')]")));
            emailInput.clear();
            emailInput.sendKeys("nguyenkhaihung1512004nb@gmail.com");

            org.openqa.selenium.WebElement passInput = driver
                    .findElement(org.openqa.selenium.By.xpath("//input[contains(@formcontrolname,'password') or contains(@formControlName,'password')]"));
            passInput.clear();
            passInput.sendKeys("hung123456");

            // Click the submit button
            org.openqa.selenium.WebElement loginBtn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(
                    org.openqa.selenium.By.cssSelector("button.ps-btn.ps-btn--fullwidth")));
            loginBtn.click();

            // Wait for redirect back to home (logged in state)
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                    org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/home"),
                    org.openqa.selenium.support.ui.ExpectedConditions.urlToBe(FRONTEND_URL + "/")));
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Client Login failed: " + e.getMessage());
        }
    }


}
