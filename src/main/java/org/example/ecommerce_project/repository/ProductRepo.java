package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String q);

    List<Product> findByActiveTrue();

    @Query("select distinct p from Product p join p.categories c where lower(c.name) = lower(:category)")
    List<Product> findByCategoryNameIgnoreCase(@Param("category") String category);
}
