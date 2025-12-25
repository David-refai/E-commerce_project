package org.example.ecommerce_project.cli;


import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/*
 * Root (parent) CLI entry point.
 *  This class starts the console application and routes the user
 *  to the appropriate sub-CLI (Customer, Product, Order, etc.).
 *  Other services and CLIs can be plugged in here later.
 * */
@Component
public class AppRouter implements CommandLineRunner {

    private final CustomerCli customerCli;
    private final ProductCli productCli;
    private final OrderCli orderCli;
    private final CartCli cartCli;
    private final ReportCli reportCli;

    public AppRouter(CustomerCli customerCli, ProductCli productCli, OrderCli orderCli, CartCli cartCli, ReportCli reportCli) {
        this.customerCli = customerCli;
        this.productCli = productCli;
        this.orderCli = orderCli;
        this.cartCli = cartCli;
        this.reportCli = reportCli;
    }

    @NullMarked
    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            System.out.println("E-commerce console started. Use the menu to navigate.");

            while (running) {
                running = showMainMenu(scanner);
            }

            System.out.println("Bye!");
        }
    }

    private boolean showMainMenu(Scanner scanner) {
        System.out.println();
        System.out.println("==== Main Menu ====");
        System.out.println("1) Customers");
        System.out.println("2) Products");
        System.out.println("3) Orders & payments");
        System.out.println("4) Add to Cart");
        System.out.println("5) Report");
        System.out.println("0) Exit");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> customerCli.showMenu(scanner);  // Delegate to Customer CLI
            case "2" -> productCli.showMenu(scanner); // Future extension
            case "3" -> orderCli.showMenu(scanner);
            case "4" -> cartCli.run(scanner);
            case "5" -> reportCli.run(scanner);
            case "0" -> {
                return false;
            }
            default -> System.out.println("Invalid choice, please try again.");
        }

        return true;
    }
}
