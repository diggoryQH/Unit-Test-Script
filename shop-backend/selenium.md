# Test Documentation - Selenium Web Testing (NongSan Shop)

## Tổng quan về Selenium Test

| Loại Test       | Framework          | Môi trường        | Mô tả                                             |
| --------------- | ------------------ | ----------------- | ------------------------------------------------- |
| E2E - Login     | JUnit 5 + Selenium | Chrome (headless) | Test luồng đăng nhập, đăng ký trên giao diện thật |
| E2E - Cart Flow | JUnit 5 + Selenium | Chrome (headless) | Test luồng tìm kiếm, xem sản phẩm, thêm giỏ hàng  |

---

## 1. LoginTest.java

**File:** `shop-backend/src/test/java/com/nongsan/selenium/tests/LoginTest.java` **Base URL:** `http://localhost:4200` **Pre-condition:** Backend đang chạy tại localhost:4200, ChromeDriver available

### 1.1. MODULE: ĐĂNG NHẬP (LOGIN)

#### TC-LI-01 - Đăng nhập thành công

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thành công với email và mật khẩu hợp lệ.
- **Lý do tạo test:** Test case positive cơ bản nhất của authentication. Khi đăng nhập thành công, hệ thống phải redirect về trang home.
- **Pre-condition:** Đã có tài khoản trong database.
- **Test Data:**
  - URL: `http://localhost:4200/sign-form`
  - Email: `duongbacdinhthoa@gmail.com`
  - Password: `123456`
- **Steps:**
  1. Mở trang sign-form
  2. Click nút "Đăng nhập / Đăng ký"
  3. Nhập email: `duongbacdinhthoa@gmail.com`
  4. Nhập password: `123456`
  5. Click nút "Đăng nhập"
  6. Đợi 3 giây
- **Expected:** URL chuyển về `/home` hoặc root URL

#### TC-LI-02 - Đăng nhập thất bại với email chưa đăng ký

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi email chưa được đăng ký trong hệ thống.
- **Lý do tạo test:** Khi nhập email không tồn tại, hệ thống phải hiển thị thông báo lỗi rõ ràng.
- **Test Data:**
  - Email: `notexist12345@gmail.com`
  - Password: `123456`
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Nhập email: `notexist12345@gmail.com`
  3. Nhập password: `123456`
  4. Click nút "Đăng nhập"
  5. Đợi 2 giây
- **Expected:** Toast message chứa "sai" / "không" / "tồn tại" / "thất bại" / "invalid"

#### TC-LI-03 - Đăng nhập thất bại với mật khẩu sai

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi nhập sai mật khẩu.
- **Lý do tạo test:** Khi nhập sai password, hệ thống phải thông báo lỗi để user biết và thử lại.
- **Test Data:**
  - Email: `duongbacdinhthoa@gmail.com`
  - Password: `WrongPassword123`
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Nhập email: `duongbacdinhthoa@gmail.com`
  3. Nhập password: `WrongPassword123`
  4. Click nút "Đăng nhập"
  5. Đợi 2 giây
- **Expected:** Toast message chứa "sai" / "mật khẩu" / "không đúng" / "thất bại"

#### TC-LI-04 - Đăng nhập thất bại với email trống

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi không nhập email.
- **Lý do tạo test:** Validation phía client phải ngăn không cho gửi request khi email trống.
- **Test Data:**
  - Email: (để trống)
  - Password: `123456`
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Nhập password: `123456` (không nhập email)
  3. Click nút "Đăng nhập"
  4. Đợi 1 giây
- **Expected:** URL vẫn ở `/sign-form` hoặc `/login`, không chuyển trang

#### TC-LI-05 - Đăng nhập thất bại với mật khẩu trống

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi không nhập mật khẩu.
- **Lý do tạo test:** Validation phía client phải ngăn không cho gửi request khi password trống.
- **Test Data:**
  - Email: `duongbacdinhthoa@gmail.com`
  - Password: (để trống)
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Nhập email: `duongbacdinhthoa@gmail.com`
  3. Click nút "Đăng nhập" (không nhập password)
  4. Đợi 1 giây
- **Expected:** URL vẫn ở `/sign-form`, không chuyển trang

#### TC-LI-06 - Đăng nhập thất bại với email không đúng định dạng

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi nhập email không đúng định dạng.
- **Lý do tạo test:** Validation email format phải hoạt động để tránh gửi request không hợp lệ.
- **Test Data:**
  - Email: `invalidemail`
  - Password: `123456`
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Nhập email: `invalidemail`
  3. Nhập password: `123456`
  4. Click nút "Đăng nhập"
  5. Đợi 1 giây
- **Expected:** Toast message chứa "email" / "invalid" / "không hợp lệ" HOẶC URL vẫn ở `/sign-form`

#### TC-LI-07 - Đăng nhập thất bại khi để trống cả email và mật khẩu

- **File:** `LoginTest.java`
- **Mô tả:** Đăng nhập thất bại khi click nút đăng nhập mà không nhập gì.
- **Lý do tạo test:** Hệ thống phải ngăn chặn request khi cả 2 trường đều trống.
- **Test Data:** Không có
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Click nút "Đăng nhập" (không nhập gì)
  3. Đợi 1 giây
- **Expected:** URL vẫn ở `/sign-form`, không chuyển trang

#### TC-LI-08 - Chuyển sang tab Đăng ký

- **File:** `LoginTest.java`
- **Mô tả:** Chuyển từ tab Đăng nhập sang tab Đăng ký thành công.
- **Lý do tạo test:** UI phải cho phép user chuyển tab để đăng ký tài khoản mới.
- **Test Data:** Không có
- **Steps:**
  1. Click nút "Đăng nhập / Đăng ký"
  2. Click tab "Đăng ký"
  3. Đợi 1 giây
- **Expected:** Form đăng ký được hiển thị

---

## 2. CartFlowTest.java

**File:** `shop-backend/src/test/java/com/nongsan/selenium/tests/CartFlowTest.java` **Base URL:** `http://localhost:4200` **Pre-condition:** Backend đang chạy, user đã đăng nhập thành công trong setup

### 2.1. MODULE: LUỒNG GIỎ HÀNG (CART FLOW)

#### TC-CF-01 - Hoàn tất quy trình mua hàng

- **File:** `CartFlowTest.java`
- **Mô tả:** Thực hiện đầy đủ quy trình: tìm kiếm sản phẩm -> xem chi tiết -> thêm vào giỏ hàng -> xem giỏ hàng.
- **Lý do tạo test:** Test E2E quan trọng nhất, đảm bảo toàn bộ luồng người dùng hoạt động liên tục từ đầu đến cuối.
- **Test Data:**
  - URL: `http://localhost:4200/home`
  - Email đăng nhập: `duongbacdinhthoa@gmail.com`
  - Password: `123456`
  - Từ khóa tìm kiếm: `Nấm`

- **Steps:**

  **Bước 1: Tìm kiếm sản phẩm**
  1. Mở trang home
  2. Clear cookies và localStorage
  3. Đăng nhập với email/password
  4. Đợi 3 giây
  5. Nhập từ khóa "Nấm" vào ô tìm kiếm
  6. Nhấn Enter hoặc click nút tìm kiếm
  7. Đợi 2 giây

  **Bước 2: Click vào sản phẩm** 8. Đợi danh sách sản phẩm hiển thị 9. Click vào sản phẩm đầu tiên 10. Đợi 2 giây

  **Bước 3: Thêm vào giỏ hàng** 11. Lấy tên sản phẩm từ trang detail 12. Click nút "Thêm vào giỏ hàng" 13. Đợi 2 giây 14. Kiểm tra toast message

  **Bước 4: Xem giỏ hàng** 15. Click icon/nút giỏ hàng để chuyển sang trang giỏ hàng 16. Đợi 2 giây

- **Expected:**
  - Bước 1: URL chứa `/search` hoặc từ khóa tìm kiếm
  - Bước 2: URL chứa `/product-detail`, tên sản phẩm không rỗng
  - Bước 3: Toast message chứa "giỏ hàng" / "thành công" / "thêm"
  - Bước 4: URL chứa `/cart`, trang giỏ hàng hiển thị, số sản phẩm trong giỏ > 0

#### TC-CF-02 - Tìm kiếm không có kết quả

- **File:** `CartFlowTest.java`
- **Mô tả:** Tìm kiếm với ký tự đặc biệt không có sản phẩm phù hợp.
- **Lý do tạo test:** Đảm bảo hệ thống xử lý đúng khi không có sản phẩm nào phù hợp với từ khóa tìm kiếm.
- **Test Data:**
  - Từ khóa tìm kiếm: `@#$%!^&*()`
- **Steps:**
  1. Mở trang home
  2. Clear cookies và localStorage
  3. Đăng nhập với email/password
  4. Đợi 3 giây
  5. Nhập từ khóa "@#$%!^&\*()" vào ô tìm kiếm
  6. Nhấn Enter hoặc click nút tìm kiếm
  7. Đợi 2 giây
- **Expected:**
  - URL chuyển sang trang tìm kiếm
  - Số lượng sản phẩm hiển thị = 0

---

## Tổng kết Test Case Selenium

| File Test         | Số lượng Test Case | Module    | Loại Test |
| ----------------- | ------------------ | --------- | --------- |
| LoginTest.java    | 8                  | LOGIN     | E2E       |
| CartFlowTest.java | 2                  | CART_FLOW | E2E       |
| **Tổng cộng**     | **10**             |           |           |

### Cấu trúc Test Class

```
BaseTest.java (abstract)
├── DriverManager (khởi tạo/tắt WebDriver)
├── Helper methods (waitForPageReady, waitForAngular, captureScreenshot, etc.)

LoginTest.java
├── SignFormPage (page object)
└── Test methods (TC-LI-01 đến TC-LI-08)

CartFlowTest.java
├── HomePage (page object)
├── SearchedPage (page object)
├── ProductDetailPage (page object)
├── CartPage (page object)
└── Test methods (TC-CF-01, TC-CF-02)
```

### Page Objects

| Page Object            | Mô tả                                        |
| ---------------------- | -------------------------------------------- |
| SignFormPage.java      | Xử lý form đăng nhập/đăng ký                 |
| HomePage.java          | Xử lý trang chủ, tìm kiếm sản phẩm           |
| SearchedPage.java      | Xử lý trang kết quả tìm kiếm                 |
| ProductDetailPage.java | Xử lý trang chi tiết sản phẩm, thêm giỏ hàng |
| CartPage.java          | Xử lý trang giỏ hàng                         |

### Lý do tạo Test Case Selenium

1. **E2E (End-to-End):** Test toàn bộ luồng người dùng từ giao diện, khác với unit test chỉ test logic backend
2. **Positive Cases:** Đảm bảo chức năng hoạt động đúng khi người dùng nhập dữ liệu hợp lệ
3. **Negative Cases:** Đảm bảo validation, thông báo lỗi hoạt động đúng
4. **Edge Cases:** Xử lý trường hợp không có kết quả tìm kiếm

### Cách chạy Selenium Tests

```bash
# Chạy tất cả Selenium tests
cd shop-backend
mvn test -Dtest="**/selenium/**/*Test"

# Chạy test cụ thể
mvn test -Dtest=LoginTest
mvn test -Dtest=CartFlowTest
```
