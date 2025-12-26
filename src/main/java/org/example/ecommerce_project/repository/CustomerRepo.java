package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCase(String email);

    @NativeQuery("SELECT max(id) FROM customer")
    Long getMaxId();
}
