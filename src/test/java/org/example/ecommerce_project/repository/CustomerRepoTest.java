package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepoTest {
    @Autowired
    private CustomerRepo customerRepo;

    @Test
    void findByEmailIgnoreCase_findsCustomerRegardlessOfCase() {
        // Arrange
        Customer c = new Customer();
        c.setName("Omar Aman");
        c.setEmail("omaraman@mail.com");
        customerRepo.save(c);

        // Act
        Optional<Customer> found = customerRepo.findByEmailIgnoreCase("OMARAMAN@MAIL.COM");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getEmail()).isEqualTo("omaraman@mail.com");
    }

    @Test
    void findByEmailIgnoreCase_returnsEmptyWhenNotFound() {
        // Act
        Optional<Customer> found = customerRepo.findByEmailIgnoreCase("unknown@example.com");

        // Assert
        assertThat(found).isEmpty();
    }
}
