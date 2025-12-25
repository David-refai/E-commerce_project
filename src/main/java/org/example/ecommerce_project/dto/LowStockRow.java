package org.example.ecommerce_project.dto;

public record LowStockRow(Long productId, String sku, String name, Integer inStock) {}
