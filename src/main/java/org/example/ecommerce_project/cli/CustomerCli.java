package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.exception.ErrorHandlerCli;
import org.example.ecommerce_project.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class CustomerCli {


    private final ErrorHandlerCli handler = new ErrorHandlerCli(false);
    // CLI layer for customer operations (uses CustomerService, no DB logic here)
    private final CustomerService customerService;

    public CustomerCli(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Inject the service used to fetch/create customers


    // Shows the customer menu once and executes the selected action
    public void showMenu(Scanner scanner) {
        System.out.println();
        System.out.println("==== Customer Menu ====");
        System.out.println("1) List customers");
        System.out.println("2) Add customer");
        System.out.println("3) Find customer by email");
        System.out.println("0) Back");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
        //TODO
            //Should each menu option runs inside a error-handling wrapper,
            case "1" -> handler.runWithHandling(this::listCustomers);
            case "2" -> handler.runWithHandling(() -> addCustomer(scanner));
            case "3" ->  handler.runWithHandling(() ->findCustomerByEmail(scanner));
            case "0" -> {
                // Return to previous menu (caller decides what to do next)
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    // Prints all customers in a simple table
    private void listCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        System.out.println();
        System.out.println("ID   | Email                      | Name");
        System.out.println("-----+----------------------------+--------------------------");

        for (Customer c : customers) {
            System.out.printf(
                    "%-4d | %-26s | %-24s%n",
                    c.getId(),
                    c.getEmail(),
                    c.getName()
            );
        }
    }

    // Reads customer input from the user and creates a new customer
    private void addCustomer(Scanner scanner) {
        Customer customer = new Customer();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        customer.setName(name);
        customer.setEmail(email);

        customerService.createCustomer(customer);
        System.out.println("Customer created with id: " + customer.getId() + " 🎉");
    }

    // Finds a customer by email and prints the result
    private void findCustomerByEmail(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        Customer customer = customerService.getCustomerByEmail(email);

        System.out.println("\nID   | Email                      | Name");
        System.out.println("-----+----------------------------+--------------------------");
        System.out.printf(
                "%-4d | %-26s | %-24s%n",
                customer.getId(),
                customer.getEmail(),
                customer.getName()
        );
    }
}
