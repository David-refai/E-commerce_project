package org.example.ecommerce_project.cli;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Category;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.dto.ProductUpdateRequest;
import org.example.ecommerce_project.exception.ErrorHandlerCli;
import org.example.ecommerce_project.services.CategoryService;
import org.example.ecommerce_project.services.ProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO
// Wrap with ErrorHandlerCli
@Component
public class ProductCli {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ErrorHandlerCli handler = new ErrorHandlerCli(false);

    public ProductCli(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    public void showMenu(Scanner scanner) {
        System.out.println();
        System.out.println("==== Product Menu ====");
        System.out.println("1) List products");
        System.out.println("2) Add product");
        System.out.println("3) Find products by name");
        System.out.println("4) Find product by SKU");
        System.out.println("5) Update product");
        System.out.println("6) Disable product");
        System.out.println("0) Back");
        System.out.print("Select: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> handler.runWithHandling(this::listProducts);
            case "2" -> handler.runWithHandling(() -> addProduct(scanner));
            case "3" -> handler.runWithHandling(() -> findProductsByName(scanner));
            case "4" -> handler.runWithHandling(() -> findProductBySku(scanner));
            case "5" -> handler.runWithHandling(() -> updateProduct(scanner));
            case "6" -> handler.runWithHandling(() -> disableProduct(scanner));
            case "0" -> {
                // Return to previous menu (caller decides what to do next)
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    // Prints all products in a simple table
    private void listProducts() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println();
        System.out.println("ID   | SKU        | Name                           | Price      | Active | In Stock | Categories");
        System.out.println("-----+------------+--------------------------------+------------+--------+----------+------------------------------------------");

        for (Product p : products) {
            System.out.printf(
                    "%-4d | %-10s | %-30s | %-10s | %-6b | %-8d | %-40s%n",
                    p.getId(),
                    p.getSku(),
                    p.getName(),
                    p.getPrice(),
                    p.isActive(),
                    p.getInventory().getInStock(),
                    p.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
            );
        }
    }

    // Reads product input from the user and creates a new product
    private void addProduct(Scanner scanner) {
        System.out.println("Enter the values for the new product, or press enter to skip. Required fields are marked with (*)");
        System.out.print("SKU (*): ");
        String sku = scanner.nextLine().trim();

        System.out.print("Name (*): ");
        String name = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Price (*): ");
        String priceString = scanner.nextLine().trim();

        Set<Category> categories = addCategories(scanner);

        System.out.print("Active (Y/N) (*): ");
        String activeString = scanner.nextLine().trim();

        System.out.print("In stock (*): ");
        String inStockString = scanner.nextLine().trim();

        // TODO
        // Wrap with ErrorHandlerCli unless already handled by the service
        BigDecimal price = new BigDecimal(priceString);
        boolean active = Stream.of("y", "yes", "t", "true").anyMatch(activeString::equalsIgnoreCase);
        int inStock = Integer.parseInt(inStockString);

        productService.createProduct(sku, name, description, price, categories, active, inStock);
        System.out.println("Product created with id: " + productService.getProductBySku(sku).getId());
    }

    // Helper method that reads category input from the user and returns the categories to add
    private Set<Category> addCategories(Scanner scanner) {
        Set<Category> categories = new HashSet<>();
        String categoryString;
        System.out.print("Categories to add (comma separated): ");
        categoryString = scanner.nextLine().trim();
        if (!categoryString.isBlank()) {
            String[] categoryNames = categoryString.split(",");
            for (String categoryName : categoryNames) {
                Category category;
                // Check if the category exists, if not - create it
                try {
                    category = categoryService.getCategoryByName(categoryName);
                } catch (EntityNotFoundException e) {
                    category = categoryService.createCategory(categoryName);
                }
                categories.add(category);
            }
        }
        return categories;
    }

    // Finds products by name and prints the result
    private void findProductsByName(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        List<Product> products = productService.getAllProductsByName(name);

        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println("ID   | SKU        | Name                           | Price      | Active | In Stock | Categories");
        System.out.println("-----+------------+--------------------------------+------------+--------+----------+------------------------------------------");

        for (Product p : products) {
            System.out.printf(
                    "%-4d | %-10s | %-30s | %-10s | %-6b | %-8d | %-40s%n",
                    p.getId(),
                    p.getSku(),
                    p.getName(),
                    p.getPrice(),
                    p.isActive(),
                    p.getInventory().getInStock(),
                    p.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
            );
        }
    }

    // Finds a product by name and prints the result
    private void findProductBySku(Scanner scanner) {
        System.out.print("SKU: ");
        String sku = scanner.nextLine().trim();

        Product product = productService.getProductBySku(sku);

        System.out.println("ID   | SKU        | Name                           | Price      | Active | In Stock | Categories");
        System.out.println("-----+------------+--------------------------------+------------+--------+----------+------------------------------------------");
        System.out.printf(
                "%-4d | %-10s | %-30s | %-10s | %-6b | %-8d | %-40s%n",
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive(),
                product.getInventory().getInStock(),
                product.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
        );
    }

    // Updates a product
    private void updateProduct(Scanner scanner) {
        System.out.print("SKU: ");
        String sku = scanner.nextLine().trim();

        Product currentProduct = productService.getProductBySku(sku);
        ProductUpdateRequest update = new ProductUpdateRequest();
        System.out.println("Enter the values you want to change, or press enter to skip.");

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            update.setName(name);
        }
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) {
            update.setDescription(description);
        }
        System.out.print("Price: ");
        String priceString = scanner.nextLine().trim();
        if (!priceString.isEmpty()) {
            update.setPrice(new BigDecimal(priceString));
        }
        System.out.print("Active (Y/N): ");
        String activeString = scanner.nextLine().trim();
        if (!activeString.isEmpty()) {
            update.setActive(Stream.of("y", "yes", "t", "true").anyMatch(activeString::equalsIgnoreCase));
        }
        System.out.print("In stock: ");
        String inStockString = scanner.nextLine().trim();
        if (!inStockString.isEmpty()) {
            update.setInStock(Integer.parseInt(inStockString));
        }
        Set<Category> categoriesForRemoval = new HashSet<>();
        Set<Category> categories = currentProduct.getCategories();
        if (!categories.isEmpty()) {
            System.out.println("Current categories:");
            for (Category category : categories) {
                System.out.println(category.getName());
            }
            categoriesForRemoval = removeCategories(scanner);
        }
        Set<Category> categoriesForAddition = addCategories(scanner);

        update.setCategoriesForRemoval(categoriesForRemoval);
        update.setCategoriesForAddition(categoriesForAddition);

        Optional<Product> updated = productService.updateProduct(sku, update);
        updated.ifPresentOrElse(
                p -> System.out.println("Product updated: " + p.getSku()),
                () -> System.out.println("No product found with SKU: " + sku)
        );
    }

    // Helper method that reads category input from the user and returns the categories to remove
    private Set<Category> removeCategories(Scanner scanner) {
        Set<Category> categories = new HashSet<>();
        String categoryString;
        System.out.print("Categories to remove (comma separated): ");
        categoryString = scanner.nextLine().trim();
        if (!categoryString.isBlank()) {
            String[] categoryNames = categoryString.split(",");
            for (String categoryName : categoryNames) {
                // Check if the category exists, if not - skip it
                //try {
                    Category category = categoryService.getCategoryByName(categoryName);
                    categories.add(category);
                //} catch (EntityNotFoundException ignored) {}
            }
        }
        return categories;
    }

    // Disables a product
    private void disableProduct(Scanner scanner) {
        System.out.print("SKU: ");
        String sku = scanner.nextLine().trim();
        productService.disableProduct(sku);
    }
}
