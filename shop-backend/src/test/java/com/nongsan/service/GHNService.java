package com.nongsan.service;

import com.nongsan.dto.GHNFeeResponse;
import com.nongsan.dto.GHNFeeResponse.FeeData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Lớp kiểm thử cho GHNService (Tích hợp Giao Hàng Nhanh).
 */
class GHNServiceTest {

    private GHNService ghnService;

    @BeforeEach
    void setUp() {
        // Khởi tạo Service cần test trước mỗi Test Case
        ghnService = new GHNService();
    }

    /**
     * Test Case ID: TC_GHN_01
     * Mô tả: Tính phí thành công khi API GHN trả về đúng định dạng (code = 200, có total fee).
     */
    @Test
    void calculateFee_testChuan1() {
        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 // Giả lập dữ liệu trả về thành công từ GHN
                                 GHNFeeResponse responseObj = new GHNFeeResponse();
                                 responseObj.setCode(200);

                                 FeeData data = new FeeData();
                                 data.setTotal(35000.0);
                                 responseObj.setData(data);

                                 when(mock.exchange(
                                         anyString(),
                                         eq(HttpMethod.GET),
                                         any(),
                                         eq(GHNFeeResponse.class)
                                 )).thenReturn(
                                         new ResponseEntity<>(responseObj, HttpStatus.OK)
                                 );
                             })) {

            Double result = ghnService.calculateFee("20308", 1454, 500);

            // Phải trả về đúng mức phí lấy từ API
            assertEquals(35000.0, result, "Phí trả về phải khớp với dữ liệu từ API GHN");
        }
    }

    /**
     * Test Case ID: TC_GHN_02
     * Mô tả: Gọi API thành công nhưng GHN báo lỗi dữ liệu (ví dụ: code = 400).
     */
    @Test
    void calculateFee_testNgoaile1() {
        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 GHNFeeResponse responseObj = new GHNFeeResponse();
                                 responseObj.setCode(400); // Mã code lỗi

                                 when(mock.exchange(
                                         anyString(),
                                         eq(HttpMethod.GET),
                                         any(),
                                         eq(GHNFeeResponse.class)
                                 )).thenReturn(
                                         new ResponseEntity<>(responseObj, HttpStatus.OK)
                                 );
                             })) {

            Double result = ghnService.calculateFee("20308", 1454, 500);

            // Bỏ qua lệnh if, phải trả về 0.0
            assertEquals(0.0, result, "Khi code API không phải 200, phí trả về phải là 0.0");
        }
    }

    /**
     * Test Case ID: TC_GHN_03
     * Mô tả: Gọi API thành công nhưng body trả về bị null.
     */
    @Test
    void calculateFee_testNgoaile2() {
        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 // Trả về body rỗng
                                 when(mock.exchange(
                                         anyString(),
                                         eq(HttpMethod.GET),
                                         any(),
                                         eq(GHNFeeResponse.class)
                                 )).thenReturn(
                                         new ResponseEntity<>(null, HttpStatus.OK)
                                 );
                             })) {

            Double result = ghnService.calculateFee("20308", 1454, 500);

            // Bỏ qua lệnh if, phải trả về 0.0
            assertEquals(0.0, result, "Khi response body là null, phí trả về phải là 0.0");
        }
    }

    /**
     * Test Case ID: TC_GHN_04
     * Mô tả: Lỗi hệ thống khi gọi API (đứt cáp, sai URL) quăng ra Exception.
     */
    @Test
    void calculateFee_testNgoaile3() {
        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 // Giả lập rớt mạng ném Exception
                                 when(mock.exchange(
                                         anyString(),
                                         eq(HttpMethod.GET),
                                         any(),
                                         eq(GHNFeeResponse.class)
                                 )).thenThrow(new RuntimeException("API lỗi kết nối mạng"));
                             })) {

            Double result = ghnService.calculateFee("20308", 1454, 500);

            // Nhảy vào catch, phải trả về 0.0
            assertEquals(0.0, result, "Khi xảy ra Exception, phí trả về phải là 0.0");
        }
    }
}