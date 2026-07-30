package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Enumerates the logical business services that make up the e-commerce benchmark.
 *
 * <p>A service represents a coarse-grained business capability (for example order
 * management or payment processing). Every emitted log record is attributed to
 * exactly one {@code ServiceName}, allowing generated datasets to be partitioned
 * and analysed per service.</p>
 *
 * <p>These values form part of the canonical logging vocabulary and are consumed
 * by later phases (log context, structured logger and scenario engine).</p>
 */
public enum ServiceName {

    /** The benchmark harness itself (lifecycle, scenario control, dataset generation). */
    BENCHMARK,

    /** Order intake, validation and lifecycle management. */
    ORDER,

    /** Stock availability, reservation and release. */
    INVENTORY,

    /** Payment authorization and settlement. */
    PAYMENT,

    /** Fraud screening of incoming orders. */
    FRAUD,

    /** Shipment preparation and dispatch. */
    SHIPPING,

    /** Customer and system notifications. */
    NOTIFICATION
}
