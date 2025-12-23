package org.example.ecommerce_project.repository;

import jakarta.persistence.criteria.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailIgnoreCase(String email);

    List<Order> findByStatus(OrderStatus status);
}
