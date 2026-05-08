package com.nongsan.selenium.checkout;

import com.nongsan.selenium.BaseSeleniumTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutFunctionalTest extends BaseSeleniumTest {

    @Test
    void TT_12_TO_14() throws InterruptedException {
        loginClient();
        ensureItemInCart();
        
        driver.get(FRONTEND_URL + "/checkout");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

        try {
            // Fill phone
            WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formControlName='phone']")));
            phoneInput.clear();
            phoneInput.sendKeys("0345678912");

            // Select Province (Wait for options to load from API)
            WebElement provinceSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='province']")));
            wait.until(d -> new Select(provinceSelect).getOptions().size() > 1);
            Select provSelect = new Select(provinceSelect);
            provSelect.selectByIndex(1); // Select the first real province
            Thread.sleep(2000);

            // Select District (Wait for options to load)
            WebElement districtSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='district']")));
            wait.until(d -> new Select(districtSelect).getOptions().size() > 1);
            Select distSelect = new Select(districtSelect);
            distSelect.selectByIndex(1);
            Thread.sleep(2000);

            // Select Ward (Wait for options to load)
            WebElement wardSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='ward']")));
            wait.until(d -> new Select(wardSelect).getOptions().size() > 1);
            Select wrdSelect = new Select(wardSelect);
            wrdSelect.selectByIndex(1);
            Thread.sleep(2000);

            // Specific address
            WebElement addressInput = driver.findElement(By.cssSelector("input[formControlName='number']"));
            addressInput.sendKeys("Số 10, Liễu Giai");

            // Wait for shipping fee to calculate
            Thread.sleep(2000);

            // Click Checkout COD
            WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Thanh toán COD')]")));
            js.executeScript("arguments[0].click();", checkoutBtn);

            // Handle Swal Confirmation
            WebElement swalConfirm = wait.until(ExpectedConditions.elementToBeClickable(By.className("swal2-confirm")));
            swalConfirm.click();

            // Wait for success and redirect to profile
            wait.until(ExpectedConditions.urlContains("/profile"));
            assertTrue(driver.getCurrentUrl().contains("/profile"), "TT_12 FAIL: Không chuyển hướng về profile sau khi đặt hàng");
            assertTrue(driver.getPageSource().contains("Lịch sử đặt hàng"), "TT_12 FAIL: Không hiển thị lịch sử đặt hàng");

        } catch (Exception e) {
            fail("TT_12_TO_14 failed: " + e.getMessage());
        }
    }

    private void ensureItemInCart() throws InterruptedException {
        driver.get(FRONTEND_URL + "/cart");
        Thread.sleep(2000);
        if (driver.getPageSource().contains("Giỏ hàng trống")) {
            addItemToCart();
        }
    }

    private void addItemToCart() throws InterruptedException {
        // Go to all products to find an available one
        driver.get(FRONTEND_URL + "/all-product");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

        try {
            // Find first available product's detail link
            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".ps-product__thumbnail a")));
            js.executeScript("arguments[0].click();", productLink);

            // On product detail page, click add to cart
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.ps-btn--black")));
            js.executeScript("arguments[0].scrollIntoView(true);", addBtn);
            js.executeScript("arguments[0].click();", addBtn);
            
            Thread.sleep(2000);
            System.out.println("DEBUG: Product added to cart successfully.");
        } catch (Exception e) {
            fail("Không thể thêm vào giỏ: " + e.getMessage());
        }
    }
}
