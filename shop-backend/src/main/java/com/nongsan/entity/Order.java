package com.nongsan.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ordersId;
	private Date orderDate;
	private Double amount;
	private String address;
	private String phone;

	// Thông tin vận chuyển
	private Double shippingFee;
	private Double weight; // Tổng khối lượng để tính phí ship

	// Trạng thái mở rộng: 0: Chờ xác nhận, 1: Đang giao, 2: Hoàn tất,
	// 3: Đã hủy, 4: Yêu cầu trả hàng, 5: Đang xử lý trả hàng, 6: Đã trả hàng/hoàn tiền
	private int status;

	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;

}
