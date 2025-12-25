package org.example.ecommerce_project.repository;


import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailIgnoreCase(String email);

    List<Order> findByStatus(OrderStatus status);

    @Query("""
            select distinct o from Order o
            left join fetch o.items i
            left join fetch i.product
            left join fetch o.customer
            where o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

}
