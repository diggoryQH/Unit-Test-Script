package com.nongsan.selenium.tests;

import com.nongsan.selenium.base.BaseTest;
import com.nongsan.selenium.pages.*;
import com.nongsan.selenium.utils.DriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;

import java.sql.*;

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

    // ==================== CLEANUP METHOD ====================

    /**
     * Xóa tất cả cart_details của tài khoản test trước khi chạy test.
     * Đảm bảo giỏ hàng sạch trước mỗi test case.
     */
    private void cleanupCartBeforeTest() {
        logInfo("=== CLEANUP: Xóa cart_details trước khi test ===");
        try (Connection conn = DriverManager.getDataSourceConnection()) {
            // Xóa cart_details theo cart_id của user test
            String sql = "DELETE cd FROM cart_details cd " +
                         "JOIN carts c ON cd.cart_id = c.cart_id " +
                         "JOIN users u ON c.user_id = u.user_id " +
                         "WHERE u.email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, VALID_EMAIL);
                int deleted = pstmt.executeUpdate();
                logInfo("Đã xóa " + deleted + " cart_details cũ của user: " + VALID_EMAIL);
            }
        } catch (SQLException e) {
            logInfo("Cleanup thất bại (có thể do chưa có dữ liệu): " + e.getMessage());
        }
    }

    /**
     * Rollback xóa cart_details theo danh sách IDs.
     * Gọi sau khi test kết thúc để dọn dẹp dữ liệu test.
     * 
     * @param cartDetailIds Danh sách IDs cần xóa
     */
    private void rollbackCartDetails(long... cartDetailIds) {
        if (cartDetailIds == null || cartDetailIds.length == 0) return;
        
        logInfo("=== ROLLBACK: Xóa " + cartDetailIds.length + " cart_details ===");
        try (Connection conn = DriverManager.getDataSourceConnection()) {
            for (long id : cartDetailIds) {
                try {
                    conn.createStatement().executeUpdate("DELETE FROM cart_details WHERE cart_detail_id = " + id);
                    logInfo("Đã xóa cart_detail_id: " + id);
                } catch (SQLException e) {
                    logInfo("Không xóa được cart_detail_id " + id + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            logInfo("Rollback thất bại: " + e.getMessage());
        }
    }

    /**
     * Lấy cart_detail_id đầu tiên trong giỏ hàng của user test.
     * @return cart_detail_id hoặc -1 nếu không có
     */
    private long getFirstCartDetailId() {
        try (Connection conn = DriverManager.getDataSourceConnection()) {
            String sql = "SELECT cd.cart_detail_id FROM cart_details cd " +
                         "JOIN carts c ON cd.cart_id = c.cart_id " +
                         "JOIN users u ON c.user_id = u.user_id " +
                         "WHERE u.email = ? LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, VALID_EMAIL);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("cart_detail_id");
                }
            }
        } catch (SQLException e) {
            logInfo("Lấy cart_detail_id thất bại: " + e.getMessage());
        }
        return -1;
    }

    @BeforeEach
    @Override
    public void setup() {
        super.setup();

        // CLEANUP: Xóa cart_details cũ trước khi test
        cleanupCartBeforeTest();

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

    @AfterEach
    public void tearDown() {
        // ROLLBACK: Xóa cart_details đã thêm trong test này
        try {
            long cartDetailId = getFirstCartDetailId();
            if (cartDetailId > 0) {
                rollbackCartDetails(cartDetailId);
            }
        } catch (Exception e) {
            logInfo("Rollback sau test thất bại: " + e.getMessage());
        }
        
        System.out.println("-".repeat(60));
    }

    // ==================== HELPER METHODS ====================

    @AfterAll
    public void tearDownAll() {
        DriverManager.quitDriver();
    }
}
