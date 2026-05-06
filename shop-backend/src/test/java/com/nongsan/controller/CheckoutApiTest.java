package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.dto.CartRequest;
import com.nongsan.entity.*;
import com.nongsan.repository.*;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import com.nongsan.utils.SendMailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-04: Đặt hàng & Thanh toán
 * File test cho: OrderApi.java — hàm checkout()
 * Endpoint: POST /api/orders/{email}
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderApi.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckoutApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private OrderRepository orderRepository;
    @MockBean private OrderDetailRepository orderDetailRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private CartRepository cartRepository;
    @MockBean private CartDetailRepository cartDetailRepository;
    @MockBean private ProductRepository productRepository;
    @MockBean private SendMailUtil sendMailUtil;

    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private AuthEntryPointJwt unauthorizedHandler;
    @MockBean private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: ĐẶT HÀNG — POST /api/orders/{email}
    // ==========================================

    /**
     * TC_CHECKOUT_01
     * Mục tiêu  : Đặt hàng thành công — email tồn tại, cartId hợp lệ, giỏ có 1 sản phẩm.
     * Đầu vào   : email = "user@gmail.com", CartRequest(cartId=1, amount=40000, shippingFee=15000)
     *             Cart(address="Hà Nội", phone="0912345678")
     *             CartDetail(productId=1, qty=2, price=40000, weight=0.5)
     * Hành vi GS: userRepository.existsByEmail → true
     *             cartRepository.existsById    → true
     *             cartDetailRepository.findByCart → [mockCartDetail]
     *             orderRepository.save → savedOrder(ordersId=1, status=0)
     * Kết quả KV: HTTP 200 OK
     *             body.ordersId = 1
     *             body.address  = "Hà Nội"
     *             body.status   = 0
     * Verify    : orderRepository.save(Order) gọi 1 lần
     *             orderDetailRepository.save(OrderDetail) gọi 1 lần
     *             cartDetailRepository.delete(mockCartDetail) gọi 1 lần
     *             sendMailUtil.sendMailOrder(savedOrder) gọi 1 lần
     */
    @Test // [Happy Path] Đặt hàng thành công — email + cartId hợp lệ, giỏ có sản phẩm
    void TC_CHECKOUT_01() throws Exception {
        String email = "user@gmail.com";
        Long cartId = 1L;

        User mockUser = new User();
        mockUser.setEmail(email);

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);
        mockCart.setAddress("Hà Nội");
        mockCart.setPhone("0912345678");

        Product mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setName("Cà rốt");
        mockProduct.setPrice(20000.0);
        mockProduct.setWeight(0.5);

        CartDetail mockCartDetail = new CartDetail();
        mockCartDetail.setCartDetailId(1L);
        mockCartDetail.setProduct(mockProduct);
        mockCartDetail.setQuantity(2);
        mockCartDetail.setPrice(40000.0);

        Order savedOrder = new Order(1L, new Date(), 40000.0, "Hà Nội",
                "0912345678", 15000.0, 1.0, 0, mockUser);

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCartId(cartId);
        cartRequest.setAmount(40000.0);
        cartRequest.setShippingFee(15000.0);

        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Arrays.asList(mockCartDetail));
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        Mockito.when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(new OrderDetail());

        mockMvc.perform(post("/api/orders/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").value(1))
                .andExpect(jsonPath("$.address").value("Hà Nội"))
                .andExpect(jsonPath("$.status").value(0));

        Mockito.verify(orderRepository).save(any(Order.class));
        Mockito.verify(orderDetailRepository).save(any(OrderDetail.class));
        Mockito.verify(cartDetailRepository).delete(mockCartDetail);
        Mockito.verify(sendMailUtil).sendMailOrder(savedOrder);
    }

    /**
     * TC_CHECKOUT_02
     * Mục tiêu  : Đặt hàng thất bại khi email người dùng không tồn tại trong hệ thống.
     * Đầu vào   : email = "ghost@gmail.com" (không có trong DB), CartRequest hợp lệ
     * Hành vi GS: cartRepository.findById      → Optional(mockCart) [được gọi trước khi check email]
     *             userRepository.existsByEmail → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify    : orderRepository.save KHÔNG được gọi
     *             sendMailUtil.sendMailOrder KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: email không tồn tại → 404, không tạo Order
    void TC_CHECKOUT_02() throws Exception {
        String invalidEmail = "ghost@gmail.com";
        Long cartId = 1L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCartId(cartId);
        cartRequest.setAmount(100000.0);

        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(userRepository.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(post("/api/orders/{email}", invalidEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository, Mockito.never()).save(any(Order.class));
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrder(any());
    }

    /**
     * TC_CHECKOUT_03
     * Mục tiêu  : Đặt hàng thất bại khi cartId không tồn tại trong hệ thống.
     * Đầu vào   : email hợp lệ, CartRequest(cartId=999) — cartId không có trong DB
     * Hành vi GS: cartRepository.findById     → Optional(mockCart) [code gọi .get() không an toàn]
     *             userRepository.existsByEmail → true
     *             cartRepository.existsById   → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify    : orderRepository.save KHÔNG được gọi
     *             sendMailUtil.sendMailOrder KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: cartId không tồn tại → 404, không tạo Order
    void TC_CHECKOUT_03() throws Exception {
        String email = "user@gmail.com";
        Long invalidCartId = 999L;

        Cart mockCart = new Cart();
        mockCart.setCartId(invalidCartId);

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCartId(invalidCartId);
        cartRequest.setAmount(100000.0);

        Mockito.when(cartRepository.findById(invalidCartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(cartRepository.existsById(invalidCartId)).thenReturn(false);

        mockMvc.perform(post("/api/orders/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository, Mockito.never()).save(any(Order.class));
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrder(any());
    }

    /**
     * TC_CHECKOUT_04
     * Mục tiêu  : Đặt hàng với nhiều sản phẩm khác nhau — kiểm tra đúng số lần gọi save.
     * Đầu vào   : Giỏ hàng có 2 CartDetail (product1 + product2)
     * Hành vi GS: cartDetailRepository.findByCart → [detail1, detail2]
     *             orderDetailRepository.save      → được gọi 2 lần (1 lần / sản phẩm)
     * Kết quả KV: HTTP 200 OK
     * Verify    : orderDetailRepository.save được gọi đúng 2 lần
     *             cartDetailRepository.delete được gọi 2 lần (1 lần / CartDetail)
     *             sendMailUtil.sendMailOrder được gọi 1 lần
     */
    @Test // [Edge Case] Giỏ có nhiều sản phẩm → lưu đúng số OrderDetail và xóa đúng số CartDetail
    void TC_CHECKOUT_04() throws Exception {
        String email = "user@gmail.com";
        Long cartId = 1L;

        User mockUser = new User();
        mockUser.setEmail(email);

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);
        mockCart.setAddress("Hà Nội");
        mockCart.setPhone("0912345678");

        Product product1 = new Product();
        product1.setProductId(1L);
        product1.setPrice(20000.0);
        product1.setWeight(0.5);

        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setPrice(30000.0);
        product2.setWeight(1.0);

        CartDetail detail1 = new CartDetail();
        detail1.setProduct(product1);
        detail1.setQuantity(2);
        detail1.setPrice(40000.0);

        CartDetail detail2 = new CartDetail();
        detail2.setProduct(product2);
        detail2.setQuantity(1);
        detail2.setPrice(30000.0);

        Order savedOrder = new Order(10L, new Date(), 70000.0, "Hà Nội",
                "0912345678", 20000.0, 2.0, 0, mockUser);

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCartId(cartId);
        cartRequest.setAmount(70000.0);
        cartRequest.setShippingFee(20000.0);

        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Arrays.asList(detail1, detail2));
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        Mockito.when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(new OrderDetail());

        mockMvc.perform(post("/api/orders/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        Mockito.verify(orderDetailRepository, Mockito.times(2)).save(any(OrderDetail.class));
        Mockito.verify(cartDetailRepository).delete(detail1);
        Mockito.verify(cartDetailRepository).delete(detail2);
        Mockito.verify(sendMailUtil).sendMailOrder(savedOrder);
    }

    /**
     * TC_CHECKOUT_05
     * Mục tiêu  : Đặt hàng với giỏ hàng rỗng (0 CartDetail) — xác nhận hành vi thực tế của hệ thống.
     *             Code hiện tại KHÔNG chặn giỏ rỗng → vẫn tạo Order với weight=0, amount=0.
     * Đầu vào   : CartRequest(cartId=1, amount=0, shippingFee=15000), giỏ hàng rỗng
     * Hành vi GS: cartDetailRepository.findByCart → [] (rỗng)
     *             orderRepository.save → savedOrder(ordersId=2, amount=0)
     * Kết quả KV: HTTP 200 OK (hệ thống không kiểm tra giỏ rỗng)
     * Verify    : orderRepository.save được gọi 1 lần (Order vẫn được tạo)
     *             orderDetailRepository.save KHÔNG được gọi (không có item)
     *             sendMailUtil.sendMailOrder được gọi 1 lần
     */
    @Test // [Edge Case] Giỏ hàng rỗng → vẫn tạo Order (hệ thống không chặn giỏ rỗng)
    void TC_CHECKOUT_05() throws Exception {
        String email = "user@gmail.com";
        Long cartId = 1L;

        User mockUser = new User();
        mockUser.setEmail(email);

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);
        mockCart.setAddress("Hà Nội");
        mockCart.setPhone("0912345678");

        Order savedOrder = new Order(2L, new Date(), 0.0, "Hà Nội",
                "0912345678", 15000.0, 0.0, 0, mockUser);

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCartId(cartId);
        cartRequest.setAmount(0.0);
        cartRequest.setShippingFee(15000.0);

        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Collections.emptyList());
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        mockMvc.perform(post("/api/orders/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        Mockito.verify(orderRepository).save(any(Order.class));
        Mockito.verify(orderDetailRepository, Mockito.never()).save(any(OrderDetail.class));
        Mockito.verify(sendMailUtil).sendMailOrder(savedOrder);
    }
}
