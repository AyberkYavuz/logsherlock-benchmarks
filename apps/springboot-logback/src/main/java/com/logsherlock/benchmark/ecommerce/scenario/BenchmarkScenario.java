package com.logsherlock.benchmark.ecommerce.scenario;

/**
 * The set of benchmark scenarios the application can execute.
 *
 * <p>A scenario selects which path an order takes through the business services
 * and therefore which sequence of log records a run produces. The outcome depends
 * only on the selected value: there is no randomness and no time-based
 * behaviour, so the same scenario always yields the same log sequence.</p>
 *
 * <p>Scenarios are executed by
 * {@code BenchmarkWorkflowService#runScenario(BenchmarkScenario)}.</p>
 */
public enum BenchmarkScenario {

    /** The healthy baseline: the order is paid, shipped and completed. */
    NORMAL,

    /** Requested stock exceeds what is available, so the order is cancelled. */
    OUT_OF_STOCK,

    /** The payment is declined, the reservation is rolled back and the order is cancelled. */
    PAYMENT_DECLINED,

    /** The order fails validation and is rejected before any fulfilment step. */
    INVALID_ORDER,

    /** The order completes, but its shipment is delayed. */
    SHIPPING_DELAY
}
