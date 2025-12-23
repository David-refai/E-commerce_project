package org.example.ecommerce_project.services;

import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.OrderItem;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.example.ecommerce_project.repository.OrderItemRepo;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepo orderRepository;
    private final OrderItemRepo orderItemRepository;
    private final CustomerRepo customerRepository;
    private final ProductRepo productRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepo orderRepository,
                        OrderItemRepo orderItemRepository,
                        CustomerRepo customerRepository,
                        ProductRepo productRepository,
                        InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> listByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }

    /**
     * Creates a new order for a customer with the given items.
     * Business rules:
     *  - Customer must exist
     *  - Order must contain at least one item
     *  - Product must exist and be active
     *  - Inventory is reserved for each item
     *  - Order status is set to NEW
     *  - Each OrderItem calculates its line total automatically
     *  - Order total is calculated automatically from OrderItems
     */

    @Transactional
    public Order createOrder(Long customerId, List<OrderItemRequest> items) {

        if (items == null || items.isEmpty()) {
//            TODO: Temporary implementation. 👇
            throw new IllegalArgumentException("items cannot be empty");
        }
//       Customer must exist
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
//                        TODO: Temporary implementation. 👇
                        new IllegalArgumentException("Customer not found with id: " + customerId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        for (OrderItemRequest req : items) {
//           Product must exist and be active
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() ->
                            //TODO: Temporary implementation. 👇
                            new IllegalArgumentException("Product not found with id: " + req.productId()));

            OrderItem item = buildOrderItem(req, product, savedOrder);
            orderItemRepository.save(item);
        }

        // Order.total will be calculated automatically
        return orderRepository.save(savedOrder);
    }

    private OrderItem buildOrderItem(
            OrderItemRequest req,
            Product product,
            Order order) {

        if (!product.isActive()) {
//            TODO: Temporary implementation. 👇
            throw new IllegalStateException("Product is not active: " + product.getSku());
        }
        // Reserve inventory (throws if not enough)

        inventoryService.reserveStock(product.getId(), req.quantity());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQty(req.quantity());
        item.setUnitPrice(product.getPrice());

        return item;
    }


    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a PAID order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        // Restore inventory
        for (OrderItem item : order.getItems()) {
            inventoryService.reserveStock(item.getProduct().getId(), item.getQty());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public record OrderItemRequest(Long productId, int quantity) {}
}
