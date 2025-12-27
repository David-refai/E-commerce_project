package org.example.ecommerce_project.cli;

import org.example.ecommerce_project.services.csv_import.BulkImportService;
import org.example.ecommerce_project.services.csv_import.ImportReport;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ImportCli {

    private final BulkImportService bulkImportService;

    // keep your current directory
    private final Path importDir = Path.of("src", "main", "java", "org", "example", "ecommerce_project", "data", "imports");

    public ImportCli(BulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    /** Call this from main menu */
    public void showMenu(Scanner sc) {
        System.out.println("Import directory: " + importDir.toAbsolutePath());

        boolean running = true;
        while (running) {
            System.out.println("\n=== CSV Import ===");
            System.out.println("1) Import Products");
            System.out.println("2) Import Categories");
            System.out.println("3) Import Customers");
            System.out.println("0) Back");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> importProducts(sc);
                case "2" -> importCategories(sc);
                case "3" -> importCustomers(sc);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public void importProducts(Scanner sc) {
        importAny(sc,
                "products",
                FileKind.PRODUCTS,
                new String[]{"sku", "name", "price"}, // minimal required
                bulkImportService::importProducts
        );
    }

    public void importCategories(Scanner sc) {
        importAny(sc,
                "categories",
                FileKind.CATEGORIES,
                new String[]{"name"},
                bulkImportService::importCategories
        );
    }

    public void importCustomers(Scanner sc) {
        importAny(sc,
                "customers",
                FileKind.CUSTOMERS,
                new String[]{"email", "name"},
                bulkImportService::importCustomers
        );
    }

    // ----------------- core -----------------

    @FunctionalInterface
    private interface ImportFn {
        ImportReport run(Path path, boolean strict);
    }

    private enum FileKind { PRODUCTS, CATEGORIES, CUSTOMERS }

    private void importAny(Scanner sc, String label, FileKind kind, String[] requiredHeaders, ImportFn fn) {
        Path csv = chooseCsvFile(sc, importDir, kind);
        if (csv == null) {
            System.out.println("Import cancelled.");
            return;
        }

        // Prevent: "Mapping for sku not found..."
        try {
            validateCsvHeaders(csv, requiredHeaders);
        } catch (IllegalArgumentException ex) {
            System.out.println("Wrong CSV selected for " + label + ": " + ex.getMessage());
            return;
        }

        boolean strict = askYesNo(sc, "Strict mode? (y/n): ", false);

        ImportReport report = fn.run(csv, strict);
        printReport(label, report);
    }

    // ----------------- file picking -----------------

    private Path chooseCsvFile(Scanner sc, Path dir, FileKind kind) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.out.println("Folder not found: " + dir.toAbsolutePath());
            return null;
        }

        List<Path> csvFiles;
        try (Stream<Path> s = Files.list(dir)) {
            csvFiles = s
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                    .filter(p -> matchesKind(p.getFileName().toString(), kind))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Failed to list files: " + e.getMessage());
            return null;
        }

        if (csvFiles.isEmpty()) {
            System.out.println("No matching CSV files found for " + kind.name().toLowerCase()
                    + " in: " + dir.toAbsolutePath());
            return null;
        }

        System.out.println("\nAvailable CSV files (" + kind.name().toLowerCase() + "):");
        for (int i = 0; i < csvFiles.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, csvFiles.get(i).getFileName());
        }
        System.out.println("0) Cancel");

        int choice = readInt(sc, "Choose file number: ", 0, csvFiles.size());
        if (choice == 0) return null;

        return csvFiles.get(choice - 1);
    }

    private boolean matchesKind(String filename, FileKind kind) {
        String n = filename.toLowerCase();
        return switch (kind) {
            case PRODUCTS -> n.contains("product");
            case CATEGORIES -> n.contains("categor");  // matches category/categories
            case CUSTOMERS -> n.contains("customer");
        };
    }

    // ----------------- header check -----------------

    private void validateCsvHeaders(Path csvPath, String[] requiredHeaders) {
        String headerLine;
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            headerLine = br.readLine();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file: " + csvPath.getFileName());
        }

        if (headerLine == null || headerLine.isBlank()) {
            throw new IllegalArgumentException("Empty CSV (missing header row).");
        }

        // Split header line and normalize each header:
        // - trim spaces
        // - remove BOM
        // - remove surrounding quotes "..."
        // - lowercase
        Set<String> headers = Arrays.stream(headerLine.split(","))
                .map(String::trim)
                .map(h -> h.replace("\uFEFF", "")) // BOM
                .map(h -> stripQuotes(h))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<String> missing = new ArrayList<>();
        for (String req : requiredHeaders) {
            if (!headers.contains(req.toLowerCase())) missing.add(req);
        }

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing header(s): " + String.join(", ", missing) +
                            ". Found: " + String.join(", ", headers)
            );
        }
    }

    private static String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }


    // ----------------- UI helpers -----------------

    private int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String in = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(in);
                if (v < min || v > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private boolean askYesNo(Scanner sc, String prompt, boolean defaultVal) {
        while (true) {
            System.out.print(prompt);
            String in = sc.nextLine().trim().toLowerCase();
            if (in.isEmpty()) return defaultVal;
            if (in.equals("y") || in.equals("yes")) return true;
            if (in.equals("n") || in.equals("no")) return false;
            System.out.println("Please answer y/n.");
        }
    }

    private void printReport(String label, ImportReport r) {
        System.out.println("Imported " + label + ": " + r.success() + "/" + r.total()
                + " (failed: " + r.failed() + ")");

        int shown = 0;
        for (Object err : r.errors()) {
            System.out.println(err);
            shown++;
            if (shown >= 10) break;
        }

        if (r.failed() > 10) {
            System.out.println("...and " + (r.failed() - 10) + " more errors");
        }
    }
}
