package org.example.ecommerce_project.e2e;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.example.ecommerce_project.repository.InventoryRepo;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.example.ecommerce_project.services.CartService;
import org.example.ecommerce_project.services.CheckoutService;
import org.example.ecommerce_project.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CheckoutEndToEndTest {


    @Autowired
    private CustomerRepo customerRepository;
    @Autowired private ProductRepo productRepo;
    @Autowired private InventoryRepo inventoryRepo;
    @Autowired private OrderRepo orderRepo;

    @Autowired private CartService cartService;
    @Autowired private CheckoutService checkoutService;

    @MockitoBean private PaymentService paymentService;


    @Test
    void e2e_createCustomer_addToCart_checkout_payment_updatesStatus_and_decreasesStock() {
        // -------- Seed minimal data --------
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer = customerRepository.save(customer);

        Product product = new Product();
        product.setSku("SKU-E2E-1");
        product.setName("E2E Product");
        product.setDescription("E2E test product");
        product.setPrice(new BigDecimal("100.00"));
        product.setActive(true);
        product = productRepo.save(product);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setInStock(10);
        inventoryRepo.save(inv);

        // -------- Prepare cart --------
        Cart cart = cartService.getCart(customer.getId());
        cart.add(product.getId(), 2);

        // -------- Make payment deterministic in test --------

        doAnswer(invocation -> {
            Long orderId = invocation.getArgument(0, Long.class);

            Order o = orderRepo.findById(orderId).orElseThrow();
            o.setStatus(OrderStatus.PAID);
            orderRepo.save(o);
            return null;
        }).when(paymentService).processPayment(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(PaymentMethod.class));

        // -------- Act: checkout --------
        Order order = checkoutService.checkout(customer.getId(), PaymentMethod.CARD);

        // -------- Assert: order status updated --------
        Order reloaded = orderRepo.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, reloaded.getStatus(), "Order must be PAID after successful payment");

        // -------- Assert: inventory decreased --------
        Inventory invAfter = inventoryRepo.findById(product.getId()).orElseThrow();
        assertEquals(8, invAfter.getInStock(), "Stock must decrease by qty (2)");

        //  cart cleared
        assertTrue(cartService.getCart(customer.getId()).isEmpty(), "Cart should be empty after checkout");
    }
}
