package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.dto.CategoryBestSeller;
import com.nongsan.dto.Statistical;
import com.nongsan.entity.Order;
import com.nongsan.entity.Product;
import com.nongsan.repository.OrderRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.StatisticalRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StatisticalApi.class)
@AutoConfigureMockMvc(addFilters = false)
class StatisticalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @MockBean
    private StatisticalRepository statisticalRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void getAdvancedReport_testChuan1() throws Exception {
	int year = 2026;
	
	// Mock financial data: [revenue, cost, shipping]
	Object[] financialData = {5000000.0, 3000000.0, 500000.0};
	Mockito.when(statisticalRepository.getFinancialData(year))
		.thenReturn(Collections.singletonList(financialData));
	
	// Mock order statistics
	Mockito.when(orderRepository.countByStatus(2)).thenReturn(150L); // success
	Mockito.when(orderRepository.countByStatus(3)).thenReturn(10L);  // canceled
	Mockito.when(orderRepository.countByStatus(6)).thenReturn(5L);   // returned

	mockMvc.perform(get("/api/statistical/advanced-report/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.totalRevenue").value(5000000.0))
		.andExpect(jsonPath("$.grossProfit").value(2000000.0))
		.andExpect(jsonPath("$.orderStats.success").value(150))
		.andExpect(jsonPath("$.orderStats.canceled").value(10))
		.andExpect(jsonPath("$.orderStats.returned").value(5));

	Mockito.verify(statisticalRepository).getFinancialData(year);
	Mockito.verify(orderRepository).countByStatus(2);
	Mockito.verify(orderRepository).countByStatus(3);
	Mockito.verify(orderRepository).countByStatus(6);
    }

    @Test
    void getRevenueByYear_testChuan1() throws Exception {
	int year = 2025;
	double revenue = 15500000.0;

	Mockito.when(statisticalRepository.getRevenueByYear(year)).thenReturn(revenue);

	mockMvc.perform(get("/api/statistical/revenue/year/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").value(revenue));

	Mockito.verify(statisticalRepository).getRevenueByYear(year);
    }

    @Test
    void getRevenueByYear_testNgoaiLe1() throws Exception {
	int year = 2020;

	Mockito.when(statisticalRepository.getRevenueByYear(year)).thenReturn(0.0);

	mockMvc.perform(get("/api/statistical/revenue/year/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").value(0.0));

	Mockito.verify(statisticalRepository).getRevenueByYear(year);
    }

    @Test
    void getYears_testChuan1() throws Exception {
	List<Integer> years = Arrays.asList(2024, 2025, 2026);

	Mockito.when(statisticalRepository.getYears()).thenReturn(years);

	mockMvc.perform(get("/api/statistical/countYear"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(3))
		.andExpect(jsonPath("$[0]").value(2024))
		.andExpect(jsonPath("$[1]").value(2025))
		.andExpect(jsonPath("$[2]").value(2026));

	Mockito.verify(statisticalRepository).getYears();
    }

    @Test
    void getAllOrderSuccess_testChuan1() throws Exception {
	Order order1 = new Order();
	order1.setOrdersId(1L);
	order1.setStatus(2);
	order1.setAmount(1000000.0);

	Order order2 = new Order();
	order2.setOrdersId(2L);
	order2.setStatus(2);
	order2.setAmount(2000000.0);

	List<Order> successOrders = Arrays.asList(order1, order2);

	Mockito.when(orderRepository.findByStatus(2)).thenReturn(successOrders);

	mockMvc.perform(get("/api/statistical/get-all-order-success"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(2))
		.andExpect(jsonPath("$[0].status").value(2))
		.andExpect(jsonPath("$[1].status").value(2));

	Mockito.verify(orderRepository).findByStatus(2);
    }

    @Test
    void getAllOrderSuccess_testNgoaiLe1() throws Exception {
	Mockito.when(orderRepository.findByStatus(2)).thenReturn(new ArrayList<>());

	mockMvc.perform(get("/api/statistical/get-all-order-success"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(0));

	Mockito.verify(orderRepository).findByStatus(2);
    }

    @Test
    void getCategoryBestSeller_testChuan1() throws Exception {
	// Mock data: [category_name, quantity, revenue]
	Object[] category = {"Trái cây", 150, 3000000};
	List<Object[]> categoryList = Collections.singletonList(category);

	Mockito.when(statisticalRepository.getCategoryBestSeller()).thenReturn(categoryList);

	mockMvc.perform(get("/api/statistical/get-category-seller"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(1))
		.andExpect(jsonPath("$[0].name").value("Trái cây"))
		.andExpect(jsonPath("$[0].count").value(150))
		.andExpect(jsonPath("$[0].amount").value(3000000.0));

	Mockito.verify(statisticalRepository).getCategoryBestSeller();
    }

    @Test
    void getInventory_testChuan1() throws Exception {
	Product product1 = new Product();
	product1.setProductId(1L);
	product1.setName("Gạo");
	product1.setQuantity(100);
	product1.setStatus(true);

	Product product2 = new Product();
	product2.setProductId(2L);
	product2.setName("Ngô");
	product2.setQuantity(50);
	product2.setStatus(true);

	List<Product> inventory = Arrays.asList(product1, product2);

	Mockito.when(productRepository.findByStatusTrueOrderByQuantityDesc()).thenReturn(inventory);

	mockMvc.perform(get("/api/statistical/get-inventory"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(2))
		.andExpect(jsonPath("$[0].name").value("Gạo"))
		.andExpect(jsonPath("$[0].quantity").value(100))
		.andExpect(jsonPath("$[1].name").value("Ngô"))
		.andExpect(jsonPath("$[1].quantity").value(50));

	Mockito.verify(productRepository).findByStatusTrueOrderByQuantityDesc();
    }

    @Test
    void getStatisticalYear_testChuan1() throws Exception {
	int year = 2026;
	
	// Mock monthly financial data: [month, revenue, cost]
	List<Object[]> monthlyData = new ArrayList<>();
	monthlyData.add(new Object[]{1, 1000000.0, 400000.0});
	monthlyData.add(new Object[]{2, 1200000.0, 500000.0});
	monthlyData.add(new Object[]{3, 1100000.0, 450000.0});
	
	Mockito.when(statisticalRepository.getMonthlyFinancials(year)).thenReturn(monthlyData);

	mockMvc.perform(get("/api/statistical/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(12)) // Should have all 12 months
		.andExpect(jsonPath("$[0].month").value(1))
		.andExpect(jsonPath("$[0].amount").value(1000000.0))
		.andExpect(jsonPath("$[0].profit").value(600000.0)) // revenue - cost
		.andExpect(jsonPath("$[1].month").value(2))
		.andExpect(jsonPath("$[1].amount").value(1200000.0))
		.andExpect(jsonPath("$[1].profit").value(700000.0));

	Mockito.verify(statisticalRepository).getMonthlyFinancials(year);
    }

    @Test
    void getStatisticalYear_testChuan2() throws Exception {
	int year = 2025;
	
	// Mock with fewer months
	List<Object[]> monthlyData = new ArrayList<>();
	monthlyData.add(new Object[]{1, 500000.0, 200000.0});
	monthlyData.add(new Object[]{12, 800000.0, 350000.0});
	
	Mockito.when(statisticalRepository.getMonthlyFinancials(year)).thenReturn(monthlyData);

	mockMvc.perform(get("/api/statistical/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(12)) // Still has all 12 months (including empty ones)
		.andExpect(jsonPath("$[0].month").value(1))
		.andExpect(jsonPath("$[0].amount").value(500000.0))
		.andExpect(jsonPath("$[11].month").value(12))
		.andExpect(jsonPath("$[11].amount").value(800000.0));

	Mockito.verify(statisticalRepository).getMonthlyFinancials(year);
    }

    @Test
    void getAdvancedReport_testChuan2() throws Exception {
	int year = 2024;
	
	// Mock financial data with null values
	Object[] financialData = {null, null, null};
	Mockito.when(statisticalRepository.getFinancialData(year))
		.thenReturn(new ArrayList<>());
	
	Mockito.when(orderRepository.countByStatus(2)).thenReturn(100L);
	Mockito.when(orderRepository.countByStatus(3)).thenReturn(5L);
	Mockito.when(orderRepository.countByStatus(6)).thenReturn(2L);

	mockMvc.perform(get("/api/statistical/advanced-report/{year}", year))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.totalRevenue").value(0.0))
		.andExpect(jsonPath("$.grossProfit").value(0.0));

	Mockito.verify(statisticalRepository).getFinancialData(year);
    }

    @Test
    void getCategoryBestSeller_testChuan2() throws Exception {
	// Mock multiple categories
	Object[] category1 = {"Trái cây", 150, 3000000};
	Object[] category2 = {"Rau xanh", 200, 2500000};
	Object[] category3 = {"Cá cạn", 80, 2000000};
	
	List<Object[]> categoryList = Arrays.asList(category1, category2, category3);

	Mockito.when(statisticalRepository.getCategoryBestSeller()).thenReturn(categoryList);

	mockMvc.perform(get("/api/statistical/get-category-seller"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(3))
		.andExpect(jsonPath("$[0].name").value("Trái cây"))
		.andExpect(jsonPath("$[1].name").value("Rau xanh"))
		.andExpect(jsonPath("$[2].name").value("Cá cạn"));

	Mockito.verify(statisticalRepository).getCategoryBestSeller();
    }

    @Test
    void getInventory_testChuan2() throws Exception {
	Product product1 = new Product();
	product1.setProductId(1L);
	product1.setName("Sản phẩm A");
	product1.setQuantity(500);
	product1.setStatus(true);

	Product product2 = new Product();
	product2.setProductId(2L);
	product2.setName("Sản phẩm B");
	product2.setQuantity(250);
	product2.setStatus(true);

	Product product3 = new Product();
	product3.setProductId(3L);
	product3.setName("Sản phẩm C");
	product3.setQuantity(100);
	product3.setStatus(true);

	List<Product> inventory = Arrays.asList(product1, product2, product3);

	Mockito.when(productRepository.findByStatusTrueOrderByQuantityDesc()).thenReturn(inventory);

	mockMvc.perform(get("/api/statistical/get-inventory"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(3))
		.andExpect(jsonPath("$[0].quantity").value(500))
		.andExpect(jsonPath("$[1].quantity").value(250))
		.andExpect(jsonPath("$[2].quantity").value(100));

	Mockito.verify(productRepository).findByStatusTrueOrderByQuantityDesc();
    }
}