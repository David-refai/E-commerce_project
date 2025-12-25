package org.example.ecommerce_project.cart;

public class CartItem {

    private final Long productId;
    private int qty;

    public CartItem(Long productId, int qty) {
        if (productId == null || productId <= 0) throw new IllegalArgumentException("productId must be positive");
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive");
        this.productId = productId;
        this.qty = qty;
    }

    public Long getProductId() { return productId; }
    public int getQty() { return qty; }

    public void addQty(int add) {
        if (add <= 0) throw new IllegalArgumentException("add must be positive");
        this.qty += add;
    }

    public void removeQty(int remove) {
        if (remove <= 0) throw new IllegalArgumentException("remove must be positive");
        this.qty -= remove;
    }
}
