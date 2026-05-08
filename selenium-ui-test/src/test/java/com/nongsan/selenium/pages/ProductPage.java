package com.nongsan.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

/**
 * Page Object cho trang Quan ly San Pham (/admin/product)
 */
public class ProductPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators ---
    private By addProductBtn = By.cssSelector("app-add-product button");
    
    // Modal Locators
    private By nameInput = By.cssSelector("input[formControlName='name']");
    private By categorySelect = By.cssSelector("select[formControlName='categoryId']");
    private By costPriceInput = By.cssSelector("input[formControlName='costPrice']");
    private By priceInput = By.cssSelector("input[formControlName='price']");
    private By discountInput = By.cssSelector("input[formControlName='discount']");
    private By quantityInput = By.cssSelector("input[formControlName='quantity']");
    private By weightInput = By.cssSelector("input[formControlName='weight']");
    private By enteredDateInput = By.cssSelector("input[formControlName='enteredDate']");
    private By expiryDateInput = By.cssSelector("input[formControlName='expiryDate']");
    private By originInput = By.cssSelector("input[formControlName='origin']");
    private By descriptionInput = By.cssSelector("textarea[formControlName='description']");
    private By fileInput = By.id("customFile");
    
    private By saveBtn = By.xpath("//button[contains(text(),'Lưu sản phẩm')]");
    
    // Table / Search
    private By searchInput = By.xpath("//input[contains(@class,'mat-input-element')]");
    private By toastMessage = By.cssSelector(".toast-message");
    
    // Validation Errors
    private By dateErrorMsg = By.xpath("//small[contains(text(),'Ngày hết hạn không được trước')]");

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void openAddModal() {
        wait.until(ExpectedConditions.elementToBeClickable(addProductBtn)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
    }

    public void enterName(String name) {
        WebElement el = driver.findElement(nameInput);
        el.clear();
        el.sendKeys(name);
    }
    
    public void enterCostPrice(String price) {
        WebElement el = driver.findElement(costPriceInput);
        el.clear();
        el.sendKeys(price);
    }
    
    public void enterPrice(String price) {
        WebElement el = driver.findElement(priceInput);
        el.clear();
        el.sendKeys(price);
    }
    
    public void enterDiscount(String discount) {
        WebElement el = driver.findElement(discountInput);
        el.clear();
        el.sendKeys(discount);
    }
    
    public void enterQuantity(String qty) {
        WebElement el = driver.findElement(quantityInput);
        el.clear();
        el.sendKeys(qty);
    }
    
    public void enterWeight(String weight) {
        WebElement el = driver.findElement(weightInput);
        el.clear();
        el.sendKeys(weight);
    }
    
    public void enterEnteredDate(String date) {
        WebElement el = driver.findElement(enteredDateInput);
        el.sendKeys(date); // Date input HTML5 may need specific format depending on browser
    }
    
    public void enterExpiryDate(String date) {
        WebElement el = driver.findElement(expiryDateInput);
        el.sendKeys(date);
    }
    
    public void enterOrigin(String origin) {
        WebElement el = driver.findElement(originInput);
        el.clear();
        el.sendKeys(origin);
    }
    
    public void enterDescription(String desc) {
        WebElement el = driver.findElement(descriptionInput);
        el.clear();
        el.sendKeys(desc);
    }
    
    public void uploadImage(String relativePath) {
        File file = new File(relativePath);
        if (file.exists()) {
            driver.findElement(fileInput).sendKeys(file.getAbsolutePath());
            // Doi anh hien thi truoc khi di tiep
            try { Thread.sleep(2000); } catch (Exception e) {}
        }
    }

    public void clickSave() {
        driver.findElement(saveBtn).click();
    }

    public boolean isSaveButtonEnabled() {
        return driver.findElement(saveBtn).isEnabled();
    }

    public String getToastMessage() {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            return toast.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean hasDateErrorMsg() {
        try {
            return driver.findElement(dateErrorMsg).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Dien tu dong cac truong hop le de test cac truong rieng le
    public void fillValidFormData() {
        enterCostPrice("5000");
        enterPrice("10000");
        enterDiscount("0");
        enterQuantity("100");
        enterWeight("5");
        // Date input format: YYYY-MM-DD or ddmmyyyy via sendKeys (browser dependent)
        enterEnteredDate("03-31-2026"); 
        enterExpiryDate("04-07-2026");
        enterOrigin("VietNam");
        enterDescription("Tuoi");
        uploadImage("src/test/resources/testdata/caithao.jpg");
    }
}
