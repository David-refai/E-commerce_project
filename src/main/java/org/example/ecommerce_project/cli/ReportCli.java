package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.dto.LowStockRow;
import org.example.ecommerce_project.dto.TopProductRow;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.services.ReportService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@Component
public class ReportCli {

    private final ReportService reportService;

    public ReportCli(ReportService reportService) {
        this.reportService = reportService;
    }

    public void run(Scanner scanner) {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> showTopProducts(scanner);
                    case "2" -> showLowStock(scanner);
                    case "3" -> showRevenueBetween(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (AppException ex) {
                System.out.println("❌ " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                System.out.println("❌ " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ Unexpected error: " + ex.getMessage());
            }

            if (running) {
                System.out.println();
                System.out.print("Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Reports ===");
        System.out.println("1) Top products (best sellers)");
        System.out.println("2) Low stock (< X)");
        System.out.println("3) Revenue between dates");
        System.out.println("0) Back");
        System.out.print("Choose: ");
    }

    /**
     * Shows top N products by units sold in a date range (PAID orders only).
     * Date range is [from, toExclusive), so toExclusive is not included.
     */
    private void showTopProducts(Scanner scanner) {
        int topN = readInt(scanner, "Top N (default 5): ", 5);

        LocalDate from = readDate(scanner, "From date (YYYY-MM-DD): ");
        LocalDate toExclusive = readDate(scanner, "To date (YYYY-MM-DD) [exclusive]: ");

        validateDateRange(from, toExclusive);

        List<TopProductRow> rows = reportService.topProducts(topN, from, toExclusive);

        if (rows.isEmpty()) {
            System.out.println("No sales in this period.");
            return;
        }

        System.out.println("\nTop products:");
        System.out.println("----------------------------------------------");
        System.out.printf("%-4s %-10s %-25s %-10s%n", "#", "SKU", "Name", "Units");
        System.out.println("----------------------------------------------");

        int rank = 1;
        for (TopProductRow r : rows) {
            System.out.printf("%-4d %-10s %-25s %-10d%n",
                    rank++,
                    safe(r.sku()),
                    truncate(safe(r.name()), 25),
                    r.unitsSold() == null ? 0 : r.unitsSold()
            );
        }
    }

    /**
     * Shows products with inventory below threshold.
     */
    private void showLowStock(Scanner scanner) {
        int threshold = readInt(scanner, "Threshold X (show in_stock < X): ", null);
        if (threshold < 0) throw new IllegalArgumentException("Threshold must be >= 0");

        List<LowStockRow> rows = reportService.lowStock(threshold);

        if (rows.isEmpty()) {
            System.out.println("No low stock products.");
            return;
        }

        System.out.println("\nLow stock:");
        System.out.println("----------------------------------------------");
        System.out.printf("%-10s %-25s %-10s%n", "SKU", "Name", "InStock");
        System.out.println("----------------------------------------------");

        for (LowStockRow r : rows) {
            System.out.printf("%-10s %-25s %-10d%n",
                    safe(r.sku()),
                    truncate(safe(r.name()), 25),
                    r.inStock() == null ? 0 : r.inStock()
            );
        }
    }

    /**
     * Shows total revenue (sum of order.total) for PAID orders between dates.
     * Date range is [from, toExclusive), so toExclusive is not included.
     */
    private void showRevenueBetween(Scanner scanner) {
        LocalDate from = readDate(scanner, "From date (YYYY-MM-DD): ");
        LocalDate toExclusive = readDate(scanner, "To date (YYYY-MM-DD) [exclusive]: ");

        validateDateRange(from, toExclusive);

        BigDecimal revenue = reportService.revenueBetween(from, toExclusive);
        if (revenue == null) revenue = BigDecimal.ZERO;

        System.out.printf("Revenue (%s -> %s): %.2f%n", from, toExclusive, revenue);
    }

    // ------------------------
    // Helpers
    // ------------------------

    private void validateDateRange(LocalDate from, LocalDate toExclusive) {
        if (!toExclusive.isAfter(from)) {
            throw new IllegalArgumentException("Invalid range: 'to' must be after 'from'.");
        }
    }

    private LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ex) {
                System.out.println("❌ Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    /**
     * Reads int with optional default.
     * If defaultValue is not null and user enters empty string, default is returned.
     */
    private int readInt(Scanner scanner, String prompt, Integer defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty() && defaultValue != null) return defaultValue;

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
