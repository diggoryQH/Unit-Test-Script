package com.nongsan.selenium.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.Connection;
import java.sql.SQLException;

public class DriverManager {

    private static WebDriver driver;
    private static final String BASE_URL = "http://localhost:4200";

    // Database connection settings
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nongsan";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    public static WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-extensions");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static WebDriver getDriverWithProfile() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--user-data-dir=C:/temp/chrome-profile");

            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * Get a database connection for rollback/cleanup operations.
     * Call this method to get a Connection object for direct database access.
     * 
     * @return Connection to the nongsan database
     * @throws SQLException if unable to connect
     */
    public static Connection getDataSourceConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
