package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.OrderItem;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.exception.ErrorHandlerCli;
import org.example.ecommerce_project.services.OrderService;
import org.example.ecommerce_project.services.PaymentService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class OrderCli {

    private final OrderService orderService;
    // Centralized error handling (set true for debug)
    private final ErrorHandlerCli errorHandler = new ErrorHandlerCli(false);

    public OrderCli(OrderService orderService) {
        this.orderService = orderService;
    }

    public void showMenu(Scanner scanner) {
        System.out.println();
        System.out.println("==== Order Menu ====");
        System.out.println("1) List all orders");
        System.out.println("2) List orders by status");
        System.out.println("3) Show order details");
        System.out.println("4) Cancel order");
        System.out.println("0) Back");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        // Route each option through the same error wrapper (no repeated try/catch in every method)
        switch (choice) {
            case "1" -> errorHandler.runWithHandling(this::listOrders);
            case "2" -> errorHandler.runWithHandling(() -> listOrdersByStatus(scanner));
            case "3" -> errorHandler.runWithHandling(() -> showOrderDetails(scanner));
            case "4" -> errorHandler.runWithHandling(() -> cancelOrder(scanner));
            case "0" -> {
                // back
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void listOrders() {
        List<Order> orders = orderService.listAll();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println();
        System.out.println("ID   | CustomerId | Status    | Total");
        System.out.println("-----+------------+-----------+-----------");

        for (Order o : orders) {
            System.out.printf(
                    "%-4d | %-10d | %-9s | %9.2f%n",
                    o.getId(),
                    o.getCustomer().getId(),
                    o.getStatus(),
                    o.getTotal()
            );
        }
    }

    private void listOrdersByStatus(Scanner scanner) {
        System.out.print("Status (NEW/PAID/CANCELLED): ");
        String statusInput = scanner.nextLine().trim().toUpperCase();

        OrderStatus status = OrderStatus.valueOf(statusInput); // IllegalArgumentException handled by ErrorHandlerCli

        List<Order> orders = orderService.listByStatus(status);
        if (orders.isEmpty()) {
            System.out.println("No orders found with status: " + status);
            return;
        }

        for (Order o : orders) {
            System.out.printf(
                    "Order %d: customerId=%d, total=%.2f%n",
                    o.getId(),
                    o.getCustomer().getId(),
                    o.getTotal()
            );
        }
    }

    private void createOrder(Scanner scanner) {
        System.out.print("Customer ID: ");
        Long customerId = Long.parseLong(scanner.nextLine().trim()); // NumberFormatException handled centrally

        List<OrderItemRequest> items = new ArrayList<>();

        while (true) {
            System.out.print("Product ID (blank to finish): ");
            String productIdInput = scanner.nextLine().trim();
            if (productIdInput.isEmpty()) break;

            long productId = Long.parseLong(productIdInput);

            Integer qty = null;
            while (qty == null) {
                System.out.print("Quantity: ");
                String qtyInput = scanner.nextLine().trim();
                try {
                    qty = Integer.parseInt(qtyInput);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid quantity, try again.");
                }
            }

            items.add(new OrderItemRequest(productId, qty));
        }

        if (items.isEmpty()) {
            System.out.println("No items, order not created.");
            return;
        }
        Order order = orderService.createOrder(customerId, items);
        System.out.println("Order created with id: " + order.getId() + ", total: " + order.getTotal());
    }

    private void showOrderDetails(Scanner scanner) {
        System.out.print("Order ID: ");
        Long orderId = Long.parseLong(scanner.nextLine().trim());

        Order order = orderService.getOrder(orderId);

        System.out.printf("Order %d, customerId=%d, status=%s, total=%.2f%n",
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getTotal()
        );

        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            System.out.println("No items.");
            return;
        }

        System.out.println("Items:");
        for (OrderItem item : items) {
            System.out.printf(
                    "  productId=%d, qty=%d, unitPrice=%.2f, lineTotal=%.2f%n",
                    item.getProduct().getId(),
                    item.getQty(),
                    item.getUnitPrice(),
                    item.getLineTotal()
            );
        }
    }

    private void cancelOrder(Scanner scanner) {
        System.out.print("Order ID to cancel: ");
        Long orderId = Long.parseLong(scanner.nextLine().trim());

        orderService.cancelOrder(orderId);
        System.out.println("Order cancelled: " + orderId);
    }

    }
