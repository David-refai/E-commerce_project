package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        customerRepo.findByEmailIgnoreCase(customer.getEmail())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Customer with email already exists: " + customer.getEmail());
                });

        return customerRepo.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        return customerRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updated) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (updated == null) {
            throw new IllegalArgumentException("Updated customer must not be null");
        }

        Customer existing = getCustomerById(id);

        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            customerRepo.findByEmailIgnoreCase(updated.getEmail())
                    .ifPresent(other -> {
                        if (!other.getId().equals(existing.getId())) {
                            throw new IllegalArgumentException("Customer with email already exists: " + updated.getEmail());
                        }
                    });
            existing.setEmail(updated.getEmail());
        }

        if (updated.getName() != null && !updated.getName().isBlank()) {
            existing.setName(updated.getName());
        }

        return customerRepo.save(existing);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer existing = getCustomerById(id);
        customerRepo.delete(existing);
    }
}
