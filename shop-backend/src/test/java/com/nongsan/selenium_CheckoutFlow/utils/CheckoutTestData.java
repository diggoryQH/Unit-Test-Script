package com.nongsan.selenium_CheckoutFlow.utils;

public class CheckoutTestData {
    // Thông tin đăng nhập
    public static final String VALID_EMAIL = "nguyenkhaihung1512004nb@gmail.com";
    public static final String VALID_PASSWORD = "hung123456";

    // Thông tin giao hàng mẫu
    public static final String TEST_PHONE = "0345678912";
    public static final String TEST_ADDRESS = "Số 10, Liễu Giai";
    
    // Chỉ số index cho Province, District, Ward
    public static final int PROVINCE_INDEX = 1;
    public static final int DISTRICT_INDEX = 1;
    public static final int WARD_INDEX = 1;

    // SQL Injection payloads
    public static final String SQLI_PHONE = "0' OR '1'='1";
    public static final String SQLI_ADDRESS = "'; DROP TABLE orders; --";

    // Phí vận chuyển và tổng tiền mẫu
    public static final String EXPECTED_SHIPPING_FEE = "31,000";

    public static final String CHECKOUT_URL = "http://localhost:4200/checkout";
    public static final String CART_URL = "http://localhost:4200/cart";
}
