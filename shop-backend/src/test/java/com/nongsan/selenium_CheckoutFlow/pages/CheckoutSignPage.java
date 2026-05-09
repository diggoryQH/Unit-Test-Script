package com.nongsan.selenium_CheckoutFlow.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CheckoutSignPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public CheckoutSignPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    public void login(String email, String password) {
        System.out.println("[INFO] Bắt đầu quy trình Login...");
        driver.manage().deleteAllCookies();
        driver.get("http://localhost:4200/home");
        
        WebElement loginBtn;
        System.out.println("[INFO] Đang tìm nút Đăng nhập trên giao diện...");
        try {
            loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Đăng nhập")));
            System.out.println("[INFO] Đã tìm thấy nút bằng LinkText. Đang click...");
            loginBtn.click();
        } catch (Exception e) {
            System.out.println("[WARN] LinkText thất bại, thử quét toàn bộ thẻ <a> có chữ Đăng nhập...");
            loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Đăng nhập')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
        }
        
        System.out.println("[INFO] Đang đợi form Đăng nhập xuất hiện...");
        WebElement signInTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//ul[@class='nav nav-tabs']//a[@href='#sign-in']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInTab);

        System.out.println("[INFO] 3. Điền Email & Password...");
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sign-in']//input[@formcontrolname='email']")));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passInput = driver.findElement(By.xpath("//div[@id='sign-in']//input[@formcontrolname='password']"));
        passInput.clear();
        passInput.sendKeys(password);

        System.out.println("[INFO] 4. Click nút Đăng nhập cuối cùng...");
        loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.ps-btn.ps-btn--fullwidth")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
        
        System.out.println("[INFO] 5. Đợi xác nhận quay về trang chủ...");
        wait.until(ExpectedConditions.urlContains("/home"));
        System.out.println("[PASS] Đăng nhập thành công!");
    }

    public void logout() {
        driver.manage().deleteAllCookies();
        driver.get("http://localhost:4200/home");
    }
}
