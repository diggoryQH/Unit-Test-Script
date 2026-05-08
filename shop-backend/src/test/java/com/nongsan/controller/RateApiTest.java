package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Order;
import com.nongsan.entity.OrderDetail;
import com.nongsan.entity.Product;
import com.nongsan.entity.Rate;
import com.nongsan.entity.User;
import com.nongsan.repository.OrderDetailRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.RateRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RateApi.class)
@AutoConfigureMockMvc(addFilters = false)
class RateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @MockBean
    private RateRepository rateRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderDetailRepository orderDetailRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void findAll_testChuan1() throws Exception {
        Rate rate = new Rate();
        rate.setId(1L);
        rate.setComment("Great product");
        rate.setRating(5.0);

        Mockito.when(rateRepository.findAllByOrderByIdDesc()).thenReturn(Collections.singletonList(rate));

        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].comment").value("Great product"));

        Mockito.verify(rateRepository).findAllByOrderByIdDesc();
    }

    @Test
    void findById_testChuan1() throws Exception {
        Long orderDetailId = 10L;
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderDetailId(orderDetailId);

        Rate rate = new Rate();
        rate.setId(1L);
        rate.setOrderDetail(orderDetail);
        rate.setRating(4.0);

        Mockito.when(orderDetailRepository.existsById(orderDetailId)).thenReturn(true);
        Mockito.when(orderDetailRepository.findById(orderDetailId)).thenReturn(Optional.of(orderDetail));
        Mockito.when(rateRepository.findByOrderDetail(orderDetail)).thenReturn(rate);

        mockMvc.perform(get("/api/rates/{orderDetailId}", orderDetailId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        Mockito.verify(orderDetailRepository).existsById(orderDetailId);
        Mockito.verify(rateRepository).findByOrderDetail(orderDetail);
    }

    @Test
    void findById_testNgoaiLe1() throws Exception {
        Long orderDetailId = 99L;

        Mockito.when(orderDetailRepository.existsById(orderDetailId)).thenReturn(false);

        mockMvc.perform(get("/api/rates/{orderDetailId}", orderDetailId))
                .andExpect(status().isNotFound());

        Mockito.verify(orderDetailRepository).existsById(orderDetailId);
        Mockito.verify(rateRepository, Mockito.never()).findByOrderDetail(any());
    }

    @Test
    void findByProduct_testChuan1() throws Exception {
        Long productId = 5L;
        Product product = new Product();
        product.setProductId(productId);

        Rate rate = new Rate();
        rate.setId(1L);
        rate.setRating(5.0);

        Mockito.when(productRepository.existsById(productId)).thenReturn(true);
        Mockito.when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        Mockito.when(rateRepository.findByProductOrderByIdDesc(product)).thenReturn(Collections.singletonList(rate));

        mockMvc.perform(get("/api/rates/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));

        Mockito.verify(productRepository).existsById(productId);
        Mockito.verify(rateRepository).findByProductOrderByIdDesc(product);
    }

    @Test
    void findByProduct_testNgoaiLe1() throws Exception {
        Long productId = 99L;

        Mockito.when(productRepository.existsById(productId)).thenReturn(false);

        mockMvc.perform(get("/api/rates/product/{id}", productId))
                .andExpect(status().isNotFound());

        Mockito.verify(productRepository).existsById(productId);
        Mockito.verify(rateRepository, Mockito.never()).findByProductOrderByIdDesc(any());
    }

    @Test
    void post_testChuan1() throws Exception {
        User user = new User();
        user.setUserId(1L);

        Product product = new Product();
        product.setProductId(2L);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderDetailId(3L);

        Rate rate = new Rate();
        rate.setId(10L);
        rate.setUser(user);
        rate.setProduct(product);
        rate.setOrderDetail(orderDetail);
        rate.setRating(5.0);
        rate.setComment("Awesome!");

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(productRepository.existsById(2L)).thenReturn(true);
        Mockito.when(orderDetailRepository.existsById(3L)).thenReturn(true);
        Mockito.when(rateRepository.save(any(Rate.class))).thenReturn(rate);

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Awesome!"));

        Mockito.verify(rateRepository).save(any(Rate.class));
    }

    @Test
    void post_testNgoaiLe1_UserNotFound() throws Exception {
        User user = new User();
        user.setUserId(99L);
        Rate rate = new Rate();
        rate.setUser(user);

        Mockito.when(userRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isNotFound());

        Mockito.verify(rateRepository, Mockito.never()).save(any(Rate.class));
    }

    @Test
    void post_testNgoaiLe2_ProductNotFound() throws Exception {
        User user = new User();
        user.setUserId(1L);
        Product product = new Product();
        product.setProductId(99L);
        Rate rate = new Rate();
        rate.setUser(user);
        rate.setProduct(product);

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(productRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isNotFound());

        Mockito.verify(rateRepository, Mockito.never()).save(any(Rate.class));
    }

    @Test
    void post_testNgoaiLe3_OrderDetailNotFound() throws Exception {
        User user = new User();
        user.setUserId(1L);
        Product product = new Product();
        product.setProductId(2L);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderDetailId(99L);

        Rate rate = new Rate();
        rate.setUser(user);
        rate.setProduct(product);
        rate.setOrderDetail(orderDetail);

        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(productRepository.existsById(2L)).thenReturn(true);
        Mockito.when(orderDetailRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isNotFound());

        Mockito.verify(rateRepository, Mockito.never()).save(any(Rate.class));
    }

    @Test
    void put_testChuan1() throws Exception {
        Rate rate = new Rate();
        rate.setId(5L);
        rate.setRating(4.0);

        Mockito.when(rateRepository.existsById(5L)).thenReturn(true);
        Mockito.when(rateRepository.save(any(Rate.class))).thenReturn(rate);

        mockMvc.perform(put("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        Mockito.verify(rateRepository).save(any(Rate.class));
    }

    @Test
    void put_testNgoaiLe1() throws Exception {
        Rate rate = new Rate();
        rate.setId(99L);

        Mockito.when(rateRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(put("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(rate)))
                .andExpect(status().isNotFound());

        Mockito.verify(rateRepository, Mockito.never()).save(any(Rate.class));
    }

    @Test
    void delete_testChuan1() throws Exception {
        Long rateId = 1L;

        Mockito.when(rateRepository.existsById(rateId)).thenReturn(true);

        mockMvc.perform(delete("/api/rates/{id}", rateId))
                .andExpect(status().isOk());

        Mockito.verify(rateRepository).deleteById(rateId);
    }

    @Test
    void delete_testNgoaiLe1() throws Exception {
        Long rateId = 99L;

        Mockito.when(rateRepository.existsById(rateId)).thenReturn(false);

        mockMvc.perform(delete("/api/rates/{id}", rateId))
                .andExpect(status().isNotFound());

        Mockito.verify(rateRepository, Mockito.never()).deleteById(any());
    }
}
