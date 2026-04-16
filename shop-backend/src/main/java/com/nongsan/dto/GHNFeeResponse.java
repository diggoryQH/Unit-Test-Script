package com.nongsan.dto;

import lombok.Data;

@Data
public class GHNFeeResponse {
    private int code;
    private String message;
    private FeeData data;

    @Data
    public static class FeeData {
        private Double total; // Đây là số tiền phí ship cuối cùng
        private Double service_fee;
    }
}