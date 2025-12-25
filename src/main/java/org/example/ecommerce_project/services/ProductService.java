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

    @Transactional
    public void createProduct(String sku, String name, String description, BigDecimal price, Set<Category> categories, boolean active, int inStock) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (inStock < 0) {
            throw new IllegalArgumentException("In stock must be greater than or equal to zero");
        }

        productRepo.findBySku(sku)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Product already exists with SKU: " + sku);
                });

        Product product = new Product(sku, name, description, price, active);
        for (Category c : categories) {
            product.addCategory(c);
        }
        product.setInventory(new Inventory(inStock));
        productRepo.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        return productRepo.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with SKU: " + sku));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        return productRepo.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsByActive() {
        return productRepo.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsByCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        return productRepo.findByCategoryNameIgnoreCase(category);
    }

    @Transactional
    public Optional<Product> updateProduct(String sku, ProductUpdateRequest update) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
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
                    throw new IllegalArgumentException("In stock must be greater than or equal to zero");
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

    @Transactional
    public Optional<Product> disableProduct(String sku) {
        return productRepo.findBySku(sku).map(tmp -> {
            tmp.setActive(false);
            return productRepo.save(tmp);
        });
    }
}
