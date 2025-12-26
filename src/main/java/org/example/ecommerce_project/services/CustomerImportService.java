package org.example.ecommerce_project.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CustomerImportService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomerRepo customerRepo;

    @Transactional
    public void importCustomers(String csvFile) {

        // Clearing the customer table and resetting the identity sequence
        entityManager.createNativeQuery("TRUNCATE TABLE customer RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.flush();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(csvFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            // Skip header
            String line = br.readLine();
            if (line == null) {
                throw new RuntimeException("CSV file is empty");
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 3) continue; // Skip invalid lines

                Long id = Long.parseLong(values[0].trim());
                String name = values[1].trim().replaceAll("^\"|\"$", "");
                String email = values[2].trim().replaceAll("^\"|\"$", "");

                // Try to create a customer as a sanity check that the columns match at least
                Customer customer = new Customer();
                customer.setId(id);
                customer.setName(name);
                customer.setEmail(email);

                // Force the id to be inserted in order to preserve the data
                String query = String.format("INSERT INTO customer (id, name, email) OVERRIDING SYSTEM VALUE VALUES (%d, '%s', '%s')", id, name, email);
                entityManager.createNativeQuery(query).executeUpdate();
            }

            // Find the max id and update the identity sequence
            Long maxId = customerRepo.getMaxId();
            String query = "ALTER SEQUENCE customer_id_seq RESTART WITH " + (maxId + 1);
            entityManager.createNativeQuery(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
