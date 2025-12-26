package org.example.ecommerce_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Min(value = 0, message = "In stock must be greater than or equal to zero")
    private int inStock;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Inventory() {
    }

    public Inventory(int inStock) {
        this.inStock = inStock;
    }

    public Long getProductId() {
        return productId;
    }

    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    public Product getProduct() {
        return product;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
