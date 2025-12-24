package org.example.ecommerce_project.dto;

import org.example.ecommerce_project.entity.Category;

import java.math.BigDecimal;
import java.util.Set;

public class ProductUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean active;
    private Integer inStock;
    private Set<Category> categoriesForRemoval;
    private Set<Category> categoriesForAddition;

    public ProductUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getInStock() {
        return inStock;
    }

    public void setInStock(Integer inStock) {
        this.inStock = inStock;
    }

    public Set<Category> getCategoriesForRemoval() {
        return categoriesForRemoval;
    }

    public void setCategoriesForRemoval(Set<Category> categoriesForRemoval) {
        this.categoriesForRemoval = categoriesForRemoval;
    }

    public Set<Category> getCategoriesForAddition() {
        return categoriesForAddition;
    }

    public void setCategoriesForAddition(Set<Category> categoriesForAddition) {
        this.categoriesForAddition = categoriesForAddition;
    }
}
