package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Category;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.CategoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    /**
     * Skapar en ny kategori om namnet är giltigt och inte redan finns
     * @param category kategorins namn
     * @return den sparade kategorin
     */
    @Transactional
    public Category createCategory(String category) {
        if (category == null || category.isBlank()) {
            throw  AppException.validation("Category name must not be blank");
        }

        categoryRepo.findByNameIgnoreCase(category)
                .ifPresent(existing -> {
                    throw AppException.businessRule("Category already exists with name: " + existing.getName());
                });

        return categoryRepo.save(new Category(category));
    }

    /**
     * Hämtar en kategori baserat på namn (case-insensitive)
     * @param name kategorins namn
     * @return kategori om den hittas
     */
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        if (name == null || name.isBlank()) {
            throw AppException.validation("Category name must not be blank");
        }
        return categoryRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> AppException.notFound("Category not found with name: " + name));
    }

    /**
     * Hämtar alla kategorier i databasen
     * @return lista med kategorier
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

}
