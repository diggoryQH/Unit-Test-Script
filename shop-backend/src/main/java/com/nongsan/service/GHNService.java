package com.nongsan.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.nongsan.dto.GHNFeeResponse;

@Service
public class GHNService {

    // Lấy thông tin từ hình ảnh bạn cung cấp
    private final String TOKEN = "8070663b-5f69-11ef-8105-4601d6f86484";
    private final String SHOP_ID = "5275649";
    private final String FEE_URL = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

    public Double calculateFee(String toWardCode, Integer toDistrictId, Integer weight) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Thiết lập Header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // GHN API Fee hỗ trợ truyền qua Query Parameters cho GET hoặc Body cho POST
        // Ở đây ta dùng cách build URL với Query Params như link bạn gửi
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FEE_URL)
                .queryParam("to_ward_code", toWardCode)
                .queryParam("to_district_id", toDistrictId)
                .queryParam("weight", weight)
                .queryParam("service_type_id", 2); // 2: Chuyển phát chuẩn

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GHNFeeResponse> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    GHNFeeResponse.class
            );

            if (response.getBody() != null && response.getBody().getCode() == 200) {
                return response.getBody().getData().getTotal();
            }
        } catch (Exception e) {
            System.out.println("Lỗi tính phí GHN: " + e.getMessage());
        }
        return 0.0; // Trả về 0 nếu lỗi
    }
}
