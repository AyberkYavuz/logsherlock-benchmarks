package com.logsherlock.benchmark.ecommerce.model;

/**
 * Lifecycle states of a {@link Shipment} within the benchmark.
 *
 * <p>The set is intentionally small and captures only the transitions that later
 * phases exercise when generating logs.</p>
 */
public enum ShipmentStatus {

    /** The shipment has been created but not yet dispatched. */
    PENDING,

    /** The shipment has been handed to the carrier. */
    DISPATCHED,

    /** The shipment has reached the customer. */
    DELIVERED,

    /** The shipment could not be created or dispatched. */
    FAILED
}
