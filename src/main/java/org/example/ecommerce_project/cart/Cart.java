package org.example.ecommerce_project.cart;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {

    private final Long customerId;
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Cart(Long customerId) {
        if (customerId == null || customerId <= 0) throw new IllegalArgumentException("customerId must be positive");
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    // Add qty for a product (no stock logic here)
    public void add(Long productId, int qty) {
        CartItem existing = items.get(productId);
        if (existing == null) {
            items.put(productId, new CartItem(productId, qty));
        } else {
            existing.addQty(qty);
        }
    }

    // Remove qty (or remove line if <= 0)
    public void remove(Long productId, int qty) {
        CartItem existing = items.get(productId);
        if (existing == null) return;

        existing.removeQty(qty);
        if (existing.getQty() <= 0) {
            items.remove(productId);
        }
    }
}
