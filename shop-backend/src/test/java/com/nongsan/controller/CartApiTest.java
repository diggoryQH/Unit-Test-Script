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

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: LẤY GIỎ HÀNG THEO EMAIL — GET /api/cart/user/{email}
    // ==========================================

    /**
     * TC_CART_01
     * Mục tiêu  : Lấy giỏ hàng thành công khi email người dùng tồn tại trong hệ thống.
     * Đầu vào   : email = "user@gmail.com" (tồn tại trong DB)
     * Hành vi GS: userRepository.existsByEmail → true
     *             userRepository.findByEmail   → Optional(mockUser)
     *             cartRepository.findByUser    → mockCart(cartId=1, address="Hà Nội", phone="0912345678")
     * Kết quả KV: HTTP 200 OK
     *             body.cartId   = 1
     *             body.address  = "Hà Nội"
     *             body.phone    = "0912345678"
     * Verify    : cartRepository.findByUser được gọi đúng 1 lần
     */
    @Test // [Happy Path] Lấy giỏ hàng thành công — email tồn tại
    void TC_CART_01() throws Exception {
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
     * TC_CART_02
     * Mục tiêu  : Trả về 404 khi email người dùng không tồn tại trong hệ thống.
     * Đầu vào   : email = "notfound@gmail.com" (không có trong DB)
     * Hành vi GS: userRepository.existsByEmail → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify    : cartRepository.findByUser KHÔNG được gọi (early return sau khi check email)
     */
    @Test // [Branch Coverage] Nhánh: email không tồn tại → 404
    void TC_CART_02() throws Exception {
        String invalidEmail = "notfound@gmail.com";

        Mockito.when(userRepository.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(get("/api/cart/user/{email}", invalidEmail))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository).existsByEmail(invalidEmail);
        Mockito.verify(cartRepository, Mockito.never()).findByUser(any());
    }

    /**
     * TC_CART_03
     * Mục tiêu  : Lấy giỏ hàng của user mới chưa thêm sản phẩm nào (giỏ rỗng).
     *             Xác nhận hệ thống trả về 200 thay vì lỗi khi amount=0, address=null.
     * Đầu vào   : email = "newuser@gmail.com" (tồn tại), cart chưa có item (amount=0)
     * Hành vi GS: cartRepository.findByUser → Cart(cartId=5, amount=0, address=null, phone=null)
     * Kết quả KV: HTTP 200 OK
     *             body.cartId = 5
     *             body.amount = 0.0
     * Verify    : cartRepository.findByUser được gọi đúng 1 lần
     */
    @Test // [Edge Case] User hợp lệ nhưng giỏ hàng chưa có item (amount=0)
    void TC_CART_03() throws Exception {
        String email = "newuser@gmail.com";

        User mockUser = new User();
        mockUser.setEmail(email);

        Cart emptyCart = new Cart();
        emptyCart.setCartId(5L);
        emptyCart.setAmount(0.0);
        emptyCart.setAddress(null);
        emptyCart.setPhone(null);
        emptyCart.setUser(mockUser);

        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(cartRepository.findByUser(mockUser)).thenReturn(emptyCart);

        mockMvc.perform(get("/api/cart/user/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(5))
                .andExpect(jsonPath("$.amount").value(0.0));

        Mockito.verify(cartRepository).findByUser(mockUser);
    }

    // ==========================================
    // MODULE: CẬP NHẬT GIỎ HÀNG — PUT /api/cart/user/{email}
    // ==========================================

    /**
     * TC_CART_04
     * Mục tiêu  : Cập nhật thành công địa chỉ và số điện thoại trong giỏ hàng.
     * Đầu vào   : email = "user@gmail.com" (tồn tại), body Cart hợp lệ
     * Hành vi GS: userRepository.existsByEmail → true
     *             cartRepository.save          → Cart đã cập nhật (address="TP. HCM", phone="0987654321")
     * Kết quả KV: HTTP 200 OK
     *             body.cartId  = 1
     *             body.address = "TP. Hồ Chí Minh"
     *             body.phone   = "0987654321"
     * Verify    : cartRepository.save được gọi đúng 1 lần
     */
    @Test // [Happy Path] Cập nhật giỏ hàng thành công — email tồn tại, body hợp lệ
    void TC_CART_04() throws Exception {
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
     * TC_CART_05
     * Mục tiêu  : Trả về 404 khi email không tồn tại, đảm bảo không lưu dữ liệu vào DB.
     * Đầu vào   : email = "ghost@gmail.com" (không tồn tại), body Cart bất kỳ
     * Hành vi GS: userRepository.existsByEmail → false
     * Kết quả KV: HTTP 404 Not Found
     * Verify    : cartRepository.save KHÔNG được gọi (không được ghi DB khi email sai)
     */
    @Test // [Branch Coverage] Nhánh: email không tồn tại → 404, không gọi save
    void TC_CART_05() throws Exception {
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
        Mockito.verify(cartRepository, Mockito.never()).save(any(Cart.class));
    }

    /**
     * TC_CART_06
     * Mục tiêu  : Cập nhật cart với địa chỉ rất dài — kiểm tra Controller không validate
     *             độ dài chuỗi, dữ liệu được lưu nguyên vẹn xuống DB.
     * Đầu vào   : email hợp lệ, address = chuỗi dài 95 ký tự
     * Hành vi GS: userRepository.existsByEmail → true
     *             cartRepository.save          → Cart với address dài
     * Kết quả KV: HTTP 200 OK
     *             body.address = chuỗi địa chỉ dài đúng như đã gửi
     * Verify    : cartRepository.save được gọi đúng 1 lần
     */
    @Test // [Edge Case] Địa chỉ rất dài — Controller không validate độ dài, lưu nguyên
    void TC_CART_06() throws Exception {
        String email = "user@gmail.com";
        String longAddress = "Số 1, Đường Nguyễn Trãi, Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội, Việt Nam";

        User mockUser = new User();
        mockUser.setEmail(email);

        Cart cartWithLongAddress = new Cart();
        cartWithLongAddress.setCartId(2L);
        cartWithLongAddress.setAddress(longAddress);
        cartWithLongAddress.setPhone("0912345678");

        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(cartRepository.save(any(Cart.class))).thenReturn(cartWithLongAddress);

        mockMvc.perform(put("/api/cart/user/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(cartWithLongAddress)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(longAddress));

        Mockito.verify(cartRepository).save(any(Cart.class));
    }
}
