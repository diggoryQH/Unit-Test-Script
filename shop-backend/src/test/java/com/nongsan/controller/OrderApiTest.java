package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-12: Xử lý đơn hàng
 * File test cho: OrderApi.java (phần xử lý đơn hàng)
 * Các hàm: findAll, getById, getByUser, updateStatus, cancel, deliver, success, updateProduct
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderApi.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderApiTest {

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

    // =========================================
    // findAll() — GET /api/orders
    // Nhánh: 1 (không có if/else)
    // =========================================

    /**
     * TC_ORDER_01 | findAll | Nhánh: happy path
     * Lấy tất cả đơn hàng thành công, trả về danh sách sắp xếp mới nhất.
     */
    @Test
    void findAll_testChuan1() throws Exception {
        Order o1 = new Order(1L, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);
        Order o2 = new Order(2L, new Date(), 200000.0, "HCM", "0987", 20000.0, 2.0, 2, null);

        Mockito.when(orderRepository.findAllByOrderByOrdersIdDesc()).thenReturn(Arrays.asList(o2, o1));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].ordersId").value(2));

        Mockito.verify(orderRepository).findAllByOrderByOrdersIdDesc();
    }

    // =========================================
    // getById() — GET /api/orders/{id}
    // Nhánh: 2 (existsById true/false)
    // =========================================

    /**
     * TC_ORDER_02 | getById | Nhánh: id tồn tại
     * Lấy đơn hàng theo ID thành công.
     */
    @Test
    void getById_testChuan1() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").value(1));

        Mockito.verify(orderRepository).existsById(orderId);
        Mockito.verify(orderRepository).findById(orderId);
    }

    /**
     * TC_ORDER_03 | getById | Nhánh: id không tồn tại → 404
     */
    @Test
    void getById_testNgoaiLe1() throws Exception {
        Long invalidId = 999L;
        Mockito.when(orderRepository.existsById(invalidId)).thenReturn(false);

        mockMvc.perform(get("/api/orders/{id}", invalidId))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository).existsById(invalidId);
        Mockito.verify(orderRepository, Mockito.never()).findById(any());
    }

    // =========================================
    // getByUser() — GET /api/orders/user/{email}
    // Nhánh: 2 (existsByEmail true/false)
    // =========================================

    /**
     * TC_ORDER_04 | getByUser | Nhánh: email tồn tại
     * Lấy danh sách đơn hàng của user theo email thành công.
     */
    @Test
    void getByUser_testChuan1() throws Exception {
        String email = "user@gmail.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        Order o1 = new Order(1L, new Date(), 50000.0, "HN", "0912", 10000.0, 0.5, 0, mockUser);
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(orderRepository.findByUserOrderByOrdersIdDesc(mockUser)).thenReturn(Arrays.asList(o1));

        mockMvc.perform(get("/api/orders/user/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        Mockito.verify(userRepository).existsByEmail(email);
        Mockito.verify(orderRepository).findByUserOrderByOrdersIdDesc(mockUser);
    }

    /**
     * TC_ORDER_05 | getByUser | Nhánh: email không tồn tại → 404
     */
    @Test
    void getByUser_testNgoaiLe1() throws Exception {
        String invalidEmail = "ghost@gmail.com";
        Mockito.when(userRepository.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/{email}", invalidEmail))
                .andExpect(status().isNotFound());

        Mockito.verify(userRepository).existsByEmail(invalidEmail);
        Mockito.verify(orderRepository, Mockito.never()).findByUserOrderByOrdersIdDesc(any());
    }

    // =========================================
    // updateStatus() — GET /api/orders/updateStatus/{id}/{status}
    // Nhánh: 5
    //   - id không tồn tại → 404
    //   - status=1 → sendMailOrderDeliver
    //   - status=2 → sendMailOrderSuccess
    //   - status=3 → sendMailOrderCancel
    //   - status=0 (khác) → không gửi mail
    // =========================================

    /**
     * TC_ORDER_06 | updateStatus | Nhánh: id không tồn tại → 404
     */
    @Test
    void updateStatus_testNgoaiLe1_idKhongTonTai() throws Exception {
        Mockito.when(orderRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", 999L, 1))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository, Mockito.never()).save(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
    }

    /**
     * TC_ORDER_07 | updateStatus | Nhánh: status=1 → gửi mail "đang giao"
     */
    @Test
    void updateStatus_testChuan1_status1_dangGiao() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 1))
                .andExpect(status().isOk());

        Mockito.verify(sendMailUtil).sendMailOrderDeliver(mockOrder);
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderSuccess(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderCancel(any());
    }

    /**
     * TC_ORDER_08 | updateStatus | Nhánh: status=2 → gửi mail "giao thành công"
     */
    @Test
    void updateStatus_testChuan2_status2_thanhCong() throws Exception {
        Long orderId = 2L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 1, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 2))
                .andExpect(status().isOk());

        Mockito.verify(sendMailUtil).sendMailOrderSuccess(mockOrder);
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderCancel(any());
    }

    /**
     * TC_ORDER_09 | updateStatus | Nhánh: status=3 → gửi mail "hủy đơn"
     */
    @Test
    void updateStatus_testChuan3_status3_huyDon() throws Exception {
        Long orderId = 3L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 3))
                .andExpect(status().isOk());

        Mockito.verify(sendMailUtil).sendMailOrderCancel(mockOrder);
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderSuccess(any());
    }

    /**
     * TC_ORDER_10 | updateStatus | Nhánh: status=0 (khác) → không gửi bất kỳ mail nào
     */
    @Test
    void updateStatus_testChuan4_statusKhac_khongGuiMail() throws Exception {
        Long orderId = 4L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 0))
                .andExpect(status().isOk());

        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderSuccess(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderCancel(any());
    }

    // =========================================
    // cancel() — GET /api/orders/cancel/{orderId}
    // Nhánh: 2 (existsById true/false)
    // =========================================

    /**
     * TC_ORDER_11 | cancel | Nhánh: id tồn tại → status=3, gửi mail hủy
     */
    @Test
    void cancel_testChuan1() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/cancel/{orderId}", orderId))
                .andExpect(status().isOk());

        Mockito.verify(orderRepository).save(mockOrder);
        Mockito.verify(sendMailUtil).sendMailOrderCancel(mockOrder);
    }

    /**
     * TC_ORDER_12 | cancel | Nhánh: id không tồn tại → 404, không gửi mail
     */
    @Test
    void cancel_testNgoaiLe1() throws Exception {
        Mockito.when(orderRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/api/orders/cancel/{orderId}", 999L))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository, Mockito.never()).save(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderCancel(any());
    }

    // =========================================
    // deliver() — GET /api/orders/deliver/{orderId}
    // Nhánh: 2 (existsById true/false)
    // =========================================

    /**
     * TC_ORDER_13 | deliver | Nhánh: id tồn tại → status=1, gửi mail giao hàng
     */
    @Test
    void deliver_testChuan1() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/deliver/{orderId}", orderId))
                .andExpect(status().isOk());

        Mockito.verify(orderRepository).save(mockOrder);
        Mockito.verify(sendMailUtil).sendMailOrderDeliver(mockOrder);
    }

    /**
     * TC_ORDER_14 | deliver | Nhánh: id không tồn tại → 404, không gửi mail
     */
    @Test
    void deliver_testNgoaiLe1() throws Exception {
        Mockito.when(orderRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/api/orders/deliver/{orderId}", 999L))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository, Mockito.never()).save(any());
        Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
    }

    // =========================================
    // success() — GET /api/orders/success/{orderId}
    // Nhánh: 1 (không có existsById — BUG đã ghi nhận)
    // =========================================

    /**
     * TC_ORDER_15 | success | Nhánh: happy path — status=2, cập nhật kho, gửi mail
     * ⚠️ Lưu ý: hàm không kiểm tra existsById (bug), chỉ test được happy path.
     */
    @Test
    void success_testChuan1() throws Exception {
        Long orderId = 1L;
        User mockUser = new User();

        Product mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setQuantity(10);
        mockProduct.setSold(5);

        OrderDetail mockDetail = new OrderDetail(1L, 2, 20000.0, mockProduct, null);

        Order mockOrder = new Order(orderId, new Date(), 40000.0, "HN", "0912", 15000.0, 1.0, 1, mockUser);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);
        Mockito.when(orderDetailRepository.findByOrder(mockOrder)).thenReturn(Arrays.asList(mockDetail));
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        Mockito.when(productRepository.save(mockProduct)).thenReturn(mockProduct);

        mockMvc.perform(get("/api/orders/success/{orderId}", orderId))
                .andExpect(status().isOk());

        // Xác nhận status=2, cập nhật kho, gửi mail thành công
        Mockito.verify(orderRepository).save(mockOrder);
        Mockito.verify(productRepository).save(mockProduct);
        Mockito.verify(sendMailUtil).sendMailOrderSuccess(mockOrder);
    }
}
