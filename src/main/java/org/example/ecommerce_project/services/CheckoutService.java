package org.example.ecommerce_project.services;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.cart.CartItem;
import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
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

    // Checkout: create order from cart + simulate payment
    @Transactional
    public Order checkout(Long customerId, PaymentMethod method) {
        Cart cart = cartService.getCart(customerId);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItemRequest> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            items.add(new OrderItemRequest(ci.getProductId(), ci.getQty()));
        }

        Order order = orderService.createOrder(customerId, items);

        // Payment simulation (90% approved) updates payment + order status + inventory if declined
        paymentService.processPayment(order.getId(), method);

        // Clear cart after successful checkout flow
        cart.clear();

        return order;
    }
}
