package com.nongsan.selenium_CheckoutFlow.base;


import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import java.sql.*;
import java.util.Date;

public abstract class BaseFlowTest {

    protected WebDriver driver;
    protected static final String FRONTEND_URL = "http://localhost:4200";
    
    @BeforeEach
    public void setup() {
        if (this.driver == null) {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--window-size=1920,1080");
            this.driver = new org.openqa.selenium.chrome.ChromeDriver(options);
            this.driver.manage().window().maximize();
            this.driver.manage().timeouts().implicitlyWait(10, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        try {
            if (driver.getCurrentUrl() != null && driver.getCurrentUrl().startsWith("http")) {
                dismissPopups(); // Chỉ dọn dẹp Popup khi đang ở trang web thực sự
            }
        } catch (Exception ignored) {}
    }

    // Không đóng trình duyệt ở đây để giữ nguyên cửa sổ theo ý người dùng
    @AfterAll
    public void tearDownAll() {
        // if (driver != null) driver.quit();
    }

    protected void waitFor(int seconds) {
        try { Thread.sleep(seconds * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    protected void startTest(String testName) {
        System.out.println("\n>>> Bat dau: " + testName + " <<<");
    }

    protected void endTest() {
        System.out.println(">>> Ket thuc bai test <<<\n");
    }

    protected void logInfo(String message) { System.out.println("[INFO] " + message); }
    protected void logPass(String message) { System.out.println("[PASS] " + message); }
    protected void logFail(String message) { System.err.println("[FAIL] " + message); }

    protected void logError(String message, Exception e) {
        System.out.println("[ERROR] " + message + " | URL hien tai: " + driver.getCurrentUrl());
        if (e != null) System.out.println("[DETAIL] " + e.getMessage());
    }

    protected void dismissPopups() {
        try {
            driver.manage().timeouts().implicitlyWait(0, java.util.concurrent.TimeUnit.SECONDS);
            java.util.List<WebElement> swalBtns = driver.findElements(By.cssSelector(".swal2-confirm"));
            if (!swalBtns.isEmpty() && swalBtns.get(0).isDisplayed()) {
                swalBtns.get(0).click();
                System.out.println("[INFO] Da dong Popup/Swal thanh cong.");
                waitFor(1);
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(10, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    protected boolean isOrderCreatedInDb(String email) {
        for (int i = 0; i < 5; i++) {
            try (Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/nongsan", "root", "123456")) {
                // Sửa u.userId thành u.user_id để khớp hoàn toàn với thực tế Database
                String sql = "SELECT o.* FROM orders o JOIN users u ON o.user_id = u.user_id WHERE u.email = ? ORDER BY o.orders_id DESC LIMIT 1";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return true;
                logInfo("Doi DB lan " + (i + 1) + ": Chua tim thay don hang cho " + email);
            } catch (SQLException e) {
                logInfo("Doi DB lan " + (i + 1) + ": Loi truy van - " + e.getMessage());
            }
            waitFor(2);
        }
        return false;
    }

    protected void deleteOrderById(long id) {
        try (Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/nongsan", "root", "123456")) {
            // Bước 1: Xóa chi tiết đơn hàng trước để tránh lỗi Foreign Key
            // Lưu ý: Tên bảng trong DB thường là snake_case (order_details)
            try {
                conn.prepareStatement("DELETE FROM order_details WHERE order_id = " + id).executeUpdate();
            } catch (Exception ignored) {
                // Thử với tên camelCase nếu snake_case fail
                conn.prepareStatement("DELETE FROM order_details WHERE orderId = " + id).executeUpdate();
            }
            
            // Bước 2: Xóa đơn hàng chính
            conn.prepareStatement("DELETE FROM orders WHERE orders_id = " + id).executeUpdate();
            System.out.println("[INFO] Rollback thanh cong cho don hang ID: " + id);
        } catch (SQLException e) {
            System.out.println("[WARN] Loi Rollback: " + e.getMessage());
        }
    }

    protected long getLastOrderId() {
        try (Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/nongsan", "root", "123456")) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT MAX(orders_id) FROM orders");
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException ignored) {}
        return -1;
    }

    protected void assertTrue(boolean condition, String message) {
        if (!condition) { 
            logFail(message); 
            Assertions.fail(message); 
        }
    }

    protected int getProductStock(String productName) {
        try (Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/nongsan", "root", "123456")) {
            // Gia su bang la products, cot la quantity và name
            String sql = "SELECT quantity FROM products WHERE name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("quantity");
        } catch (SQLException e) {
            logInfo("Loi truy van ton kho cho " + productName + ": " + e.getMessage());
        }
        return -1;
    }
}
