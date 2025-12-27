package org.example.ecommerce_project.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class ErrorHandlerCli {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerCli.class);

    private final boolean debug;

    public ErrorHandlerCli(boolean debug) {
        this.debug = debug;
    }


// runWithHandling: Runs the given action safely. It catches AppException, database errors, and any other exception,
// prints a clear message to the user, and logs full stack traces only when debug is enabled.

    public void runWithHandling(Runnable action) {
        try {
            action.run();

        } catch (AppException ex) {
            handleAppException(ex);

        } catch (ConstraintViolationException ex) {
            System.out.println("Validation error:");
            for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
                System.out.println("- ❌" + v.getPropertyPath() + ": " + v.getMessage());
            }

            if (debug) {
                log.error("Constraint validation error in CLI action", ex);
            }

        } catch (DataAccessException ex) {
            Throwable cause = ex.getMostSpecificCause();

            String msg;
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                msg = cause.getMessage();
            } else if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                msg = ex.getMessage();
            } else {
                msg = "Database error (no details available).";
            }

            System.out.println("Database error: " + msg);

            if (debug) {
                log.error("Database error in CLI action", ex);
            }

        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());

            if (debug) {
                log.error("Unexpected error in CLI action", ex);
            }
        }
    }

// handleAppException: Handles AppException by type (validation, not found, business rule, etc.),
// prints the right message for the user, and logs extra details in debug mode.

    private void handleAppException(AppException ex) {
        switch (ex.getType()) {
            case VALIDATION -> System.out.println("Validation error: " + ex.getMessage());
            case NOT_FOUND -> System.out.println("Not found: " + ex.getMessage());
            case BUSINESS_RULE -> System.out.println("Business rule error: " + ex.getMessage());
            default -> System.out.println("Error: " + ex.getMessage());
        }

        if (debug) {
            log.debug("AppException in CLI action: type={}, message={}", ex.getType(), ex.getMessage(), ex);
        }
    }
}
