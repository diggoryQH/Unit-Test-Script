package com.nongsan.selenium.test;

import com.nongsan.selenium.base.BaseTest;
import com.nongsan.selenium.helper.DbHelper;
import com.nongsan.selenium.helper.ExcelReporter;
import com.nongsan.selenium.model.TestResult;
import com.nongsan.selenium.pages.CategoryPage;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryTest extends BaseTest {

    private CategoryPage categoryPage;
    private static final String VALID_CATEGORY = "Trái cây 5001"; // suffix to avoid conflict
    private static final String DUPLICATE_CATEGORY = "Rau";

    @BeforeEach
    public void setUp() {
        driver.get(props.getProperty("base.url") + "/admin/category");
        categoryPage = new CategoryPage(driver);
        // Wait for page load
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void cleanUp() {
        DbHelper.rollbackCategoryByName(VALID_CATEGORY);
        DbHelper.rollbackCategoryByName("  ");
        DbHelper.rollbackCategoryByName("@#$%&*Rau");
    }

    @Test
    @Order(1)
    public void tc_DM03_addValidCategory() {
        String testId = "DM_03";
        String purpose = "Kiểm tra thêm danh mục mới hợp lệ";
        String steps = "1. Click Thêm nhãn hàng\n2. Nhập tên\n3. Click Thêm";
        String testData = "Tên: " + VALID_CATEGORY;
        String expected = "Thông báo 'Thêm thành công', hiển thị trong DB";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName(VALID_CATEGORY);
            categoryPage.clickAdd();

            String toastMsg = categoryPage.getToastMessage();
            boolean inTable = categoryPage.isCategoryInTable(VALID_CATEGORY);
            boolean inDb = DbHelper.categoryExistsByName(VALID_CATEGORY);

            if (toastMsg != null && toastMsg.contains("Thêm thành công") && inDb) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Toast: " + toastMsg + ", DB: " + inDb + ", Table: " + inTable;
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose, steps, testData, "tc_DM03_addValidCategory",
                    expected, actual, note));
            DbHelper.rollbackCategoryByName(VALID_CATEGORY); // Clean up immediately for repeatability
        }
    }

    @Test
    @Order(2)
    public void tc_DM04_addDuplicateName() {
        String testId = "DM_04";
        String purpose = "Kiểm tra thêm danh mục trùng tên đã tồn tại";
        String steps = "1. Click Thêm\n2. Nhập 'Rau'\n3. Click Thêm";
        String testData = "Tên: " + DUPLICATE_CATEGORY;
        String expected = "Hiển thị lỗi 'Tên danh mục đã tồn tại'";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName(DUPLICATE_CATEGORY);
            categoryPage.clickAdd();

            String toastMsg = categoryPage.getToastMessage();
            if (toastMsg != null && toastMsg.contains("Thêm thành công")) {
                actual = "FAIL";
                note = "Bug xác nhận: Hiển thị thông báo 'Thêm thành công' thay vì báo lỗi.";
            } else if (toastMsg != null && toastMsg.contains("tồn tại")) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Toast message: " + toastMsg;
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose, steps, testData, "tc_DM04_addDuplicateName",
                    expected, actual, note));
            DbHelper.rollbackDuplicateCategoryByName(DUPLICATE_CATEGORY);
        }
    }

    @Test
    @Order(3)
    public void tc_DM05_emptyName() {
        String testId = "DM_05";
        String purpose = "Kiểm tra thêm danh mục với tên để trống";
        String steps = "1. Click Thêm\n2. Tên trống\n3. Click Thêm";
        String testData = "Tên: (trống)";
        String expected = "Không click được nút Thêm, thông báo Hãy nhập đúng tên nhãn hàng";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName("");

            if (!categoryPage.isAddButtonEnabled()) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Nút Thêm vẫn active dù để trống tên.";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(
                    new TestResult(testId, purpose, steps, testData, "tc_DM05_emptyName", expected, actual, note));
        }
    }

    @Test
    @Order(4)
    public void tc_DM07_longName286() {
        String testId = "DM_07";
        String purpose = "Kiểm tra thêm danh mục với tên dài 286 ký tự";
        String steps = "1. Click Thêm\n2. Tên 286 ký tự\n3. Click Thêm";
        String longName = "a".repeat(286);
        String testData = "Tên: 286 chars";
        String expected = "Thêm không thành công vì quá dài";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName(longName);
            categoryPage.clickAdd();
            boolean inDb = DbHelper.categoryExistsByName(longName);

            String toastMsg = categoryPage.getToastMessage();
            if (!inDb) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Toast: " + toastMsg;
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(
                    new TestResult(testId, purpose, steps, testData, "tc_DM07_longName286", expected, actual, note));
            DbHelper.rollbackCategoryByName(longName);
        }
    }

    @Test
    @Order(5)
    public void tc_DM08_twoSpacesName() {
        String testId = "DM_08";
        String purpose = "Kiểm tra thêm danh mục với tên có >= 2 dấu Space";
        String steps = "1. Click Thêm\n2. Nhập '  '\n3. Click Thêm";
        String testData = "Tên: '  '";
        String expected = "Không click được nút Thêm, thông báo Hãy nhập đúng...";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName("  ");

            if (categoryPage.isAddButtonEnabled()) {
                categoryPage.clickAdd();
                String toastMsg = categoryPage.getToastMessage();
                actual = "FAIL";
                note = "Bug xác nhận: Nút thêm active, hiển thị: " + toastMsg;
            } else {
                actual = "PASS";
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(
                    new TestResult(testId, purpose, steps, testData, "tc_DM08_twoSpacesName", expected, actual, note));
            DbHelper.rollbackCategoryByName("  ");
        }
    }

    @Test
    @Order(6)
    public void tc_DM09_specialCharsName() {
        String testId = "DM_09";
        String purpose = "Kiểm tra thêm danh mục với tên chứa ký tự đặc biệt";
        String steps = "1. Click Thêm\n2. Nhập '@#$%&*Rau'\n3. Click Thêm";
        String testData = "Tên: '@#$%&*Rau'";
        String expected = "Lỗi 'Tên danh mục không được chứa ký tự đặc biệt'";
        String actual = "";
        String note = "";

        try {
            categoryPage.openAddModal();
            categoryPage.enterCategoryName("@#$%&*Rau");
            categoryPage.clickAdd();

            String toastMsg = categoryPage.getToastMessage();
            if (toastMsg != null && toastMsg.contains("Thêm thành công")) {
                actual = "FAIL";
                note = "Bug xác nhận: Thêm thành công dù tên chứa ký tự đặc biệt.";
            } else if (toastMsg != null && toastMsg.contains("ký tự đặc biệt")) {
                actual = "PASS";
            } else {
                actual = "FAIL";
                note = "Toast: " + toastMsg;
            }
        } catch (Exception e) {
            actual = "ERROR";
            note = e.getMessage();
        } finally {
            ExcelReporter.addResult(new TestResult(testId, purpose, steps, testData, "tc_DM09_specialCharsName",
                    expected, actual, note));
            DbHelper.rollbackCategoryByName("@#$%&*Rau");
        }
    }
}
