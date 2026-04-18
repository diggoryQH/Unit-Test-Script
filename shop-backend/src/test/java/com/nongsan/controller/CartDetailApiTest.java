package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Cart;
import com.nongsan.entity.CartDetail;
import com.nongsan.entity.Product;
import com.nongsan.repository.CartDetailRepository;
import com.nongsan.repository.CartRepository;
import com.nongsan.repository.ProductRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-03: Quản lý giỏ hàng
 * File test cho: CartDetailApi.java
 * Endpoint base: /api/cartDetail
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(CartDetailApi.class)
@AutoConfigureMockMvc(addFilters = false)
class CartDetailApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartDetailRepository cartDetailRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ProductRepository productRepository;

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
    // MODULE: LẤY DANH SÁCH SẢN PHẨM TRONG GIỎ THEO CART ID (GET)
    // ==========================================

    /**
     * Test Case ID: TC_CARTDETAIL_01
     * Mô tả: Lấy danh sách sản phẩm trong giỏ thành công khi cartId tồn tại.
     */
    @Test
    void getByCartId_testChuan1() throws Exception {
        Long validCartId = 1L;

        Cart mockCart = new Cart();
        mockCart.setCartId(validCartId);

        CartDetail item1 = new CartDetail();
        item1.setCartDetailId(1L);
        item1.setQuantity(2);
        item1.setPrice(50000.0);

        CartDetail item2 = new CartDetail();
        item2.setCartDetailId(2L);
        item2.setQuantity(1);
        item2.setPrice(30000.0);

        Mockito.when(cartRepository.existsById(validCartId)).thenReturn(true);
        Mockito.when(cartRepository.findById(validCartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/cartDetail/cart/{id}", validCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].quantity").value(1));

        Mockito.verify(cartRepository).existsById(validCartId);
        Mockito.verify(cartDetailRepository).findByCart(mockCart);
    }

    /**
     * Test Case ID: TC_CARTDETAIL_02
     * Mô tả: Trả về 404 Not Found khi cartId không tồn tại trong hệ thống.
     */
    @Test
    void getByCartId_testNgoaiLe1() throws Exception {
        Long invalidCartId = 999L;

        Mockito.when(cartRepository.existsById(invalidCartId)).thenReturn(false);

        mockMvc.perform(get("/api/cartDetail/cart/{id}", invalidCartId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartRepository).existsById(invalidCartId);
        Mockito.verify(cartDetailRepository, Mockito.never()).findByCart(any());
    }

    // ==========================================
    // MODULE: LẤY 1 ITEM TRONG GIỎ THEO CARTDETAIL ID (GET)
    // ==========================================

    /**
     * Test Case ID: TC_CARTDETAIL_03
     * Mô tả: Lấy thành công một item trong giỏ khi cartDetailId tồn tại.
     */
    @Test
    void getOne_testChuan1() throws Exception {
        Long validDetailId = 1L;

        CartDetail mockDetail = new CartDetail();
        mockDetail.setCartDetailId(validDetailId);
        mockDetail.setQuantity(3);
        mockDetail.setPrice(90000.0);

        Mockito.when(cartDetailRepository.existsById(validDetailId)).thenReturn(true);
        Mockito.when(cartDetailRepository.findById(validDetailId)).thenReturn(Optional.of(mockDetail));

        mockMvc.perform(get("/api/cartDetail/{id}", validDetailId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartDetailId").value(1))
                .andExpect(jsonPath("$.quantity").value(3));

        Mockito.verify(cartDetailRepository).existsById(validDetailId);
        Mockito.verify(cartDetailRepository).findById(validDetailId);
    }

    /**
     * Test Case ID: TC_CARTDETAIL_04
     * Mô tả: Trả về 404 Not Found khi cartDetailId không tồn tại.
     */
    @Test
    void getOne_testNgoaiLe1() throws Exception {
        Long invalidDetailId = 999L;

        Mockito.when(cartDetailRepository.existsById(invalidDetailId)).thenReturn(false);

        mockMvc.perform(get("/api/cartDetail/{id}", invalidDetailId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository).existsById(invalidDetailId);
        Mockito.verify(cartDetailRepository, Mockito.never()).findById(any());
    }

    // ==========================================
    // MODULE: THÊM SẢN PHẨM VÀO GIỎ HÀNG (POST)
    // ==========================================

    /**
     * Test Case ID: TC_CARTDETAIL_05
     * Mô tả: Thêm sản phẩm mới vào giỏ thành công khi sản phẩm chưa có trong giỏ.
     */
    @Test
    void post_testChuan1_themMoi() throws Exception {
        Long cartId = 1L;
        Long productId = 10L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        Product mockProduct = new Product();
        mockProduct.setProductId(productId);

        CartDetail newDetail = new CartDetail();
        newDetail.setCart(mockCart);
        newDetail.setProduct(mockProduct);
        newDetail.setQuantity(1);
        newDetail.setPrice(50000.0);

        CartDetail savedDetail = new CartDetail();
        savedDetail.setCartDetailId(1L);
        savedDetail.setCart(mockCart);
        savedDetail.setProduct(mockProduct);
        savedDetail.setQuantity(1);
        savedDetail.setPrice(50000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Arrays.asList(mockProduct));
        Mockito.when(productRepository.findByProductIdAndStatusTrue(productId)).thenReturn(mockProduct);
        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        // Giỏ hàng hiện tại rỗng → không có sản phẩm trùng → thêm mới
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Collections.emptyList());
        Mockito.when(cartDetailRepository.save(any(CartDetail.class))).thenReturn(savedDetail);

        mockMvc.perform(post("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartDetailId").value(1))
                .andExpect(jsonPath("$.quantity").value(1));

        Mockito.verify(cartDetailRepository).save(any(CartDetail.class));
    }

    /**
     * Test Case ID: TC_CARTDETAIL_06
     * Mô tả: Thêm sản phẩm đã có trong giỏ → tự động cộng thêm số lượng và giá.
     */
    @Test
    void post_testChuan2_congSoLuong() throws Exception {
        Long cartId = 1L;
        Long productId = 10L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        Product mockProduct = new Product();
        mockProduct.setProductId(productId);

        // Item đã có trong giỏ với số lượng 2
        CartDetail existingDetail = new CartDetail();
        existingDetail.setCartDetailId(5L);
        existingDetail.setCart(mockCart);
        existingDetail.setProduct(mockProduct);
        existingDetail.setQuantity(2);
        existingDetail.setPrice(100000.0);

        // Item mới thêm vào (cùng productId)
        CartDetail newDetail = new CartDetail();
        newDetail.setCart(mockCart);
        newDetail.setProduct(mockProduct);
        newDetail.setQuantity(1);
        newDetail.setPrice(50000.0);

        // Item sau khi cộng: quantity = 3, price = 150000
        CartDetail updatedDetail = new CartDetail();
        updatedDetail.setCartDetailId(5L);
        updatedDetail.setQuantity(3);
        updatedDetail.setPrice(150000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Arrays.asList(mockProduct));
        Mockito.when(productRepository.findByProductIdAndStatusTrue(productId)).thenReturn(mockProduct);
        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        // Giỏ hàng đã có sản phẩm này
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Arrays.asList(existingDetail));
        Mockito.when(cartDetailRepository.save(existingDetail)).thenReturn(updatedDetail);

        mockMvc.perform(post("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.price").value(150000.0));

        // Phải gọi save (cộng thêm vào item cũ, không tạo mới)
        Mockito.verify(cartDetailRepository).save(existingDetail);
    }

    /**
     * Test Case ID: TC_CARTDETAIL_07
     * Mô tả: Trả về 404 khi cartId không tồn tại, không lưu gì vào DB.
     */
    @Test
    void post_testNgoaiLe1_cartKhongTonTai() throws Exception {
        Long invalidCartId = 999L;

        Cart mockCart = new Cart();
        mockCart.setCartId(invalidCartId);

        Product mockProduct = new Product();
        mockProduct.setProductId(1L);

        CartDetail newDetail = new CartDetail();
        newDetail.setCart(mockCart);
        newDetail.setProduct(mockProduct);
        newDetail.setQuantity(1);
        newDetail.setPrice(50000.0);

        Mockito.when(cartRepository.existsById(invalidCartId)).thenReturn(false);

        mockMvc.perform(post("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isNotFound());

        Mockito.verify(cartRepository).existsById(invalidCartId);
        Mockito.verify(cartDetailRepository, Mockito.never()).save(any());
    }

    /**
     * Test Case ID: TC_CARTDETAIL_08
     * Mô tả: Trả về 404 khi sản phẩm không active (status=false), không lưu vào DB.
     */
    @Test
    void post_testNgoaiLe2_sanPhamKhongActive() throws Exception {
        Long cartId = 1L;
        Long inactiveProductId = 20L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        Product inactiveProduct = new Product();
        inactiveProduct.setProductId(inactiveProductId);

        CartDetail newDetail = new CartDetail();
        newDetail.setCart(mockCart);
        newDetail.setProduct(inactiveProduct);
        newDetail.setQuantity(1);
        newDetail.setPrice(50000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        // findByStatusTrue không trả về sản phẩm inactiveProductId
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Collections.emptyList());
        Mockito.when(productRepository.findByProductIdAndStatusTrue(inactiveProductId)).thenReturn(null);

        mockMvc.perform(post("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository, Mockito.never()).save(any());
    }

    // ==========================================
    // MODULE: CẬP NHẬT SỐ LƯỢNG ITEM TRONG GIỎ (PUT)
    // ==========================================

    /**
     * Test Case ID: TC_CARTDETAIL_09
     * Mô tả: Cập nhật item trong giỏ hàng thành công khi cartId hợp lệ.
     */
    @Test
    void put_testChuan1() throws Exception {
        Long cartId = 1L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        CartDetail detailToUpdate = new CartDetail();
        detailToUpdate.setCartDetailId(5L);
        detailToUpdate.setCart(mockCart);
        detailToUpdate.setQuantity(5);
        detailToUpdate.setPrice(250000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(cartDetailRepository.save(any(CartDetail.class))).thenReturn(detailToUpdate);

        mockMvc.perform(put("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(detailToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(250000.0));

        Mockito.verify(cartRepository).existsById(cartId);
        Mockito.verify(cartDetailRepository).save(any(CartDetail.class));
    }

    /**
     * Test Case ID: TC_CARTDETAIL_10
     * Mô tả: Trả về 404 khi cập nhật item nhưng cartId không tồn tại.
     */
    @Test
    void put_testNgoaiLe1() throws Exception {
        Long invalidCartId = 999L;

        Cart mockCart = new Cart();
        mockCart.setCartId(invalidCartId);

        CartDetail detailToUpdate = new CartDetail();
        detailToUpdate.setCartDetailId(5L);
        detailToUpdate.setCart(mockCart);
        detailToUpdate.setQuantity(2);

        Mockito.when(cartRepository.existsById(invalidCartId)).thenReturn(false);

        mockMvc.perform(put("/api/cartDetail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(detailToUpdate)))
                .andExpect(status().isNotFound());

        Mockito.verify(cartRepository).existsById(invalidCartId);
        Mockito.verify(cartDetailRepository, Mockito.never()).save(any());
    }

    // ==========================================
    // MODULE: XÓA ITEM KHỎI GIỎ HÀNG (DELETE)
    // ==========================================

    /**
     * Test Case ID: TC_CARTDETAIL_11
     * Mô tả: Xóa item khỏi giỏ hàng thành công khi cartDetailId tồn tại.
     */
    @Test
    void delete_testChuan1() throws Exception {
        Long validDetailId = 5L;

        Mockito.when(cartDetailRepository.existsById(validDetailId)).thenReturn(true);

        mockMvc.perform(delete("/api/cartDetail/{id}", validDetailId))
                .andExpect(status().isOk());

        Mockito.verify(cartDetailRepository).existsById(validDetailId);
        Mockito.verify(cartDetailRepository).deleteById(validDetailId);
    }

    /**
     * Test Case ID: TC_CARTDETAIL_12
     * Mô tả: Trả về 404 Not Found khi xóa item không tồn tại, không gọi deleteById.
     */
    @Test
    void delete_testNgoaiLe1() throws Exception {
        Long invalidDetailId = 999L;

        Mockito.when(cartDetailRepository.existsById(invalidDetailId)).thenReturn(false);

        mockMvc.perform(delete("/api/cartDetail/{id}", invalidDetailId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository).existsById(invalidDetailId);
        Mockito.verify(cartDetailRepository, Mockito.never()).deleteById(any());
    }
}
