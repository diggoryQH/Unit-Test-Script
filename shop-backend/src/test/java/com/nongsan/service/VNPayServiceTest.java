package com.nongsan.service;

import com.nongsan.config.VnPayConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REQ-04: Đặt hàng & Thanh toán
 * Unit test cho: VNPayService.java (không cần Spring context)
 * Hàm:
 * - createOrder(Long total, String orderInfor, String urlReturn)
 * - orderReturn(HttpServletRequest request)
 */
class VNPayServiceTest {

    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        // VNPayService không có dependency @Autowired → khởi tạo trực tiếp (pure unit
        // test)
        vnPayService = new VNPayService();
    }

    // ==========================================
    // MODULE: TẠO URL THANH TOÁN — createOrder()
    // ==========================================

    /**
     * TC_VPS_01
     * Mục tiêu : Tạo URL thanh toán thành công với amount hợp lệ.
     * Xác minh URL chứa đầy đủ các tham số bắt buộc của VNPay.
     * Đầu vào : total=100000L, orderInfo="Thanh toan don hang",
     * urlReturn="http://localhost:4200"
     * Kết quả KV: resultUrl != null
     * resultUrl bắt đầu bằng "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
     * URL chứa: vnp_TmnCode=ITNF0PR5
     * URL chứa: vnp_Amount=10000000 (100000 * 100)
     * URL chứa: vnp_CurrCode=VND
     * URL chứa: vnp_SecureHash=
     * URL chứa: vnp_ReturnUrl=
     * URL chứa: vnp_Version=2.1.0
     * URL chứa: vnp_Command=pay
     */
    @Test // [Happy Path] Tạo URL hợp lệ — amount chuẩn, URL chứa đủ tham số bắt buộc
          // VNPay
    void TC_VPS_01() {
        Long amount = 100000L;
        String orderInfo = "Thanh toan don hang";
        String urlReturn = "http://localhost:4200";

        String resultUrl = vnPayService.createOrder(amount, orderInfo, urlReturn);

        assertNotNull(resultUrl, "URL không được null");
        assertTrue(resultUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"),
                "URL phải bắt đầu bằng VNPay sandbox URL");
        assertTrue(resultUrl.contains("vnp_TmnCode=ITNF0PR5"), "Thiếu vnp_TmnCode");
        assertTrue(resultUrl.contains("vnp_Amount=10000000"), "Amount phải x100: 100000 * 100 = 10000000");
        assertTrue(resultUrl.contains("vnp_CurrCode=VND"), "Thiếu vnp_CurrCode");
        assertTrue(resultUrl.contains("vnp_SecureHash="), "Thiếu chữ ký vnp_SecureHash");
        assertTrue(resultUrl.contains("vnp_ReturnUrl="), "Thiếu vnp_ReturnUrl");
        assertTrue(resultUrl.contains("vnp_Version=2.1.0"), "Thiếu vnp_Version");
        assertTrue(resultUrl.contains("vnp_Command=pay"), "Thiếu vnp_Command");
    }

    /**
     * TC_VPS_02
     * Mục tiêu : Tạo URL với amount rất lớn (đơn hàng 10 triệu VND) — kiểm tra
     * không overflow.
     * Đầu vào : total = 10000000L (10 triệu VND)
     * Kết quả KV: resultUrl != null
     * URL chứa: vnp_Amount=1000000000 (10_000_000 * 100)
     * URL chứa: vnp_SecureHash= (chữ ký vẫn được tạo bình thường)
     */
    @Test // [Edge Case] Amount rất lớn (10 triệu VND) — kiểm tra không overflow
    void TC_VPS_02() {
        Long amount = 10000000L;
        String orderInfo = "Don hang lon";
        String urlReturn = "http://localhost:4200";

        String resultUrl = vnPayService.createOrder(amount, orderInfo, urlReturn);

        assertNotNull(resultUrl, "URL không được null");
        assertTrue(resultUrl.contains("vnp_Amount=1000000000"),
                "Amount 10_000_000 * 100 phải = 1_000_000_000");
        assertTrue(resultUrl.contains("vnp_SecureHash="), "Thiếu chữ ký");
    }

    /**
     * TC_VPS_03
     * Mục tiêu : Tạo URL với amount = 1 (giá trị nhỏ nhất có ý nghĩa) — kiểm tra
     * giá trị biên.
     * Đầu vào : total = 1L
     * Kết quả KV: URL chứa: vnp_Amount=100 (1 * 100)
     * URL không null và hợp lệ
     */
    @Test // [Edge Case] Amount = 1 (giá trị biên nhỏ nhất) — vnp_Amount phải = 100
    void TC_VPS_03() {
        Long amount = 1L;
        String orderInfo = "Test don hang nho";
        String urlReturn = "http://localhost:4200";

        String resultUrl = vnPayService.createOrder(amount, orderInfo, urlReturn);

        assertNotNull(resultUrl, "URL không được null");
        assertTrue(resultUrl.contains("vnp_Amount=100"),
                "Amount 1 * 100 phải = 100");
    }

    // ==========================================
    // MODULE: XÁC THỰC CALLBACK VNPAY — orderReturn()
    // Nhánh 1: signValue == secureHash && status=="00" → return 1
    // Nhánh 2: signValue == secureHash && status!="00" → return 0
    // Nhánh 3: signValue != secureHash → return -1
    // ==========================================

    /**
     * TC_VPS_04
     * Mục tiêu : Chữ ký HMAC-SHA512 hợp lệ, TransactionStatus = "00" → giao dịch
     * thành công.
     * Đầu vào : MockHttpServletRequest với đầy đủ params,
     * vnp_TransactionStatus="00"
     * vnp_SecureHash được tính đúng từ VnPayConfig.hashAllFields()
     * Kết quả KV: result = 1
     */
    @Test // [Happy Path] Chữ ký đúng + TransactionStatus=00 → return 1 (thành công)
    void TC_VPS_04() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Thanh+toan+don+hang");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "TXN123456");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TxnRef", "12345678");

        Map<String, String> encodedParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
            String encodedVal = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
            encodedParams.put(encodedKey, encodedVal);
            request.addParameter(entry.getKey(), entry.getValue());
        }
        String validHash = VnPayConfig.hashAllFields(encodedParams);
        request.addParameter("vnp_SecureHash", validHash);

        int result = vnPayService.orderReturn(request);

        assertEquals(1, result, "Chữ ký hợp lệ + TransactionStatus=00 phải trả về 1");
    }

    /**
     * TC_VPS_05
     * Mục tiêu : Chữ ký hợp lệ nhưng TransactionStatus ≠ "00" (user hủy, mã 02) →
     * giao dịch thất bại.
     * Đầu vào : MockHttpServletRequest, vnp_TransactionStatus="02",
     * vnp_ResponseCode="24"
     * vnp_SecureHash được tính đúng từ VnPayConfig.hashAllFields()
     * Kết quả KV: result = 0
     */
    @Test // [Branch Coverage] Nhánh: chữ ký đúng nhưng TransactionStatus≠00 → return 0
          // (thất bại)
    void TC_VPS_05() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Thanh+toan+don+hang");
        params.put("vnp_ResponseCode", "24");
        params.put("vnp_TransactionNo", "TXN999");
        params.put("vnp_TransactionStatus", "02"); // ← khác "00"
        params.put("vnp_TxnRef", "99999999");

        Map<String, String> encodedParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
            String encodedVal = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
            encodedParams.put(encodedKey, encodedVal);
            request.addParameter(entry.getKey(), entry.getValue());
        }
        String validHash = VnPayConfig.hashAllFields(encodedParams);
        request.addParameter("vnp_SecureHash", validHash);

        int result = vnPayService.orderReturn(request);

        assertEquals(0, result, "Chữ ký hợp lệ + TransactionStatus≠00 phải trả về 0");
    }

    /**
     * TC_VPS_06
     * Mục tiêu : Chữ ký HMAC-SHA512 không khớp (request giả mạo hoặc bị tampered) →
     * bảo mật thất bại.
     * Đầu vào : MockHttpServletRequest với vnp_SecureHash =
     * "INVALID_FAKE_HASH_XYZ_123456"
     * (không khớp với dữ liệu params)
     * Kết quả KV: result = -1
     */
    @Test // [Branch Coverage] Nhánh: chữ ký không khớp (request giả mạo) → return -1
    void TC_VPS_06() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("vnp_Amount", "10000000");
        request.addParameter("vnp_ResponseCode", "00");
        request.addParameter("vnp_TransactionStatus", "00");
        request.addParameter("vnp_TxnRef", "12345678");
        request.addParameter("vnp_SecureHash", "INVALID_FAKE_HASH_XYZ_123456");

        int result = vnPayService.orderReturn(request);

        assertEquals(-1, result, "Chữ ký sai phải trả về -1 (bảo mật)");
    }

    /**
     * TC_VPS_07
     * Mục tiêu : Request hoàn toàn rỗng — không có bất kỳ tham số VNPay nào.
     * Kịch bản: Hacker gọi thẳng API không truyền params.
     * Đầu vào : MockHttpServletRequest trống (không addParameter gì cả)
     * Kết quả KV: result = -1 (hash rỗng không khớp → sai chữ ký)
     */
    @Test // [Edge Case] Request hoàn toàn rỗng, không có tham số VNPay nào → return -1
    void TC_VPS_07() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // Không thêm bất kỳ tham số nào

        int result = vnPayService.orderReturn(request);

        assertEquals(-1, result,
                "Request rỗng không có tham số VNPay phải trả về -1");
    }
}
