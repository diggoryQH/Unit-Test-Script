package com.nongsan.selenium.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.nongsan.selenium.helper.ExcelReporter;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * BaseTest chua cac thiet lap chung cho WebDriver.
 */
public abstract class BaseTest {

    protected static WebDriver driver;
    protected static Properties props = new Properties();

    @BeforeAll
    public static void setUpSuite() {
        // Load config
        try (InputStream in = BaseTest.class.getResourceAsStream("/selenium.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Setup WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Bo comment neu muon chay an
        
        driver = new ChromeDriver(options);
        
        int implicitWait = Integer.parseInt(props.getProperty("implicit.wait.sec", "10"));
        int pageLoadTimeout = Integer.parseInt(props.getProperty("page.load.timeout.sec", "30"));
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().window().maximize();

        // Thuc hien dang nhap 1 lan o day de share session cho cac test
        loginAsAdmin();
    }

    private static void loginAsAdmin() {
        String baseUrl = props.getProperty("base.url", "http://localhost:4201");
        String email = props.getProperty("admin.email");
        String pass = props.getProperty("admin.password");

        driver.get(baseUrl + "/login");
        
        try {
            // Tam thoi viet xpath/css truc tiep o day, sau refactor sang LoginPage
            org.openqa.selenium.WebElement emailInput = driver.findElement(org.openqa.selenium.By.cssSelector("input[formControlName='email']"));
            emailInput.sendKeys(email);
            
            org.openqa.selenium.WebElement passInput = driver.findElement(org.openqa.selenium.By.cssSelector("input[formControlName='password']"));
            passInput.sendKeys(pass);
            
            org.openqa.selenium.WebElement loginBtn = driver.findElement(org.openqa.selenium.By.xpath("//button[contains(text(),'Đăng nhập')]"));
            loginBtn.click();
            
            // Wait for login success (redirects to /admin)
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/admin"));
                
            System.out.println("Dang nhap admin thanh cong.");
        } catch (Exception e) {
            System.err.println("Dang nhap that bai. Kiem tra lai server hoac account.");
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void tearDownSuite() {
        if (driver != null) {
            driver.quit();
        }
        // Ghi ket qua test ra file Excel cuoi cung
        ExcelReporter.flush();
    }
}
