package com.nongsan.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProductDetailPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public ProductDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(500));
        PageFactory.initElements(driver, this);
    }

    public String getProductName() {
        try {
            By nameLocator = By.xpath("//app-product-detail//h1");
            WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(nameLocator));
            return nameElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void clickAddToCartButton() {
        By addToCartLocator = By.xpath(
            "//app-product-detail//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'thêm')] " +
            "| //app-product-detail//button[contains(@class, 'ps-btn')]"
        );
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(addToCartLocator));
        scrollToElement(addToCartBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartBtn);
        log("Clicked Add to Cart button");
    }

    public String getToastMessage() {
        try {
            By toastLocator = By.cssSelector(".toast-success, .toast-error, [class*='toast'], .swal2-toast");
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            return toast.getText();
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
                By toastLocator = By.cssSelector(".toast-success, .toast-error, [class*='toast'], .swal2-toast");
                WebElement toast = driver.findElement(toastLocator);
                return toast.getText();
            } catch (Exception ex) {
                return "";
            }
        }
    }

    public void navigateToCart() {
        By cartLinkLocator = By.xpath("//app-header//a[contains(@href, '/cart')]");
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(cartLinkLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartLink);
        log("Clicked Cart icon from header");
        waitForPageReady();
        waitForAngular();
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

    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    private void log(String message) {
        System.out.println("[ProductDetailPage] " + message);
    }
}
