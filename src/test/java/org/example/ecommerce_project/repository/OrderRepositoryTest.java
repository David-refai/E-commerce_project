package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.OrderItem;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired private CustomerRepo customerRepo;
    @Autowired private ProductRepo productRepo;
    @Autowired private OrderRepo orderRepo;

    @Test
    void orderRepo_saveOrder_persistsItems_andTotals_andRelations() {

        // --- Seed minimal data ---
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("orderrepo@test.com");
        customer = customerRepo.save(customer);

        Product p1 = new Product();
        p1.setSku("SKU-OR-1");
        p1.setName("Product 1");
        p1.setDescription("Test");
        p1.setPrice(new BigDecimal("10.00"));
        p1.setActive(true);
        p1 = productRepo.save(p1);

        Product p2 = new Product();
        p2.setSku("SKU-OR-2");
        p2.setName("Product 2");
        p2.setDescription("Test");
        p2.setPrice(new BigDecimal("20.00"));
        p2.setActive(true);
        p2 = productRepo.save(p2);

        // --- Create Order + 2 items ---
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);

        OrderItem i1 = new OrderItem();
        i1.setProduct(p1);
        i1.setQty(2);
        i1.setUnitPrice(p1.getPrice());

        OrderItem i2 = new OrderItem();
        i2.setProduct(p2);
        i2.setQty(1);
        i2.setUnitPrice(p2.getPrice());

        order.addItem(i1);
        order.addItem(i2);

        // --- First save persists order + items ---
        Order saved = orderRepo.saveAndFlush(order);
        assertNotNull(saved.getId());

        // --- Reload (ensures we read what is actually in DB) ---
        Order reloaded = orderRepo.findById(saved.getId()).orElseThrow();

        assertEquals(customer.getId(), reloaded.getCustomer().getId());
        assertEquals(OrderStatus.NEW, reloaded.getStatus());
        assertEquals(2, reloaded.getItems().size(), "Order should have 2 items");

        // Verify item relations + line totals
        for (OrderItem it : reloaded.getItems()) {
            assertNotNull(it.getId(), "OrderItem must be persisted (id assigned)");
            assertNotNull(it.getOrder());
            assertEquals(reloaded.getId(), it.getOrder().getId());
            assertNotNull(it.getProduct());

            BigDecimal expectedLine = it.getUnitPrice()
                    .multiply(BigDecimal.valueOf(it.getQty()))
                    .setScale(2, RoundingMode.HALF_UP);

            assertNotNull(it.getLineTotal(), "OrderItem.lineTotal must not be null");
            assertEquals(0, expectedLine.compareTo(it.getLineTotal()),
                    "OrderItem.lineTotal should equal unitPrice * qty");
        }

        // --- Now force a total update AFTER items exist ---
        // This makes @PreUpdate run and writes correct total to DB
        reloaded.recalcTotal();
        orderRepo.saveAndFlush(reloaded);

        Order reloaded2 = orderRepo.findById(saved.getId()).orElseThrow();

        BigDecimal expectedTotal = reloaded2.getItems().stream()
                .map(it -> it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        assertNotNull(reloaded2.getTotal());
        assertEquals(0, expectedTotal.compareTo(reloaded2.getTotal()),
                "Order.total should equal sum(unitPrice*qty) from persisted items");
    }
}
