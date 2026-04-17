package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.entity.Category;
import com.nongsan.entity.Product;
import com.nongsan.repository.CategoryRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductApi.class)
@AutoConfigureMockMvc(addFilters = false) // Tạm tắt filter security để test API logic
class ProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Giả lập các Repository để không tác động DB thật trong quá trình Unit Test
    @MockBean
    private ProductRepository productRepositoryMock;

    @MockBean
    private CategoryRepository categoryRepositoryMock;

    // Giả lập Security
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: LẤY SẢN PHẨM THEO DANH MỤC (CATEGORY)
    // ==========================================

    /**
     * Test Case ID: TC_01
     * Mô tả: Lấy danh sách sản phẩm thành công khi ID danh mục tồn tại.
     */
    @Test
    void getByCategory_testChuan1() throws Exception {
        Long validCategoryId = 1L;
        Category mockCategory = new Category();
        mockCategory.setCategoryId(validCategoryId);

        Product mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setName("Rau cải xanh");

        Mockito.when(categoryRepositoryMock.existsById(validCategoryId)).thenReturn(true);
        Mockito.when(categoryRepositoryMock.findById(validCategoryId)).thenReturn(Optional.of(mockCategory));
        Mockito.when(productRepositoryMock.findByCategory(mockCategory)).thenReturn(Arrays.asList(mockProduct));

        mockMvc.perform(get("/api/products/category/{id}", validCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rau cải xanh"));

        Mockito.verify(categoryRepositoryMock).existsById(validCategoryId);
        Mockito.verify(productRepositoryMock).findByCategory(mockCategory);
    }

    /**
     * Test Case ID: TC_02
     * Mô tả: Báo lỗi Not Found khi cố lấy sản phẩm của danh mục không tồn tại.
     */
    @Test
    void getByCategory_testNgoaiLe1() throws Exception {
        Long invalidCategoryId = 999L;

        Mockito.when(categoryRepositoryMock.existsById(invalidCategoryId)).thenReturn(false);

        mockMvc.perform(get("/api/products/category/{id}", invalidCategoryId))
                .andExpect(status().isNotFound());

        Mockito.verify(categoryRepositoryMock).existsById(invalidCategoryId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findByCategory(any());
    }

    // ==========================================
    // MODULE: LẤY SẢN PHẨM THEO ID
    // ==========================================

    /**
     * Test Case ID: TC_03
     * Mô tả: Lấy sản phẩm thành công khi ID sản phẩm tồn tại.
     */
    @Test
    void getById_testChuan1() throws Exception {
        Long validProductId = 1L;

        Product mockProduct = new Product();
        mockProduct.setProductId(validProductId);
        mockProduct.setName("Cam Sành");

        Mockito.when(productRepositoryMock.existsById(validProductId)).thenReturn(true);
        Mockito.when(productRepositoryMock.findById(validProductId)).thenReturn(Optional.of(mockProduct));

        mockMvc.perform(get("/api/products/{id}", validProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cam Sành"));

        Mockito.verify(productRepositoryMock).existsById(validProductId);
        Mockito.verify(productRepositoryMock).findById(validProductId);
    }

    /**
     * Test Case ID: TC_04
     * Mô tả: Báo lỗi Not Found khi ID sản phẩm không tồn tại.
     */
    @Test
    void getById_testNgoaiLe1() throws Exception {
        Long invalidProductId = 999L;

        Mockito.when(productRepositoryMock.existsById(invalidProductId)).thenReturn(false);

        mockMvc.perform(get("/api/products/{id}", invalidProductId))
                .andExpect(status().isNotFound());

        Mockito.verify(productRepositoryMock).existsById(invalidProductId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findById(any());
    }

    // ==========================================
    // MODULE: THÊM MỚI SẢN PHẨM (CREATE/POST)
    // ==========================================

    /**
     * Test Case ID: TC_05
     * Mô tả: Thêm mới sản phẩm thành công khi thông tin hợp lệ và ID chưa tồn tại.
     */
    @Test
    void post_testChuan1() throws Exception {
        Product newProduct = new Product();
        newProduct.setProductId(10L);
        newProduct.setName("Táo Mĩ");

        Mockito.when(productRepositoryMock.existsById(10L)).thenReturn(false);
        Mockito.when(productRepositoryMock.save(any(Product.class))).thenReturn(newProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Táo Mĩ"));

        Mockito.verify(productRepositoryMock).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_06
     * Mô tả: Thêm mới sản phẩm thất bại (Bad Request) nếu ID đã tồn tại trong DB.
     */
    @Test
    void post_testNgoaiLe1() throws Exception {
        Product existingProduct = new Product();
        existingProduct.setProductId(10L);

        Mockito.when(productRepositoryMock.existsById(10L)).thenReturn(true);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(existingProduct)))
                .andExpect(status().isBadRequest());

        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    // ==========================================
    // MODULE: CẬP NHẬT SẢN PHẨM (UPDATE/PUT)
    // ==========================================

    /**
     * Test Case ID: TC_07
     * Mô tả: Cập nhật sản phẩm thành công khi thông tin hợp lệ, ID khớp và đã có trong DB.
     */
    @Test
    void put_testChuan1() throws Exception {
        Long targetId = 5L;

        Product updatePayload = new Product();
        updatePayload.setProductId(targetId);
        updatePayload.setName("Chuối Nam Mỹ Cập Nhật");

        Mockito.when(productRepositoryMock.existsById(targetId)).thenReturn(true);
        Mockito.when(productRepositoryMock.save(any(Product.class))).thenReturn(updatePayload);

        mockMvc.perform(put("/api/products/{id}", targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chuối Nam Mỹ Cập Nhật"));

        Mockito.verify(productRepositoryMock).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_08
     * Mô tả: Cập nhật thất bại khi ID trên URL Path KHÔNG khớp với ID của body JSON.
     */
    @Test
    void put_testNgoaiLe1() throws Exception {
        Long pathId = 5L;

        Product updatePayload = new Product();
        updatePayload.setProductId(99L);

        mockMvc.perform(put("/api/products/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isBadRequest());

        Mockito.verify(productRepositoryMock, Mockito.never()).existsById(any());
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_09
     * Mô tả: Cập nhật thất bại khi ID tồn tại trên path nhưng không tồn tại trong database.
     */
    @Test
    void put_testNgoaiLe2() throws Exception {
        Long targetId = 5L;

        Product updatePayload = new Product();
        updatePayload.setProductId(targetId);

        Mockito.when(productRepositoryMock.existsById(targetId)).thenReturn(false);

        mockMvc.perform(put("/api/products/{id}", targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound());

        Mockito.verify(productRepositoryMock).existsById(targetId);
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    // ==========================================
    // MODULE: XÓA SẢN PHẨM (SOFT DELETE)
    // ==========================================

    /**
     * Test Case ID: TC_10
     * Mô tả: Xóa mềm sản phẩm (cập nhật status = false) thành công.
     */
    @Test
    void delete_testChuan1() throws Exception {
        Long deleteId = 7L;

        Product productToDelete = new Product();
        productToDelete.setProductId(deleteId);
        productToDelete.setStatus(true);

        Mockito.when(productRepositoryMock.existsById(deleteId)).thenReturn(true);
        Mockito.when(productRepositoryMock.findById(deleteId)).thenReturn(Optional.of(productToDelete));

        mockMvc.perform(delete("/api/products/{id}", deleteId))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(productToDelete.getStatus());

        Mockito.verify(productRepositoryMock).save(productToDelete);
    }

    /**
     * Test Case ID: TC_11
     * Mô tả: Xóa sản phẩm thất bại khi ID không tồn tại.
     */
    @Test
    void delete_testNgoaiLe1() throws Exception {
        Long invalidDeleteId = 999L;

        Mockito.when(productRepositoryMock.existsById(invalidDeleteId)).thenReturn(false);

        mockMvc.perform(delete("/api/products/{id}", invalidDeleteId))
                .andExpect(status().isNotFound());

        Mockito.verify(productRepositoryMock).existsById(invalidDeleteId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findById(any());
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    // ==========================================
    // MODULE: IMPORT DỮ LIỆU SẢN PHẨM
    // ==========================================

    /**
     * Test Case ID: TC_12
     * Mô tả: Import danh sách sản phẩm thành công thông qua hàm saveAll.
     */
    @Test
    void importCsv_testChuan1() throws Exception {
        Product p1 = new Product();
        p1.setProductId(1L);
        p1.setName("Sản phẩm 1");

        Product p2 = new Product();
        p2.setProductId(2L);
        p2.setName("Sản phẩm 2");

        List<Product> importList = Arrays.asList(p1, p2);

        Mockito.when(productRepositoryMock.saveAll(any())).thenReturn(importList);

        mockMvc.perform(post("/api/products/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(importList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        Mockito.verify(productRepositoryMock).saveAll(any());
    }

    // ==========================================
    // MODULE: CÁC ENDPOINT LẤY DANH SÁCH (GET)
    // ==========================================

    /**
     * Test Case ID: TC_13
     * Mô tả: Lấy tất cả sản phẩm đang có trạng thái kích hoạt (status = true).
     */
    @Test
    void getAll_testChuan1() throws Exception {
        Product activeProduct = new Product();
        activeProduct.setName("Active Sp");

        Mockito.when(productRepositoryMock.findByStatusTrue()).thenReturn(Arrays.asList(activeProduct));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        Mockito.verify(productRepositoryMock).findByStatusTrue();
    }

    /**
     * Test Case ID: TC_14
     * Mô tả: Lấy danh sách sản phẩm bán chạy nhất cho User (status = true, sort by sold desc).
     */
    @Test
    void getBestSeller_testChuan1() throws Exception {
        Mockito.when(productRepositoryMock.findByStatusTrueOrderBySoldDesc()).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products/bestseller"))
                .andExpect(status().isOk());

        Mockito.verify(productRepositoryMock).findByStatusTrueOrderBySoldDesc();
    }

    /**
     * Test Case ID: TC_15
     * Mô tả: Lấy top 10 sản phẩm bán chạy nhất cho Admin (bỏ qua status).
     */
    @Test
    void getBestSellerAdmin_testChuan1() throws Exception {
        Mockito.when(productRepositoryMock.findTop10ByOrderBySoldDesc()).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products/bestseller-admin"))
                .andExpect(status().isOk());

        Mockito.verify(productRepositoryMock).findTop10ByOrderBySoldDesc();
    }

    /**
     * Test Case ID: TC_16
     * Mô tả: Lấy danh sách sản phẩm mới nhất (sắp xếp theo EnteredDate).
     */
    @Test
    void getLasted_testChuan1() throws Exception {
        Mockito.when(productRepositoryMock.findByStatusTrueOrderByEnteredDateDesc()).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products/latest"))
                .andExpect(status().isOk());

        Mockito.verify(productRepositoryMock).findByStatusTrueOrderByEnteredDateDesc();
    }

    /**
     * Test Case ID: TC_17
     * Mô tả: Lấy danh sách sản phẩm được đánh giá cao.
     */
    @Test
    void getRated_testChuan1() throws Exception {
        Mockito.when(productRepositoryMock.findProductRated()).thenReturn(Arrays.asList(new Product()));

        mockMvc.perform(get("/api/products/rated"))
                .andExpect(status().isOk());

        Mockito.verify(productRepositoryMock).findProductRated();
    }

    /**
     * Test Case ID: TC_18
     * Mô tả: Lấy danh sách sản phẩm gợi ý dựa trên categoryId và productId.
     */
    @Test
    void suggest_testChuan1() throws Exception {
        Long categoryId = 3L;
        Long productId = 10L;

        Product suggestedProduct = new Product();
        suggestedProduct.setName("Gợi ý 1");

        Mockito.when(productRepositoryMock.findProductSuggest(categoryId, productId, categoryId, categoryId))
                .thenReturn(Arrays.asList(suggestedProduct));

        mockMvc.perform(get("/api/products/suggest/{categoryId}/{productId}", categoryId, productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        Mockito.verify(productRepositoryMock)
                .findProductSuggest(categoryId, productId, categoryId, categoryId);
    }
}