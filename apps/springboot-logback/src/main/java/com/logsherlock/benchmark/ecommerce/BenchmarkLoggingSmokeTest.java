package com.logsherlock.benchmark.ecommerce;

import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.service.BenchmarkWorkflowService;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Temporary startup smoke test that runs one benchmark workflow.
 *
 * <p>It is a thin entry point only: it delegates to
 * {@link BenchmarkWorkflowService#runNormalScenario()} and then prints the
 * resulting {@link BenchmarkState} counts and the stock level of the ordered
 * product. It contains no orchestration and no business logic; all benchmark logs
 * are emitted by the business services.</p>
 *
 * <p>This class is intended to be removed once the workflow has been
 * verified.</p>
 */
@Component
public class BenchmarkLoggingSmokeTest implements CommandLineRunner {

    private final BenchmarkWorkflowService benchmarkWorkflowService;
    private final BenchmarkState benchmarkState;

    public BenchmarkLoggingSmokeTest(
            BenchmarkWorkflowService benchmarkWorkflowService,
            BenchmarkState benchmarkState) {
        this.benchmarkWorkflowService = benchmarkWorkflowService;
        this.benchmarkState = benchmarkState;
    }

    @Override
    public void run(String... args) {
        Order order = benchmarkWorkflowService.runNormalScenario();

        System.out.println("========== ORDER ==========");
        System.out.println(order);

        System.out.println("========== PRODUCT ==========");
        System.out.println(benchmarkState.getProducts().get(order.getProductId()));

        System.out.println("========== STATE ==========");
        System.out.println("Orders: " + benchmarkState.getOrders().size());
        System.out.println("Payments: " + benchmarkState.getPayments().size());
        System.out.println("Shipments: " + benchmarkState.getShipments().size());
    }
}
