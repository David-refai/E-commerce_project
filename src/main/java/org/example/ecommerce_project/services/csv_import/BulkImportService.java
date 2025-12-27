package org.example.ecommerce_project.services.csv_import;
import org.apache.commons.csv.CSVRecord;
import org.example.ecommerce_project.entity.Category;
import org.example.ecommerce_project.entity.Customer;
import org.example.ecommerce_project.entity.Product;
import org.example.ecommerce_project.repository.CategoryRepo;
import org.example.ecommerce_project.repository.CustomerRepo;
import org.example.ecommerce_project.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkImportService {

    private final ProductRepo productRepository;
    private final CategoryRepo categoryRepository;
    private final CustomerRepo customerRepository;

    public BulkImportService(ProductRepo productRepository,
                             CategoryRepo categoryRepository,
                             CustomerRepo customerRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public ImportReport importCategories(Path csvPath, boolean strict) {
        var records = CsvUtil.read(csvPath);
        ImportReport report = new ImportReport();

        int rowNum = 1; // header is skipped; we count data rows
        for (CSVRecord r : records) {
            report.incTotal();
            String raw = r.toString();

            try {
                String name = required(r, "name");
                upsertCategory(name);
                report.incSuccess();

            } catch (Exception ex) {
                report.addError(rowNum, ex.getMessage(), raw);
                if (strict) throw ex;
            }
            rowNum++;
        }
        return report;
    }

    @Transactional
    public ImportReport importCustomers(Path csvPath, boolean strict) {
        var records = CsvUtil.read(csvPath);
        ImportReport report = new ImportReport();

        int rowNum = 1;
        for (CSVRecord r : records) {
            report.incTotal();
            String raw = r.toString();

            try {
                String email = required(r, "email");
                String name  = required(r, "name");

                upsertCustomer(email, name);
                report.incSuccess();

            } catch (Exception ex) {
                report.addError(rowNum, ex.getMessage(), raw);
                if (strict) throw ex;
            }
            rowNum++;
        }
        return report;
    }

    @Transactional
    public ImportReport importProducts(Path csvPath, boolean strict) {
        var records = CsvUtil.read(csvPath);
        ImportReport report = new ImportReport();

        int rowNum = 1;
        for (CSVRecord r : records) {
            report.incTotal();
            String raw = r.toString();

            try {
                String sku = required(r, "sku");
                String name = required(r, "name");
                String description = optional(r, "description", "");
                BigDecimal price = parseMoney(required(r, "price"));
                boolean active = Boolean.parseBoolean(optional(r, "active", "true"));

                // categories: "Electronics;Home"
                Set<Category> categories = parseCategories(optional(r, "categories", ""));

                upsertProduct(sku, name, description, price, active, categories);
                report.incSuccess();

            } catch (Exception ex) {
                report.addError(rowNum, ex.getMessage(), raw);
                if (strict) throw ex;
            }
            rowNum++;
        }
        return report;
    }

    // ----------------- helpers -----------------

    private Category upsertCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name.trim())));
    }

    private Customer upsertCustomer(String email, String name) {
        return customerRepository.findByEmailIgnoreCase(email)
                .map(existing -> {
                    existing.setName(name.trim());
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setEmail(email.trim().toLowerCase());
                    c.setName(name.trim());
                    return customerRepository.save(c);
                });
    }

    private Product upsertProduct(String sku, String name, String description,
                                  BigDecimal price, boolean active, Set<Category> categories) {

        return productRepository.findBySku(sku)
                .map(p -> {
                    p.setName(name.trim());
                    p.setDescription(description);
                    p.setPrice(price);
                    p.setActive(active);
                    p.setCategories(categories);
                    return productRepository.save(p);
                })
                .orElseGet(() -> {
                    Product p = new Product();
                    p.setSku(sku.trim());
                    p.setName(name.trim());
                    p.setDescription(description);
                    p.setPrice(price);
                    p.setActive(active);
                    p.setCategories(categories);
                    return productRepository.save(p);
                });
    }

    private Set<Category> parseCategories(String categoriesCell) {
        if (categoriesCell == null || categoriesCell.isBlank()) return Set.of();

        return Arrays.stream(categoriesCell.split(";"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::upsertCategory)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String required(CSVRecord r, String header) {
        String v = r.get(header);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Missing required field: " + header);
        return v.trim();
    }

    private static String optional(CSVRecord r, String header, String defaultValue) {
        try {
            String v = r.get(header);
            return (v == null || v.isBlank()) ? defaultValue : v.trim();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static BigDecimal parseMoney(String s) {
        try {
            // tillåt "199.00" och "199,00"
            String normalized = s.replace(",", ".");
            return new BigDecimal(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid price: " + s);
        }
    }
}
