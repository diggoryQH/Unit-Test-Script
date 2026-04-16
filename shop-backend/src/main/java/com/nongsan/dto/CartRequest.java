package com.nongsan.dto;

import lombok.Data;

@Data
public class CartRequest {

    private Long cartId;
    private Double amount;
    private String address;
    private String phone;
    private Double shippingFee;

}
