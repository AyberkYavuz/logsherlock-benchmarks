package com.logsherlock.benchmark.ecommerce.web.dto;

/**
 * Error payload returned for every failed request.
 *
 * @param status  the HTTP status code
 * @param error   the HTTP reason phrase
 * @param message the human-readable cause
 */
public record ErrorResponse(int status, String error, String message) {
}
