package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {
    List<Inventory> findByInStockLessThan(int threshold);
}
