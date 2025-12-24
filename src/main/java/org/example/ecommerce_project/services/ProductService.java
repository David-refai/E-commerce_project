package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @Transactional
    public void createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("Product SKU must not be blank");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }

        productRepo.findBySku(product.getSku())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Product already exists with SKU: " + product.getSku());
                });

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
            throw new IllegalArgumentException("Id must not be blank");
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
    public Optional<Product> updateProduct(String sku, Product newProduct) {
        return productRepo.findBySku(sku).map(tmp -> {
            if (newProduct.getSku() != null && !newProduct.getSku().isBlank()) {
                tmp.setSku(newProduct.getSku());
            }
            if (newProduct.getName() != null && !newProduct.getName().isBlank()) {
                tmp.setName(newProduct.getName());
            }
            if (newProduct.getDescription() != null && !newProduct.getDescription().isBlank()) {
                tmp.setDescription(newProduct.getDescription());
            }
            if (newProduct.getPrice() != null && newProduct.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                tmp.setPrice(newProduct.getPrice());
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
