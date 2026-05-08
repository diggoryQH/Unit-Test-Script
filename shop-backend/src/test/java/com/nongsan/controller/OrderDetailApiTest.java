package com.nongsan.controller;

import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Order;
import com.nongsan.entity.OrderDetail;
import com.nongsan.repository.OrderDetailRepository;
import com.nongsan.repository.OrderRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
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
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-12: Xử lý đơn hàng
 * File test cho: OderDetailApi.java
 * Hàm: getByOrder(Long id)
 * Endpoint: GET /api/orderDetail/order/{id}
 * Nhánh: 2 (existsById true/false)
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(OderDetailApi.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderDetailApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private OrderDetailRepository orderDetailRepository;
    @MockBean private OrderRepository orderRepository;

    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private AuthEntryPointJwt unauthorizedHandler;
    @MockBean private AuthTokenFilter authTokenFilter;

    // =========================================
    // getByOrder() — GET /api/orderDetail/order/{id}
    // Nhánh: 2 (orderId tồn tại / không tồn tại)
    // =========================================

    /**
     * TC_ORDERDETAIL_01 | getByOrder | Nhánh: orderId tồn tại
     * Lấy chi tiết sản phẩm trong đơn hàng thành công.
     */
    @Test
    void getByOrder_testChuan1() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 2, null);

        OrderDetail d1 = new OrderDetail(1L, 2, 40000.0, null, mockOrder);
        OrderDetail d2 = new OrderDetail(2L, 1, 30000.0, null, mockOrder);

        Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        Mockito.when(orderDetailRepository.findByOrder(mockOrder)).thenReturn(Arrays.asList(d1, d2));

        mockMvc.perform(get("/api/orderDetail/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].quantity").value(1));

        Mockito.verify(orderRepository).existsById(orderId);
        Mockito.verify(orderDetailRepository).findByOrder(mockOrder);
    }

    /**
     * TC_ORDERDETAIL_02 | getByOrder | Nhánh: orderId không tồn tại → 404
     */
    @Test
    void getByOrder_testNgoaiLe1() throws Exception {
        Long invalidId = 999L;
        Mockito.when(orderRepository.existsById(invalidId)).thenReturn(false);

        mockMvc.perform(get("/api/orderDetail/order/{id}", invalidId))
                .andExpect(status().isNotFound());

        Mockito.verify(orderRepository).existsById(invalidId);
        Mockito.verify(orderDetailRepository, Mockito.never()).findByOrder(any());
    }
}
