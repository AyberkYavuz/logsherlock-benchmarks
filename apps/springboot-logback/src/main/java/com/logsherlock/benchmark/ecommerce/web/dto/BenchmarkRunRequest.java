package com.logsherlock.benchmark.ecommerce.web.dto;

import jakarta.validation.constraints.NotNull;

import com.logsherlock.benchmark.ecommerce.scenario.BenchmarkScenario;

/**
 * Request body of a benchmark run.
 *
 * <p>An unknown scenario name is rejected while the body is being read; a missing
 * one is rejected by validation. Both yield {@code 400}.</p>
 *
 * @param scenario the scenario to execute
 */
public record BenchmarkRunRequest(@NotNull(message = "scenario is required") BenchmarkScenario scenario) {
}
