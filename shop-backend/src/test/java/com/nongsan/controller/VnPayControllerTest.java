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

    // Giả lập Security (bắt buộc để WebMvcTest không lỗi)
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: TẠO URL THANH TOÁN VNPAY (POST)
    // ==========================================

    /**
     * Test Case ID: TC_VNPAY_01
     * Mô tả: Tạo URL thanh toán thành công khi amount hợp lệ.
     *        Kết quả: 200 OK + trả về JSON có field "paymentUrl".
     */
    @Test
    void createPayment_testChuan1() throws Exception {
        String fakePaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_SecureHash=abc123";

        Mockito.when(vnPayService.createOrder(anyLong(), anyString(), anyString()))
                .thenReturn(fakePaymentUrl);

        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(100000.0); // 100,000 VND

        mockMvc.perform(post("/api/vnpay/create-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").value(fakePaymentUrl));

        Mockito.verify(vnPayService).createOrder(eq(100000L), anyString(), anyString());
    }

    // ==========================================
    // MODULE: XỬ LÝ CALLBACK VNPAY (GET)
    // ==========================================

    /**
     * Test Case ID: TC_VNPAY_02
     * Mô tả: Thanh toán thành công — VNPay gọi callback với chữ ký hợp lệ
     *        và vnp_TransactionStatus = 00.
     *        Kết quả: 200 OK + status = "SUCCESS".
     */
    @Test
    void vnpayReturn_testChuan1_thanhCong() throws Exception {
        // orderReturn trả về 1 = thành công
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
     * Test Case ID: TC_VNPAY_03
     * Mô tả: Thanh toán thất bại — VNPay gọi callback với chữ ký hợp lệ
     *        nhưng vnp_TransactionStatus != 00 (người dùng hủy hoặc lỗi ngân hàng).
     *        Kết quả: 200 OK + status = "FAILED".
     */
    @Test
    void vnpayReturn_testNgoaiLe1_giaoDichThatBai() throws Exception {
        // orderReturn trả về 0 = giao dịch thất bại (hash đúng nhưng status != 00)
        Mockito.when(vnPayService.orderReturn(any())).thenReturn(0);

        mockMvc.perform(get("/api/vnpay/return")
                        .param("vnp_OrderInfo", "Thanh toan don hang")
                        .param("vnp_PayDate", "20260417120000")
                        .param("vnp_TransactionNo", "TXN999")
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "24")  // 24 = user cancelled
                        .param("vnp_SecureHash", "validhash123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.vnp_ResponseCode").value("24"));

        Mockito.verify(vnPayService).orderReturn(any());
    }

    /**
     * Test Case ID: TC_VNPAY_04
     * Mô tả: Sai chữ ký bảo mật — chữ ký VNPay không khớp (có thể bị giả mạo).
     *        Kết quả: 400 Bad Request + status = "INVALID_SIGNATURE".
     */
    @Test
    void vnpayReturn_testNgoaiLe2_saiChuKy() throws Exception {
        // orderReturn trả về -1 = sai chữ ký (bảo mật)
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
}
