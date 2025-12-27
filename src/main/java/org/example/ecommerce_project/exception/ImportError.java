package org.example.ecommerce_project.exception;

public record ImportError(int row, String message, String rawLine) {}
