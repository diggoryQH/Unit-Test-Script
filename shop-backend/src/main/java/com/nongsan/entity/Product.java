package com.nongsan.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "products")
public class Product implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;
	private String name;
	private int quantity;
	private Double price;

	// --- MỚI: Giá vốn để tính lợi nhuận ---
	private Double costPrice;

	private int discount;
	private String image;
	private String description;
	private LocalDate enteredDate; // Ngày nhập kho

	// --- MỚI: Ngày hết hạn ---
	private LocalDate expiryDate;

	private Boolean status;
	private int sold;

	// --- MỚI: Đơn vị tính và Nguồn gốc ---
	private Double weight; // Khối lượng sản phẩm để tính phí ship
	private String unit; // Vd: "kg", "bó", "combo"
	private String origin; // Vd: "VietGAP", "Đà Lạt", "Organic"

	@ManyToOne
	@JoinColumn(name = "categoryId")
	private Category category;

	@Override
  public String toString() {
    return "Product [productId=" + productId + ", name=" + name + ", quantity=" + quantity + ", price=" + price
        + ", expiryDate=" + expiryDate + ", status=" + status + ", category=" + category + "]";
  }

}
