package org.example.ecommerce_project.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {
}
