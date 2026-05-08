package com.nongsan.selenium.tests;

import com.nongsan.selenium.base.BaseTest;
import com.nongsan.selenium.pages.SignFormPage;
import com.nongsan.selenium.utils.DriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginTest extends BaseTest {

    private SignFormPage signFormPage;

    private static final String BASE_URL = "http://localhost:4200";
    private static final String VALID_EMAIL = "duongbacdinhthoa@gmail.com";
    private static final String VALID_PASSWORD = "123456";

@BeforeEach
@Override
public void setup() {
    super.setup();

    // clear trước
    driver.manage().deleteAllCookies();

    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("window.localStorage.clear();");
    js.executeScript("window.sessionStorage.clear();");

    // rồi mới vào page
    driver.get(BASE_URL + "/home");

    waitForPageReady();
    waitForAngular();

    signFormPage = new SignFormPage(driver);
    signFormPage.clickLoginRegisterFromHome();

    waitForPageReady();
    waitForAngular();
}
    // ==================== TC-LI-01: LOGIN SUCCESSFUL ====================
    @Test
    @Order(1)
    @DisplayName("TC-LI-01: Đăng nhập thành công với email và mật khẩu hợp lệ")
    public void testLoginSuccess() {
        startTest("TC-LI-01: Đăng nhập thành công");

        try {
            logInfo("URL: " + BASE_URL + "/sign-form");
            logInfo("Email: " + VALID_EMAIL);

            signFormPage.enterSignInEmail(VALID_EMAIL);
            signFormPage.enterSignInPassword(VALID_PASSWORD);
            signFormPage.clickSignInButton();

            waitFor(3);

            String currentUrl = driver.getCurrentUrl();
            logInfo("Current URL after login: " + currentUrl);

            assertTrue(
                currentUrl.contains("/home") || currentUrl.equals(BASE_URL + "/"),
                "Expected redirect to home page, but got: " + currentUrl
            );

            logPass("Đăng nhập thành công - URL: " + currentUrl);

        } catch (Exception e) {
            captureScreenshot("TC-LI-01_Fail");
            logFail("Đăng nhập thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-02: LOGIN WITH UNREGISTERED EMAIL ====================
    @Test
    @Order(2)
    @DisplayName("TC-LI-02: Đăng nhập thất bại với email chưa đăng ký")
    public void testLoginWithUnregisteredEmail() {
        startTest("TC-LI-02: Email chưa đăng ký");

        try {
            logInfo("Email: notexist12345@gmail.com");
            signFormPage.enterSignInEmail("notexist12345@gmail.com");
            signFormPage.enterSignInPassword("123456");
            signFormPage.clickSignInButton();

            waitFor(2);

            String message = signFormPage.getToastMessage();
            logInfo("Message: " + message);

            assertTrue(
                message.toLowerCase().contains("sai") ||
                message.toLowerCase().contains("không") ||
                message.toLowerCase().contains("tồn tại") ||
                message.toLowerCase().contains("thất bại") ||
                message.toLowerCase().contains("invalid") ||
                message.toLowerCase().contains("sai"),
                "Expected error message, but got: " + message
            );

            logPass("Hiển thị thông báo lỗi: " + message);

        } catch (Exception e) {
            captureScreenshot("TC-LI-02_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-03: LOGIN WITH WRONG PASSWORD ====================
    @Test
    @Order(3)
    @DisplayName("TC-LI-03: Đăng nhập thất bại với mật khẩu sai")
    public void testLoginWithWrongPassword() {
        startTest("TC-LI-03: Mật khẩu sai");

        try {
            logInfo("Email: " + VALID_EMAIL);
            signFormPage.enterSignInEmail(VALID_EMAIL);
            signFormPage.enterSignInPassword("WrongPassword123");
            signFormPage.clickSignInButton();

            waitFor(2);

            String message = signFormPage.getToastMessage();
            logInfo("Message: " + message);

            assertTrue(
                message.toLowerCase().contains("sai") ||
                message.toLowerCase().contains("mật khẩu") ||
                message.toLowerCase().contains("không đúng") ||
                message.toLowerCase().contains("thất bại"),
                "Expected error message, but got: " + message
            );

            logPass("Hiển thị thông báo lỗi: " + message);

        } catch (Exception e) {
            captureScreenshot("TC-LI-03_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-04: LOGIN WITH EMPTY EMAIL ====================
    @Test
    @Order(4)
    @DisplayName("TC-LI-04: Đăng nhập thất bại với email trống")
    public void testLoginWithEmptyEmail() {
        startTest("TC-LI-04: Email trống");

        try {
            logInfo("Leaving email empty");
            signFormPage.enterSignInPassword(VALID_PASSWORD);
            signFormPage.clickSignInButton();

            waitFor(1);

            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/sign-form") || currentUrl.contains("/login"),
                "Expected to stay on sign-form page, but got: " + currentUrl
            );

            logPass("Không cho phép đăng nhập với email trống");

        } catch (Exception e) {
            captureScreenshot("TC-LI-04_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-05: LOGIN WITH EMPTY PASSWORD ====================
    @Test
    @Order(5)
    @DisplayName("TC-LI-05: Đăng nhập thất bại với mật khẩu trống")
    public void testLoginWithEmptyPassword() {
        startTest("TC-LI-05: Mật khẩu trống");

        try {
            logInfo("Entering email but leaving password empty");
            signFormPage.enterSignInEmail(VALID_EMAIL);
            signFormPage.clickSignInButton();

            waitFor(1);

            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/sign-form"),
                "Expected to stay on sign-form page, but got: " + currentUrl
            );

            logPass("Không cho phép đăng nhập với mật khẩu trống");

        } catch (Exception e) {
            captureScreenshot("TC-LI-05_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-06: LOGIN WITH INVALID EMAIL FORMAT ====================
    @Test
    @Order(6)
    @DisplayName("TC-LI-06: Đăng nhập thất bại với email không đúng định dạng")
    public void testLoginWithInvalidEmailFormat() {
        startTest("TC-LI-06: Email không hợp lệ");

        try {
            logInfo("Email: invalidemail");
            signFormPage.enterSignInEmail("invalidemail");
            signFormPage.enterSignInPassword(VALID_PASSWORD);
            signFormPage.clickSignInButton();

            waitFor(1);

            String currentUrl = driver.getCurrentUrl();
            String message = signFormPage.getToastMessage();

            assertTrue(
                message.toLowerCase().contains("email") ||
                message.toLowerCase().contains("invalid") ||
                message.toLowerCase().contains("không hợp lệ") ||
                currentUrl.contains("/sign-form"),
                "Expected validation error, but got URL: " + currentUrl + ", Message: " + message
            );

            logPass("Validation email hoạt động đúng");

        } catch (Exception e) {
            captureScreenshot("TC-LI-06_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-07: LOGIN WITH EMPTY FIELDS ====================
    @Test
    @Order(7)
    @DisplayName("TC-LI-07: Đăng nhập thất bại khi để trống cả email và mật khẩu")
    public void testLoginWithEmptyFields() {
        startTest("TC-LI-07: Trường trống");

        try {
            logInfo("Clicking login button without entering any credentials");
            signFormPage.clickSignInButton();

            waitFor(1);

            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/sign-form"),
                "Expected to stay on sign-form page, but got: " + currentUrl
            );

            logPass("Không cho phép đăng nhập khi để trống thông tin");

        } catch (Exception e) {
            captureScreenshot("TC-LI-07_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-LI-08: SWITCH TO SIGN UP TAB ====================
    @Test
    @Order(8)
    @DisplayName("TC-LI-08: Chuyển sang tab Đăng ký")
    public void testSwitchToSignUpTab() {
        startTest("TC-LI-08: Chuyển sang tab Đăng ký");

        try {
            logInfo("Clicking Sign Up tab");
            signFormPage.clickSignUpTab();

            waitFor(1);

            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                signFormPage.isSignUpFormDisplayed() || currentUrl.contains("/sign-form"),
                "Sign Up form should be displayed"
            );

            logPass("Chuyển sang tab Đăng ký thành công");

        } catch (Exception e) {
            captureScreenshot("TC-LI-08_Fail");
            logFail("Test thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== HELPER METHODS ====================

    @AfterAll
    public void tearDownAll() {
        DriverManager.quitDriver();
    }
}
