package com.nongsan.utils;

import java.nio.charset.StandardCharsets; // CẦN IMPORT NÀY

// Trong VnPayUtil.java

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            
            // SỬA ĐỔI: Chỉ định rõ ràng UTF-8 cho Khóa bí mật
            SecretKeySpec secretKey = 
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);

            // SỬA ĐỔI: Chỉ định rõ ràng UTF-8 cho Dữ liệu băm
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hash = new StringBuilder();

            for (byte b : bytes) {
                // Đảm bảo định dạng Hexa chữ thường (VNPAY yêu cầu)
                hash.append(String.format("%02x", b));
            }
            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException("Cannot hash", e);
        }
    }
}