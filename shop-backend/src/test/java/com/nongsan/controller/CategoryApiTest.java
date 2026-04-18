package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.entity.Category;
import com.nongsan.repository.CategoryRepository;
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
@WebMvcTest(CategoryApi.class)
@AutoConfigureMockMvc(addFilters = false) // Tạm tắt filter security để test API logic
class CategoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Giả lập Repository
    @MockBean
    private CategoryRepository repo;

    // Giả lập Security (Giữ lại theo form chuẩn của project)
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // ==========================================
    // MODULE: LẤY DANH SÁCH DANH MỤC (GET ALL)
    // ==========================================

    /**
     * Test Case ID: TC_CAT_01
     * Mô tả: Lấy danh sách tất cả danh mục thành công.
     */
    @Test
    void getAll_testChuan1() throws Exception {
        Category c1 = new Category();
        c1.setCategoryId(1L);
        c1.setCategoryName("Rau củ");

        Category c2 = new Category();
        c2.setCategoryId(2L);
        c2.setCategoryName("Trái cây");

        List<Category> categoryList = Arrays.asList(c1, c2);

        Mockito.when(repo.findAll()).thenReturn(categoryList);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Rau củ"))
                .andExpect(jsonPath("$[1].categoryName").value("Trái cây"));

        Mockito.verify(repo).findAll();
    }

    // ==========================================
    // MODULE: LẤY DANH MỤC THEO ID (GET BY ID)
    // ==========================================

    /**
     * Test Case ID: TC_CAT_02
     * Mô tả: Lấy danh mục thành công khi ID tồn tại.
     */
    @Test
    void getById_testChuan1() throws Exception {
        Long validId = 1L;
        Category mockCategory = new Category();
        mockCategory.setCategoryId(validId);
        mockCategory.setCategoryName("Thịt tươi");

        Mockito.when(repo.existsById(validId)).thenReturn(true);
        Mockito.when(repo.findById(validId)).thenReturn(Optional.of(mockCategory));

        mockMvc.perform(get("/api/categories/{id}", validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Thịt tươi"));

        Mockito.verify(repo).existsById(validId);
        Mockito.verify(repo).findById(validId);
    }

    /**
     * Test Case ID: TC_CAT_03
     * Mô tả: Lấy danh mục thất bại (Not Found) khi ID không tồn tại.
     */
    @Test
    void getById_testNgoaiLe1() throws Exception {
        Long invalidId = 999L;

        Mockito.when(repo.existsById(invalidId)).thenReturn(false);

        mockMvc.perform(get("/api/categories/{id}", invalidId))
                .andExpect(status().isNotFound());

        Mockito.verify(repo).existsById(invalidId);
        Mockito.verify(repo, Mockito.never()).findById(any());
    }

    // ==========================================
    // MODULE: THÊM MỚI DANH MỤC (POST)
    // ==========================================

    /**
     * Test Case ID: TC_CAT_04
     * Mô tả: Thêm mới danh mục thành công khi ID chưa tồn tại.
     */
    @Test
    void post_testChuan1() throws Exception {
        Category newCategory = new Category();
        newCategory.setCategoryId(10L);
        newCategory.setCategoryName("Hải sản");

        Mockito.when(repo.existsById(10L)).thenReturn(false);
        Mockito.when(repo.save(any(Category.class))).thenReturn(newCategory);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Hải sản"));

        Mockito.verify(repo).existsById(10L);
        Mockito.verify(repo).save(any(Category.class));
    }

    /**
     * Test Case ID: TC_CAT_05
     * Mô tả: Thêm mới thất bại (Bad Request) khi ID danh mục đã tồn tại trong DB.
     */
    @Test
    void post_testNgoaiLe1() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setCategoryId(10L);

        Mockito.when(repo.existsById(10L)).thenReturn(true);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(existingCategory)))
                .andExpect(status().isBadRequest());

        Mockito.verify(repo).existsById(10L);
        Mockito.verify(repo, Mockito.never()).save(any(Category.class));
    }

    // ==========================================
    // MODULE: CẬP NHẬT DANH MỤC (PUT)
    // ==========================================

    /**
     * Test Case ID: TC_CAT_06
     * Mô tả: Cập nhật danh mục thành công khi ID khớp và tồn tại trong DB.
     */
    @Test
    void put_testChuan1() throws Exception {
        Long targetId = 5L;

        Category updatePayload = new Category();
        updatePayload.setCategoryId(targetId);
        updatePayload.setCategoryName("Hạt khô cập nhật");

        Mockito.when(repo.existsById(targetId)).thenReturn(true);
        Mockito.when(repo.save(any(Category.class))).thenReturn(updatePayload);

        mockMvc.perform(put("/api/categories/{id}", targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Hạt khô cập nhật"));

        Mockito.verify(repo).existsById(targetId);
        Mockito.verify(repo).save(any(Category.class));
    }

    /**
     * Test Case ID: TC_CAT_07
     * Mô tả: Cập nhật thất bại (Bad Request) khi ID trên Path và ID trong Body JSON không khớp.
     */
    @Test
    void put_testNgoaiLe1() throws Exception {
        Long pathId = 5L;

        Category updatePayload = new Category();
        updatePayload.setCategoryId(99L); // Khác với pathId

        mockMvc.perform(put("/api/categories/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isBadRequest());

        Mockito.verify(repo, Mockito.never()).existsById(any());
        Mockito.verify(repo, Mockito.never()).save(any(Category.class));
    }

    /**
     * Test Case ID: TC_CAT_08
     * Mô tả: Cập nhật thất bại (Not Found) khi ID trên path hợp lệ nhưng không có trong DB.
     */
    @Test
    void put_testNgoaiLe2() throws Exception {
        Long targetId = 5L;

        Category updatePayload = new Category();
        updatePayload.setCategoryId(targetId);

        Mockito.when(repo.existsById(targetId)).thenReturn(false);

        mockMvc.perform(put("/api/categories/{id}", targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound());

        Mockito.verify(repo).existsById(targetId);
        Mockito.verify(repo, Mockito.never()).save(any(Category.class));
    }

    // ==========================================
    // MODULE: XÓA DANH MỤC (DELETE)
    // ==========================================

    /**
     * Test Case ID: TC_CAT_09
     * Mô tả: Xóa danh mục (Hard Delete) thành công khi ID tồn tại.
     */
    @Test
    void delete_testChuan1() throws Exception {
        Long deleteId = 7L;

        Mockito.when(repo.existsById(deleteId)).thenReturn(true);

        mockMvc.perform(delete("/api/categories/{id}", deleteId))
                .andExpect(status().isOk());

        Mockito.verify(repo).existsById(deleteId);
        Mockito.verify(repo).deleteById(deleteId);
    }

    /**
     * Test Case ID: TC_CAT_10
     * Mô tả: Xóa danh mục thất bại (Not Found) khi ID không tồn tại.
     */
    @Test
    void delete_testNgoaiLe1() throws Exception {
        Long invalidDeleteId = 999L;

        Mockito.when(repo.existsById(invalidDeleteId)).thenReturn(false);

        mockMvc.perform(delete("/api/categories/{id}", invalidDeleteId))
                .andExpect(status().isNotFound());

        Mockito.verify(repo).existsById(invalidDeleteId);
        Mockito.verify(repo, Mockito.never()).deleteById(any());
    }
}