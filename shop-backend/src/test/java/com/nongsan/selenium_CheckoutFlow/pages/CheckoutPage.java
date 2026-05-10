package com.nongsan.selenium_CheckoutFlow.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckoutPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    private void safeType(By locator, String text) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                el.clear();
                el.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    public void enterPhone(String phone) {
        safeType(By.xpath("//input[@formcontrolname='phone']"), phone);
    }

    public void selectProvince(int index) {
        WebElement provinceSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='province']")));
        wait.until(d -> new Select(provinceSelect).getOptions().size() > 1);
        Select select = new Select(provinceSelect);
        select.selectByIndex(index);
    }

    public void selectDistrict(int index) {
        WebElement districtSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='district']")));
        wait.until(d -> new Select(districtSelect).getOptions().size() > 1);
        Select select = new Select(districtSelect);
        select.selectByIndex(index);
    }

    public void selectWard(int index) {
        WebElement wardSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[formControlName='ward']")));
        wait.until(d -> new Select(wardSelect).getOptions().size() > 1);
        Select select = new Select(wardSelect);
        select.selectByIndex(index);
    }

    public void enterNumber(String number) {
        WebElement addressInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formControlName='number']")));
        addressInput.clear();
        addressInput.sendKeys(number);
    }

    public void clickCheckoutCOD() {
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Thanh toán COD')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkoutBtn);
    }

    public void selectVNPay() {
        try {
            // Click trực tiếp vào nút "Thanh toán VNPAY" (nút màu xanh dương trong ảnh)
            WebElement vnpayBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Thanh toán VNPAY')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", vnpayBtn);
        } catch (Exception e) {
            // Nếu click xong bị redirect ngay lập tức sang VNPay, ta bỏ qua lỗi Stale/NoSuchElement
            if (!driver.getCurrentUrl().contains("vnpayment.vn")) throw e;
        }
    }

    public String getShippingFee() {
        try {
            WebElement feeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Phí vận chuyển')]/following-sibling::span")));
            return feeElement.getText();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getTotalPay() {
        try {
            WebElement totalElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(),'Tổng cộng')]/span")));
            return totalElement.getText();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getToastMessage() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement toast = shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("swal2-title")));
            return toast.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void confirmSwal() {
        WebElement swalConfirm = wait.until(ExpectedConditions.elementToBeClickable(By.className("swal2-confirm")));
        swalConfirm.click();
    }

    public boolean isRedirectedToProfile() {
        try {
            return wait.until(ExpectedConditions.urlContains("/profile"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOrderHistoryDisplayed() {
        return driver.getPageSource().contains("Lịch sử đặt hàng");
    }
}
