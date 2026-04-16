package com.nongsan.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nongsan.dto.CartRequest;
import com.nongsan.entity.Cart;
import com.nongsan.entity.CartDetail;
import com.nongsan.entity.Order;
import com.nongsan.entity.OrderDetail;
import com.nongsan.entity.Product;
import com.nongsan.repository.CartDetailRepository;
import com.nongsan.repository.CartRepository;
import com.nongsan.repository.OrderDetailRepository;
import com.nongsan.repository.OrderRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.UserRepository;
import com.nongsan.utils.SendMailUtil;

@CrossOrigin("*")
@RestController
@RequestMapping("api/orders")
public class OrderApi {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CartRepository cartRepository;

	@Autowired
	CartDetailRepository cartDetailRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	SendMailUtil senMail;

	@GetMapping
	public ResponseEntity<List<Order>> findAll() {
		return ResponseEntity.ok(orderRepository.findAllByOrderByOrdersIdDesc());
	}

	@GetMapping("{id}")
	public ResponseEntity<Order> getById(@PathVariable("id") Long id) {
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(orderRepository.findById(id).get());
	}

	@GetMapping("/user/{email}")
	public ResponseEntity<List<Order>> getByUser(@PathVariable("email") String email) {
		if (!userRepository.existsByEmail(email)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity
				.ok(orderRepository.findByUserOrderByOrdersIdDesc(userRepository.findByEmail(email).get()));
	}

	@GetMapping("updateStatus/{id}/{status}")
	public ResponseEntity<Void> updateStatus(@PathVariable("id") Long id, @PathVariable("status") int status) {
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		Order order = orderRepository.findById(id).get();
		order.setStatus(status);
		orderRepository.save(order);

		// Gửi mail thông báo tùy theo trạng thái
		if (status == 3 || status == 6)
			senMail.sendMailOrderCancel(order);
		if (status == 1)
			senMail.sendMailOrderDeliver(order);
		if (status == 2)
			senMail.sendMailOrderSuccess(order);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/{email}")
	public ResponseEntity<Order> checkout(@PathVariable("email") String email, @RequestBody CartRequest cartRequest) {
		Cart cart = cartRepository.findById(cartRequest.getCartId()).get();

		if (!userRepository.existsByEmail(email)) {
			return ResponseEntity.notFound().build();
		}
		if (!cartRepository.existsById(cart.getCartId())) {
			return ResponseEntity.notFound().build();
		}
		List<CartDetail> items = cartDetailRepository.findByCart(cart);

		Double weight = 0.0;
		for (CartDetail i : items) {
			weight += i.getProduct().getWeight() * i.getQuantity();
		}

		Order order = orderRepository.save(new Order(0L, new Date(), cartRequest.getAmount(), cart.getAddress(),
				cart.getPhone(), cartRequest.getShippingFee(), weight, 0,
				userRepository.findByEmail(email).get()));

		for (CartDetail i : items) {
			OrderDetail orderDetail = new OrderDetail(0L, i.getQuantity(), i.getProduct().getPrice(), i.getProduct(), order);
			orderDetailRepository.save(orderDetail);
		}
		// cartDetailRepository.deleteByCart(cart);
		for (CartDetail i : items) {
			cartDetailRepository.delete(i);
		}
		senMail.sendMailOrder(order);
		return ResponseEntity.ok(order);
	}

	@GetMapping("cancel/{orderId}")
	public ResponseEntity<Void> cancel(@PathVariable("orderId") Long id) {
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		Order order = orderRepository.findById(id).get();
		order.setStatus(3);
		orderRepository.save(order);
		senMail.sendMailOrderCancel(order);
		return ResponseEntity.ok().build();
	}

	@GetMapping("deliver/{orderId}")
	public ResponseEntity<Void> deliver(@PathVariable("orderId") Long id) {
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		Order order = orderRepository.findById(id).get();
		order.setStatus(1);
		orderRepository.save(order);
		senMail.sendMailOrderDeliver(order);
		return ResponseEntity.ok().build();
	}

	@GetMapping("success/{orderId}")
	public ResponseEntity<Void> success(@PathVariable("orderId") Long id) {
			Order order = orderRepository.findById(id).get();
			order.setStatus(2);
			orderRepository.save(order);
			updateProduct(order); // Cập nhật kho khi thành công
			senMail.sendMailOrderSuccess(order);
			return ResponseEntity.ok().build();
	}

	public void updateProduct(Order order) {
		List<OrderDetail> listOrderDetail = orderDetailRepository.findByOrder(order);
		for (OrderDetail orderDetail : listOrderDetail) {
			Product product = productRepository.findById(orderDetail.getProduct().getProductId()).get();
			if (product != null) {
				product.setQuantity(product.getQuantity() - orderDetail.getQuantity());
				product.setSold(product.getSold() + orderDetail.getQuantity());
				productRepository.save(product);
			}
		}
	}

}
