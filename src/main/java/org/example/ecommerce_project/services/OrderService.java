package org.example.ecommerce_project.services;

import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.OrderItem;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.entity.enums.PaymentStatus;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepo orderRepository;
    private final CustomerRepo customerRepository;
    private final ProductRepo productRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepo orderRepository,
                        CustomerRepo customerRepository,
                        ProductRepo productRepository,
                        InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
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
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> AppException.notFound("Order not found: " + id));
    }


    /**
     * Creates a new order for a customer with the given items.
     * Business rules:
     * - Customer must exist
     * - Order must contain at least one item
     * - Product must exist and be active
     * - Inventory is reserved for each item
     * - Order status is set to NEW
     * - Each OrderItem calculates its line total automatically
     * - Order total is calculated automatically from OrderItems
     */


    @Transactional
    public Order createOrder(Long customerId, List<OrderItemRequest> items) {
        if (customerId == null || customerId <= 0) {
            throw AppException.validation("customerId must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw AppException.validation("items cannot be empty");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> AppException.notFound("Customer not found with id: " + customerId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);

        for (OrderItemRequest req : items) {
            if (req.quantity() <= 0) {
                throw AppException.validation("quantity must be positive");
            }

            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> AppException.notFound("Product not found with id: " + req.productId()));

            if (!product.isActive()) {
                throw AppException.businessRule("Product is not active: " + product.getSku());
            }

            // ✅ reserve inventory using request productId (stable in unit tests)
            inventoryService.reserveStock(req.productId(), req.quantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQty(req.quantity());
            item.setUnitPrice(product.getPrice());

            item.setLineTotal(product.getPrice()
                    .multiply(java.math.BigDecimal.valueOf(req.quantity()))
                    .setScale(2, java.math.RoundingMode.HALF_UP));

            order.addItem(item);
        }

        order.recalcTotal();
        return orderRepository.save(order);
    }



    private OrderItem buildOrderItem(
            OrderItemRequest req,
            Product product,
            Order order) {

        if (!product.isActive()) {
            throw AppException.businessRule("Product is not active: " + product.getSku());

        }
        // Reserve inventory (throws if not enough)
        inventoryService.reserveStock(product.getId(), req.quantity());

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQty(req.quantity());
        item.setUnitPrice(product.getPrice());

        return item;
    }


    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            throw AppException.businessRule("Order is already PAID");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.PENDING) {
            order.setPayment(null);
        }

        // Restore inventory
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQty());
        }


        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

}
