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

	@Autowired
	private StatisticalRepository statisticalRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		seedData();
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
