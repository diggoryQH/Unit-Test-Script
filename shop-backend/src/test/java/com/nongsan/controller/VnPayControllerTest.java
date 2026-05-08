package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.dto.CreatePaymentRequest;
import com.nongsan.service.VNPayService;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-04: Đặt hàng & Thanh toán
 * File test cho: VnPayController.java — createPayment() & vnpayReturn()
 * Endpoints:
 *   POST /api/vnpay/create-payment
 *   GET  /api/vnpay/return
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VnPayController.class)
@AutoConfigureMockMvc(addFilters = false)
class VnPayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VNPayService vnPayService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: TẠO URL THANH TOÁN — POST /api/vnpay/create-payment
    // ==========================================

    /**
     * TC_VNPAY_01
     * Mục tiêu  : Tạo URL thanh toán thành công khi amount hợp lệ.
     * Đầu vào   : body CreatePaymentRequest(amount=100000.0)
     * Hành vi GS: vnPayService.createOrder(100000L, ...) → fakePaymentUrl
     * Kết quả KV: HTTP 200 OK
     *             body.paymentUrl = fakePaymentUrl (URL sandbox VNPay)
     * Verify    : vnPayService.createOrder được gọi với amount=100000L (amount.longValue())
     */
    @Test // [Happy Path] Tạo URL thanh toán thành công — amount hợp lệ
    void TC_VNPAY_01() throws Exception {
        String fakePaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_SecureHash=abc123";

        Mockito.when(vnPayService.createOrder(anyLong(), anyString(), anyString()))
                .thenReturn(fakePaymentUrl);

        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(100000.0);

        mockMvc.perform(post("/api/vnpay/create-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").value(fakePaymentUrl));

        Mockito.verify(vnPayService).createOrder(eq(100000L), anyString(), anyString());
    }

    /**
     * TC_VNPAY_02
     * Mục tiêu  : Gửi amount = 0 — xác nhận Controller không validate tại tầng này,
     *             vẫn chuyển tiếp cho Service xử lý (0.0.longValue() = 0L).
     * Đầu vào   : body CreatePaymentRequest(amount=0.0)
     * Hành vi GS: vnPayService.createOrder(0L, ...) → fakePaymentUrl
     * Kết quả KV: HTTP 200 OK (Controller không chặn amount=0)
     * Verify    : vnPayService.createOrder được gọi với amount=0L
     */
    @Test // [Edge Case] Amount = 0 — Controller không validate, vẫn chuyển tiếp cho Service
    void TC_VNPAY_02() throws Exception {
        String fakePaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=0";

        Mockito.when(vnPayService.createOrder(anyLong(), anyString(), anyString()))
                .thenReturn(fakePaymentUrl);

        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(0.0);

        mockMvc.perform(post("/api/vnpay/create-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").isNotEmpty());

        Mockito.verify(vnPayService).createOrder(eq(0L), anyString(), anyString());
    }

    // ==========================================
    // MODULE: XỬ LÝ CALLBACK VNPAY — GET /api/vnpay/return
    // ==========================================

    /**
     * TC_VNPAY_03
     * Mục tiêu  : Thanh toán thành công — chữ ký hợp lệ, vnp_TransactionStatus = "00".
     * Đầu vào   : query params đầy đủ, vnp_ResponseCode="00", vnp_SecureHash hợp lệ
     * Hành vi GS: vnPayService.orderReturn(request) → 1 (thành công)
     * Kết quả KV: HTTP 200 OK
     *             body.status             = "SUCCESS"
     *             body.vnp_ResponseCode   = "00"
     *             body.vnp_TransactionNo  = "TXN123456"
     * Verify    : vnPayService.orderReturn được gọi đúng 1 lần
     */
    @Test // [Happy Path] Callback VNPay — chữ ký hợp lệ + status=00 → SUCCESS
    void TC_VNPAY_03() throws Exception {
        Mockito.when(vnPayService.orderReturn(any())).thenReturn(1);

        mockMvc.perform(get("/api/vnpay/return")
                        .param("vnp_OrderInfo", "Thanh toan don hang")
                        .param("vnp_PayDate", "20260417120000")
                        .param("vnp_TransactionNo", "TXN123456")
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "validhash123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.vnp_ResponseCode").value("00"))
                .andExpect(jsonPath("$.vnp_TransactionNo").value("TXN123456"));

        Mockito.verify(vnPayService).orderReturn(any());
    }

    /**
     * TC_VNPAY_04
     * Mục tiêu  : Thanh toán thất bại — chữ ký hợp lệ nhưng user hủy giao dịch (ResponseCode="24").
     * Đầu vào   : vnp_ResponseCode="24", vnp_SecureHash hợp lệ
     * Hành vi GS: vnPayService.orderReturn(request) → 0 (giao dịch thất bại)
     * Kết quả KV: HTTP 200 OK
     *             body.status           = "FAILED"
     *             body.vnp_ResponseCode = "24"
     * Verify    : vnPayService.orderReturn được gọi đúng 1 lần
     */
    @Test // [Branch Coverage] Nhánh: chữ ký đúng, status≠00 (user hủy) → FAILED
    void TC_VNPAY_04() throws Exception {
        Mockito.when(vnPayService.orderReturn(any())).thenReturn(0);

        mockMvc.perform(get("/api/vnpay/return")
                        .param("vnp_OrderInfo", "Thanh toan don hang")
                        .param("vnp_PayDate", "20260417120000")
                        .param("vnp_TransactionNo", "TXN999")
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "24")
                        .param("vnp_SecureHash", "validhash123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.vnp_ResponseCode").value("24"));

        Mockito.verify(vnPayService).orderReturn(any());
    }

    /**
     * TC_VNPAY_05
     * Mục tiêu  : Sai chữ ký bảo mật — request có thể bị giả mạo (INVALID_SIGNATURE).
     * Đầu vào   : vnp_SecureHash = "INVALID_HASH_XYZ" (không khớp với dữ liệu)
     * Hành vi GS: vnPayService.orderReturn(request) → -1 (sai chữ ký)
     * Kết quả KV: HTTP 400 Bad Request
     *             body.status = "INVALID_SIGNATURE"
     * Verify    : vnPayService.orderReturn được gọi đúng 1 lần
     */
    @Test // [Branch Coverage] Nhánh: chữ ký sai → 400 Bad Request + INVALID_SIGNATURE
    void TC_VNPAY_05() throws Exception {
        Mockito.when(vnPayService.orderReturn(any())).thenReturn(-1);

        mockMvc.perform(get("/api/vnpay/return")
                        .param("vnp_OrderInfo", "Thanh toan don hang")
                        .param("vnp_PayDate", "20260417120000")
                        .param("vnp_TransactionNo", "TXN000")
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "INVALID_HASH_XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID_SIGNATURE"));

        Mockito.verify(vnPayService).orderReturn(any());
    }

    /**
     * TC_VNPAY_06
     * Mục tiêu  : Callback thiếu tham số vnp_Amount — kiểm tra Controller không crash.
     *             Controller dùng request.getParameter() → trả về null nếu thiếu tham số.
     *             Hệ thống vẫn xử lý dựa vào kết quả của orderReturn.
     * Đầu vào   : Chỉ truyền vnp_OrderInfo, vnp_ResponseCode, vnp_SecureHash (thiếu vnp_Amount)
     * Hành vi GS: vnPayService.orderReturn(request) → 1 (giả sử chữ ký vẫn hợp lệ)
     * Kết quả KV: HTTP 200 OK + status="SUCCESS"
     *             body.vnp_Amount = null (key không có trong response vì giá trị null)
     * Verify    : vnPayService.orderReturn được gọi đúng 1 lần, hệ thống không ném exception
     */
    @Test // [Edge Case] Thiếu tham số vnp_Amount — hệ thống không crash, vẫn trả kết quả
    void TC_VNPAY_06() throws Exception {
        Mockito.when(vnPayService.orderReturn(any())).thenReturn(1);

        mockMvc.perform(get("/api/vnpay/return")
                        .param("vnp_OrderInfo", "Thanh toan don hang")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "validhash123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        Mockito.verify(vnPayService).orderReturn(any());
    }
}
