package com.nongsan.selenium_CheckoutFlow.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CartPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void goToCartFromHeader() {
        // Đợi header tải ổn định
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//header")));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='header__extra' and @href='/cart']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartIcon);
        wait.until(ExpectedConditions.urlContains("/cart"));
    }

    public int getCartItemCount() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//app-cart//table")));
            return driver.findElements(By.xpath("//app-cart//table//tbody//tr")).size();
        } catch (Exception e) { return 0; }
    }

    public void updateQuantity(int rowIndex, String quantity) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//app-cart//table//tbody//tr[" + rowIndex + "]//input[@type='number']")
        ));
        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(quantity);
        input.sendKeys(Keys.ENTER);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    public void pressArrowUp(int rowIndex, int times) {
        By locator = By.xpath("//app-cart//table//tbody//tr[" + rowIndex + "]//input[@type='number']");
        
        for (int i = 0; i < times; i++) {
            try {
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
                String before = input.getAttribute("value");
                
                input.sendKeys(Keys.ARROW_UP);
                input.sendKeys(Keys.ENTER);
                
                // Đợi một chút để Angular ngOnInit() tải lại xong
                waitFor(1); 
                
                input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                String after = input.getAttribute("value");
                
                System.out.println("[INFO] Lan bam " + (i+1) + ": " + before + " -> " + after);
                
                // Nếu đã đạt đến mức không thể tăng được nữa sau 2 lần thử thì mới dừng
                if (before.equals(after) && i > 2) {
                     break;
                }
            } catch (Exception e) {
                waitFor(1);
            }
        }
    }
    
    private void waitFor(int seconds) {
        try { Thread.sleep(seconds * 1000); } catch (InterruptedException ignored) {}
    }

    public String getQuantityValue(int rowIndex) {
        WebElement input = driver.findElement(By.xpath("//app-cart//table//tbody//tr[" + rowIndex + "]//input[@type='number']"));
        return input.getAttribute("value");
    }

    public void removeItem(int rowIndex) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//app-cart//table//tbody//tr[" + rowIndex + "]//a[i[contains(@class,'icon-cross')]]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isCheckoutButtonVisible() {
        try {
            WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//app-cart//a[contains(text(),'Thanh toán')]")));
            return btn.isDisplayed();
        } catch (Exception e) { return false; }
    }
}
