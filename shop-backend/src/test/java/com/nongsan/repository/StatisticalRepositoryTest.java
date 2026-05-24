package com.nongsan.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.nongsan.entity.Category;
import com.nongsan.entity.Order;
import com.nongsan.entity.OrderDetail;
import com.nongsan.entity.Product;
import com.nongsan.entity.User;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StatisticalRepositoryTest {

	private static final Logger log = LoggerFactory.getLogger(StatisticalRepositoryTest.class);

	@Autowired
	private StatisticalRepository statisticalRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		seedData();
		verifySeededData();
	}

	// ==========================================
	// MODULE: THỐNG KÊ THEO THÁNG
	// ==========================================

	/**
	 * Test Case ID: TC_REPO_01
	 * Mô tả: Lấy doanh thu theo tháng trong năm, nhóm theo tháng.
	 */
	@Test
	void getMonthOfYear_shouldReturnRevenueGroupedByMonth() {
		List<Object[]> rows = statisticalRepository.getMonthOfYear(2025);

		Map<Integer, Double> totalsByMonth = toMonthMap(rows, 1, 0);

		assertEquals(2, totalsByMonth.size());
		assertEquals(90.0, totalsByMonth.get(1), 0.0001);
		assertEquals(180.0, totalsByMonth.get(2), 0.0001);
	}

	// ==========================================
	// MODULE: LẤY DANH SÁCH NĂM
	// ==========================================

	/**
	 * Test Case ID: TC_REPO_02
	 * Mô tả: Lấy danh sách các năm có đơn hàng hoàn thành, sắp xếp giảm dần.
	 */
	@Test
	void getYears_shouldReturnDistinctYearsInDescendingOrder() {
		List<Integer> years = statisticalRepository.getYears();

		assertEquals(List.of(2025, 2024), years);
	}

	// ==========================================
	// MODULE: THỐNG KÊ DOANH THU
	// ==========================================

	/**
	 * Test Case ID: TC_REPO_03
	 * Mô tả: Lấy tổng doanh thu năm, chỉ tính đơn hàng đã hoàn thành (status=2).
	 */
	@Test
	void getRevenueByYear_shouldReturnCompletedOrderRevenueOnly() {
		Double revenue = statisticalRepository.getRevenueByYear(2025);

		assertEquals(270.0, revenue, 0.0001);
	}

	/**
	 * Test Case ID: TC_REPO_04
	 * Mô tả: Lấy thống kê doanh thu theo danh mục sản phẩm bán chạy.
	 */
	@Test
	void getCategoryBestSeller_shouldReturnCategoryTotals() {
		List<Object[]> rows = statisticalRepository.getCategoryBestSeller();

		assertFalse(rows.isEmpty());

		Object[] firstRow = rows.get(0);
		assertEquals("Fruit", firstRow[0]);
		assertEquals(8L, ((Number) firstRow[1]).longValue());
		assertEquals(160.0, ((Number) firstRow[2]).doubleValue(), 0.0001);
	}

	// ==========================================
	// MODULE: THỐNG KÊ TÀI CHÍNH
	// ==========================================

	/**
	 * Test Case ID: TC_REPO_05
	 * Mô tả: Lấy tổng doanh thu, chi phí và phí vận chuyển trong năm.
	 */
	@Test
	void getFinancialData_shouldReturnRevenueCostAndShippingTotals() {
		List<Object[]> rows = statisticalRepository.getFinancialData(2025);

		assertEquals(1, rows.size());

		Object[] row = rows.get(0);
		assertEquals(270.0, ((Number) row[0]).doubleValue(), 0.0001);
		assertEquals(70.0, ((Number) row[1]).doubleValue(), 0.0001);
		assertEquals(30.0, ((Number) row[2]).doubleValue(), 0.0001);
	}

	/**
	 * Test Case ID: TC_REPO_06
	 * Mô tả: Lấy doanh thu và chi phí theo từng tháng trong năm.
	 */
	@Test
	void getMonthlyFinancials_shouldReturnMonthlyRevenueAndCost() {
		List<Object[]> rows = statisticalRepository.getMonthlyFinancials(2025);

		Map<Integer, Object[]> totalsByMonth = new HashMap<>();
		for (Object[] row : rows) {
			totalsByMonth.put(((Number) row[0]).intValue(), row);
		}

		assertEquals(2, totalsByMonth.size());
		assertMonthlyFinancialRow(totalsByMonth.get(1), 180.0, 35.0);
		assertMonthlyFinancialRow(totalsByMonth.get(2), 360.0, 35.0);
	}

	private void seedData() {
		Category fruit = new Category();
		fruit.setCategoryName("Fruit");
		entityManager.persist(fruit);

		Category vegetable = new Category();
		vegetable.setCategoryName("Vegetable");
		entityManager.persist(vegetable);

		User customer = new User();
		customer.setName("Test Customer");
		customer.setEmail("customer@example.com");
		customer.setPassword("secret");
		customer.setPhone("0909000000");
		customer.setAddress("Test Address");
		customer.setGender(Boolean.TRUE);
		customer.setImage("image.png");
		customer.setRegisterDate(LocalDate.of(2024, 12, 1));
		customer.setStatus(Boolean.TRUE);
		customer.setToken("token");
		entityManager.persist(customer);

		Product apple = createProduct("Apple", 20.0, 10.0, fruit);
		Product carrot = createProduct("Carrot", 15.0, 5.0, vegetable);
		entityManager.persist(apple);
		entityManager.persist(carrot);

		Order januaryOrder = createOrder(customer, dateOf(2025, 1, 10), 100.0, 10.0, 2);
		Order februaryOrder = createOrder(customer, dateOf(2025, 2, 15), 200.0, 20.0, 2);
		Order pendingOrder = createOrder(customer, dateOf(2025, 2, 20), 300.0, 30.0, 1);
		Order previousYearOrder = createOrder(customer, dateOf(2024, 12, 31), 400.0, 40.0, 2);
		entityManager.persist(januaryOrder);
		entityManager.persist(februaryOrder);
		entityManager.persist(pendingOrder);
		entityManager.persist(previousYearOrder);

		entityManager.persist(createOrderDetail(januaryOrder, apple, 3, 20.0));
		entityManager.persist(createOrderDetail(januaryOrder, carrot, 1, 15.0));
		entityManager.persist(createOrderDetail(februaryOrder, apple, 1, 20.0));
		entityManager.persist(createOrderDetail(februaryOrder, carrot, 5, 15.0));
		entityManager.persist(createOrderDetail(pendingOrder, apple, 10, 20.0));
		entityManager.persist(createOrderDetail(previousYearOrder, apple, 4, 20.0));

		entityManager.flush();
		entityManager.clear();

		assertNotNull(statisticalRepository);
	}

	private void verifySeededData() {
		log.info("=== VERIFYING SEEDED DATA ===");

		// Verify Categories
		List<Map<String, Object>> categories = jdbcTemplate.queryForList(
				"SELECT category_name FROM categories");
		log.info("Categories: {} records", categories.size());
		categories.forEach(c -> log.info("  - {}", c.get("category_name")));
		assertEquals(2, categories.size());
		assertTrue(categories.stream().anyMatch(c -> "Fruit".equals(c.get("category_name"))));
		assertTrue(categories.stream().anyMatch(c -> "Vegetable".equals(c.get("category_name"))));

		// Verify Users
		List<Map<String, Object>> users = jdbcTemplate.queryForList(
				"SELECT name, email FROM users WHERE email = ?", "customer@example.com");
		log.info("Users: {} record", users.size());
		users.forEach(u -> log.info("  - {} ({})", u.get("name"), u.get("email")));
		assertEquals(1, users.size());
		assertEquals("Test Customer", users.get(0).get("name"));
		assertEquals("customer@example.com", users.get(0).get("email"));

		// Verify Products
		List<Map<String, Object>> products = jdbcTemplate.queryForList(
				"SELECT name, price, cost_price FROM products");
		log.info("Products: {} records", products.size());
		products.forEach(p -> log.info("  - {} (price: {}, cost: {})",
				p.get("name"), p.get("price"), p.get("cost_price")));
		assertEquals(2, products.size());
		assertTrue(products.stream().anyMatch(p -> "Apple".equals(p.get("name")) && ((Number) p.get("price")).doubleValue() == 20.0));
		assertTrue(products.stream().anyMatch(p -> "Carrot".equals(p.get("name")) && ((Number) p.get("price")).doubleValue() == 15.0));

		// Verify Orders - 4 orders: 2 completed in 2025, 1 pending in 2025, 1 completed in 2024
		List<Map<String, Object>> orders = jdbcTemplate.queryForList(
				"SELECT amount, shipping_fee, status FROM orders");
		log.info("Orders: {} records", orders.size());

		// Count completed orders (status=2) for 2025
		List<Map<String, Object>> completedOrders2025 = jdbcTemplate.queryForList(
				"SELECT COUNT(*) as cnt FROM orders WHERE status = 2 AND YEAR(order_date) = 2025");
		log.info("  Completed 2025: {} orders", completedOrders2025.get(0).get("cnt"));
		assertEquals(2, ((Number) completedOrders2025.get(0).get("cnt")).intValue());

		// Count pending order (status=1) for 2025
		List<Map<String, Object>> pendingOrders2025 = jdbcTemplate.queryForList(
				"SELECT COUNT(*) as cnt FROM orders WHERE status = 1 AND YEAR(order_date) = 2025");
		log.info("  Pending 2025: {} orders", pendingOrders2025.get(0).get("cnt"));
		assertEquals(1, ((Number) pendingOrders2025.get(0).get("cnt")).intValue());

		// Count completed orders for 2024
		List<Map<String, Object>> completedOrders2024 = jdbcTemplate.queryForList(
				"SELECT COUNT(*) as cnt FROM orders WHERE status = 2 AND YEAR(order_date) = 2024");
		log.info("  Completed 2024: {} orders", completedOrders2024.get(0).get("cnt"));
		assertEquals(1, ((Number) completedOrders2024.get(0).get("cnt")).intValue());

		// Verify OrderDetails - total quantity: 3+1+1+5+10+4 = 24
		List<Map<String, Object>> orderDetails = jdbcTemplate.queryForList(
				"SELECT SUM(quantity) as total_quantity FROM order_details");
		log.info("OrderDetails total quantity: {}", orderDetails.get(0).get("total_quantity"));
		assertEquals(24, ((Number) orderDetails.get(0).get("total_quantity")).intValue());

		// Verify OrderDetails count = 6 details
		List<Map<String, Object>> detailCount = jdbcTemplate.queryForList(
				"SELECT COUNT(*) as cnt FROM order_details");
		log.info("OrderDetails count: {} details", detailCount.get(0).get("cnt"));
		assertEquals(6, ((Number) detailCount.get(0).get("cnt")).intValue());

		log.info("=== VERIFICATION PASSED ===");
	}

	private Product createProduct(String name, double price, double costPrice, Category category) {
		Product product = new Product();
		product.setName(name);
		product.setQuantity(100);
		product.setPrice(price);
		product.setCostPrice(costPrice);
		product.setDiscount(0);
		product.setImage(name.toLowerCase() + ".png");
		product.setDescription(name + " description");
		product.setEnteredDate(LocalDate.of(2024, 1, 1));
		product.setExpiryDate(LocalDate.of(2026, 1, 1));
		product.setStatus(Boolean.TRUE);
		product.setSold(0);
		product.setWeight(1.0);
		product.setUnit("kg");
		product.setOrigin("VN");
		product.setCategory(category);
		return product;
	}

	private Order createOrder(User customer, Date orderDate, double amount, double shippingFee, int status) {
		Order order = new Order();
		order.setUser(customer);
		order.setOrderDate(orderDate);
		order.setAmount(amount);
		order.setAddress("Order Address");
		order.setPhone("0909000000");
		order.setShippingFee(shippingFee);
		order.setWeight(2.0);
		order.setStatus(status);
		return order;
	}

	private OrderDetail createOrderDetail(Order order, Product product, int quantity, double price) {
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.setOrder(order);
		orderDetail.setProduct(product);
		orderDetail.setQuantity(quantity);
		orderDetail.setPrice(price);
		return orderDetail;
	}

	private Date dateOf(int year, int month, int day) {
		return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private Map<Integer, Double> toMonthMap(List<Object[]> rows, int monthIndex, int valueIndex) {
		Map<Integer, Double> totals = new HashMap<>();
		for (Object[] row : rows) {
			totals.put(((Number) row[monthIndex]).intValue(), ((Number) row[valueIndex]).doubleValue());
		}
		return totals;
	}

	private void assertMonthlyFinancialRow(Object[] row, double expectedRevenue, double expectedCost) {
		assertTrue(row != null);
		assertEquals(expectedRevenue, ((Number) row[1]).doubleValue(), 0.0001);
		assertEquals(expectedCost, ((Number) row[2]).doubleValue(), 0.0001);
	}
}
