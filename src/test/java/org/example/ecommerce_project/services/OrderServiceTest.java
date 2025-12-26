package org.example.ecommerce_project.services;

import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.*;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepository;

    @Mock
    private CustomerRepo customerRepository;

    @Mock
    private ProductRepo productRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setActive(true);

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(OrderStatus.NEW);

        // Setup test order item
        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQty(2);
        testOrderItem.setUnitPrice(BigDecimal.valueOf(100.00));
        testOrderItem.setOrder(testOrder);

        testOrder.getItems().add(testOrderItem);
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order createdOrder = orderService.createOrder(1L, List.of(itemRequest));

        // Assert
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getCustomer().getId()).isEqualTo(1L);
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        verify(inventoryService, times(1)).reserveStock(1L, 2);
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void createOrder_WithNonExistingCustomer_ShouldThrowException() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 1);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(999L, List.of(itemRequest)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void createOrder_WithEmptyItems_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(1L, List.of()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("items cannot be empty");
    }

    @Test
    void getOrder_WithExistingId_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Order foundOrder = orderService.getOrder(1L);

        // Assert
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(1L);
    }

    @Test
    void getOrder_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Order not found: 999");

        verify(orderRepository).findByIdWithDetails(999L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void cancelOrder_WithNewOrder_ShouldCancelOrder() {
        // Arrange
        testOrder.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(inventoryService, times(1)).releaseStock(1L, 2);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelOrder_WithPaidOrder_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Order is already PAID");
    }

    @Test
    void listAll_ShouldReturnAllOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(List.of(testOrder));

        // Act
        List<Order> orders = orderService.listAll();

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void listByStatus_ShouldReturnFilteredOrders() {
        // Arrange
        when(orderRepository.findByStatus(OrderStatus.NEW)).thenReturn(List.of(testOrder));

        // Act
        List<Order> orders = orderService.listByStatus(OrderStatus.NEW);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.NEW);
    }
}
