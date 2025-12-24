package org.example.ecommerce_project.repository;


public interface TopSellingProductView {
    Long getProductId();
    String getSku();
    String getName();
    Long getQtySold();
}
