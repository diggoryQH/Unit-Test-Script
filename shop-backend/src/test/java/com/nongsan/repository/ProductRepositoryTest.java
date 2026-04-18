package com.nongsan.repository;

import com.nongsan.entity.Category;
import com.nongsan.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test") // Đọc cấu hình từ application-test.properties
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //  ROLLBACK:
    // Mọi dữ liệu insert trong class này đều sẽ tự động ROLLBACK sau khi test kết thúc.

    private Category catFruit;
    private Category catVeggie;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUpData() {
        // 1. Dựng dữ liệu danh mục
        catFruit = new Category();
        catFruit.setCategoryName("Trái cây");
        catFruit = categoryRepository.save(catFruit);

        catVeggie = new Category();
        catVeggie.setCategoryName("Rau củ");
        catVeggie = categoryRepository.save(catVeggie);

        // 2. Dựng dữ liệu sản phẩm
        product1 = new Product();
        product1.setName("Táo");
        product1.setCategory(catFruit);
        product1 = productRepository.save(product1);

        product2 = new Product();
        product2.setName("Cam");
        product2.setCategory(catFruit);
        product2 = productRepository.save(product2);

        product3 = new Product();
        product3.setName("Cà rốt");
        product3.setCategory(catVeggie);
        product3 = productRepository.save(product3);

        // 3. Dựng dữ liệu bảng Rates bằng SQL thuần
        // Chú ý: Tên bảng (rates) và tên cột (product_id, rating) phải khớp với Entity của bạn
        jdbcTemplate.execute("INSERT INTO rates (product_id, rating) VALUES (" + product1.getProductId() + ", 5)"); // Táo 5 sao
        jdbcTemplate.execute("INSERT INTO rates (product_id, rating) VALUES (" + product2.getProductId() + ", 3)"); // Cam 3 sao
    }

    /**
     * Test Case ID: TC_01_REPO
     * Mô tả: Lấy danh sách sản phẩm được đánh giá sắp xếp theo rating giảm dần.
     */
    @Test
    void test_FindProductRated_ReturnsProductsOrderedByRating() {
        List<Product> ratedProducts = productRepository.findProductRated();

        Assertions.assertNotNull(ratedProducts, "Danh sách không được null");
        Assertions.assertTrue(ratedProducts.size() >= 2, "Phải có ít nhất 2 sản phẩm");

        // Táo (5 sao) phải xếp trên Cam (3 sao)
        Assertions.assertEquals("Táo", ratedProducts.get(0).getName(), "Sản phẩm đánh giá cao hơn phải xếp trên");
    }

    /**
     * Test Case ID: TC_02_REPO
     * Mô tả: Lấy danh sách gợi ý ưu tiên cùng danh mục (loại trừ chính nó), sau đó đến khác danh mục.
     */
    @Test
    void test_FindProductSuggest_ReturnsProperlyOrderedList() {
        Long suggestCategoryId = catFruit.getCategoryId();
        Long excludeProductId = product1.getProductId(); // Loại Táo ra khỏi danh sách

        List<Product> suggestedProducts = productRepository.findProductSuggest(
                suggestCategoryId,
                excludeProductId,
                suggestCategoryId,
                suggestCategoryId
        );

        Assertions.assertNotNull(suggestedProducts);

        // Đảm bảo không chứa Táo (Sản phẩm đang xem)
        boolean containsExcluded = suggestedProducts.stream()
                .anyMatch(p -> p.getProductId().equals(excludeProductId));
        Assertions.assertFalse(containsExcluded, "Không được chứa sản phẩm đang xem (Táo)");

        // Cam (Cùng loại Trái cây) phải được xếp trước Cà rốt (Khác loại Rau củ)
        if (suggestedProducts.size() >= 2) {
            Product firstSuggest = suggestedProducts.get(0);
            Assertions.assertEquals(catFruit.getCategoryId(), firstSuggest.getCategory().getCategoryId(),
                    "Sản phẩm cùng danh mục phải được ưu tiên số 1");
        }
    }
}