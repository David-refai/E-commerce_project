package org.example.ecommerce_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Min(0)
    private int inStock;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    public Inventory() {}

    public Inventory(int inStock, Product product) {
        this.inStock = inStock;
        this.product = product;
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

    public void setProduct(Product product) {
        this.product = product;
    }
}
