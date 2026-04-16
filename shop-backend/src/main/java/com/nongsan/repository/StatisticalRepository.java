package com.nongsan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nongsan.entity.Product;

@Repository
public interface StatisticalRepository extends JpaRepository<Product, Long> {

	@Query(value = "SELECT SUM(amount - shipping_fee), MONTH(order_date) FROM orders WHERE YEAR(order_date) = ?1 AND status = 2 GROUP BY MONTH(order_date)", nativeQuery = true)
	List<Object[]> getMonthOfYear(int year);

	@Query(value = "SELECT DISTINCT YEAR(order_date) FROM orders ORDER BY YEAR(order_date) DESC", nativeQuery = true)
	List<Integer> getYears();

	@Query(value = "SELECT SUM(amount - shipping_fee) FROM orders WHERE YEAR(order_date) = ?1 AND status = 2", nativeQuery = true)
	Double getRevenueByYear(int year);

	// SQL lấy Top danh mục bán chạy: Tên danh mục | Số lượng bán | Doanh thu
	@Query(value = "SELECT c.category_name, SUM(od.quantity), SUM(od.price * od.quantity) " +
			"FROM order_details od " +
			"JOIN products p ON od.product_id = p.product_id " +
			"JOIN categories c ON p.category_id = c.category_id " +
			"JOIN orders o ON od.order_id = o.orders_id " +
			"WHERE o.status = 2 " +
			"GROUP BY c.category_name ORDER BY SUM(od.quantity) DESC", nativeQuery = true)
	List<Object[]> getCategoryBestSeller();

	// SQL tính Lợi nhuận gộp (Doanh thu - Giá vốn - Phí ship) cho 1 năm cụ thể
	@Query(value = "SELECT " +
			"SUM(o.amount - o.shipping_fee) as total_revenue, " +
			"SUM(od.quantity * p.cost_price) as total_cost, " +
			"SUM(o.shipping_fee) as total_shipping " +
			"FROM orders o " +
			"JOIN order_details od ON o.orders_id = od.order_id " +
			"JOIN products p ON od.product_id = p.product_id " +
			"WHERE YEAR(o.order_date) = ?1 AND o.status = 2", nativeQuery = true)
	List<Object[]> getFinancialData(int year);

	@Query(value = "SELECT MONTH(o.order_date), SUM(o.amount - o.shipping_fee), SUM(od.quantity * p.cost_price) " +
			"FROM orders o " +
			"JOIN order_details od ON o.orders_id = od.order_id " +
			"JOIN products p ON od.product_id = p.product_id " +
			"WHERE YEAR(o.order_date) = ?1 AND o.status = 2 " +
			"GROUP BY MONTH(o.order_date)", nativeQuery = true)
	List<Object[]> getMonthlyFinancials(int year);
}
