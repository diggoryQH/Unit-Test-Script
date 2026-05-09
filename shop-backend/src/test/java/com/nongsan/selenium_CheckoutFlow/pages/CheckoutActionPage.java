package com.nongsan.selenium_CheckoutFlow.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CheckoutActionPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public CheckoutActionPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void searchAndAddProduct(String keyword) {
        // Đợi ô search sẵn sàng
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("search")));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        searchInput.sendKeys(Keys.ENTER);

        // Đợi kết quả tìm kiếm xuất hiện và click sản phẩm đầu tiên
        try {
            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//app-product//a")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", productLink);
            Thread.sleep(1000); // Đợi cuộn trang ổn định
            productLink.click();
        } catch (Exception e) {
            // Nếu click thường lỗi, dùng JS Click
            WebElement productLink = driver.findElement(By.xpath("//app-product//a"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", productLink);
        }

        // Click Thêm vào giỏ
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Thêm vào giỏ')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
    }
}
