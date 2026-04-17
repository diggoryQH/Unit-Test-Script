package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.entity.Favorite;
import com.nongsan.entity.Product;
import com.nongsan.entity.User;
import com.nongsan.repository.FavoriteRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.repository.UserRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FavoritesApi.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt Security Filter để test API Logic
class FavoritesApiTest {

    @Autowired
    private MockMvc mockMvc;

    // Giả lập các Repository liên quan
    @MockBean
    private FavoriteRepository favoriteRepositoryMock;

    @MockBean
    private UserRepository userRepositoryMock;

    @MockBean
    private ProductRepository productRepositoryMock;

    // Giả lập các thành phần Security bắt buộc
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper jsonMapper;

    // LƯU Ý VỀ ROLLBACK:
    // Vì đang dùng @WebMvcTest và @MockBean, mọi thao tác "lưu", "xóa" chỉ là giả lập
    // trên bộ nhớ ảo (mock). Do đó, DB thực tế an toàn 100% và không cần cấu hình Rollback.

    // ==========================================
    // MODULE: LẤY FAVORITE THEO EMAIL
    // ==========================================

    /**
     * Test Case ID: TC_01
     * Mô tả: Lấy danh sách yêu thích thành công khi Email tồn tại.
     */
    @Test
    void findByEmail_testChuan1() throws Exception {
        String testEmail = "test@abc.com";
        User mockUser = new User(); mockUser.setEmail(testEmail);
        Favorite mockFav = new Favorite(); mockFav.setFavoriteId(1L);

        Mockito.when(userRepositoryMock.existsByEmail(testEmail)).thenReturn(true);
        Mockito.when(userRepositoryMock.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));
        Mockito.when(favoriteRepositoryMock.findByUser(mockUser)).thenReturn(Arrays.asList(mockFav));

        mockMvc.perform(get("/api/favorites/email/{email}", testEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].favoriteId").value(1));

        // CheckDB
        Mockito.verify(userRepositoryMock).existsByEmail(testEmail);
        Mockito.verify(favoriteRepositoryMock).findByUser(mockUser);
    }

    /**
     * Test Case ID: TC_02
     * Mô tả: Báo lỗi Not Found khi Email không tồn tại.
     */
    @Test
    void findByEmail_testNgoaiLe1() throws Exception {
        String invalidEmail = "none@abc.com";

        Mockito.when(userRepositoryMock.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/email/{email}", invalidEmail))
                .andExpect(status().isNotFound());

        // CheckDB: Dừng lại ngay sau khi check exists, không truy vấn danh sách
        Mockito.verify(userRepositoryMock).existsByEmail(invalidEmail);
        Mockito.verify(favoriteRepositoryMock, Mockito.never()).findByUser(any());
    }

    // ==========================================
    // MODULE: ĐẾM LƯỢT YÊU THÍCH THEO SẢN PHẨM
    // ==========================================

    /**
     * Test Case ID: TC_03
     * Mô tả: Trả về số lượng yêu thích (count) khi Sản phẩm tồn tại.
     */
    @Test
    void findByProduct_testChuan1() throws Exception {
        Long productId = 1L;
        Product mockProduct = new Product(); mockProduct.setProductId(productId);

        Mockito.when(productRepositoryMock.existsById(productId)).thenReturn(true);
        Mockito.when(productRepositoryMock.getById(productId)).thenReturn(mockProduct);
        Mockito.when(favoriteRepositoryMock.countByProduct(mockProduct)).thenReturn(5);

        mockMvc.perform(get("/api/favorites/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        // CheckDB
        Mockito.verify(productRepositoryMock).existsById(productId);
        Mockito.verify(favoriteRepositoryMock).countByProduct(mockProduct);
    }

    /**
     * Test Case ID: TC_04
     * Mô tả: Báo lỗi Not Found khi Sản phẩm không tồn tại.
     */
    @Test
    void findByProduct_testNgoaiLe1() throws Exception {
        Long invalidProductId = 99L;

        Mockito.when(productRepositoryMock.existsById(invalidProductId)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/product/{id}", invalidProductId))
                .andExpect(status().isNotFound());

        // CheckDB
        Mockito.verify(productRepositoryMock).existsById(invalidProductId);
        Mockito.verify(favoriteRepositoryMock, Mockito.never()).countByProduct(any());
    }

    // ==========================================
    // MODULE: LẤY CHI TIẾT FAVORITE THEO SẢN PHẨM & EMAIL
    // ==========================================

    /**
     * Test Case ID: TC_05
     * Mô tả: Lấy bản ghi Favorite thành công khi cả Sản phẩm và User đều tồn tại.
     */
    @Test
    void findByProductAndUser_testChuan1() throws Exception {
        Long productId = 1L;
        String email = "user@abc.com";

        Product mockProduct = new Product(); mockProduct.setProductId(productId);
        User mockUser = new User(); mockUser.setEmail(email);
        Favorite mockFav = new Favorite(); mockFav.setFavoriteId(10L);

        Mockito.when(userRepositoryMock.existsByEmail(email)).thenReturn(true);
        Mockito.when(productRepositoryMock.existsById(productId)).thenReturn(true);

        Mockito.when(productRepositoryMock.findById(productId)).thenReturn(Optional.of(mockProduct));
        Mockito.when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(mockUser));
        Mockito.when(favoriteRepositoryMock.findByProductAndUser(mockProduct, mockUser)).thenReturn(mockFav);

        mockMvc.perform(get("/api/favorites/{productId}/{email}", productId, email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoriteId").value(10));

        // CheckDB
        Mockito.verify(favoriteRepositoryMock).findByProductAndUser(mockProduct, mockUser);
    }

    /**
     * Test Case ID: TC_06
     * Mô tả: Báo lỗi Not Found khi User tồn tại nhưng Sản phẩm KHÔNG tồn tại.
     */
    @Test
    void findByProductAndUser_testNgoaiLe1() throws Exception {
        Long invalidProductId = 99L;
        String email = "user@abc.com";

        Mockito.when(userRepositoryMock.existsByEmail(email)).thenReturn(true);
        Mockito.when(productRepositoryMock.existsById(invalidProductId)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/{productId}/{email}", invalidProductId, email))
                .andExpect(status().isNotFound());

        // CheckDB
        Mockito.verify(favoriteRepositoryMock, Mockito.never()).findByProductAndUser(any(), any());
    }

    /**
     * Test Case ID: TC_07
     * Mô tả: Báo lỗi Not Found khi User KHÔNG tồn tại (Bỏ qua luôn check Sản phẩm).
     */
    @Test
    void findByProductAndUser_testNgoaiLe2() throws Exception {
        Long productId = 1L;
        String invalidEmail = "none@abc.com";

        Mockito.when(userRepositoryMock.existsByEmail(invalidEmail)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/{productId}/{email}", productId, invalidEmail))
                .andExpect(status().isNotFound());

        // CheckDB
        Mockito.verify(productRepositoryMock, Mockito.never()).existsById(any());
    }

    // ==========================================
    // MODULE: THÊM MỚI FAVORITE (POST)
    // ==========================================

    /**
     * Test Case ID: TC_08
     * Mô tả: Lưu Favorite thành công (Lưu ý: API endpoint là /email nhưng body là Favorite).
     */
    @Test
    void post_testChuan1() throws Exception {
        Favorite newFavorite = new Favorite();
        newFavorite.setFavoriteId(100L);

        Mockito.when(favoriteRepositoryMock.save(any(Favorite.class))).thenReturn(newFavorite);

        mockMvc.perform(post("/api/favorites/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newFavorite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoriteId").value(100));

        // CheckDB
        Mockito.verify(favoriteRepositoryMock).save(any(Favorite.class));
    }

    // ==========================================
    // MODULE: XÓA FAVORITE (DELETE)
    // ==========================================

    /**
     * Test Case ID: TC_09
     * Mô tả: Xóa Favorite thành công khi ID tồn tại.
     */
    @Test
    void delete_testChuan1() throws Exception {
        Long targetId = 7L;

        Mockito.when(favoriteRepositoryMock.existsById(targetId)).thenReturn(true);

        mockMvc.perform(delete("/api/favorites/{id}", targetId))
                .andExpect(status().isOk());

        // CheckDB
        Mockito.verify(favoriteRepositoryMock).deleteById(targetId);
    }

    /**
     * Test Case ID: TC_10
     * Mô tả: Xóa Favorite thất bại khi ID không tồn tại.
     */
    @Test
    void delete_testNgoaiLe1() throws Exception {
        Long invalidId = 999L;

        Mockito.when(favoriteRepositoryMock.existsById(invalidId)).thenReturn(false);

        mockMvc.perform(delete("/api/favorites/{id}", invalidId))
                .andExpect(status().isNotFound());

        // CheckDB
        Mockito.verify(favoriteRepositoryMock).existsById(invalidId);
        Mockito.verify(favoriteRepositoryMock, Mockito.never()).deleteById(any());
    }
}