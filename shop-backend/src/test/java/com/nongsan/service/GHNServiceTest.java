package com.nongsan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class GHNServiceTest {

	private static final String EXPECTED_URL =
			"https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee?to_ward_code=12345&to_district_id=1458&weight=1000&service_type_id=2";

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private GHNService ghnService;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		ghnService = new TestableGHNService(restTemplate);
	}

	@Test
	void calculateFee_shouldReturnTotalWhenApiRespondsSuccessfully() {
		server.expect(requestTo(EXPECTED_URL))
				.andExpect(header("Token", "8070663b-5f69-11ef-8105-4601d6f86484"))
				.andExpect(header("ShopId", "5275649"))
				.andRespond(withSuccess(
						"{\"code\":200,\"message\":\"Success\",\"data\":{\"total\":15000.0,\"service_fee\":12000.0}}",
						MediaType.APPLICATION_JSON));

		Double fee = ghnService.calculateFee("12345", 1458, 1000);

		assertEquals(15000.0, fee, 0.0001);
		server.verify();
	}

	@Test
	void calculateFee_shouldReturnZeroWhenApiCodeIsNotSuccess() {
		server.expect(requestTo(EXPECTED_URL))
				.andRespond(withSuccess(
						"{\"code\":400,\"message\":\"Invalid request\",\"data\":{\"total\":15000.0}}",
						MediaType.APPLICATION_JSON));

		Double fee = ghnService.calculateFee("12345", 1458, 1000);

		assertEquals(0.0, fee, 0.0001);
		server.verify();
	}

	@Test
	void calculateFee_shouldReturnZeroWhenGatewayFails() {
		server.expect(requestTo(EXPECTED_URL))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		Double fee = ghnService.calculateFee("12345", 1458, 1000);

		assertEquals(0.0, fee, 0.0001);
		server.verify();
	}

	private static class TestableGHNService extends GHNService {

		private final RestTemplate restTemplate;

		private TestableGHNService(RestTemplate restTemplate) {
			this.restTemplate = restTemplate;
		}

		@Override
		protected RestTemplate createRestTemplate() {
			return restTemplate;
		}
	}
}
