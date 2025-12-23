package org.example.ecommerce_project.repository;

import jakarta.persistence.criteria.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailIgnoreCase(String email);

    List<Order> findByStatus(OrderStatus status);
}
