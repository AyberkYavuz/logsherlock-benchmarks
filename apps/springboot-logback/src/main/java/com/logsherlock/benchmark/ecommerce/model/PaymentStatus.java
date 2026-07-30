package com.logsherlock.benchmark.ecommerce.model;

/**
 * Lifecycle states of a {@link Payment} within the benchmark.
 *
 * <p>The set is intentionally small and captures only the transitions that later
 * phases exercise when generating logs.</p>
 */
public enum PaymentStatus {

    /** The payment has been created but not yet authorized. */
    PENDING,

    /** The payment was authorized and settled successfully. */
    AUTHORIZED,

    /** The payment attempt was declined or errored. */
    FAILED,

    /** The payment was refunded after settlement. */
    REFUNDED
}
