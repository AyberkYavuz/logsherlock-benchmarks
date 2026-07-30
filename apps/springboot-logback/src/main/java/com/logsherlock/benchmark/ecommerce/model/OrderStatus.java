package com.logsherlock.benchmark.ecommerce.model;

/**
 * Lifecycle states of an {@link Order} within the benchmark.
 *
 * <p>The set is intentionally small and captures only the transitions that later
 * phases exercise when generating logs.</p>
 */
public enum OrderStatus {

    /** The order has been created but not yet validated. */
    CREATED,

    /** The order has passed validation and is ready for fulfilment. */
    VALIDATED,

    /** The order has been fully processed. */
    COMPLETED,

    /** The order was cancelled before completion. */
    CANCELLED,

    /** The order failed during processing. */
    FAILED
}
