package com.nongsan.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SignFormPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // ==================== NAVIGATION TABS ====================

    @FindBy(xpath = "//ul[@class='nav nav-tabs']//a[@href='#sign-in']")
    private WebElement signInTab;

    @FindBy(xpath = "//ul[@class='nav nav-tabs']//a[@href='#sign-up']")
    private WebElement signUpTab;

    // ==================== SIGN-IN FORM ELEMENTS (inside #sign-in div) ====================

    @FindBy(xpath = "//div[@id='sign-in']//input[@formcontrolname='email']")
    private WebElement signInEmailInput;

    @FindBy(xpath = "//div[@id='sign-in']//input[@formcontrolname='password']")
    private WebElement signInPasswordInput;

    @FindBy(xpath = "//div[@id='sign-in']//button[@type='submit']")
    private WebElement signInButton;

    @FindBy(xpath = "//div[@id='sign-in']//a[contains(text(),'Quên mật khẩu')]")
    private WebElement forgotPasswordLink;

    // ==================== SIGN-UP FORM ELEMENTS (inside #sign-up div) ====================

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='email']")
    private WebElement signUpEmailInput;

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='otp']")
    private WebElement signUpOtpInput;

    @FindBy(xpath = "//div[@id='sign-up']//a[contains(text(),'Lấy mã xác thực OTP')]")
    private WebElement getOtpButton;

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='name']")
    private WebElement signUpNameInput;

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='password']")
    private WebElement signUpPasswordInput;

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='phone']")
    private WebElement signUpPhoneInput;

    @FindBy(xpath = "//div[@id='sign-up']//input[@formcontrolname='address']")
    private WebElement signUpAddressInput;

    @FindBy(xpath = "//div[@id='sign-up']//button[@type='submit']")
    private WebElement signUpButton;

    // ==================== MESSAGE ELEMENTS ====================

    @FindBy(css = ".toast-error, .toast-success, [class*='toast']")
    private WebElement toastMessage;

    @FindBy(css = ".swal2-popup, .swal2-modal")
    private WebElement sweetAlert;

    // Constructor
    public SignFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(500));
        PageFactory.initElements(driver, this);
    }

    // ==================== NAVIGATION FROM HOME PAGE ====================

public void clickLoginRegisterFromHome() {
    By loginLinkLocator = By.xpath("//a[contains(@href, '/sign-form')]");

    log("Waiting for login link...");

    WebElement loginLink = wait.until(
        ExpectedConditions.presenceOfElementLocated(loginLinkLocator)
    );

    log("Element found");

    String linkText = loginLink.getText();
    String linkHref = loginLink.getAttribute("href");

    log("Found login link - Text: '" + linkText + "', Href: " + linkHref);

    scrollToElement(loginLink);

    ((JavascriptExecutor) driver)
        .executeScript("arguments[0].click();", loginLink);

    log("Clicked 'Đăng nhập & Đăng ký' from home page");

    waitForPageReady();
}

    // ==================== NAVIGATION METHODS ====================

    public void clickSignInTab() {
        waitForElementClickable(signInTab);
        signInTab.click();
        waitForPageReady();
        log("Clicked Sign In tab");
    }

    public void clickSignUpTab() {
        waitForElementClickable(signUpTab);
        signUpTab.click();
        waitForPageReady();
        log("Clicked Sign Up tab");
    }

    // ==================== SIGN-IN METHODS ====================

    public void enterSignInEmail(String email) {
        waitForElementVisible(signInEmailInput);
        signInEmailInput.clear();
        signInEmailInput.sendKeys(email);
        log("Entered sign-in email: " + email);
    }

    public void enterSignInPassword(String password) {
        waitForElementVisible(signInPasswordInput);
        signInPasswordInput.clear();
        signInPasswordInput.sendKeys(password);
        log("Entered sign-in password: [HIDDEN]");
    }

    public void clickSignInButton() {
        waitForElementClickable(signInButton);
        signInButton.click();
        log("Clicked Sign In button");
    }

    public void signIn(String email, String password) {
        clickSignInTab();
        enterSignInEmail(email);
        enterSignInPassword(password);
        clickSignInButton();
    }

    public void clickForgotPassword() {
        waitForElementClickable(forgotPasswordLink);
        forgotPasswordLink.click();
        log("Clicked Forgot Password link");
    }

    // ==================== SIGN-UP METHODS ====================

    public void enterSignUpEmail(String email) {
        waitForElementVisible(signUpEmailInput);
        scrollToElement(signUpEmailInput);
        signUpEmailInput.clear();
        signUpEmailInput.sendKeys(email);
        log("Entered sign-up email: " + email);
    }

    public void clickGetOtpButton() {
        waitForElementClickable(getOtpButton);
        getOtpButton.click();
        log("Clicked Get OTP button");
    }

    public void enterSignUpOtp(String otp) {
        waitForElementVisible(signUpOtpInput);
        signUpOtpInput.clear();
        signUpOtpInput.sendKeys(otp);
        log("Entered OTP: [HIDDEN]");
    }

    public void enterSignUpName(String name) {
        waitForElementVisible(signUpNameInput);
        signUpNameInput.clear();
        signUpNameInput.sendKeys(name);
        log("Entered name: " + name);
    }

    public void enterSignUpPassword(String password) {
        waitForElementVisible(signUpPasswordInput);
        signUpPasswordInput.clear();
        signUpPasswordInput.sendKeys(password);
        log("Entered password: [HIDDEN]");
    }

    public void enterSignUpPhone(String phone) {
        waitForElementVisible(signUpPhoneInput);
        signUpPhoneInput.clear();
        signUpPhoneInput.sendKeys(phone);
        log("Entered phone: " + phone);
    }

    public void enterSignUpAddress(String address) {
        waitForElementVisible(signUpAddressInput);
        signUpAddressInput.clear();
        signUpAddressInput.sendKeys(address);
        log("Entered address: " + address);
    }

    public void clickSignUpButton() {
        waitForElementClickable(signUpButton);
        signUpButton.click();
        log("Clicked Sign Up button");
    }

    public void signUp(String email, String name, String password, String phone, String address) {
        clickSignUpTab();
        enterSignUpEmail(email);
        clickGetOtpButton();
        waitForOtpSent();
        enterSignUpOtp("123456");
        enterSignUpName(name);
        enterSignUpPassword(password);
        enterSignUpPhone(phone);
        enterSignUpAddress(address);
        clickSignUpButton();
    }

    // ==================== ASSERTION METHODS ====================

    public boolean isSignInFormDisplayed() {
        try {
            waitForElementVisible(signInEmailInput);
            return signInButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignUpFormDisplayed() {
        try {
            clickSignUpTab();
            waitForElementVisible(signUpEmailInput);
            return signUpButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getToastMessage() {
        try {
            waitForElementVisible(toastMessage);
            return toastMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isLoginSuccessful() {
        try {
            waitForPageReady();
            String currentUrl = driver.getCurrentUrl();
            return currentUrl.contains("/home") || currentUrl.equals("http://localhost:4200/");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignupSuccessful() {
        try {
            waitForPageReady();
            String currentUrl = driver.getCurrentUrl();
            String message = getToastMessage();
            return message.toLowerCase().contains("thành công") || message.toLowerCase().contains("đăng ký");
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== HELPER METHODS ====================

    private WebElement waitForElementVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    private WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    private void waitForOtpSent() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForPageReady() {
        // Wait for Angular to be ready
        wait.until(webDriver -> {
            Boolean isAngularReady = (Boolean) ((JavascriptExecutor) webDriver)
                .executeScript("return window.ng && document.readyState === 'complete'");
            return isAngularReady != null && isAngularReady;
        });
    }

    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    private void log(String message) {
        System.out.println("[SignFormPage] " + message);
    }

    public WebDriver getDriver() {
        return driver;
    }
}
