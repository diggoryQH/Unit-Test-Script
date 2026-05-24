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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Các hàm: findAll, getById, getByUser, updateStatus, cancel, deliver, success,
 * updateProduct
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderApi.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderApiTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private OrderRepository orderRepository;
        @MockBean
        private OrderDetailRepository orderDetailRepository;
        @MockBean
        private UserRepository userRepository;
        @MockBean
        private CartRepository cartRepository;
        @MockBean
        private CartDetailRepository cartDetailRepository;
        @MockBean
        private ProductRepository productRepository;
        @MockBean
        private SendMailUtil sendMailUtil;

        @MockBean
        private UserDetailsServiceImpl userDetailsService;
        @MockBean
        private AuthEntryPointJwt unauthorizedHandler;
        @MockBean
        private AuthTokenFilter authTokenFilter;

        @Autowired
        private ObjectMapper jsonMapper;

        /**
         * Test Case ID: TC_ORDER_01
         * Mô tả: Lấy tất cả đơn hàng thành công, trả về danh sách được sắp xếp mới
         * nhất.
         */
        @Test
        void findAll_testChuan1() throws Exception {
                Order o1 = new Order(1L, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);
                Order o2 = new Order(2L, new Date(), 200000.0, "HCM", "0987", 20000.0, 2.0, 2, null);

                Mockito.when(orderRepository.findAllByOrderByOrdersIdDesc()).thenReturn(Arrays.asList(o2, o1));

                mockMvc.perform(get("/api/orders"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(2))
                                .andExpect(jsonPath("$[0].ordersId").value(2))
                                .andExpect(jsonPath("$[0].address").value("HCM"))
                                .andExpect(jsonPath("$[1].ordersId").value(1));

                Mockito.verify(orderRepository).findAllByOrderByOrdersIdDesc();
        }

        /**
         * Test Case ID: TC_ORDER_02
         * Mô tả: Lấy đơn hàng theo ID thành công khi ID đơn hàng tồn tại.
         */
        @Test
        void getById_testChuan1() throws Exception {
                Long orderId = 1L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 0, null);

                Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
                Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

                mockMvc.perform(get("/api/orders/{id}", orderId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ordersId").value(1))
                                .andExpect(jsonPath("$.address").value("HN"))
                                .andExpect(jsonPath("$.phone").value("0912"));

                Mockito.verify(orderRepository).existsById(orderId);
                Mockito.verify(orderRepository).findById(orderId);
        }

        /**
         * Test Case ID: TC_ORDER_03
         * Mô tả: Báo lỗi Not Found khi lấy đơn hàng với ID không tồn tại.
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

        /**
         * Test Case ID: TC_ORDER_04
         * Mô tả: Lấy danh sách đơn hàng của người dùng thành công khi email tồn tại.
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
                                .andExpect(jsonPath("$.size()").value(1))
                                .andExpect(jsonPath("$[0].ordersId").value(1))
                                .andExpect(jsonPath("$[0].address").value("HN"));

                Mockito.verify(userRepository).existsByEmail(email);
                Mockito.verify(orderRepository).findByUserOrderByOrdersIdDesc(mockUser);
        }

        /**
         * Test Case ID: TC_ORDER_05
         * Mô tả: Báo lỗi Not Found khi lấy đơn hàng của người dùng với email không tồn
         * tại.
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

        /**
         * Test Case ID: TC_ORDER_06
         * Mô tả: Cập nhật trạng thái thất bại (Not Found) khi ID đơn hàng không tồn
         * tại.
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
         * Test Case ID: TC_ORDER_07
         * Mô tả: Cập nhật trạng thái giao hàng (status=1) thành công và gửi mail thông
         * báo đơn hàng đang giao.
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
         * Test Case ID: TC_ORDER_08
         * Mô tả: Cập nhật trạng thái giao thành công (status=2) thành công và gửi mail
         * thông báo hoàn tất giao hàng.
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
         * Test Case ID: TC_ORDER_09
         * Mô tả: Xác nhận lỗi BUG khi cập nhật trạng thái giao thành công (status=2)
         * nhưng hệ thống không gọi hàm trừ số lượng kho.
         */
        @Test
        void updateStatus_testChuan2b_status2_loiKhongTruKho() throws Exception {
                Long orderId = 2L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 1, null);

                Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
                Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

                mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 2))
                                .andExpect(status().isOk());

                // BUG: productRepository KHÔNG BAO GIỜ được gọi để update số lượng
                Mockito.verify(productRepository).save(any(Product.class));
        }

        /**
         * Test Case ID: TC_ORDER_10
         * Mô tả: Cập nhật trạng thái hủy đơn (status=3) thành công và gửi mail thông
         * báo hủy đơn.
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
         * Test Case ID: TC_ORDER_11
         * Mô tả: Cập nhật các trạng thái khác (status=0) thành công mà không kích hoạt
         * gửi email thông báo.
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

        /**
         * Test Case ID: TC_ORDER_12
         * Mô tả: Cập nhật trạng thái trả hàng hoàn tiền (status=6) thành công và gửi
         * mail thông báo hủy đơn/trả hàng.
         */
        @Test
        void updateStatus_testChuan5_status6_traHangHoanTien() throws Exception {
                Long orderId = 5L;
                Order mockOrder = new Order(orderId, new Date(), 100000.0, "HN", "0912", 15000.0, 1.0, 4, null);

                Mockito.when(orderRepository.existsById(orderId)).thenReturn(true);
                Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);

                mockMvc.perform(get("/api/orders/updateStatus/{id}/{status}", orderId, 6))
                                .andExpect(status().isOk());

                // status=6 phải gửi mail hủy (cùng nhánh với status=3)
                Mockito.verify(sendMailUtil).sendMailOrderCancel(mockOrder);
                Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
                Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderSuccess(any());
        }

        /**
         * Test Case ID: TC_ORDER_13
         * Mô tả: Hủy đơn hàng thành công (chuyển sang status=3) và gửi email thông báo
         * hủy khi ID đơn hàng tồn tại.
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
         * Test Case ID: TC_ORDER_14
         * Mô tả: Báo lỗi Not Found khi gọi hủy đơn hàng với ID không tồn tại.
         */
        @Test
        void cancel_testNgoaiLe1() throws Exception {
                Mockito.when(orderRepository.existsById(999L)).thenReturn(false);

                mockMvc.perform(get("/api/orders/cancel/{orderId}", 999L))
                                .andExpect(status().isNotFound());

                Mockito.verify(orderRepository, Mockito.never()).save(any());
                Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderCancel(any());
        }

        /**
         * Test Case ID: TC_ORDER_15
         * Mô tả: Giao đơn hàng thành công (chuyển sang status=1) và gửi email thông báo
         * đang giao khi ID đơn hàng tồn tại.
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
         * Test Case ID: TC_ORDER_16
         * Mô tả: Báo lỗi Not Found khi gọi trạng thái đang giao cho đơn hàng có ID
         * không tồn tại.
         */
        @Test
        void deliver_testNgoaiLe1() throws Exception {
                Mockito.when(orderRepository.existsById(999L)).thenReturn(false);

                mockMvc.perform(get("/api/orders/deliver/{orderId}", 999L))
                                .andExpect(status().isNotFound());

                Mockito.verify(orderRepository, Mockito.never()).save(any());
                Mockito.verify(sendMailUtil, Mockito.never()).sendMailOrderDeliver(any());
        }

        /**
         * Test Case ID: TC_ORDER_17
         * Mô tả: Xác nhận giao đơn hàng thành công (status=2), thực hiện trừ số lượng
         * sản phẩm trong kho và gửi email thông báo.
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

        /**
         * Test Case ID: TC_ORDER_18
         * Mô tả: Kiểm tra tính toán chính xác của số lượng tồn kho (quantity) và số
         * lượng đã bán (sold) sau khi xác nhận đơn thành công.
         */
        @Test
        void success_testChuan2_kiemTraGiaTriTruKho() throws Exception {
                Long orderId = 2L;

                Product mockProduct = new Product();
                mockProduct.setProductId(10L);
                mockProduct.setQuantity(10); // Tồn kho ban đầu
                mockProduct.setSold(5); // Đã bán ban đầu

                // Đơn hàng gồm 2 sản phẩm productId=10
                OrderDetail mockDetail = new OrderDetail(1L, 2, 40000.0, mockProduct, null);

                Order mockOrder = new Order(orderId, new Date(), 40000.0, "HCM", "0987", 20000.0, 1.0, 1, null);

                Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
                Mockito.when(orderRepository.save(mockOrder)).thenReturn(mockOrder);
                Mockito.when(orderDetailRepository.findByOrder(mockOrder)).thenReturn(Arrays.asList(mockDetail));
                Mockito.when(productRepository.findById(10L)).thenReturn(Optional.of(mockProduct));
                Mockito.when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

                mockMvc.perform(get("/api/orders/success/{orderId}", orderId))
                                .andExpect(status().isOk());

                // Bắt đối tượng Product được truyền vào save() để kiểm tra giá trị thực tế
                ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
                Mockito.verify(productRepository).save(productCaptor.capture());

                Product savedProduct = productCaptor.getValue();
                assertEquals(8, savedProduct.getQuantity(),
                                "[BUG DETECT] quantity phải giảm đúng: 10 - 2 = 8, nhưng thực tế là "
                                                + savedProduct.getQuantity());
                assertEquals(7, savedProduct.getSold(),
                                "[BUG DETECT] sold phải tăng đúng: 5 + 2 = 7, nhưng thực tế là "
                                                + savedProduct.getSold());
        }
}
