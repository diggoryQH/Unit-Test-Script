package com.nongsan.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object cho trang Quan ly Danh muc (/admin/category)
 */
public class CategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators ---
    private By addCategoryBtn = By.cssSelector("app-add-category button");
    
    // Modal Locators
    private By categoryNameInput = By.id("categoryName");
    private By submitAddBtn = By.xpath("//ngb-modal-window//button[contains(., 'Thêm')]");
    
    // Toast Locator
    private By toastMessage = By.cssSelector(".toast-message");
    
    // Table Locators
    private By searchInput = By.xpath("//input[contains(@class,'mat-input-element')]");

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void openAddModal() {
        wait.until(ExpectedConditions.elementToBeClickable(addCategoryBtn)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
    }

    public void enterCategoryName(String name) {
        WebElement input = driver.findElement(categoryNameInput);
        input.clear();
        input.sendKeys(name);
    }

    public void clickAdd() {
        driver.findElement(submitAddBtn).click();
    }

    public boolean isAddButtonEnabled() {
        return driver.findElement(submitAddBtn).isEnabled();
    }

    public String getToastMessage() {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            return toast.getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCategoryInTable(String name) {
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        searchBox.clear();
        searchBox.sendKeys(name);
        
        try {
            // Cho filter cap nhat
            Thread.sleep(1000); 
            // Kiem tra ten trong cot t2 cua bang
            By cellLocator = By.xpath("//mat-cell[contains(@class,'cdk-column-categoryName') and text()='" + name + "']");
            return driver.findElements(cellLocator).size() > 0;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
