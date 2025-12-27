package org.example.ecommerce_project.services;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.cart.CartItem;
import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CheckoutService(CartService cartService, OrderService orderService, PaymentService paymentService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * Genomför checkout: skapar en order från kundvagnen och utför betalning
     * @param customerId kundens ID
     * @param method vald betalningsmetod
     * @return skapad order
     */
    @Transactional
    public Order checkout(Long customerId, PaymentMethod method) {
        Cart cart = cartService.getCart(customerId);
        if (cart.isEmpty()) {
            throw AppException.validation("Cart is empty");
        }

        List<OrderItemRequest> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            items.add(new OrderItemRequest(ci.getProductId(), ci.getQty()));
        }

        // Skapar ordern baserat på kundvagnens innehåll
        Order order = orderService.createOrder(customerId, items);

        // Simulerar betalning (uppdaterar orderstatus och lager vid misslyckande)
        paymentService.processPayment(order.getId(), method);

        // Tömmer kundvagnen efter lyckad checkout
        cart.clear();

        return order;
    }
}
