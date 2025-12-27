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
     * Kontrollerar att produkt-ID är positivt
     */
    private static void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw AppException.validation("productId must be a positive number");
        }
    }

    /**
     * Validerar att ett värde är > 0
     */
    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw AppException.validation(name + " must be positive");
        }
    }

    /**
     * Validerar att ett värde inte är negativt
     */
    private static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw AppException.validation(name + " must be >= 0");
        }
    }

    /**
     * Hämtar lagersaldo för en produkt, returnerar 0 om ingen rad finns
     * @param productId produktens ID
     * @return antal i lager
     */
    @Transactional(readOnly = true)
    public int getStockForProduct(Long productId) {
        requirePositiveId(productId);
        return inventoryRepo.findById(productId)
                .map(Inventory::getInStock)
                .orElse(0);
    }

    /**
     * Sätter lagersaldo direkt (>=0), skapar rad om saknas
     * @param productId produktens ID
     * @param quantity nytt saldo
     * @return uppdaterad inventeringsrad
     */
    @Transactional
    public Inventory setStock(Long productId, int quantity) {
        requirePositiveId(productId);
        requireNonNegative(quantity, "quantity");

        Inventory inv = getOrCreateInventory(productId);
        inv.setInStock(quantity);
        return inventoryRepo.save(inv);
    }

    /**
     * Ökar lagersaldo med ett positivt antal
     * @param productId produktens ID
     * @param quantity mängd att lägga till
     * @return uppdaterad inventeringsrad
     */
    @Transactional
    public Inventory releaseStock(Long productId, int quantity) {
        requirePositiveId(productId);
        requirePositive(quantity, "quantity");

        Inventory inv = getOrCreateInventory(productId);
        inv.setInStock(inv.getInStock() + quantity);
        return inventoryRepo.save(inv);
    }

    /**
     * Minskar lagersaldo (reservation) med ett positivt antal
     * Kastar fel om lager saknas
     * @param productId produktens ID
     * @param quantity mängd att reservera
     * @return uppdaterad inventeringsrad
     */
    @Transactional
    public Inventory reserveStock(Long productId, int quantity) {
        requirePositiveId(productId);
        requirePositive(quantity, "quantity");

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

    /**
     * Hämtar produkter med lägre lagersaldo än angiven gräns
     * @param threshold gränsvärde
     * @return lista med inventeringsrader
     */
    @Transactional(readOnly = true)
    public List<Inventory> findLowStock(int threshold) {
        requireNonNegative(threshold, "threshold");
        return inventoryRepo.findByInStockLessThan(threshold);
    }

    /**
     * Hämtar eller skapar inventeringspost för en produkt
     * @param productId produktens ID
     * @return inventeringsrad
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
