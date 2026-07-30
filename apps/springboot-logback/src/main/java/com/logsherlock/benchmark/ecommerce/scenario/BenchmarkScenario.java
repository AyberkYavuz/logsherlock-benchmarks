package com.logsherlock.benchmark.ecommerce.scenario;

/**
 * The set of benchmark scenarios the application can operate under.
 *
 * <p>A scenario determines the behaviour (and therefore the shape of the logs)
 * that later phases produce. The active scenario will eventually be switched at
 * runtime through a {@code /scenario} endpoint; this phase only defines the
 * vocabulary.</p>
 */
public enum BenchmarkScenario {

    /** The healthy baseline: everything succeeds. */
    NORMAL,

    /** The payment provider is unavailable. */
    PAYMENT_PROVIDER_DOWN,

    /** Payment authorization exceeds its timeout. */
    PAYMENT_TIMEOUT,

    /** Requested stock is not available. */
    INVENTORY_SHORTAGE,

    /** The shipping service is unavailable. */
    SHIPPING_SERVICE_DOWN,

    /** Fraud detection errors out during screening. */
    FRAUD_DETECTION_FAILURE
}
