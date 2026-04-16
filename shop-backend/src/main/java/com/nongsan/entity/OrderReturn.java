package com.nongsan.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_returns")
public class OrderReturn implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "orderId")
    private Order order;
    
    private String reason; // Hàng hư hỏng, Giao sai...
    private String description;
    private String evidenceImage; // Link ảnh/video bằng chứng
    private Double refundAmount;
    private int status; // 0: Chờ duyệt, 1: Chấp nhận, 2: Từ chối
    private String adminNote;

}