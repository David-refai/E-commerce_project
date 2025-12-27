package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.dto.ProductUpdateRequest;
import org.example.ecommerce_project.entity.Category;
import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    /**
     * Skapar en ny produkt med kopplade kategorier och initialt lagersaldo
     * Validerar indata och säkerställer att SKU är unik
     *
     * @param sku produktens SKU
     * @param name produktnamn
     * @param description produktbeskrivning
     * @param price pris > 0
     * @param categories kategorier att koppla
     * @param active om produkten är aktiv
     * @param inStock initialt lagersaldo (>=0)
     */
    @Transactional
    public void createProduct(String sku, String name, String description, BigDecimal price,
                              Set<Category> categories, boolean active, int inStock) {
        if (sku == null || sku.isBlank()) {
            throw AppException.validation("SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw AppException.validation("Name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw AppException.validation("Description must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.validation("Price must be greater than zero");
        }
        if (inStock < 0) {
            throw AppException.validation("In stock must be greater than or equal to zero");
        }

        productRepo.findBySku(sku)
                .ifPresent(existing -> {
                    throw AppException.businessRule("Product already exists with SKU: " + sku);
                });

        Product product = new Product(sku, name, description, price, active);
        for (Category c : categories) {
            product.addCategory(c);
        }
        product.setInventory(new Inventory(inStock));
        productRepo.save(product);
    }

    /**
     * Hämtar alla produkter
     * @return lista av produkter
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    /**
     * Hämtar produkt via ID, kastar fel om den inte finns
     * @param id produktens ID
     * @return produkt
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        if (id == null) {
            throw AppException.validation("Id must not be null");
        }
        return productRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Product not found with id: " + id));
    }

    /**
     * Hämtar produkt via SKU, kastar fel om den inte finns
     * @param sku produktens SKU
     * @return produkt
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw AppException.validation("SKU must not be blank");
        }
        return productRepo.findBySku(sku)
                .orElseThrow(() -> AppException.notFound("Product not found with SKU: " + sku));
    }

    /**
     * Söker produkter vars namn innehåller given text (case-insensitive)
     * @param name del av produktnamn
     * @return lista av matchande produkter
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsByName(String name) {
        if (name == null || name.isBlank()) {
            throw AppException.validation("Product name must not be blank");
        }
        return productRepo.findByNameContainingIgnoreCase(name);
    }

    /**
     * Uppdaterar produktfält baserat på SKU och ett update-objekt
     * Endast icke-null och giltiga fält uppdateras
     *
     * @param sku produktens SKU
     * @param update värden att uppdatera
     * @return Optional med uppdaterad produkt, tom om SKU inte finns
     */
    @Transactional
    public Optional<Product> updateProduct(String sku, ProductUpdateRequest update) {
        if (sku == null || sku.isBlank()) {
            throw AppException.validation("SKU must not be blank");
        }
        if (update == null) {
            throw AppException.businessRule("Update request must not be null");
        }

        return productRepo.findBySku(sku).map(tmp -> {
            if (update.getName() != null && !update.getName().isBlank()) {
                tmp.setName(update.getName());
            }
            if (update.getDescription() != null && !update.getDescription().isBlank()) {
                tmp.setDescription(update.getDescription());
            }
            if (update.getPrice() != null && update.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                tmp.setPrice(update.getPrice());
            }
            if (update.getActive() != null) {
                tmp.setActive(update.getActive());
            }
            if (update.getInStock() != null) {
                if (update.getInStock() < 0) {
                    throw AppException.businessRule("In stock must be greater than or equal to zero");
                }
                Inventory inventory = tmp.getInventory();
                if (inventory == null) {
                    inventory = new Inventory();
                    tmp.setInventory(inventory);
                }
                inventory.setInStock(update.getInStock());
            }
            if (update.getCategoriesForRemoval() != null) {
                for (Category c : update.getCategoriesForRemoval()) {
                    tmp.removeCategory(c);
                }
            }
            if (update.getCategoriesForAddition() != null) {
                for (Category c : update.getCategoriesForAddition()) {
                    tmp.addCategory(c);
                }
            }

            return productRepo.save(tmp);
        });
    }

    /**
     * Inaktiverar (disablar) en produkt baserat på SKU
     * @param sku produktens SKU
     * @return Optional med uppdaterad produkt, tom om SKU inte finns
     */
    @Transactional
    public Optional<Product> disableProduct(String sku) {
        return productRepo.findBySku(sku).map(tmp -> {
            tmp.setActive(false);
            return productRepo.save(tmp);
        });
    }
}
