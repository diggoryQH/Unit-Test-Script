package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Order;
import com.nongsan.entity.OrderReturn;
import com.nongsan.repository.OrderRepository;
import com.nongsan.repository.OrderReturnRepository;
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

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REQ-12: Xử lý đơn hàng
 * File test cho: OrderReturnApi.java
 * Các hàm:
 * - createRequest (POST /api/returns/request) — 2 nhánh
 * - getByOrderId (GET /api/returns/order/{id}) — 2 nhánh
 * - processReturn (PUT /api/returns/admin/process) — 3 nhánh
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderReturnApi.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderReturnApiTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private OrderReturnRepository returnRepo;
        @MockBean
        private OrderRepository orderRepo;

        @MockBean
        private UserDetailsServiceImpl userDetailsService;
        @MockBean
        private AuthEntryPointJwt unauthorizedHandler;
        @MockBean
        private AuthTokenFilter authTokenFilter;

        @Autowired
        private ObjectMapper jsonMapper;

        /**
         * Test Case ID: TC_RETURN_01
         * Mô tả: Tạo yêu cầu hoàn/hủy đơn hàng thành công và cập nhật trạng thái đơn
         * hàng sang chờ xử lý (status=4).
         */
        @Test
        void createRequest_testChuan1() throws Exception {
                Long orderId = 1L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 2, null);

                OrderReturn req = new OrderReturn();
                req.setOrder(mockOrder);
                req.setReason("Hàng bị hư hỏng");
                req.setDescription("Mô tả chi tiết");

                OrderReturn savedReturn = new OrderReturn(1L, mockOrder, "Hàng bị hư hỏng",
                                "Mô tả", null, null, 0, null);

                Mockito.when(orderRepo.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(returnRepo.save(any(OrderReturn.class))).thenReturn(savedReturn);
                Mockito.when(orderRepo.save(mockOrder)).thenReturn(mockOrder);

                mockMvc.perform(post("/api/returns/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(0))
                                .andExpect(jsonPath("$.reason").value("Hàng bị hư hỏng"));

                // Xác nhận Order được cập nhật sang status=4
                Mockito.verify(orderRepo).save(mockOrder);
                Mockito.verify(returnRepo).save(any(OrderReturn.class));
        }

        /**
         * Test Case ID: TC_RETURN_02
         * Mô tả: Ném ngoại lệ (Exception) khi cố tạo yêu cầu hoàn/hủy cho một đơn hàng
         * không tồn tại.
         */
        @Test
        void createRequest_testNgoaiLe1_donHangKhongTonTai() throws Exception {
                Long invalidOrderId = 999L;
                Order invalidOrder = new Order();
                invalidOrder.setOrdersId(invalidOrderId);

                OrderReturn req = new OrderReturn();
                req.setOrder(invalidOrder);
                req.setReason("Lý do");

                Mockito.when(orderRepo.findById(invalidOrderId))
                                .thenThrow(new RuntimeException("Không tìm thấy đơn hàng"));

                // NestedServletException bao bọc RuntimeException — dùng assertThrows để bắt
                org.junit.jupiter.api.Assertions.assertThrows(
                                Exception.class,
                                () -> mockMvc.perform(post("/api/returns/request")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(jsonMapper.writeValueAsString(req))));

                // Xác nhận không gọi save khi không tìm thấy đơn hàng
                Mockito.verify(returnRepo, Mockito.never()).save(any());
        }

        /**
         * Test Case ID: TC_RETURN_03
         * Mô tả: Lấy thông tin yêu cầu trả hàng thành công khi đơn hàng có tồn tại yêu
         * cầu đó.
         */
        @Test
        void getByOrderId_testChuan1() throws Exception {
                Long orderId = 1L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 4, null);
                OrderReturn mockReturn = new OrderReturn(1L, mockOrder, "Hư hỏng",
                                "Mô tả", null, null, 0, null);

                Mockito.when(orderRepo.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(returnRepo.findByOrder(mockOrder)).thenReturn(mockReturn);

                mockMvc.perform(get("/api/returns/order/{orderId}", orderId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value(0))
                                .andExpect(jsonPath("$.reason").value("Hư hỏng"));

                Mockito.verify(returnRepo).findByOrder(mockOrder);
        }

        /**
         * Test Case ID: TC_RETURN_04
         * Mô tả: Báo lỗi Not Found khi tìm kiếm yêu cầu trả hàng trên một đơn hàng
         * không có bất kỳ yêu cầu nào.
         */
        @Test
        void getByOrderId_testNgoaiLe1_khongCoYeuCau() throws Exception {
                Long orderId = 2L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 2, null);

                Mockito.when(orderRepo.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(returnRepo.findByOrder(mockOrder)).thenReturn(null);

                mockMvc.perform(get("/api/returns/order/{orderId}", orderId))
                                .andExpect(status().isNotFound());
        }

        /**
         * Test Case ID: TC_RETURN_05
         * Mô tả: Xử lý yêu cầu hoàn trả thành công với hành động chấp nhận trả hàng và
         * hoàn tiền (chuyển sang status=6).
         */
        @Test
        void processReturn_testChuan1_chapNhanTraHang() throws Exception {
                Long returnId = 1L;
                Order mockOrder = new Order(1L, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 4, null);
                OrderReturn mockReturn = new OrderReturn(returnId, mockOrder, "Hư hỏng",
                                "Mô tả", null, 100000.0, 0, null); // refundAmount != null

                Mockito.when(returnRepo.findById(returnId)).thenReturn(Optional.of(mockReturn));
                Mockito.when(orderRepo.save(mockOrder)).thenReturn(mockOrder);
                mockReturn.setStatus(1);
                Mockito.when(returnRepo.save(mockReturn)).thenReturn(mockReturn);

                mockMvc.perform(put("/api/returns/admin/process/{id}", returnId)
                                .param("action", "1")
                                .param("note", "Chấp nhận trả hàng"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(1));

                // Order phải chuyển sang status=6 (trả hàng/hoàn tiền)
                Mockito.verify(orderRepo).save(mockOrder);
                assert mockOrder.getStatus() == 6;
        }

        /**
         * Test Case ID: TC_RETURN_06
         * Mô tả: Xử lý yêu cầu hoàn trả thành công với hành động chấp nhận hủy đơn
         * (chuyển sang status=3) do không có số tiền hoàn.
         */
        @Test
        void processReturn_testChuan2_chapNhanHuy() throws Exception {
                Long returnId = 2L;
                Order mockOrder = new Order(2L, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 4, null);
                OrderReturn mockReturn = new OrderReturn(returnId, mockOrder, "Không cần",
                                "Mô tả", null, null, 0, null); // refundAmount == null

                Mockito.when(returnRepo.findById(returnId)).thenReturn(Optional.of(mockReturn));
                Mockito.when(orderRepo.save(mockOrder)).thenReturn(mockOrder);
                mockReturn.setStatus(1);
                Mockito.when(returnRepo.save(mockReturn)).thenReturn(mockReturn);

                mockMvc.perform(put("/api/returns/admin/process/{id}", returnId)
                                .param("action", "1")
                                .param("note", "Chấp nhận hủy"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(1));

                // Order phải chuyển sang status=3 (hủy)
                Mockito.verify(orderRepo).save(mockOrder);
                assert mockOrder.getStatus() == 3;
        }

        /**
         * Test Case ID: TC_RETURN_07
         * Mô tả: Xử lý từ chối yêu cầu hoàn trả, đưa trạng thái đơn hàng và trạng thái
         * yêu cầu trả hàng về lại bình thường (status=2).
         */
        @Test
        void processReturn_testNgoaiLe1_tuChoi() throws Exception {
                Long returnId = 3L;
                Order mockOrder = new Order(3L, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 4, null);
                OrderReturn mockReturn = new OrderReturn(returnId, mockOrder, "Hư hỏng",
                                "Mô tả", null, 100000.0, 0, null);

                Mockito.when(returnRepo.findById(returnId)).thenReturn(Optional.of(mockReturn));
                Mockito.when(orderRepo.save(mockOrder)).thenReturn(mockOrder);
                mockReturn.setStatus(2);
                Mockito.when(returnRepo.save(mockReturn)).thenReturn(mockReturn);

                mockMvc.perform(put("/api/returns/admin/process/{id}", returnId)
                                .param("action", "0")
                                .param("note", "Từ chối vì không đủ bằng chứng"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(2));

                // Từ chối → ret.status=2, order.status=2
                Mockito.verify(orderRepo).save(mockOrder);
                assert mockOrder.getStatus() == 2;
        }
}
