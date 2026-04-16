package com.nongsan.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nongsan.dto.CreatePaymentRequest;
import com.nongsan.service.VNPayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VnPayController {

  private final VNPayService vnPayService;

  /**
   * API này được gọi bởi Angular sau khi VNPAY chuyển hướng về Frontend.
   * Tên Endpoint: /api/vnpay/check-payment-status (tôi sẽ đổi thành /vnpay/return như yêu cầu)
   * Angular sẽ truyền toàn bộ query params (bao gồm vnp_SecureHash) vào request này.
   */
  @GetMapping("/vnpay/return") // Endpoint này sẽ nhận các tham số VNPAY từ Angular
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {

        // Bước 1: Kiểm tra chữ ký và trạng thái giao dịch
        // Logic nằm trong VNPayService.orderReturn (Đã đảm bảo không bị encode params)
        int paymentStatus = vnPayService.orderReturn(request);

        // Lấy các tham số VNPAY
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String amount = request.getParameter("vnp_Amount"); // Số tiền (đã nhân 100)
        String responseCode = request.getParameter("vnp_ResponseCode");
        
        // Tạo đối tượng JSON trả về cho Angular
        Map<String, Object> response = new HashMap<>();
        
        // Thêm các thông tin cơ bản
        response.put("vnp_OrderInfo", orderInfo);
        response.put("vnp_PayDate", paymentTime);
        response.put("vnp_TransactionNo", transactionId);
        response.put("vnp_Amount", amount);
        response.put("vnp_ResponseCode", responseCode);

        // Bước 2: Xử lý trạng thái cuối cùng
        if (paymentStatus == 1) {
            // Trường hợp 1: Thành công (Chữ ký Hợp lệ và ResponseCode = 00)
            // THÊM LOGIC CẬP NHẬT DATABASE (Order status = PAID) TẠI ĐÂY
            // ví dụ: orderService.markOrderAsPaid(extractOrderId(orderInfo));
            
            response.put("status", "SUCCESS");
            response.put("message", "Thanh toán thành công qua VNPAY.");
            return ResponseEntity.ok(response);
            
        } else if (paymentStatus == 0) {
            // Trường hợp 2: Thất bại (Chữ ký Hợp lệ nhưng ResponseCode != 00)
            // THÊM LOGIC CẬP NHẬT DATABASE (Order status = FAILED) TẠI ĐÂY
            
            response.put("status", "FAILED");
            response.put("message", "Giao dịch thất bại. Mã VNPAY: " + responseCode);
            return ResponseEntity.ok(response);
            
        } else {
            // Trường hợp 3: Sai chữ ký (paymentStatus = -1)
            response.put("status", "INVALID_SIGNATURE");
            response.put("message", "Lỗi bảo mật: Sai chữ ký (Mã lỗi 97).");
            return ResponseEntity.badRequest().body(response);
        }
    }

  @PostMapping("/vnpay/create-payment")
  public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest req, HttpServletRequest request) throws Exception {
      // String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
      String baseUrl = "http://localhost:4200";
        String vnpayUrl = vnPayService.createOrder(req.getAmount().longValue(), "Thanh toan don hang", baseUrl);
      
    Map<String, String> res = new HashMap<>();
    res.put("paymentUrl", vnpayUrl);
    return ResponseEntity.ok(res);
  }

  

}

