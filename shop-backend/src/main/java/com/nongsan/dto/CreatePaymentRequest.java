package com.nongsan.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Double amount;
    private String phone;
    private String address;
}

