package com.logsherlock.benchmark.ecommerce.web;

/**
 * Thrown by a controller when a requested entity does not exist.
 *
 * <p>Translated into a {@code 404} response by {@link GlobalExceptionHandler}.</p>
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the message returned to the caller
     */
    public NotFoundException(String message) {
        super(message);
    }
}
