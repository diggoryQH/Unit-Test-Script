package com.nongsan.selenium.test;

import com.nongsan.selenium.base.BaseTest;
import com.nongsan.selenium.helper.DbHelper;
import com.nongsan.selenium.helper.ExcelReporter;
import com.nongsan.selenium.model.TestResult;
import com.nongsan.selenium.pages.ProductPage;
import org.junit.jupiter.api.*;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductTest extends BaseTest {

    private ProductPage productPage;
    private static final String VALID_PROD = "Cải thảo 5001";

    @BeforeEach
    public void setUp() {
        driver.get(props.getProperty("base.url") + "/admin/product");
        productPage = new ProductPage(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void cleanUp() {
        DbHelper.rollbackProductByName(VALID_PROD);
        DbHelper.rollbackProductByName("@#$%&*!");
    }

    @Test
    @Order(1)
    public void tc_SP19_addValidProduct() {
        String testId = "SP_19";
        String purpose = "Kiểm tra thêm sản phẩm mới với thông tin hợp lệ";
        String expected = "Hệ thống thông báo 'Thêm sản phẩm thành công'";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName(VALID_PROD);
            productPage.clickSave();

            String toastMsg = productPage.getToastMessage();
            boolean inDb = DbHelper.productExistsByName(VALID_PROD);

            if (toastMsg != null && toastMsg.contains("Thêm sản phẩm thành công") && inDb) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Toast: " + toastMsg + ", DB exists: " + inDb;
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin và chọn sản phẩm trên sidebar\n2. Click nút 'Thêm sản phẩm'\n3. Điền đầy đủ thông tin hợp lệ\n6. Click Lưu sản phẩm",
                    VALID_PROD, "tc_SP19_addValidProduct", expected, actual, note));
            DbHelper.rollbackProductByName(VALID_PROD);
        }
    }

    @Test
    @Order(2)
    public void tc_SP20_emptyName() {
        String testId = "SP_20";
        String purpose = "Kiểm tra thêm sản phẩm khi bỏ trống tên sản phẩm";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("");

            if (!productPage.isSaveButtonEnabled()) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Nút lưu vẫn active";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Để trống trường Tên sản phẩm\n3. Điền đầy đủ các trường còn lại\n4. Click Lưu sản phẩm",
                    "Name empty", "tc_SP20_emptyName", expected, actual, note));
        }
    }

    @Test
    @Order(3)
    public void tc_SP21_costPriceNegative() {
        String testId = "SP_21";
        String purpose = "Kiểm tra thêm sản phẩm với giá vốn nhỏ hơn 0";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_21");
            productPage.enterCostPrice("-1000");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu vẫn active khi giá vốn < 0";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Nhập Giá vốn = -1000\n3. Điền đầy đủ trường còn lại\n4. Click button Lưu sản phẩm",
                    "Giá vốn = -1000", "tc_SP21_costPriceNegative", expected, actual, note));
        }
    }

    @Test
    @Order(4)
    public void tc_SP22_priceLessThan1000() {
        String testId = "SP_22";
        String purpose = "Kiểm tra thêm sản phẩm với giá bán nhở hơn 1000";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_22");
            productPage.enterPrice("500");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu vẫn active khi giá bán < 1000";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Nhập Giá bán = 500\n3. Điền đầy đủ trường còn lại\n4. Click button Lưu sản phẩm",
                    "Giá bán = 500", "tc_SP22_priceLessThan1000", expected, actual, note));
        }
    }

    @Test
    @Order(5)
    public void tc_SP23_floatPrice() {
        String testId = "SP_23";
        String purpose = "Kiểm tra thêm sản phẩm với giá bán/giá vốn không phải số nguyên";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_23");
            productPage.enterCostPrice("1000.1");
            productPage.enterPrice("2000.1");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu vẫn active khi giá là số thập phân";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Nhập Giá vốn = 1000.1, Giá bán = 2000.1\n3. Điền đầy đủ trường còn lại\n4. Click button Lưu sản phẩm",
                    "Giá = float", "tc_SP23_floatPrice", expected, actual, note));
        }
    }

    @Test
    @Order(6)
    public void tc_SP24_costGreaterThanPrice() {
        String testId = "SP_24";
        String purpose = "Kiểm tra thêm sản phẩm với giá vốn lớn hơn giá bán";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_24");
            productPage.enterCostPrice("10000");
            productPage.enterPrice("5000");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu vẫn active khi Giá vốn > Giá bán";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Nhập Giá vốn = 10000, Giá bán = 5000\n3. Điền đầy đủ trường còn lại\n4. Click button Lưu sản phẩm",
                    "Vốn 10k, Bán 5k", "tc_SP24_costGreaterThanPrice", expected, actual, note));
        }
    }

    @Test
    @Order(7)
    public void tc_SP25_specialCharsInName() {
        String testId = "SP_25";
        String purpose = "Kiểm tra thêm sản phẩm với tên chứa ký tự đặc biệt";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("@#$%&*!");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu active dù tên chứa ký tự đặc biệt";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Admin → Click 'Thêm sản phẩm'\n2. Nhập Tên = '@#$%&*!'\n3. Điền đầy đủ các trường còn lại\n4. Click Lưu\n5. Quan sát kết quả",
                    "@#$%&*!", "tc_SP25_specialCharsInName", expected, actual, note));
        }
    }

    @Test
    @Order(8)
    public void tc_SP26_noImageUpload() {
        String testId = "SP_26";
        String purpose = "Kiểm tra thêm sản phẩm không upload ảnh";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_26");
            // Reload page to clear image, then open modal again and don't upload
            driver.navigate().refresh();
            productPage = new ProductPage(driver);
            productPage.openAddModal();
            productPage.enterCostPrice("5000");
            productPage.enterPrice("10000");
            productPage.enterDiscount("0");
            productPage.enterQuantity("100");
            productPage.enterWeight("5");
            productPage.enterEnteredDate("03-31-2026");
            productPage.enterExpiryDate("04-07-2026");
            productPage.enterOrigin("VietNam");
            productPage.enterDescription("Tuoi");
            productPage.enterName("Test SP_26 no img");

            if (productPage.isSaveButtonEnabled()) {
                actual = "FAIL";
                note = "Bug xác nhận: Nút lưu active dù không up ảnh";
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Admin → Click 'Thêm sản phẩm'\n2. Điền đầy đủ thông tin nhưng không upload ảnh\n3. Click Lưu\n4. Quan sát phản hồi",
                    "No Image", "tc_SP26_noImageUpload", expected, actual, note));
        }
    }

    @Test
    @Order(9)
    public void tc_SP27_expiryBeforeEntered() {
        String testId = "SP_27";
        String purpose = "Kiểm tra thêm sản phẩm với ngày hết hạn trước ngày nhập";
        String expected = "Hiển thị lỗi 'Ngày hết hạn không được trước ngày nhập!'";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_27");
            productPage.enterEnteredDate("03-31-2026");
            productPage.enterExpiryDate("03-20-2026");

            if (productPage.hasDateErrorMsg() && !productPage.isSaveButtonEnabled()) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Không chặn lỗi ngày hết hạn trước ngày nhập";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Đăng nhập Admin → Click 'Thêm sản phẩm'\n2. Nhập Ngày nhập = 31/03/2026\n3. Nhập Ngày hết hạn = 20/03/2026\n4. Click Submit\n5. Quan sát thông báo",
                    "Exp < Entered", "tc_SP27_expiryBeforeEntered", expected, actual, note));
        }
    }

    @Test
    @Order(10)
    public void tc_SP28_missingOrigin() {
        String testId = "SP_28";
        String purpose = "Kiểm tra thêm sản phẩm với bỏ trống nguồn gốc";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_28");
            productPage.enterOrigin("");

            if (!productPage.isSaveButtonEnabled()) {
                actual = "PASS";
                note = "";
            } else {
                actual = "FAIL";
                note = "Button disabled đúng, nhưng thiếu response lỗi hiển thị.";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Admin → Click 'Thêm sản phẩm'\n2. Bỏ trống trường nguồn gốc\n3. Điền đầy đủ các trường còn lại\n4. Click Lưu",
                    "Origin empty", "tc_SP28_missingOrigin", expected, actual, note));
        }
    }

    @Test
    @Order(11)
    public void tc_SP29_missingDescription() {
        String testId = "SP_29";
        String purpose = "Kiểm tra thêm sản phẩm với bỏ trống mô tả";
        String expected = "Button lưu sản phẩm không được active";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_29");
            productPage.enterDescription("");

            if (!productPage.isSaveButtonEnabled()) {
                actual = "PASS";
                note = "";
            } else {
                actual = "FAIL";
                note = "Button disabled đúng, nhưng thiếu response lỗi hiển thị.";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Admin → Click 'Thêm sản phẩm'\n2. Bỏ trống trường mô tả\n3. Điền đầy đủ các trường còn lại\n4. Click Lưu",
                    "Description empty", "tc_SP29_missingDescription", expected, actual, note));
        }
    }

    @Test
    @Order(12)
    public void tc_SP30_wrongImageFormat() {
        String testId = "SP_30";
        String purpose = "Kiểm tra upload ảnh sai định dạng (.pdf)";
        String expected = "Hệ thống từ chối file";
        String actual = "";
        String note = "";

        try {
            productPage.openAddModal();
            productPage.fillValidFormData();
            productPage.enterName("Test SP_30");

            // Upload PDF instead
            productPage.uploadImage("src/test/resources/testdata/doc.pdf");

            // Try to wait to see if image is rejected, normally browser/Cloudinary rejects
            // it
            // Assuming no toast message means it failed silently or button became disabled
            // In typical cases, Cloudinary will fail and Toastr might show "Upload failed"
            // We'll just click save and see if it goes through
            if (productPage.isSaveButtonEnabled()) {
                productPage.clickSave();
                String toast = productPage.getToastMessage();
                if (toast != null && toast.contains("thành công")) {
                    actual = "FAIL";
                    note = "Upload file PDF thành công, không chặn định dạng.";
                    DbHelper.rollbackProductByName("Test SP_30");
                } else {
                    actual = "PASS";
                }
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose,
                    "1. Admin → Click 'Thêm sản phẩm'\n2. Thử upload file .pdf hoặc .txt vào trường ảnh\n3. Quan sát phản hồi",
                    "doc.pdf", "tc_SP30_wrongImageFormat", expected, actual, note));
        }
    }
}
