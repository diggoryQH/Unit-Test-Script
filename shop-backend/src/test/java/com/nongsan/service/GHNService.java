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

class GHNServiceTest {

    private GHNService ghnService;

    @BeforeEach
    void setUp() {
        ghnService = new GHNService();
    }

    @Test
    void calculateFee_testChuan1() {

        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

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

            assertEquals(35000.0, result);
        }
    }

    @Test
    void calculateFee_testNgoaile1() {

        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 GHNFeeResponse responseObj = new GHNFeeResponse();
                                 responseObj.setCode(400);

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

            assertEquals(0.0, result);
        }
    }

    @Test
    void calculateFee_testNgoaile2() {

        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

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

            assertEquals(0.0, result);
        }
    }

    @Test
    void calculateFee_testNgoaile3() {

        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 when(mock.exchange(
                                         anyString(),
                                         eq(HttpMethod.GET),
                                         any(),
                                         eq(GHNFeeResponse.class)
                                 )).thenThrow(new RuntimeException("API lỗi"));
                             })) {

            Double result = ghnService.calculateFee("20308", 1454, 500);

            assertEquals(0.0, result);
        }
    }

    @Test
    void calculateFee_testChuan2() {

        try (MockedConstruction<RestTemplate> mocked =
                     Mockito.mockConstruction(RestTemplate.class,
                             (mock, context) -> {

                                 GHNFeeResponse responseObj = new GHNFeeResponse();
                                 responseObj.setCode(200);

                                 FeeData data = new FeeData();
                                 data.setTotal(15000.0);
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

            ghnService.calculateFee("20308", 1454, 300);

            RestTemplate mockTemplate = mocked.constructed().get(0);

            verify(mockTemplate, times(1)).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(),
                    eq(GHNFeeResponse.class)
            );
        }
    }
}