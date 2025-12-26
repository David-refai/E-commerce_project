package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Category;
import org.example.ecommerce_project.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepoTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepo productRepo;

    private Category electronics;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Create test categories
        electronics = new Category();
        electronics.setName("Electronics");
        entityManager.persist(electronics);

        Category clothing = new Category();
        clothing.setName("Clothing");
        entityManager.persist(clothing);

        // Create test products
        product1 = new Product("SKU123", "Laptop", "High performance laptop", new BigDecimal("999.99"), true);
        product1.addCategory(electronics);
        product1 = entityManager.persist(product1);

        product2 = new Product("SKU456", "T-Shirt", "Cotton t-shirt", new BigDecimal("19.99"), true);
        product2.addCategory(clothing);
        product2 = entityManager.persist(product2);

        // Create an inactive product
        Product inactiveProduct = new Product("SKU789", "Old Phone", "Old model phone", new BigDecimal("49.99"), false);
        inactiveProduct.addCategory(electronics);
        entityManager.persist(inactiveProduct);

        entityManager.flush();
    }

    @Test
    void findBySku_ShouldReturnProduct_WhenSkuExists() {
        // When
        Optional<Product> found = productRepo.findBySku("SKU123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSku()).isEqualTo("SKU123");
        assertThat(found.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void findBySku_ShouldReturnEmpty_WhenSkuDoesNotExist() {
        // When
        Optional<Product> found = productRepo.findBySku("NON_EXISTENT_SKU");

        // Then
        assertThat(found).isNotPresent();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // When
        List<Product> products = productRepo.findByNameContainingIgnoreCase("t");

        // Then
        assertThat(products).hasSize(3);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "laptop" ,"T-Shirt");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        // When
        List<Product> products = productRepo.findByNameContainingIgnoreCase("lApToP");

        // Then
        assertThat(products).hasSize(2);
        assertThat(products.getFirst().getName()).isEqualTo("laptop");
    }

    @Test
    void findByActiveTrue_ShouldReturnOnlyActiveProducts() {
        // When
        List<Product> activeProducts = productRepo.findByActiveTrue();

        // Then
        assertThat(activeProducts).hasSize(3);
        assertThat(activeProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "laptop" ,"T-Shirt");
    }

    @Test
    void findByCategoryNameIgnoreCase_ShouldReturnProductsInCategory() {
        // When
        List<Product> electronicsProducts = productRepo.findByCategoryNameIgnoreCase("electronics");

        // Then
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Old Phone");
    }

    @Test
    void findByCategoryNameIgnoreCase_ShouldBeCaseInsensitive() {
        // When
        List<Product> electronicsProducts = productRepo.findByCategoryNameIgnoreCase("ELECTRONICS");

        // Then
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Old Phone");
    }

    @Test
    void save_ShouldPersistProduct() {
        // Given
        Product newProduct = new Product("SKU999", "New Product", "Brand new product", new BigDecimal("99.99"), true);
        newProduct.addCategory(electronics);

        // When
        Product savedProduct = productRepo.save(newProduct);

        // Then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(productRepo.findById(savedProduct.getId())).isPresent();
    }

    @Test
    void delete_ShouldRemoveProduct() {
        // When
        productRepo.delete(product1);
        entityManager.flush();

        // Then
        assertThat(productRepo.findById(product1.getId())).isNotPresent();
    }
}
