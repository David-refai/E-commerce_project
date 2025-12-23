package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Category;
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

    @Transactional
    public Category createCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }

        categoryRepo.findByNameIgnoreCase(category.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category already exists with name: " + category.getName());
                });

        return categoryRepo.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        return categoryRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Transactional
    public Category updateCategory(Long id, Category updated) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (updated == null) {
            throw new IllegalArgumentException("Updated category must not be null");
        }

        Category existing = getCategoryById(id);

        if (updated.getName() != null && !updated.getName().isBlank()) {
            categoryRepo.findByNameIgnoreCase(updated.getName())
                    .ifPresent(other -> {
                        if (other.getId() != null && !other.getId().equals(existing.getId())) {
                            throw new IllegalArgumentException("Category already exists with name: " + updated.getName());
                        }
                    });
            existing.setName(updated.getName());
        }

        return categoryRepo.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category existing = getCategoryById(id);
        categoryRepo.delete(existing);
    }
}
