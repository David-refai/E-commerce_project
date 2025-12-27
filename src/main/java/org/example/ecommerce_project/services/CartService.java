package org.example.ecommerce_project.services;

import org.example.ecommerce_project.cart.Cart;
import org.example.ecommerce_project.cart.CartItem;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private final ProductRepo productRepo;
    private final InventoryService inventoryService;

    // Sparar kundens kundvagn kopplad till deras ID
    private final Map<Long, Cart> carts = new HashMap<>();

    public CartService(ProductRepo productRepo, InventoryService inventoryService) {
        this.productRepo = productRepo;
        this.inventoryService = inventoryService;
    }

    /**
     * Hämtar kundens cart, eller skapar en ny om den inte finns
     * @param customerId kundens ID
     * @return Cart
     */
    public Cart getCart(Long customerId) {
        if (customerId == null || customerId <= 0) throw AppException.validation("customerId must be positive");
        return carts.computeIfAbsent(customerId, Cart::new);
    }

    /**
     * Lägger till en produkt i kundvagnen efter att ha kollat lagersaldo
     * @param customerId kundens ID
     * @param productId produktens ID
     * @param qty antal att lägga till
     */
    public void addToCart(Long customerId, Long productId, int qty) {
        if (qty <= 0) throw AppException.validation("qty must be positive");

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> AppException.notFound(
                        "Product not found with id: " + productId
                ));

        if (!product.isActive()) {
            throw AppException.businessRule("Product is not active: " + product.getSku());
        }

        Cart cart = getCart(customerId);

        int stock = inventoryService.getStockForProduct(productId);
        int currentInCart = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .mapToInt(CartItem::getQty)
                .sum();

        if (currentInCart + qty > stock) {
            throw AppException.validation("Not enough stock. In cart: " + currentInCart + ", stock: " + stock);
        }

        cart.add(productId, qty);
    }

    /**
     * Tar bort en viss mängd av en produkt från kundvagnen
     * @param customerId kundens ID
     * @param productId produktens ID
     * @param qty antal att ta bort
     */
    public void removeFromCart(Long customerId, Long productId, int qty) {
        if (qty <= 0) throw AppException.validation("qty must be positive");
        Cart cart = getCart(customerId);
        cart.remove(productId, qty);
    }

    /**
     * Tar bort allt innehåll i kundens kundvagn
     * @param customerId kundens ID
     */
    public void clearCart(Long customerId) {
        getCart(customerId).clear();
    }
}
