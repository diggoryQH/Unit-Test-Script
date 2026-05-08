package com.nongsan.selenium.tests;

import com.nongsan.selenium.base.BaseTest;
import com.nongsan.selenium.pages.*;
import com.nongsan.selenium.utils.DriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CartFlowTest extends BaseTest {

    private HomePage homePage;
    private SearchedPage searchedPage;
    private ProductDetailPage productDetailPage;
    private CartPage cartPage;

    private static final String BASE_URL = "http://localhost:4200";
    private static final String VALID_EMAIL = "duongbacdinhthoa@gmail.com";
    private static final String VALID_PASSWORD = "123456";
    private static final String SEARCH_KEYWORD = "Nấm";
    private static final String SEARCH_NO_RESULT = "@#$%!^&*()";

    @BeforeEach
    @Override
    public void setup() {
        super.setup();

        driver.manage().deleteAllCookies();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.localStorage.clear();");
        js.executeScript("window.sessionStorage.clear();");

        driver.get(BASE_URL + "/home");

        waitForPageReady();
        waitForAngular();

        SignFormPage signFormPage = new SignFormPage(driver);
        signFormPage.clickLoginRegisterFromHome();

        waitForPageReady();
        waitForAngular();

        signFormPage.enterSignInEmail(VALID_EMAIL);
        signFormPage.enterSignInPassword(VALID_PASSWORD);
        signFormPage.clickSignInButton();

        waitForPageReady();
        waitForAngular();
        waitFor(3);

        homePage = new HomePage(driver);
        searchedPage = new SearchedPage(driver);
        productDetailPage = new ProductDetailPage(driver);
        cartPage = new CartPage(driver);
    }

    // ==================== TC-CF-01: COMPLETE FLOW (Search -> Detail -> Add Cart -> View Cart) ====================
    @Test
    @Order(1)
    @DisplayName("TC-CF-01: Hoàn tất quy trình từ tìm kiếm đến xem giỏ hàng")
    public void testCompleteCartFlow() {
        startTest("TC-CF-01: Hoàn tất quy trình mua hàng");

        try {
            logInfo("=== Bước 1: Tìm kiếm sản phẩm ===");
            homePage.searchAndNavigate(SEARCH_KEYWORD);
            waitFor(2);

            String searchUrl = driver.getCurrentUrl();
            logInfo("Search URL: " + searchUrl);
            assertTrue(
                searchUrl.contains("/search") || searchUrl.contains(SEARCH_KEYWORD),
                "Expected to be on search page"
            );
            logPass("Bước 1 hoàn thành - URL: " + searchUrl);

            logInfo("=== Bước 2: Click vào sản phẩm ===");
            searchedPage.clickFirstProduct();
            waitFor(2);

            String detailUrl = driver.getCurrentUrl();
            String productName = productDetailPage.getProductName();
            logInfo("Detail URL: " + detailUrl + ", Product: " + productName);
            assertTrue(
                detailUrl.contains("/product-detail"),
                "Expected to be on product detail page"
            );
            assertFalse(productName.isEmpty(), "Product name should be displayed");
            logPass("Bước 2 hoàn thành - Sản phẩm: " + productName);

            logInfo("=== Bước 3: Thêm vào giỏ hàng ===");
            productDetailPage.clickAddToCartButton();
            waitFor(2);

            String toastMessage = productDetailPage.getToastMessage();
            logInfo("Toast message: " + toastMessage);
            assertTrue(
                toastMessage.toLowerCase().contains("giỏ hàng") ||
                toastMessage.toLowerCase().contains("thành công") ||
                toastMessage.toLowerCase().contains("thêm"),
                "Expected success toast"
            );
            logPass("Bước 3 hoàn thành - Toast: " + toastMessage);

            logInfo("=== Bước 4: Xem giỏ hàng ===");
            productDetailPage.navigateToCart();
            waitFor(2);

            String cartUrl = driver.getCurrentUrl();
            logInfo("Cart URL: " + cartUrl);
            assertTrue(
                cartUrl.contains("/cart"),
                "Expected to be on cart page"
            );
            assertTrue(
                cartPage.isCartPageDisplayed(),
                "Cart page should be displayed"
            );
            assertTrue(
                cartPage.getCartItemCount() > 0,
                "Cart should have at least 1 item"
            );
            logPass("Bước 4 hoàn thành - Cart URL: " + cartUrl);

            logPass("=== QUY TRÌNH HOÀN TẤT ===");
            logPass("Tìm kiếm -> Chi tiết sản phẩm -> Thêm giỏ hàng -> Xem giỏ hàng");

        } catch (Exception e) {
            captureScreenshot("TC-CF-01_Fail");
            logFail("Quy trình thất bại: " + e.getMessage());
            fail("Test failed: " + e.getMessage());
        } finally {
            endTest();
        }
    }

    // ==================== TC-CF-02: SEARCH NO RESULT ====================
    @Test
    @Order(2)
    @DisplayName("TC-CF-02: Tìm kiếm với ký tự đặc biệt - Không có sản phẩm")
    public void testSearchNoResult() {
        startTest("TC-CF-02: Tìm kiếm không có kết quả");

        try {
            logInfo("Keyword: " + SEARCH_NO_RESULT);

            homePage.searchAndNavigate(SEARCH_NO_RESULT);
            waitFor(2);

            String searchUrl = driver.getCurrentUrl();
            logInfo("Search URL: " + searchUrl);
            assertTrue(
                searchUrl.contains("/search") || searchUrl.contains("search"),
                "Expected to be on search page"
            );

            int productCount = searchedPage.getProductCount();
            logInfo("Product count in search results: " + productCount);
            assertEquals(0, productCount, "Expected 0 product when searching with special characters");

            logPass("Tìm kiếm không có kết quả - Count: " + productCount);

        } catch (Exception e) {
            captureScreenshot("TC-CF-02_Fail");
            logFail("Tìm kiếm thất bại: " + e.getMessage());
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
