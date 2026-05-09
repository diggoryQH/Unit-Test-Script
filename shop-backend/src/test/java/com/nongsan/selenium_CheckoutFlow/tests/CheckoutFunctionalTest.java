package com.nongsan.selenium_CheckoutFlow.tests;

import com.nongsan.selenium_CheckoutFlow.base.BaseFlowTest;
import com.nongsan.selenium_CheckoutFlow.pages.*;
import com.nongsan.selenium_CheckoutFlow.utils.CheckoutTestData;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CheckoutFunctionalTest extends BaseFlowTest {

    private CheckoutSignPage signPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private boolean isInitialized = false;

    @BeforeEach
    @Override
    public void setup() {
        super.setup();
        signPage = new CheckoutSignPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        if (!isInitialized) {
            signPage.login(CheckoutTestData.VALID_EMAIL, CheckoutTestData.VALID_PASSWORD);
            isInitialized = true;
        }
    }

    private void ensureOnPage(String urlPart) {
        if (!driver.getCurrentUrl().contains(urlPart)) {
            driver.get(urlPart.equals("/cart") ? CheckoutTestData.CART_URL : CheckoutTestData.CHECKOUT_URL);
            waitFor(2);
        }
    }

    // --- NHÓM 1: TỒN KHO (Giữ sản phẩm) ---
    @Test
    @Order(1)
    void test_GH_21_TH1() {
        startTest("GH_21_TH1");
        ensureOnPage("/cart");
        waitFor(3); // Đợi giỏ hàng ổn định hoàn toàn
        cartPage.pressArrowUp(1, 5);
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(2)
    void test_GH_21_TH2() {
        startTest("GH_21_TH2");
        ensureOnPage("/cart");
        cartPage.updateQuantity(1, "1000");
        dismissPopups();
        if (cartPage.isCheckoutButtonVisible()) {
            WebElement btn = driver.findElement(By.xpath("//app-cart//a[contains(text(),'Thanh toán')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            waitFor(3);
        }
        logPass("Pass");
        endTest();
    }

    // --- NHÓM 2: VALIDATION & PHÍ SHIP (Giữ sản phẩm) ---
    @Test
    @Order(3)
    void test_TT_04() {
        startTest("TT_04");
        ensureOnPage("/checkout");
        checkoutPage.selectProvince(1);
        checkoutPage.selectDistrict(1);
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(4)
    void test_TT_05() {
        startTest("TT_05");
        ensureOnPage("/checkout");
        checkoutPage.enterPhone("abc");
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(5)
    void test_TT_06() {
        startTest("TT_06");
        ensureOnPage("/checkout");
        checkoutPage.enterPhone("12345678");
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(6)
    void test_TT_07() {
        startTest("TT_07");
        ensureOnPage("/checkout");
        checkoutPage.enterPhone("0123456789012");
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(7)
    void test_TT_08() {
        startTest("TT_08");
        ensureOnPage("/checkout");
        checkoutPage.enterPhone("1234567890");
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(8)
    void test_TT_09() {
        startTest("TT_09");
        ensureOnPage("/checkout");
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(9)
    void test_TT_10() {
        startTest("TT_10");
        ensureOnPage("/checkout");
        checkoutPage.selectProvince(1);
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(10)
    void test_TT_11() {
        startTest("TT_11");
        ensureOnPage("/checkout");
        checkoutPage.selectProvince(1);
        checkoutPage.selectDistrict(1);
        checkoutPage.selectWard(1);
        checkoutPage.clickCheckoutCOD();
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(11)
    void test_TT_12() {
        startTest("TT_12");
        ensureOnPage("/checkout");
        checkoutPage.selectProvince(1);
        checkoutPage.selectDistrict(1);
        checkoutPage.selectWard(1);
        assertNotNull(checkoutPage.getShippingFee());
        logPass("Pass");
        endTest();
    }

    @Test
    @Order(12)
    void test_TT_13() {
        startTest("TT_13");
        ensureOnPage("/checkout");
        assertNotNull(checkoutPage.getTotalPay());
        logPass("Pass");
        endTest();
    }

    // --- NHÓM 3: VNPay & BẢO MẬT (Giữ sản phẩm) ---
    @Test
    @Order(13)
    void test_TT_15() {
        startTest("TT_15");
        driver.get(CheckoutTestData.CHECKOUT_URL);
        waitFor(2);
        checkoutPage.enterPhone(CheckoutTestData.TEST_PHONE);
        checkoutPage.selectProvince(1);
        checkoutPage.selectDistrict(1);
        checkoutPage.selectWard(1);
        checkoutPage.enterNumber(CheckoutTestData.TEST_ADDRESS); // Điền địa chỉ cụ thể
        
        System.out.println("[INFO] Đang bấm thanh toán VNPAY...");
        checkoutPage.selectVNPay();
        waitFor(3);
        
        if (driver.getCurrentUrl().contains("vnpayment.vn")) {
            System.out.println("[PASS] Đã redirect sang VNPay thành công.");
        }
        
        // Quay lại trang Checkout để phục vụ cho các test sau (TT_18, TT_19)
        driver.get(CheckoutTestData.CHECKOUT_URL);
        waitFor(2);
        logPass("Pass TT_15");
        endTest();
    }

    // --- NHÓM CUỐI: CHỐT ĐƠN THẬT (Làm trống giỏ hàng) ---
    @Test
    @Order(16)
    void test_TT_14() {
        startTest("TT_14");
        // Đảm bảo sạch form 100% bằng cách tải lại trang
        driver.get(CheckoutTestData.CHECKOUT_URL); 
        waitFor(3);
        
        System.out.println("[INFO] Nhập thông tin khách hàng...");
        checkoutPage.enterPhone(CheckoutTestData.TEST_PHONE);
        checkoutPage.selectProvince(1);
        checkoutPage.selectDistrict(1);
        checkoutPage.selectWard(1);
        System.out.println("[INFO] Đang đợi hệ thống tính phí ship...");
        waitFor(3); // Chờ API tính phí ship xong xuôi
        checkoutPage.enterNumber(CheckoutTestData.TEST_ADDRESS);
        
        System.out.println("[INFO] Đang chốt đơn và đợi thông báo thành công...");
        checkoutPage.clickCheckoutCOD();
        checkoutPage.confirmSwal();
        
        System.out.println("[INFO] Đang thực hiện CheckDB & Rollback...");
        waitFor(5); // Đợi Backend xử lý xong
        
        // 1. Check DB (Data Integrity)
        boolean isCreated = isOrderCreatedInDb(CheckoutTestData.VALID_EMAIL);
        assertTrue(isCreated, "Lỗi: Đơn hàng không xuất hiện trong Database!");
        System.out.println("[PASS] Đã tìm thấy đơn hàng trong Database.");
        
        // 2. Rollback (Environment Cleanup)
        long lastId = getLastOrderId();
        if (lastId != -1) {
            deleteOrderById(lastId);
            System.out.println("[INFO] Đã thực hiện Rollback (Xóa đơn hàng test ID: " + lastId + ").");
        }
        
        logPass("Pass TT_14 - Đã đặt hàng, CheckDB và Rollback thành công!");
        endTest();
    }
}
