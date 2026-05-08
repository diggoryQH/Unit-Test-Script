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

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: LẤY DANH SÁCH ITEM THEO CART ID — GET /api/cartDetail/cart/{id}
    // ==========================================

    /**
     * TC_CTD_01
     * Mục tiêu : Lấy danh sách sản phẩm trong giỏ thành công khi cartId tồn tại và
     * có items.
     * Đầu vào : cartId = 1 (tồn tại, có 2 items)
     * Hành vi GS: cartRepository.existsById → true
     * cartRepository.findById → Optional(mockCart)
     * cartDetailRepository.findByCart → [item1(qty=2), item2(qty=1)]
     * Kết quả KV: HTTP 200 OK
     * body.size() = 2
     * body[0].quantity = 2
     * body[1].quantity = 1
     * Verify : cartDetailRepository.findByCart được gọi đúng 1 lần
     */
    @Test // [Happy Path] Lấy danh sách item thành công — cartId tồn tại và có items
    void TC_CTD_01() throws Exception {
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
     * TC_CTD_02
     * Mục tiêu : Trả về 404 khi cartId không tồn tại trong DB.
     * Đầu vào : cartId = 999 (không có trong DB)
     * Hành vi GS: cartRepository.existsById → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.findByCart KHÔNG được gọi (early return)
     */
    @Test // [Branch Coverage] Nhánh: cartId không tồn tại → 404, không gọi findByCart
    void TC_CTD_02() throws Exception {
        Long invalidCartId = 999L;

        Mockito.when(cartRepository.existsById(invalidCartId)).thenReturn(false);

        mockMvc.perform(get("/api/cartDetail/cart/{id}", invalidCartId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartRepository).existsById(invalidCartId);
        Mockito.verify(cartDetailRepository, Mockito.never()).findByCart(any());
    }

    /**
     * TC_CTD_03
     * Mục tiêu : Giỏ hàng tồn tại nhưng chưa có sản phẩm nào → trả về mảng rỗng
     * (không phải 404).
     * Đầu vào : cartId = 2 (tồn tại, chưa có item nào)
     * Hành vi GS: cartRepository.existsById → true
     * cartDetailRepository.findByCart → [] (danh sách rỗng)
     * Kết quả KV: HTTP 200 OK
     * body.size() = 0
     * Verify : cartDetailRepository.findByCart được gọi đúng 1 lần
     */
    @Test // [Edge Case] Giỏ tồn tại nhưng rỗng → 200 OK với mảng rỗng
    void TC_CTD_03() throws Exception {
        Long cartId = 2L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cartDetail/cart/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        Mockito.verify(cartDetailRepository).findByCart(mockCart);
    }

    // ==========================================
    // MODULE: LẤY 1 ITEM THEO CARTDETAIL ID — GET /api/cartDetail/{id}
    // ==========================================

    /**
     * TC_CTD_04
     * Mục tiêu : Lấy thành công thông tin một item trong giỏ khi cartDetailId tồn
     * tại.
     * Đầu vào : cartDetailId = 1 (tồn tại, quantity=3, price=90000)
     * Hành vi GS: cartDetailRepository.existsById → true
     * cartDetailRepository.findById → Optional(mockDetail)
     * Kết quả KV: HTTP 200 OK
     * body.cartDetailId = 1
     * body.quantity = 3
     * Verify : findById được gọi đúng 1 lần
     */
    @Test // [Happy Path] Lấy 1 item thành công — cartDetailId tồn tại
    void TC_CTD_04() throws Exception {
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
     * TC_CTD_05
     * Mục tiêu : Trả về 404 khi cartDetailId không tồn tại.
     * Đầu vào : cartDetailId = 999 (không có trong DB)
     * Hành vi GS: cartDetailRepository.existsById → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.findById KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: cartDetailId không tồn tại → 404
    void TC_CTD_05() throws Exception {
        Long invalidDetailId = 999L;

        Mockito.when(cartDetailRepository.existsById(invalidDetailId)).thenReturn(false);

        mockMvc.perform(get("/api/cartDetail/{id}", invalidDetailId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository).existsById(invalidDetailId);
        Mockito.verify(cartDetailRepository, Mockito.never()).findById(any());
    }

    // ==========================================
    // MODULE: THÊM SẢN PHẨM VÀO GIỎ HÀNG — POST /api/cartDetail
    // ==========================================

    /**
     * TC_CTD_06
     * Mục tiêu : Thêm sản phẩm chưa có trong giỏ → tạo CartDetail mới.
     * Đầu vào : body CartDetail(cartId=1, productId=10, qty=1, price=50000)
     * Giỏ hàng hiện tại rỗng (không có item nào)
     * Hành vi GS: cartRepository.existsById → true
     * productRepository.findByStatusTrue → [mockProduct]
     * productRepository.findByProductIdAndStatusTrue(10) → mockProduct
     * cartDetailRepository.findByCart → [] (rỗng)
     * cartDetailRepository.save → savedDetail(cartDetailId=1)
     * Kết quả KV: HTTP 200 OK
     * body.cartDetailId = 1
     * body.quantity = 1
     * Verify : cartDetailRepository.save được gọi đúng 1 lần (tạo mới)
     */
    @Test // [Happy Path] Thêm sản phẩm mới — sản phẩm chưa có trong giỏ → tạo CartDetail
          // mới
    void TC_CTD_06() throws Exception {
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
     * TC_CTD_07
     * Mục tiêu : Thêm sản phẩm đã có trong giỏ → cộng dồn số lượng và giá, không
     * tạo mới.
     * Đầu vào : body CartDetail(productId=10, qty=1, price=50000)
     * Giỏ hàng đã có item productId=10 với qty=2, price=100000
     * Hành vi GS: cartDetailRepository.findByCart → [existingDetail(qty=2,
     * price=100000)]
     * cartDetailRepository.save(existingDetail) → updatedDetail(qty=3,
     * price=150000)
     * Kết quả KV: HTTP 200 OK
     * body.quantity = 3 (2 + 1)
     * body.price = 150000.0 (100000 + 50000)
     * Verify : save được gọi với existingDetail (cập nhật item cũ, không tạo mới)
     */
    @Test // [Branch Coverage] Nhánh: sản phẩm đã có trong giỏ → cộng dồn qty + price
    void TC_CTD_07() throws Exception {
        Long cartId = 1L;
        Long productId = 10L;

        Cart mockCart = new Cart();
        mockCart.setCartId(cartId);

        Product mockProduct = new Product();
        mockProduct.setProductId(productId);

        CartDetail existingDetail = new CartDetail();
        existingDetail.setCartDetailId(5L);
        existingDetail.setCart(mockCart);
        existingDetail.setProduct(mockProduct);
        existingDetail.setQuantity(2);
        existingDetail.setPrice(100000.0);

        CartDetail newDetail = new CartDetail();
        newDetail.setCart(mockCart);
        newDetail.setProduct(mockProduct);
        newDetail.setQuantity(1);
        newDetail.setPrice(50000.0);

        CartDetail updatedDetail = new CartDetail();
        updatedDetail.setCartDetailId(5L);
        updatedDetail.setQuantity(3);
        updatedDetail.setPrice(150000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Arrays.asList(mockProduct));
        Mockito.when(productRepository.findByProductIdAndStatusTrue(productId)).thenReturn(mockProduct);
        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Arrays.asList(existingDetail));
        Mockito.when(cartDetailRepository.save(existingDetail)).thenReturn(updatedDetail);

        mockMvc.perform(post("/api/cartDetail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.price").value(150000.0));

        Mockito.verify(cartDetailRepository).save(existingDetail);
    }

    /**
     * TC_CTD_08
     * Mục tiêu : Trả về 404 khi cartId không tồn tại, không lưu gì vào DB.
     * Đầu vào : body CartDetail(cartId=999, ...) — cartId không có trong DB
     * Hành vi GS: cartRepository.existsById → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.save KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: cartId không tồn tại → 404, không gọi save
    void TC_CTD_08() throws Exception {
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
     * TC_CTD_09
     * Mục tiêu : Trả về 404 khi sản phẩm không active (status=false) — không được
     * thêm vào giỏ.
     * Đầu vào : body CartDetail(productId=20) — productId=20 không có trong danh
     * sách active
     * Hành vi GS: cartRepository.existsById → true
     * productRepository.findByStatusTrue → [] (không có sản phẩm active nào)
     * productRepository.findByProductIdAndStatusTrue(20) → null
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.save KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: sản phẩm không active (status=false) → 404
    void TC_CTD_09() throws Exception {
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
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Collections.emptyList());
        Mockito.when(productRepository.findByProductIdAndStatusTrue(inactiveProductId)).thenReturn(null);

        mockMvc.perform(post("/api/cartDetail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository, Mockito.never()).save(any());
    }

    /**
     * TC_CTD_10
     * Mục tiêu : Thêm sản phẩm vào giỏ hàng đang rỗng hoàn toàn (lần mua đầu tiên
     * của user).
     * Xác nhận sản phẩm được tạo mới (không phải cộng dồn).
     * Đầu vào : body CartDetail(cartId=1, productId=10, qty=1, price=75000)
     * cartDetailRepository.findByCart → [] (giỏ rỗng)
     * Hành vi GS: cartDetailRepository.save → savedDetail(cartDetailId=99,
     * price=75000)
     * Kết quả KV: HTTP 200 OK
     * body.cartDetailId = 99
     * body.price = 75000.0
     * Verify : save được gọi 1 lần (tạo item mới vì giỏ rỗng, không có gì để cộng
     * dồn)
     */
    @Test // [Edge Case] Lần đầu tiên thêm hàng — giỏ rỗng hoàn toàn → tạo item mới
    void TC_CTD_10() throws Exception {
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
        newDetail.setPrice(75000.0);

        CartDetail savedDetail = new CartDetail();
        savedDetail.setCartDetailId(99L);
        savedDetail.setCart(mockCart);
        savedDetail.setProduct(mockProduct);
        savedDetail.setQuantity(1);
        savedDetail.setPrice(75000.0);

        Mockito.when(cartRepository.existsById(cartId)).thenReturn(true);
        Mockito.when(productRepository.findByStatusTrue()).thenReturn(Arrays.asList(mockProduct));
        Mockito.when(productRepository.findByProductIdAndStatusTrue(productId)).thenReturn(mockProduct);
        Mockito.when(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart));
        Mockito.when(cartDetailRepository.findByCart(mockCart)).thenReturn(Collections.emptyList());
        Mockito.when(cartDetailRepository.save(any(CartDetail.class))).thenReturn(savedDetail);

        mockMvc.perform(post("/api/cartDetail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newDetail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartDetailId").value(99))
                .andExpect(jsonPath("$.price").value(75000.0));

        Mockito.verify(cartDetailRepository).save(any(CartDetail.class));
    }

    // ==========================================
    // MODULE: CẬP NHẬT SỐ LƯỢNG ITEM — PUT /api/cartDetail
    // ==========================================

    /**
     * TC_CTD_11
     * Mục tiêu : Cập nhật số lượng và giá của một item trong giỏ hàng thành công.
     * Đầu vào : body CartDetail(cartDetailId=5, cartId=1, qty=5, price=250000)
     * Hành vi GS: cartRepository.existsById → true
     * cartDetailRepository.save → detailToUpdate(qty=5, price=250000)
     * Kết quả KV: HTTP 200 OK
     * body.quantity = 5
     * body.price = 250000.0
     * Verify : cartDetailRepository.save được gọi đúng 1 lần
     */
    @Test // [Happy Path] Cập nhật qty và price của item thành công
    void TC_CTD_11() throws Exception {
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
     * TC_CTD_12
     * Mục tiêu : Trả về 404 khi cập nhật item nhưng cartId không tồn tại.
     * Đầu vào : body CartDetail(cartId=999, ...) — cartId không có trong DB
     * Hành vi GS: cartRepository.existsById → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.save KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: cartId không tồn tại khi PUT → 404, không save
    void TC_CTD_12() throws Exception {
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
    // MODULE: XÓA ITEM KHỎI GIỎ HÀNG — DELETE /api/cartDetail/{id}
    // ==========================================

    /**
     * TC_CTD_13
     * Mục tiêu : Xóa item khỏi giỏ hàng thành công khi cartDetailId tồn tại.
     * Đầu vào : cartDetailId = 5 (tồn tại)
     * Hành vi GS: cartDetailRepository.existsById → true
     * Kết quả KV: HTTP 200 OK
     * Verify : deleteById(5) được gọi đúng 1 lần
     */
    @Test // [Happy Path] Xóa item thành công — cartDetailId tồn tại
    void TC_CTD_13() throws Exception {
        Long validDetailId = 5L;

        Mockito.when(cartDetailRepository.existsById(validDetailId)).thenReturn(true);

        mockMvc.perform(delete("/api/cartDetail/{id}", validDetailId))
                .andExpect(status().isOk());

        Mockito.verify(cartDetailRepository).existsById(validDetailId);
        Mockito.verify(cartDetailRepository).deleteById(validDetailId);
    }

    /**
     * TC_CTD_14
     * Mục tiêu : Trả về 404 khi xóa item không tồn tại, deleteById không được gọi.
     * Đầu vào : cartDetailId = 999 (không có trong DB)
     * Hành vi GS: cartDetailRepository.existsById → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify : cartDetailRepository.deleteById KHÔNG được gọi
     */
    @Test // [Branch Coverage] Nhánh: cartDetailId không tồn tại → 404, không gọi
          // deleteById
    void TC_CTD_14() throws Exception {
        Long invalidDetailId = 999L;

        Mockito.when(cartDetailRepository.existsById(invalidDetailId)).thenReturn(false);

        mockMvc.perform(delete("/api/cartDetail/{id}", invalidDetailId))
                .andExpect(status().isNotFound());

        Mockito.verify(cartDetailRepository).existsById(invalidDetailId);
        Mockito.verify(cartDetailRepository, Mockito.never()).deleteById(any());
    }
}
