package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.cart.CartItem;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.ProductRepo;
import org.example.ecommerce_project.services.CartService;
import org.example.ecommerce_project.services.CheckoutService;
import org.example.ecommerce_project.services.InventoryService;
import org.example.ecommerce_project.services.PaymentService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Scanner;
@Component
public class CartCli {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final PaymentService paymentService;
    private final ProductRepo productRepo;
    private final InventoryService inventoryService;

    // Selected customer for "per kund" cart
    private Long selectedCustomerId;

    public CartCli(
            CartService cartService,
            CheckoutService checkoutService,
            PaymentService paymentService,
            ProductRepo productRepo,
            InventoryService inventoryService
    ) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.paymentService = paymentService;
        this.productRepo = productRepo;
        this.inventoryService = inventoryService;
    }

    // Entry point from main menu
    public void run(Scanner sc) {
        boolean running = true;

        while (running) {
            showMenu();

            String cmd = sc.nextLine().trim();
            try {
                switch (cmd) {
                    case "1" -> selectCustomer(sc);
                    case "2" -> add(sc);
                    case "3" -> remove(sc);
                    case "4" -> show();
                    case "5" -> checkout(sc);
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (AppException | IllegalArgumentException | IllegalStateException ex) {
                System.out.println("❌ " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    void showMenu() {
        System.out.println("\n=== Cart ===");
        System.out.println("Selected customerId: " + (selectedCustomerId == null ? "-" : selectedCustomerId));
        System.out.println("1) Select customer");
        System.out.println("2) Add product");
        System.out.println("3) Remove product");
        System.out.println("4) Show cart");
        System.out.println("5) Checkout");
        System.out.println("0) Back");
        System.out.print("> ");
    }

    private void requireCustomerSelected() {
        if (selectedCustomerId == null) {
            throw new IllegalStateException("No customer selected. Use 'Select customer' first.");
        }
    }

    private void selectCustomer(Scanner sc) {
        System.out.print("Customer id: ");
        String s = sc.nextLine().trim();
        long id = Long.parseLong(s);
        if (id <= 0) throw new IllegalArgumentException("customerId must be positive");
        selectedCustomerId = id;
        System.out.println("Selected customerId = " + selectedCustomerId);
    }

    private void add(Scanner sc) {
        requireCustomerSelected();

        System.out.print("Product id: ");
        long productId = Long.parseLong(sc.nextLine().trim());

        System.out.print("Qty: ");
        int qty = Integer.parseInt(sc.nextLine().trim());

        cartService.addToCart(selectedCustomerId, productId, qty);
        System.out.println("Added to cart.");
    }

    private void remove(Scanner sc) {
        requireCustomerSelected();

        System.out.print("Product id: ");
        long productId = Long.parseLong(sc.nextLine().trim());

        System.out.print("Qty to remove: ");
        int qty = Integer.parseInt(sc.nextLine().trim());

        cartService.removeFromCart(selectedCustomerId, productId, qty);
        System.out.println("Removed from cart.");
    }

    private void show() {
        requireCustomerSelected();

        Cart cart = cartService.getCart(selectedCustomerId);
        if (cart.isEmpty()) {
            System.out.println("(Cart is empty)");
            return;
        }

        System.out.println("\n--- Cart items ---");
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            Product p = productRepo.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + ci.getProductId()));

            BigDecimal unitPrice = p.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQty()));

            int stock = inventoryService.getStockForProduct(p.getId());

            System.out.printf("ProductId=%d | %s | qty=%d | price=%s | line=%s | stock=%d%n",
                    p.getId(), p.getName(), ci.getQty(), unitPrice, lineTotal, stock);

            subtotal = subtotal.add(lineTotal);
        }

        System.out.println("Subtotal: " + subtotal);
    }

    private void checkout(Scanner sc) {
        requireCustomerSelected();

        System.out.print("Payment method (CARD/INVOICE): ");
        PaymentMethod method = PaymentMethod.valueOf(sc.nextLine().trim().toUpperCase());

        Order order = checkoutService.checkout(selectedCustomerId, method);
        System.out.println();
        System.out.println("ID   | CustomerId | Status    | Total");
        System.out.println("-----+------------+-----------+-----------");

            System.out.printf(
                    "%-4d | %-10d | %-9s | %9.2f%n",
                    order.getId(),
                    order.getCustomer().getId(),
                    order.getStatus(),
                    order.getTotal()
            );
    }

}
