package com.logsherlock.benchmark.ecommerce.web.dto;

/**
 * Result of a benchmark run.
 *
 * @param scenario the scenario that was executed
 * @param status   the run status, {@code "completed"} once the workflow returned
 */
public record BenchmarkRunResponse(String scenario, String status) {
}
