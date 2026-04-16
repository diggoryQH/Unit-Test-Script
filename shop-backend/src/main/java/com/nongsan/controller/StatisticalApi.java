package com.nongsan.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nongsan.dto.CategoryBestSeller;
import com.nongsan.dto.Statistical;
import com.nongsan.entity.Order;
import com.nongsan.entity.Product;
import com.nongsan.repository.OrderRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.StatisticalRepository;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/statistical")
public class StatisticalApi {

	@Autowired
	StatisticalRepository statisticalRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ProductRepository productRepository;

	@GetMapping("/advanced-report/{year}")
	public ResponseEntity<Map<String, Object>> getAdvancedReport(@PathVariable int year) {
		Map<String, Object> report = new HashMap<>();

		// 1. Lấy dữ liệu tài chính
		List<Object[]> financial = statisticalRepository.getFinancialData(year);
		double revenue = 0, cost = 0, shipping = 0;
		if (!financial.isEmpty() && financial.get(0)[0] != null) {
			revenue = ((Number) financial.get(0)[0]).doubleValue();
			cost = ((Number) financial.get(0)[1]).doubleValue();
			shipping = ((Number) financial.get(0)[2]).doubleValue();
		}

		report.put("totalRevenue", revenue);
		report.put("grossProfit", revenue - cost);

		// 2. Thống kê trạng thái đơn hàng
		Map<String, Long> statusStats = new HashMap<>();
		statusStats.put("success", orderRepository.countByStatus(2));
		statusStats.put("canceled", orderRepository.countByStatus(3));
		statusStats.put("returned", orderRepository.countByStatus(6));
		report.put("orderStats", statusStats);

		return ResponseEntity.ok(report);
	}

	@GetMapping("/countYear")
	public ResponseEntity<List<Integer>> getYears() {
		return ResponseEntity.ok(statisticalRepository.getYears());
	}

	@GetMapping("/revenue/year/{year}")
	public ResponseEntity<Double> getRevenueByYear(@PathVariable("year") int year) {
		return ResponseEntity.ok(statisticalRepository.getRevenueByYear(year));
	}

	@GetMapping("/get-all-order-success")
	public ResponseEntity<List<Order>> getAllOrderSuccess() {
		return ResponseEntity.ok(orderRepository.findByStatus(2));
	}

	@GetMapping("/get-category-seller")
	public ResponseEntity<List<CategoryBestSeller>> getCategoryBestSeller() {
			List<Object[]> list = statisticalRepository.getCategoryBestSeller();
			List<CategoryBestSeller> listCategoryBestSeller = new ArrayList<>();
			
			for (Object[] objects : list) {
					// SQL: 0:category_name | 1:SUM(quantity) | 2:SUM(price*quantity)
					CategoryBestSeller dto = new CategoryBestSeller();
					dto.setName(String.valueOf(objects[0])); // Cột 0 là Tên
					dto.setCount(Integer.parseInt(String.valueOf(objects[1]))); // Cột 1 là Số lượng
					dto.setAmount(Double.valueOf(String.valueOf(objects[2]))); // Cột 2 là Doanh thu
					
					listCategoryBestSeller.add(dto);
			}
			return ResponseEntity.ok(listCategoryBestSeller);
	}

	@GetMapping("/get-inventory")
	public ResponseEntity<List<Product>> getInventory() {
		return ResponseEntity.ok(productRepository.findByStatusTrueOrderByQuantityDesc());
	}

	@GetMapping("{year}")
	public ResponseEntity<List<Statistical>> getStatisticalYear(@PathVariable("year") int year) {
			List<Object[]> list = statisticalRepository.getMonthlyFinancials(year);
			
			// Khởi tạo 12 tháng mặc định bằng 0
			Map<Integer, Statistical> monthMap = new HashMap<>();
			for (int i = 1; i <= 12; i++) {
					monthMap.put(i, new Statistical(i, 0.0, 0.0)); // Giả sử DTO có: Month, Revenue, Profit
			}

			for (Object[] obj : list) {
					int month = ((Number) obj[0]).intValue();
					double revenue = ((Number) obj[1]).doubleValue();
					double cost = ((Number) obj[2]).doubleValue();
					double profit = revenue - cost; // Tính lợi nhuận
					
					monthMap.put(month, new Statistical(month, revenue, profit));
			}

			return ResponseEntity.ok(new ArrayList<>(monthMap.values()));
	}

}
