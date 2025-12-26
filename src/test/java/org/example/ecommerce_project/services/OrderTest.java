package org.example.ecommerce_project.services;

import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    @Mock private OrderRepo orderRepo;
    @Mock private ProductRepo productRepo;
    @Mock private CustomerRepo customerRepo;
    @Mock private InventoryService inventoryService;

    @InjectMocks private OrderService orderService;

    @Captor private ArgumentCaptor<Order> orderCaptor;

    @Test
    void placeOrder_happyPath_createsOrder_reservesStock_calculatesTotal() {
        // given
        Long customerId = 1L;

        Customer customer = new Customer();
        // لو لازم: customer.setEmail(...); customer.setName(...);

        Product p1 = new Product();
        p1.setActive(true);
        p1.setPrice(new BigDecimal("10.00"));
        p1.setSku("SKU-1");

        Product p2 = new Product();
        p2.setActive(true);
        p2.setPrice(new BigDecimal("20.00"));
        p2.setSku("SKU-2");

        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepo.findById(10L)).thenReturn(Optional.of(p1));
        when(productRepo.findById(20L)).thenReturn(Optional.of(p2));

        // IMPORTANT: return the same order object that was passed in
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(10L, 2),
                new OrderItemRequest(20L, 1)
        );

        // when
        Order result = orderService.createOrder(customerId, items);

        // then (basic)
        assertNotNull(result);
        assertEquals(OrderStatus.NEW, result.getStatus(), "status should be NEW");

        // capture the LAST saved order (should be after items added + total calculated)
        verify(orderRepo, atLeastOnce()).save(orderCaptor.capture());
        Order lastSaved = orderCaptor.getValue();

        assertNotNull(lastSaved.getItems(), "items list must not be null");
        assertEquals(2, lastSaved.getItems().size(), "order must contain 2 items before final save");

        assertNotNull(lastSaved.getTotal(), "total must not be null before final save");

        // total expected 40.00
        assertEquals(0, new BigDecimal("40.00").compareTo(lastSaved.getTotal()),
                "Order total should be 40.00");

        // also ensure returned result has same computed total
        assertEquals(0, new BigDecimal("40.00").compareTo(result.getTotal()),
                "Returned order should also have total 40.00");

        // verify inventory reservation calls (happy path)
        verify(inventoryService).reserveStock(10L, 2);
        verify(inventoryService).reserveStock(20L, 1);
    }
}
