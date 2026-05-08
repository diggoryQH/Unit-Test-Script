package com.nongsan.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SearchedPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public SearchedPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(500));
        PageFactory.initElements(driver, this);
    }

    public void clickFirstProduct() {
        try {
            By productLinkLocator = By.xpath(
                "(//div[contains(@class,'ps-product__thumbnail')]//a[contains(@href,'/product-detail')])[1]"
            );

            WebElement firstProduct = wait.until(
                ExpectedConditions.presenceOfElementLocated(productLinkLocator)
            );
            scrollToElement(firstProduct);
            wait.until(ExpectedConditions.elementToBeClickable(productLinkLocator));

            String href = firstProduct.getAttribute("href");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstProduct);

            wait.until(ExpectedConditions.urlContains("/product-detail"));
            waitForAngular();
            log("Clicked first product from search results: " + href);

        } catch (Exception e) {
            log("Lỗi khi click sản phẩm: " + e.getMessage());
            throw e;
        }
    }

    public String getSearchKeywordDisplayed() {
        try {
            By keywordLocator = By.xpath("//app-search//b");
            WebElement keywordElement = wait.until(ExpectedConditions.visibilityOfElementLocated(keywordLocator));
            String text = keywordElement.getText();
            log("Search keyword displayed: " + text);
            return text;
        } catch (Exception e) {
            log("Could not find search keyword: " + e.getMessage());
            return "";
        }
    }

    public int getProductCount() {
        try {
            By countLocator = By.xpath(
                "//p[contains(.,'trong số')]//strong[2]"
            );
            WebElement countElement = wait.until(ExpectedConditions.visibilityOfElementLocated(countLocator));
            String text = countElement.getText().replaceAll("[^0-9]", "");
            log("Product count: " + text);
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (Exception e) {
            log("Could not find product count: " + e.getMessage());
            return 0;
        }
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
            Boolean isAngularFound = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return document.querySelector('[ng-version]') !== null;");
            
            if (isAngularFound != null && isAngularFound) {
                Thread.sleep(500); // Chỉ cần nghỉ một chút cho DOM ổn định
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not check Angular: " + e.getMessage());
        }
    }

    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    private void log(String message) {
        System.out.println("[SearchedPage] " + message);
    }
}
