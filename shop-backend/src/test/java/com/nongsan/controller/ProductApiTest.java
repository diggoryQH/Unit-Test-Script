package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.entity.Product;
import com.nongsan.entity.Category;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.CategoryRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductApi.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository repo;

    @MockBean
    private CategoryRepository cRepo;

    // 🔥 MOCK FULL SECURITY (QUAN TRỌNG NHẤT)
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // ================== GET ALL ==================
    @Test
    void shouldReturnAllProducts() throws Exception {
        when(repo.findByStatusTrue()).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    // ================== GET BY ID ==================
    @Test
    void shouldReturnProductById() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(true);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(repo.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    // ================== POST ==================
    @Test
    void shouldCreateProduct() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(false);
        when(repo.save(any(Product.class))).thenReturn(p);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenDuplicateId() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(true);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    // ================== PUT ==================
    @Test
    void shouldUpdateProduct() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(true);
        when(repo.save(any(Product.class))).thenReturn(p);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenIdMismatch() throws Exception {
        Product p = new Product();
        p.setProductId(2L);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdateNotFound() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(false);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isNotFound());
    }

    // ================== DELETE ==================
    @Test
    void shouldDeleteProduct() throws Exception {
        Product p = new Product();
        p.setProductId(1L);

        when(repo.existsById(1L)).thenReturn(true);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        verify(repo).save(any(Product.class));
    }

    @Test
    void shouldReturn404WhenDeleteNotFound() throws Exception {
        when(repo.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    // ================== CATEGORY ==================
    @Test
    void shouldReturnProductsByCategory() throws Exception {
        Category c = new Category();
        c.setCategoryId(1L);

        when(cRepo.existsById(1L)).thenReturn(true);
        when(cRepo.findById(1L)).thenReturn(Optional.of(c));
        when(repo.findByCategory(c)).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenCategoryNotExist() throws Exception {
        when(cRepo.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isNotFound());
    }
}