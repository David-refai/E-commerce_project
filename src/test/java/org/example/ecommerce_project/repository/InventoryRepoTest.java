package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryRepoTest {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private InventoryRepo inventoryRepo;

    @Test
    void inventoryRepo_createProductAndInventory_findByProductId_updateStock_persists() {

        // 1) Create Product
        Product product = new Product();
        product.setSku("SKU-INV-1");
        product.setName("Inventory Test Product");
        product.setDescription("Test");
        product.setPrice(new BigDecimal("99.90"));
        product.setActive(true);

        product = productRepo.save(product);

        // 2) Create Inventory (MapsId -> product_id = product.id)
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setInStock(10);

        inventoryRepo.save(inventory);

        // 3) Fetch Inventory via productId (PK)
        Inventory found = inventoryRepo.findById(product.getId()).orElseThrow();

        assertEquals(product.getId(), found.getProductId());
        assertEquals(10, found.getInStock());

        // 4) Update stock and verify persistence
        found.setInStock(6);
        inventoryRepo.save(found);

        Inventory updated = inventoryRepo.findById(product.getId()).orElseThrow();
        assertEquals(6, updated.getInStock());
    }

    @Test
    void inventoryRepo_findLowStock_lessThanThreshold() {

        Product p1 = new Product();
        p1.setSku("SKU-LS-1");
        p1.setName("Low stock product");
        p1.setDescription("Test");
        p1.setPrice(new BigDecimal("50.00"));
        p1.setActive(true);
        p1 = productRepo.save(p1);

        Inventory i1 = new Inventory();
        i1.setProduct(p1);
        i1.setInStock(3);
        inventoryRepo.save(i1);

        // when
        var lowStock = inventoryRepo.findByInStockLessThan(5);

        // then
        assertEquals(1, lowStock.size());
        assertEquals(3, lowStock.getFirst().getInStock());
    }
}
