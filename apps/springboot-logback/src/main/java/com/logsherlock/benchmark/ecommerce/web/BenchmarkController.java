package com.logsherlock.benchmark.ecommerce.web;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logsherlock.benchmark.ecommerce.service.BenchmarkWorkflowService;
import com.logsherlock.benchmark.ecommerce.web.dto.BenchmarkRunRequest;
import com.logsherlock.benchmark.ecommerce.web.dto.BenchmarkRunResponse;

/**
 * Triggers a benchmark scenario over HTTP.
 *
 * <p>The endpoint is a thin trigger: it delegates straight to
 * {@link BenchmarkWorkflowService#runScenario} and therefore produces exactly the
 * same workflow, and exactly the same log records, as the startup smoke test. It
 * duplicates no orchestration and emits no log records of its own.</p>
 */
@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private static final String COMPLETED = "completed";

    private final BenchmarkWorkflowService benchmarkWorkflowService;

    /**
     * Creates the controller.
     *
     * @param benchmarkWorkflowService the only workflow orchestrator
     */
    public BenchmarkController(BenchmarkWorkflowService benchmarkWorkflowService) {
        this.benchmarkWorkflowService = benchmarkWorkflowService;
    }

    /**
     * Runs one benchmark scenario synchronously.
     *
     * @param request the scenario to execute
     * @return the executed scenario and its run status
     */
    @PostMapping("/run")
    public BenchmarkRunResponse run(@Valid @RequestBody BenchmarkRunRequest request) {
        benchmarkWorkflowService.runScenario(request.scenario());
        return new BenchmarkRunResponse(request.scenario().name(), COMPLETED);
    }
}
