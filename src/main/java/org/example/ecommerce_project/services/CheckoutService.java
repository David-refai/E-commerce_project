package org.example.ecommerce_project.services;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.cart.CartItem;
import org.example.ecommerce_project.dto.OrderItemRequest;
import org.example.ecommerce_project.entity.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutService(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @Transactional
    public Order checkout(Long customerId) {
        Cart cart = cartService.getCart(customerId);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItemRequest> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            items.add(new OrderItemRequest(ci.getProductId(), ci.getQty()));
        }

        Order order = orderService.createOrder(customerId, items);

        cart.clear();
        return order;
    }
}
