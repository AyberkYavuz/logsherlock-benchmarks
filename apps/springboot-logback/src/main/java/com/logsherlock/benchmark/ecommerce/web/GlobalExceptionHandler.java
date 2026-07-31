package com.logsherlock.benchmark.ecommerce.web;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.logsherlock.benchmark.ecommerce.scenario.BenchmarkScenario;
import com.logsherlock.benchmark.ecommerce.web.dto.ErrorResponse;

/**
 * Translates exceptions raised by the REST layer into JSON error payloads.
 *
 * <p>Deliberately silent: it emits no log records, so the benchmark log stream
 * stays exactly as the business services produced it.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles a request for an entity that does not exist.
     *
     * @param exception the exception raised by the controller
     * @return a {@code 404} payload
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Handles a request body that failed validation, for example a benchmark run
     * without a scenario.
     *
     * @param exception the validation failure
     * @return a {@code 400} payload
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request body");
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles a request body that could not be read at all, for example a benchmark
     * run naming an unknown scenario.
     *
     * @param exception the read failure
     * @return a {@code 400} payload
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed request body; supported scenarios are "
                        + Arrays.toString(BenchmarkScenario.values()));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
