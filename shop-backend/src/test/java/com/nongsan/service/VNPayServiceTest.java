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
 *   - createOrder(Long total, String orderInfor, String urlReturn)
 *   - orderReturn(HttpServletRequest request)
 */
class VNPayServiceTest {

    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        // VNPayService không có dependency @Autowired → khởi tạo trực tiếp
        vnPayService = new VNPayService();
    }

    // ==========================================
    // MODULE: TẠO URL THANH TOÁN (createOrder)
    // ==========================================

    /**
     * Test Case ID: TC_VNPAYSERVICE_01
     * Mô tả: Tạo URL thanh toán thành công — URL chứa đầy đủ các tham số bắt buộc của VNPay.
     *        Nhánh: Happy path duy nhất (không có if trong createOrder).
     */
    @Test
    void createOrder_testChuan1_urlHopLe() {
        Long amount = 100000L;
        String orderInfo = "Thanh toan don hang";
        String urlReturn = "http://localhost:4200";

        String resultUrl = vnPayService.createOrder(amount, orderInfo, urlReturn);

        // URL phải không null và là URL thanh toán sandbox VNPay
        assertNotNull(resultUrl, "URL không được null");
        assertTrue(resultUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"),
                "URL phải bắt đầu bằng VNPay sandbox URL");

        // Phải chứa các tham số bắt buộc
        assertTrue(resultUrl.contains("vnp_TmnCode=ITNF0PR5"), "Thiếu vnp_TmnCode");
        assertTrue(resultUrl.contains("vnp_Amount=10000000"), "Amount phải x100: 100000 * 100 = 10000000");
        assertTrue(resultUrl.contains("vnp_CurrCode=VND"), "Thiếu vnp_CurrCode");
        assertTrue(resultUrl.contains("vnp_SecureHash="), "Thiếu chữ ký vnp_SecureHash");
        assertTrue(resultUrl.contains("vnp_ReturnUrl="), "Thiếu vnp_ReturnUrl");
    }

    // ==========================================
    // MODULE: XÁC THỰC CALLBACK VNPAY (orderReturn)
    // Có 3 nhánh:
    //   Nhánh 1: Chữ ký đúng + TransactionStatus=00  → return 1 (thành công)
    //   Nhánh 2: Chữ ký đúng + TransactionStatus≠00  → return 0 (thất bại)
    //   Nhánh 3: Chữ ký sai                          → return -1 (bảo mật)
    // ==========================================

    /**
     * TC_VNPAYSERVICE_02
     * Mô tả: Nhánh 1 — Chữ ký HMAC-SHA512 hợp lệ và TransactionStatus = "00" → trả về 1.
     */
    @Test
    void orderReturn_testChuan1_thanhCong() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // Tạo các tham số giao dịch
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Thanh+toan+don+hang");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "TXN123456");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TxnRef", "12345678");

        // Tính hash đúng dựa trên các tham số trên (dùng VnPayConfig thực tế)
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

        assertEquals(1, result, "Chữ ký hợp lệ + status 00 phải trả về 1 (thành công)");
    }

    /**
     * TC_VNPAYSERVICE_03
     * Mô tả: Nhánh 2 — Chữ ký HMAC-SHA512 hợp lệ nhưng TransactionStatus ≠ "00" → trả về 0.
     */
    @Test
    void orderReturn_testNgoaiLe1_giaoDichThatBai() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // Giao dịch bị hủy (user cancel): ResponseCode = 24, TransactionStatus = 02
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Thanh+toan+don+hang");
        params.put("vnp_ResponseCode", "24");
        params.put("vnp_TransactionNo", "TXN999");
        params.put("vnp_TransactionStatus", "02"); // ← không phải "00"
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

        assertEquals(0, result, "Chữ ký hợp lệ + status ≠ 00 phải trả về 0 (thất bại)");
    }

    /**
     * TC_VNPAYSERVICE_04
     * Mô tả: Nhánh 3 — Chữ ký HMAC-SHA512 không hợp lệ (giả mạo) → trả về -1.
     */
    @Test
    void orderReturn_testNgoaiLe2_saiChuKy() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("vnp_Amount", "10000000");
        request.addParameter("vnp_ResponseCode", "00");
        request.addParameter("vnp_TransactionStatus", "00");
        request.addParameter("vnp_TxnRef", "12345678");
        // Truyền hash SAI (giả mạo)
        request.addParameter("vnp_SecureHash", "INVALID_FAKE_HASH_XYZ_123456");

        int result = vnPayService.orderReturn(request);

        assertEquals(-1, result, "Chữ ký không hợp lệ phải trả về -1 (bảo mật)");
    }
}
