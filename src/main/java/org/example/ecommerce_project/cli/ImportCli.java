package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.exception.ErrorHandlerCli;
import org.example.ecommerce_project.services.CustomerImportService;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Scanner;

@Component
public class ImportCli {

    private final ErrorHandlerCli handler = new ErrorHandlerCli(false);
    private final CustomerImportService customerImportService;

    public ImportCli(CustomerImportService customerImportService) {
        this.customerImportService = customerImportService;
    }

    public void showMenu(Scanner scanner) {
        System.out.println();
        System.out.println("==== Import Menu ====");
        System.out.println("CSV files are imported from src/main/resources/import");
        System.out.println("1) Import Customers from CSV");
        System.out.println("0) Back");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> handler.runWithHandling(() -> importCustomers(scanner));
            case "0" -> {
                // Return to previous menu (caller decides what to do next)
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void importCustomers(Scanner scanner) {
        System.out.println("Enter the file name where customers are stored: ");
        String customerCsv = scanner.nextLine().trim();
        if (!Objects.equals(customerCsv.substring(customerCsv.length() - 4), ".csv")) {
            customerCsv += ".csv";
        }
        customerImportService.importCustomers(customerCsv);
    }
}
