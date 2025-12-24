package org.example.ecommerce_project.exception;

public class AppException extends RuntimeException {

    public enum ErrorType {
        VALIDATION,
        NOT_FOUND,
        BUSINESS_RULE
    }

    private final ErrorType type;

    public AppException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    // Helper factory methods for cleaner code
    public static AppException validation(String message) {
        return new AppException(ErrorType.VALIDATION, message);
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorType.NOT_FOUND, message);
    }

    public static AppException businessRule(String message) {
        return new AppException(ErrorType.BUSINESS_RULE, message);
    }
}
