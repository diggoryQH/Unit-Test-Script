package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Cart;
import com.nongsan.entity.User;
import com.nongsan.repository.CartDetailRepository;
import com.nongsan.repository.CartRepository;
import com.nongsan.repository.UserRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-03: Quản lý giỏ hàng
 * File test cho: CartApi.java
 * Endpoint base: /api/cart
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(CartApi.class)
@AutoConfigureMockMvc(addFilters = false)
class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private CartDetailRepository cartDetailRepository;

    @MockBean
    private UserRepository userRepository;

    // Giả lập Security (bắt buộc để WebMvcTest không lỗi)
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: LẤY GIỎ HÀNG THEO EMAIL (GET)
    // ==========================================

    /**
     * Test Case ID: TC_CART_01
     * Mô tả: Lấy giỏ hàng thành công khi email người dùng tồn tại trong hệ thống.
     */
    @Test
    void getCartUser_testChuan1() throws Exception {
        String validEmail = "user@gmail.com";

        User mockUser = new User();
        mockUser.setEmail(validEmail);

        Cart mockCart = new Cart();
        mockCart.setCartId(1L);
        mockCart.setAmount(150000.0);
        mockCart.setAddress("Hà Nội");
        mockCart.setPhone("0912345678");
        mockCart.setUser(mockUser);

        Mockito.when(userRepository.existsByEmail(validEmail)).thenReturn(true);
        Mockito.when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(mockUser));
        Mockito.when(cartRepository.findByUser(mockUser)).thenReturn(mockCart);

        mockMvc.perform(get("/api/cart/user/{email}", validEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.address").value("Hà Nội"))
                .andExpect(jsonPath("$.phone").value("0912345678"));

        Mockito.verify(userRepository).existsByEmail(validEmail);
        Mockito.verify(cartRepository).findByUser(mockUser);
    }

    /**
     * Test Case ID: TC_CART_02
     * Mô tả: Trả về 404 Not Found khi email người dùng không tồn tại trong hệ thống.
     */
    @Test
    void getCartUser_testNgoaiLe1() throws Exception {
        String invalidEmail = "notfound@gmail.com";

        Mockito.when(userRepository.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(get("/api/cart/user/{email}", invalidEmail))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository).existsByEmail(invalidEmail);
        // Đảm bảo không truy vấn DB giỏ hàng khi email không tồn tại
        Mockito.verify(cartRepository, Mockito.never()).findByUser(any());
    }

    // ==========================================
    // MODULE: CẬP NHẬT GIỎ HÀNG (PUT)
    // ==========================================

    /**
     * Test Case ID: TC_CART_03
     * Mô tả: Cập nhật giỏ hàng thành công khi email tồn tại và dữ liệu cart hợp lệ.
     */
    @Test
    void putCartUser_testChuan1() throws Exception {
        String validEmail = "user@gmail.com";

        User mockUser = new User();
        mockUser.setEmail(validEmail);

        Cart cartToUpdate = new Cart();
        cartToUpdate.setCartId(1L);
        cartToUpdate.setAmount(200000.0);
        cartToUpdate.setAddress("TP. Hồ Chí Minh");
        cartToUpdate.setPhone("0987654321");
        cartToUpdate.setUser(mockUser);

        Mockito.when(userRepository.existsByEmail(validEmail)).thenReturn(true);
        Mockito.when(cartRepository.save(any(Cart.class))).thenReturn(cartToUpdate);

        mockMvc.perform(put("/api/cart/user/{email}", validEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.address").value("TP. Hồ Chí Minh"))
                .andExpect(jsonPath("$.phone").value("0987654321"));

        Mockito.verify(userRepository).existsByEmail(validEmail);
        Mockito.verify(cartRepository).save(any(Cart.class));
    }

    /**
     * Test Case ID: TC_CART_04
     * Mô tả: Trả về 404 Not Found khi email không tồn tại, không lưu dữ liệu vào DB.
     */
    @Test
    void putCartUser_testNgoaiLe1() throws Exception {
        String invalidEmail = "ghost@gmail.com";

        Cart cartPayload = new Cart();
        cartPayload.setCartId(1L);
        cartPayload.setAddress("Đà Nẵng");

        Mockito.when(userRepository.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(put("/api/cart/user/{email}", invalidEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartPayload)))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository).existsByEmail(invalidEmail);
        // Đảm bảo không gọi save khi email không tồn tại
        Mockito.verify(cartRepository, Mockito.never()).save(any(Cart.class));
    }
}
