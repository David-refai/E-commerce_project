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

    /**
     * Hämtar alla ordrar i systemet
     * @return lista av ordrar
     */
    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    /**
     * Hämtar ordrar baserat på status
     * @param status orderstatus
     */
    @Transactional(readOnly = true)
    public List<Order> listByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Hämtar en order inklusive dess detaljer
     * @param id orderns ID
     */
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> AppException.notFound("Order not found: " + id));
    }

    /**
     * Skapar en ny order för en kund med angivna orderrader
     * - Validerar kund och produkter
     * - Reserverar lager för varje orderrad
     * - Sätter status till NEW
     * - Beräknar ordertotal automatiskt
     * @param customerId kundens ID
     * @param items beställda artiklar
     * @return skapad order
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

            // Reserverar lager för vald produkt
            inventoryService.reserveStock(req.productId(), req.quantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQty(req.quantity());
            item.setUnitPrice(product.getPrice());

            // Beräknar totalsumma för raden
            item.setLineTotal(product.getPrice()
                    .multiply(java.math.BigDecimal.valueOf(req.quantity()))
                    .setScale(2, java.math.RoundingMode.HALF_UP));

            order.addItem(item);
        }

        // Beräknar totalsumma för ordern
        order.recalcTotal();
        return orderRepository.save(order);
    }

    /**
     * Avbryter en order om den inte är betald
     * Återställer lager för varje artikel och hanterar betalningskoppling
     * @param orderId orderns ID
     */
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found with id: " + orderId));

        // Kan inte avbryta redan betald order
        if (order.getStatus() == OrderStatus.PAID) {
            throw AppException.businessRule("Order is already PAID");
        }

        // Om redan avbruten så gör ingenting
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        // Om betalning väntar → ta bort kopplingen
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.PENDING) {
            order.setPayment(null);
        }

        // Återställer reserverat lager
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQty());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // buildOrderItem används ej längre i createOrder, men lämnas kvar ev. för framtida refaktorering
    private OrderItem buildOrderItem(OrderItemRequest req, Product product, Order order) {
        if (!product.isActive()) {
            throw AppException.businessRule("Product is not active: " + product.getSku());
        }
        inventoryService.reserveStock(product.getId(), req.quantity());

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQty(req.quantity());
        item.setUnitPrice(product.getPrice());

        return item;
    }

}
