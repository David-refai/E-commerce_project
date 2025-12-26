package org.example.ecommerce_project.services;

import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.InventoryRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepo inventoryRepo;
    private final ProductRepo productRepo;

    public InventoryService(InventoryRepo inventoryRepo, ProductRepo productRepo) {
        this.inventoryRepo = inventoryRepo;
        this.productRepo = productRepo;
    }

    /**
     * TODO: Placeholder logic. 👇
     * Subject to change after full investigation and verification
     * of requirements and business constraints.
     */
    private static void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw AppException.validation("productId must be a positive number");
        }
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw AppException.validation(name + " must be positive");
        }
    }

    private static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw AppException.validation(name + " must be >= 0");
        }
    }

    @Transactional(readOnly = true)
    public int getStockForProduct(Long productId) {
        requirePositiveId(productId); // TODO 👈
        return inventoryRepo.findById(productId)
                .map(Inventory::getInStock)
                .orElse(0);
    }

    /**
     * Set exact stock value (>= 0). Creates inventory row if missing.
     */
    @Transactional
    public Inventory setStock(Long productId, int quantity) {

        requirePositiveId(productId); // TODO 👈
        requireNonNegative(quantity, "quantity");  // TODO 👈

        Inventory inv = getOrCreateInventory(productId);
        inv.setInStock(quantity);
        return inventoryRepo.save(inv);
    }

    /**
     * Increase stock by a positive amount. Creates inventory row if missing.
     */
    @Transactional
    public Inventory releaseStock(Long productId, int quantity) {
        requirePositiveId(productId); //TODO 👈
        requirePositive(quantity, "quantity");  // TODO 👈

        Inventory inv = getOrCreateInventory(productId);
        inv.setInStock(inv.getInStock() + quantity);
        return inventoryRepo.save(inv);
    }

    /**
     * Reserve (decrease) stock by a positive amount.
     * Throws if not enough stock. Creates inventory row if missing (0 stock).
     */
    @Transactional
    public Inventory reserveStock(Long productId, int quantity) {
        requirePositiveId(productId); // TODO 👈
        requirePositive(quantity, "quantity"); // TODO 👈

        Inventory inv = getOrCreateInventory(productId);
        int current = inv.getInStock();

        if (current < quantity) {
            throw AppException.validation(
                    "Not enough stock for product id: " + productId +
                            ". Available: " + current + ", requested: " + quantity
            );
        }

        inv.setInStock(current - quantity);
        return inventoryRepo.save(inv);
    }

    @Transactional(readOnly = true)
    public List<Inventory> findLowStock(int threshold) {
        requireNonNegative(threshold, "threshold"); // TODO 👈
        return inventoryRepo.findByInStockLessThan(threshold);
    }

    /**
     * Ensures inventory exists:
     * - If inventory row exists -> returns it
     * - If missing -> creates it with 0 stock (and ties it to Product via @MapsId)
     */
    private Inventory getOrCreateInventory(Long productId) {
        return inventoryRepo.findById(productId).orElseGet(() -> {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

            Inventory inv = new Inventory();
            inv.setProduct(product);
            inv.setInStock(0);

            return inventoryRepo.save(inv);
        });
    }
}
