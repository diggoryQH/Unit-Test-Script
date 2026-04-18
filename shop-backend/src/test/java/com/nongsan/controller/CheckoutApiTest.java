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

    // Giả lập Security
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private AuthEntryPointJwt unauthorizedHandler;
    @MockBean private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: ĐẶT HÀNG (CHECKOUT)
    // ==========================================

    /**
     * Test Case ID: TC_CHECKOUT_01
     * Mô tả: Đặt hàng thành công — email tồn tại, cartId hợp lệ, có sản phẩm trong giỏ.
     *        Kết quả: Tạo Order + OrderDetails, xóa CartDetails, gửi mail xác nhận.
     */
    @Test
    void checkout_testChuan1() throws Exception {
        String email = "user@gmail.com";
        Long cartId = 1L;

        // Chuẩn bị dữ liệu
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

        // Mock hành vi
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

        // Xác nhận: Order và OrderDetail được lưu, CartDetail bị xóa, mail được gửi
        Mockito.verify(orderRepository).save(any(Order.class));
        Mockito.verify(orderDetailRepository).save(any(OrderDetail.class));
        Mockito.verify(cartDetailRepository).delete(mockCartDetail);
        Mockito.verify(sendMailUtil).sendMailOrder(savedOrder);
    }

    /**
     * Test Case ID: TC_CHECKOUT_02
     * Mô tả: Đặt hàng thất bại — email người dùng không tồn tại trong hệ thống.
     *        Kết quả: 404 Not Found, không tạo Order.
     */
    @Test
    void checkout_testNgoaiLe1_emailKhongTonTai() throws Exception {
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

        // Đảm bảo không tạo Order khi email không tồn tại
        Mockito.verify(orderRepository, Mockito.never()).save(any(Order.class));
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrder(any());
    }

    /**
     * Test Case ID: TC_CHECKOUT_03
     * Mô tả: Đặt hàng thất bại — cartId không tồn tại trong hệ thống.
     *        Kết quả: 404 Not Found, không tạo Order.
     */
    @Test
    void checkout_testNgoaiLe2_cartKhongTonTai() throws Exception {
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
}
