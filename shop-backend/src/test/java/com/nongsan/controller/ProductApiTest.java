package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.Category;
import com.nongsan.entity.Product;
import com.nongsan.repository.CategoryRepository;
import com.nongsan.repository.ProductRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho ProductApi Controller.
 *
 * Tiêu chuẩn áp dụng:
 *   - FIRST: Fast, Isolated, Repeatable, Self-Validating, Timely
 *   - AAA Pattern: Arrange / Act / Assert
 *
 * @WebMvcTest chỉ load Controller layer, không khởi động full Spring context.
 * Tất cả Repository và Security bean đều được mock → không chạm DB/network thật.
 */
@WebMvcTest(ProductApi.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt Security filter để tập trung test logic API
class ProductApiTest {

    // ==========================================
    // DEPENDENCIES
    // ==========================================

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @MockBean
    private ProductRepository productRepositoryMock;

    @MockBean
    private CategoryRepository categoryRepositoryMock;

    // --- Mock Security beans (bắt buộc khi @WebMvcTest load SecurityConfig) ---
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    // ==========================================
    // SHARED TEST DATA
    // ==========================================

    private Product sampleProduct;
    private Category sampleCategory;

    /**
     * Khởi tạo dữ liệu dùng chung trước mỗi test.
     * Đảm bảo Isolated: mỗi test bắt đầu với state sạch, không bị ảnh hưởng bởi test trước.
     */
    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setCategoryId(1L);

        sampleProduct = new Product();
        sampleProduct.setProductId(1L);
        sampleProduct.setName("Rau cải xanh");
        sampleProduct.setPrice(15000.0);
        sampleProduct.setStatus(true);
        sampleProduct.setCategory(sampleCategory);
    }

    // ==========================================
    // MODULE: LẤY SẢN PHẨM THEO DANH MỤC (CATEGORY)
    // ==========================================

    /**
     * Test Case ID: TC_PRO_01
     * Mô tả: Lấy danh sách sản phẩm thành công khi ID danh mục tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_01 - CategoryId tồn tại → trả về 200 và danh sách sản phẩm đúng")
    void getByCategory_testChuan1() throws Exception {
        // Arrange
        Long validCategoryId = 1L;
        Mockito.when(categoryRepositoryMock.existsById(validCategoryId)).thenReturn(true);
        Mockito.when(categoryRepositoryMock.findById(validCategoryId)).thenReturn(Optional.of(sampleCategory));
        Mockito.when(productRepositoryMock.findByCategory(sampleCategory)).thenReturn(Arrays.asList(sampleProduct));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/category/{id}", validCategoryId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rau cải xanh"))
                .andExpect(jsonPath("$[0].price").value(15000.0));
        Mockito.verify(categoryRepositoryMock).existsById(validCategoryId);
        Mockito.verify(productRepositoryMock).findByCategory(sampleCategory);
    }

    /**
     * Test Case ID: TC_PRO_02
     * Mô tả: Báo lỗi Not Found khi cố lấy sản phẩm của danh mục không tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_02 - CategoryId không tồn tại → trả về 404, không truy vấn sản phẩm")
    void getByCategory_testNgoaiLe1() throws Exception {
        // Arrange
        Long invalidCategoryId = 999L;
        Mockito.when(categoryRepositoryMock.existsById(invalidCategoryId)).thenReturn(false);

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/category/{id}", invalidCategoryId));

        // Assert
        result.andExpect(status().isNotFound());
        Mockito.verify(categoryRepositoryMock).existsById(invalidCategoryId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findByCategory(any());
    }

    /**
     * Test Case ID: TC_PRO_03
     * Mô tả: Category tồn tại nhưng chưa có sản phẩm nào → trả về mảng rỗng, không phải 404.
     */
    @Test
    @DisplayName("TC_PRO_03 - CategoryId tồn tại nhưng không có sản phẩm → trả về 200 và mảng rỗng")
    void getByCategory_testNgoaiLe2() throws Exception {
        // Arrange
        Long validCategoryId = 1L;
        Mockito.when(categoryRepositoryMock.existsById(validCategoryId)).thenReturn(true);
        Mockito.when(categoryRepositoryMock.findById(validCategoryId)).thenReturn(Optional.of(sampleCategory));
        Mockito.when(productRepositoryMock.findByCategory(sampleCategory)).thenReturn(List.of());

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/category/{id}", validCategoryId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        Mockito.verify(productRepositoryMock).findByCategory(sampleCategory);
    }

    // ==========================================
    // MODULE: LẤY SẢN PHẨM THEO ID
    // ==========================================

    /**
     * Test Case ID: TC_PRO_04
     * Mô tả: Lấy sản phẩm thành công khi ID sản phẩm tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_04 - ID tồn tại → trả về 200 và thông tin sản phẩm đúng")
    void getById_testChuan1() throws Exception {
        // Arrange
        Long validProductId = 1L;
        Mockito.when(productRepositoryMock.existsById(validProductId)).thenReturn(true);
        Mockito.when(productRepositoryMock.findById(validProductId)).thenReturn(Optional.of(sampleProduct));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/{id}", validProductId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Rau cải xanh"));
        Mockito.verify(productRepositoryMock).existsById(validProductId);
        Mockito.verify(productRepositoryMock).findById(validProductId);
    }

    /**
     * Test Case ID: TC_PRO_05
     * Mô tả: Báo lỗi Not Found khi ID sản phẩm không tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_05 - ID không tồn tại → trả về 404, không gọi findById")
    void getById_testNgoaiLe1() throws Exception {
        // Arrange
        Long invalidProductId = 999L;
        Mockito.when(productRepositoryMock.existsById(invalidProductId)).thenReturn(false);

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/{id}", invalidProductId));

        // Assert
        result.andExpect(status().isNotFound());
        Mockito.verify(productRepositoryMock).existsById(invalidProductId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findById(any());
    }

    // ==========================================
    // MODULE: THÊM MỚI SẢN PHẨM (CREATE/POST)
    // ==========================================

    /**
     * Test Case ID: TC_PRO_06
     * Mô tả: Thêm mới sản phẩm thành công khi thông tin hợp lệ và ID chưa tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_06 - ID chưa tồn tại → lưu thành công, trả về 200 và sản phẩm")
    void post_testChuan1() throws Exception {
        // Arrange
        Product newProduct = new Product();
        newProduct.setProductId(10L);
        newProduct.setName("Táo Mĩ");
        Mockito.when(productRepositoryMock.existsById(10L)).thenReturn(false);
        Mockito.when(productRepositoryMock.save(any(Product.class))).thenReturn(newProduct);

        // Act
        ResultActions result = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newProduct)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.name").value("Táo Mĩ"));
        Mockito.verify(productRepositoryMock).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_PRO_07
     * Mô tả: Thêm mới sản phẩm thất bại (Bad Request) nếu ID đã tồn tại trong DB.
     */
    @Test
    @DisplayName("TC_PRO_07 - ID đã tồn tại trong DB → trả về 400, không gọi save")
    void post_testNgoaiLe1() throws Exception {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setProductId(10L);
        Mockito.when(productRepositoryMock.existsById(10L)).thenReturn(true);

        // Act
        ResultActions result = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(existingProduct)));

        // Assert
        result.andExpect(status().isBadRequest());
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_PRO_08
     * Mô tả: Thêm sản phẩm khi không truyền ID (productId = null).
     */
    @Test
    @DisplayName("TC_PRO_08 - Không truyền ID (null) → existsById(null)=false, lưu thành công")
    void post_testNgoaiLe2() throws Exception {
        // Arrange
        Product productWithoutId = new Product();
        productWithoutId.setName("Cà chua bi");
        Mockito.when(productRepositoryMock.existsById(null)).thenReturn(false);
        Mockito.when(productRepositoryMock.save(any(Product.class))).thenReturn(productWithoutId);

        // Act
        ResultActions result = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(productWithoutId)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cà chua bi"));
        Mockito.verify(productRepositoryMock).save(any(Product.class));
    }

    // ==========================================
    // MODULE: CẬP NHẬT SẢN PHẨM (UPDATE/PUT)
    // ==========================================

    /**
     * Test Case ID: TC_PRO_09
     * Mô tả: Cập nhật sản phẩm thành công khi thông tin hợp lệ, ID khớp và đã có trong DB.
     */
    @Test
    @DisplayName("TC_PRO_09 - ID hợp lệ, khớp body, tồn tại DB → trả về 200 và sản phẩm đã cập nhật")
    void put_testChuan1() throws Exception {
        // Arrange
        Long targetId = 5L;
        Product updatePayload = new Product();
        updatePayload.setProductId(targetId);
        updatePayload.setName("Chuối Nam Mỹ Cập Nhật");
        Mockito.when(productRepositoryMock.existsById(targetId)).thenReturn(true);
        Mockito.when(productRepositoryMock.save(any(Product.class))).thenReturn(updatePayload);

        // Act
        ResultActions result = mockMvc.perform(put("/api/products/{id}", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updatePayload)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(5))
                .andExpect(jsonPath("$.name").value("Chuối Nam Mỹ Cập Nhật"));
        Mockito.verify(productRepositoryMock).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_PRO_10
     * Mô tả: Cập nhật thất bại khi ID trên URL Path KHÔNG khớp với ID của body JSON.
     */
    @Test
    @DisplayName("TC_PRO_10 - ID path không khớp body → trả về 400, không gọi DB")
    void put_testNgoaiLe1() throws Exception {
        // Arrange
        Long pathId = 5L;
        Product updatePayload = new Product();
        updatePayload.setProductId(99L); // ID không khớp với path

        // Act
        ResultActions result = mockMvc.perform(put("/api/products/{id}", pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updatePayload)));

        // Assert
        result.andExpect(status().isBadRequest());
        Mockito.verify(productRepositoryMock, Mockito.never()).existsById(any());
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    /**
     * Test Case ID: TC_PRO_11
     * Mô tả: Cập nhật thất bại khi ID tồn tại trên path nhưng không tồn tại trong database.
     */
    @Test
    @DisplayName("TC_PRO_11 - ID khớp body nhưng không tồn tại trong DB → trả về 404")
    void put_testNgoaiLe2() throws Exception {
        // Arrange
        Long targetId = 5L;
        Product updatePayload = new Product();
        updatePayload.setProductId(targetId);
        Mockito.when(productRepositoryMock.existsById(targetId)).thenReturn(false);

        // Act
        ResultActions result = mockMvc.perform(put("/api/products/{id}", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updatePayload)));

        // Assert
        result.andExpect(status().isNotFound());
        Mockito.verify(productRepositoryMock).existsById(targetId);
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    // ==========================================
    // MODULE: XÓA SẢN PHẨM (SOFT DELETE)
    // ==========================================

    /**
     * Test Case ID: TC_PRO_12
     * Mô tả: Xóa mềm sản phẩm (cập nhật status = false) thành công.
     */
    @Test
    @DisplayName("TC_PRO_12 - ID tồn tại → set status=false, gọi save, trả về 200")
    void delete_testChuan1() throws Exception {
        // Arrange
        Long deleteId = 7L;
        Product productToDelete = new Product();
        productToDelete.setProductId(deleteId);
        productToDelete.setStatus(true);
        Mockito.when(productRepositoryMock.existsById(deleteId)).thenReturn(true);
        Mockito.when(productRepositoryMock.findById(deleteId)).thenReturn(Optional.of(productToDelete));

        // Act
        ResultActions result = mockMvc.perform(delete("/api/products/{id}", deleteId));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(productToDelete.getStatus(), "Sau khi xóa mềm, status phải là false");
        Mockito.verify(productRepositoryMock).save(productToDelete);
    }

    /**
     * Test Case ID: TC_PRO_13
     * Mô tả: Xóa sản phẩm thất bại khi ID không tồn tại.
     */
    @Test
    @DisplayName("TC_PRO_13 - ID không tồn tại → trả về 404, không gọi findById hay save")
    void delete_testNgoaiLe1() throws Exception {
        // Arrange
        Long invalidDeleteId = 999L;
        Mockito.when(productRepositoryMock.existsById(invalidDeleteId)).thenReturn(false);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/products/{id}", invalidDeleteId));

        // Assert
        result.andExpect(status().isNotFound());
        Mockito.verify(productRepositoryMock).existsById(invalidDeleteId);
        Mockito.verify(productRepositoryMock, Mockito.never()).findById(any());
        Mockito.verify(productRepositoryMock, Mockito.never()).save(any(Product.class));
    }

    // ==========================================
    // MODULE: IMPORT DỮ LIỆU SẢN PHẨM
    // ==========================================

    /**
     * Test Case ID: TC_PRO_14
     * Mô tả: Import danh sách sản phẩm thành công thông qua hàm saveAll.
     */
    @Test
    @DisplayName("TC_PRO_14 - Import danh sách hợp lệ → trả về 200 và toàn bộ danh sách")
    void importCsv_testChuan1() throws Exception {
        // Arrange
        Product p1 = new Product();
        p1.setProductId(1L);
        p1.setName("Sản phẩm 1");

        Product p2 = new Product();
        p2.setProductId(2L);
        p2.setName("Sản phẩm 2");

        List<Product> importList = Arrays.asList(p1, p2);
        Mockito.when(productRepositoryMock.saveAll(any())).thenReturn(importList);

        // Act
        ResultActions result = mockMvc.perform(post("/api/products/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(importList)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sản phẩm 1"))
                .andExpect(jsonPath("$[1].name").value("Sản phẩm 2"));
        Mockito.verify(productRepositoryMock).saveAll(any());
    }

    // ==========================================
    // MODULE: CÁC ENDPOINT LẤY DANH SÁCH (GET)
    // ==========================================

    /**
     * Test Case ID: TC_PRO_15
     * Mô tả: Lấy tất cả sản phẩm đang bán - có trạng thái kích hoạt (status = true).
     */
    @Test
    @DisplayName("TC_PRO_15 - Có sản phẩm active → trả về 200 và danh sách đúng tên")
    void getAll_testChuan1() throws Exception {
        // Arrange
        Product activeProduct = new Product();
        activeProduct.setName("Sản phẩm đang bán");
        Mockito.when(productRepositoryMock.findByStatusTrue()).thenReturn(Arrays.asList(activeProduct));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Sản phẩm đang bán"));
        Mockito.verify(productRepositoryMock).findByStatusTrue();
    }

    /**
     * Test Case ID: TC_PRO_16
     * Mô tả: Không có sản phẩm nào active → trả về mảng rỗng, không phải 404.
     */
    @Test
    @DisplayName("TC_PRO_16 - Không có sản phẩm active → trả về 200 và mảng rỗng")
    void getAll_testNgoaiLe1() throws Exception {
        // Arrange
        Mockito.when(productRepositoryMock.findByStatusTrue()).thenReturn(List.of());

        // Act
        ResultActions result = mockMvc.perform(get("/api/products"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        Mockito.verify(productRepositoryMock).findByStatusTrue();
    }

    /**
     * Test Case ID: TC_PRO_17
     * Mô tả: Lấy danh sách sản phẩm bán chạy nhất cho User (status = true, sort by sold desc).
     */
    @Test
    @DisplayName("TC_PRO_17 - getBestSeller → trả về 200, đúng số lượng, đúng thứ tự sold desc")
    void getBestSeller_testChuan1() throws Exception {
        // Arrange
        // Mock trả về đã sắp xếp sẵn (giả định Repository đã làm đúng)
        // Controller chỉ có trách nhiệm gọi đúng method và trả nguyên kết quả ra
        Product first = new Product();
        first.setName("Dưa hấu");
        first.setSold(500);

        Product second = new Product();
        second.setName("Rau muống");
        second.setSold(200);

        Mockito.when(productRepositoryMock.findByStatusTrueOrderBySoldDesc())
                .thenReturn(Arrays.asList(first, second));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/bestseller"));

        // Assert — verify Controller gọi đúng method VÀ trả nguyên thứ tự ra response
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dưa hấu"))    // sold cao hơn → đứng trước
                .andExpect(jsonPath("$[0].sold").value(500))
                .andExpect(jsonPath("$[1].name").value("Rau muống"))  // sold thấp hơn → đứng sau
                .andExpect(jsonPath("$[1].sold").value(200));
        Mockito.verify(productRepositoryMock).findByStatusTrueOrderBySoldDesc();
    }

    /**
     * Test Case ID: TC_PRO_18
     * Mô tả: Lấy top 10 sản phẩm bán chạy nhất cho Admin (bỏ qua status).
     */
    @Test
    @DisplayName("TC_PRO_18 - getBestSellerAdmin → trả về 200 và top 10 theo sold")
    void getBestSellerAdmin_testChuan1() throws Exception {
        // Arrange
        Product adminBestSeller = new Product();
        adminBestSeller.setName("Bơ Đắk Lắk");
        adminBestSeller.setSold(1000);
        Mockito.when(productRepositoryMock.findTop10ByOrderBySoldDesc())
                .thenReturn(Arrays.asList(adminBestSeller));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/bestseller-admin"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bơ Đắk Lắk"))
                .andExpect(jsonPath("$[0].sold").value(1000));
        Mockito.verify(productRepositoryMock).findTop10ByOrderBySoldDesc();
    }

    /**
     * Test Case ID: TC_PRO_19
     * Mô tả: Lấy danh sách sản phẩm mới nhất (sắp xếp theo EnteredDate).
     */
    @Test
    @DisplayName("TC_PRO_19 - getLasted → trả về 200 và danh sách sắp xếp theo enteredDate giảm dần")
    void getLasted_testChuan1() throws Exception {
        // Arrange
        Product newerProduct = new Product();
        newerProduct.setName("Măng tây tươi");
        newerProduct.setEnteredDate(LocalDate.of(2024, 6, 10));

        Product olderProduct = new Product();
        olderProduct.setName("Cà rốt baby");
        olderProduct.setEnteredDate(LocalDate.of(2024, 3, 1));

        // Repository đã sắp xếp DESC → sản phẩm mới hơn đứng trước
        Mockito.when(productRepositoryMock.findByStatusTrueOrderByEnteredDateDesc())
                .thenReturn(Arrays.asList(newerProduct, olderProduct));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/latest"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Măng tây tươi"))
                .andExpect(jsonPath("$[0].enteredDate").value("2024-06-10"))
                .andExpect(jsonPath("$[1].name").value("Cà rốt baby"))
                .andExpect(jsonPath("$[1].enteredDate").value("2024-03-01"));
        Mockito.verify(productRepositoryMock).findByStatusTrueOrderByEnteredDateDesc();
    }

    /**
     * Test Case ID: TC_PRO_20
     * Mô tả: Lấy danh sách sản phẩm được đánh giá cao.
     */
    @Test
    @DisplayName("TC_PRO_20 - getRated → trả về 200 và danh sách sản phẩm rated")
    void getRated_testChuan1() throws Exception {
        // Arrange
        Product ratedProduct = new Product();
        ratedProduct.setName("Cam Vinh");
        Mockito.when(productRepositoryMock.findProductRated())
                .thenReturn(Arrays.asList(ratedProduct));

        // Act
        ResultActions result = mockMvc.perform(get("/api/products/rated"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cam Vinh"));
        Mockito.verify(productRepositoryMock).findProductRated();
    }

    /**
     * Test Case ID: TC_PRO_21
     * Mô tả: Lấy danh sách sản phẩm gợi ý dựa trên categoryId và productId.
     */
    @Test
    @DisplayName("TC_PRO_21 - suggest với ID hợp lệ → trả về 200 và danh sách gợi ý")
    void suggest_testChuan1() throws Exception {
        // Arrange
        Long categoryId = 3L;
        Long productId = 10L;
        Product suggestedProduct = new Product();
        suggestedProduct.setName("Gợi ý 1");
        Mockito.when(productRepositoryMock.findProductSuggest(categoryId, productId, categoryId, categoryId))
                .thenReturn(Arrays.asList(suggestedProduct));

        // Act
        ResultActions result = mockMvc.perform(
                get("/api/products/suggest/{categoryId}/{productId}", categoryId, productId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gợi ý 1"));
        Mockito.verify(productRepositoryMock)
                .findProductSuggest(categoryId, productId, categoryId, categoryId);
    }

}