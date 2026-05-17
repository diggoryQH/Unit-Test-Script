package com.nongsan.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(500));
        PageFactory.initElements(driver, this);
    }

    public boolean isCartPageDisplayed() {
        try {
            By cartHeaderLocator = By.xpath("//app-cart//h1[contains(text(),'Giỏ Hàng')]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(cartHeaderLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCartItemCount() {
        try {
            By rowsLocator = By.xpath("//app-cart//table[contains(@class,'ps-table--shopping-cart')]//tbody//tr");
            List<WebElement> rows = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(rowsLocator)
            );
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isProductInCart(String productName) {
        try {
            By productLocator = By.xpath(
                "//app-cart//table[contains(@class,'ps-table--shopping-cart')]//td[contains(text(),'" + productName + "')]"
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(productLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCartProductName() {
        try {
            By nameLocator = By.xpath(
                "//app-cart//table[contains(@class,'ps-table--shopping-cart')]//a[contains(@href,'/product-detail')]"
            );
            WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(nameLocator));
            return nameElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getCartTotalText() {
        try {
            By totalLocator = By.xpath(
                "//app-cart//h3[contains(text(),'Tổng tiền') or contains(.,'Tổng tiền')]"
            );
            WebElement totalElement = wait.until(ExpectedConditions.visibilityOfElementLocated(totalLocator));
            return totalElement.getText();
        } catch (Exception e) {
            return "";
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

    private void log(String message) {
        System.out.println("[CartPage] " + message);
    }

    /**
     * Xóa sản phẩm đầu tiên trong giỏ hàng bằng cách click vào nút xóa.
     * @return true nếu xóa thành công, false nếu không có sản phẩm để xóa
     */
    @SuppressWarnings("unused")
    public boolean removeFirstCartItem() {
        try {
            // Tìm nút xóa của sản phẩm đầu tiên trong giỏ hàng
            By deleteButtonLocator = By.xpath(
                "//app-cart//table[contains(@class,'ps-table--shopping-cart')]//tbody//tr[1]//a[contains(@class,'ps-remove')] " +
                "| //app-cart//table[contains(@class,'ps-table--shopping-cart')]//tbody//tr[1]//button[contains(@class,'remove')]"
            );
            
            WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(deleteButtonLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
            log("Đã click nút xóa sản phẩm đầu tiên");
            
            // Đợi toast message hoặc cập nhật UI
            Thread.sleep(1500);
            return true;
        } catch (Exception e) {
            log("Không tìm thấy nút xóa hoặc giỏ hàng trống: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy cartDetailId của sản phẩm đầu tiên trong giỏ hàng.
     * @return cartDetailId hoặc -1 nếu không tìm thấy
     */
    public long getFirstCartDetailId() {
        try {
            // Tìm phần tử chứa cartDetailId (thường nằm trong data attribute hoặc URL)
            By firstItemLocator = By.xpath(
                "//app-cart//table[contains(@class,'ps-table--shopping-cart')]//tbody//tr[1]"
            );
            WebElement firstRow = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemLocator));
            
            // Thử lấy từ data attribute
            String dataId = firstRow.getDomAttribute("data-id");
            if (dataId != null && !dataId.isEmpty()) {
                return Long.parseLong(dataId);
            }
            
            // Thử lấy từ URL trong link sản phẩm
            By productLinkLocator = By.xpath(
                "//app-cart//table[contains(@class,'ps-table--shopping-cart')]//tbody//tr[1]//a[contains(@href,'/product-detail')]"
            );
            WebElement productLink = firstRow.findElement(productLinkLocator);
            String href = productLink.getDomAttribute("href");
            
            // Trích xuất ID từ URL, ví dụ: /product-detail/123 -> 123
            if (href != null && href.contains("/product-detail/")) {
                String[] parts = href.split("/product-detail/");
                if (parts.length > 1) {
                    return Long.parseLong(parts[1].split("[?]")[0]);
                }
            }
            
            return -1;
        } catch (Exception e) {
            log("Không lấy được cartDetailId: " + e.getMessage());
            return -1;
        }
    }
}
