package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.exception.AppException;
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

    /**
     * Skapar en ny kund efter validering och kontroll om e-post redan finns
     * @param customer kundobjekt att spara
     * @return sparad kund
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw AppException.validation("Customer must not be null");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw AppException.validation("Email must not be blank");
        }
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw AppException.validation("Name must not be blank");
        }

        customerRepo.findByEmailIgnoreCase(customer.getEmail())
                .ifPresent(existing -> {
                    throw AppException.businessRule("Customer with email already exists: " + customer.getEmail());
                });

        return customerRepo.save(customer);
    }

    /**
     * Hämtar kund med ID, annars kastas ett fel
     * @param id kundens ID
     * @return kundobjekt
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        if (id == null) {
            throw AppException.validation("Id must not be null");
        }
        return customerRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Customer not found with id: " + id));
    }

    /**
     * Hämtar kund med e-postadress (case-insensitive)
     * @param email kundens e-post
     * @return kundobjekt
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw AppException.validation("Email must not be blank");
        }
        return customerRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> AppException.notFound("Customer not found with email: " + email));
    }

    /**
     * Hämtar alla kunder i systemet
     * @return lista av kunder
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    /**
     * Uppdaterar en kunds information efter validering av indata
     * @param id kundens ID
     * @param updated nytt kundobjekt med nya värden
     * @return uppdaterad kund
     */
    @Transactional
    public Customer updateCustomer(Long id, Customer updated) {
        if (id == null) {
            throw AppException.validation("Id must not be null");
        }
        if (updated == null) {
            throw AppException.validation("Updated customer must not be null");
        }

        // Hämtar kund som ska uppdateras
        Customer existing = getCustomerById(id);

        // Uppdatera e-post om giltigt och ej upptaget av annan
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            customerRepo.findByEmailIgnoreCase(updated.getEmail())
                    .ifPresent(other -> {
                        if (!other.getId().equals(existing.getId())) {
                            throw AppException.businessRule("Customer with email already exists: " + updated.getEmail());
                        }
                    });
            existing.setEmail(updated.getEmail());
        }

        // Uppdatera namn om angivet
        if (updated.getName() != null && !updated.getName().isBlank()) {
            existing.setName(updated.getName());
        }

        return customerRepo.save(existing);
    }
}
