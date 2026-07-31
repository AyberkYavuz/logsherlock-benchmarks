package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Canonical taxonomy of business and lifecycle events emitted by the benchmark.
 *
 * <p>Every log record produced by the benchmark is tagged with exactly one
 * {@code LogEvent}. This provides a strongly typed, stable vocabulary that
 * downstream tooling (dataset generation, LogSherlock investigations) can rely
 * on for filtering, grouping and correlation.</p>
 *
 * <p>Each event carries a {@link LogLevel default severity} that reflects its
 * typical meaning (for example an order cancellation is a {@code WARN} while a
 * payment timeout is an {@code ERROR}). The default may be overridden by callers
 * when a specific occurrence warrants a different severity.</p>
 */
public enum LogEvent {

    // ---------------------------------------------------------------------
    // Application
    // ---------------------------------------------------------------------

    /** The application has begun its startup sequence. */
    APPLICATION_STARTING(LogLevel.INFO),

    /** The application has completed startup and is ready. */
    APPLICATION_STARTED(LogLevel.INFO),

    /** The application has begun its shutdown sequence. */
    APPLICATION_STOPPING(LogLevel.INFO),

    /** The application has fully stopped. */
    APPLICATION_STOPPED(LogLevel.INFO),

    // ---------------------------------------------------------------------
    // Order
    // ---------------------------------------------------------------------

    /** An inbound order has been received. */
    ORDER_RECEIVED(LogLevel.INFO),

    /** An order has passed validation. */
    ORDER_VALIDATED(LogLevel.INFO),

    /** An order has been rejected during validation. */
    ORDER_VALIDATION_FAILED(LogLevel.WARN),

    /** An order has been persisted / created. */
    ORDER_CREATED(LogLevel.INFO),

    /** An order has reached its completed state. */
    ORDER_COMPLETED(LogLevel.INFO),

    /** An order has been cancelled. */
    ORDER_CANCELLED(LogLevel.WARN),

    // ---------------------------------------------------------------------
    // Inventory
    // ---------------------------------------------------------------------

    /** An inventory availability check has started. */
    INVENTORY_CHECK_STARTED(LogLevel.INFO),

    /** Stock has been reserved for an order. */
    INVENTORY_RESERVED(LogLevel.INFO),

    /** Insufficient stock is available to fulfil an order. */
    INVENTORY_SHORTAGE(LogLevel.WARN),

    /** Previously reserved stock has been released. */
    INVENTORY_RELEASED(LogLevel.INFO),

    // ---------------------------------------------------------------------
    // Payment
    // ---------------------------------------------------------------------

    /** A payment has been requested from the provider. */
    PAYMENT_REQUESTED(LogLevel.INFO),

    /** A payment has been authorized. */
    PAYMENT_AUTHORIZED(LogLevel.INFO),

    /** A payment has been declined by the provider. */
    PAYMENT_DECLINED(LogLevel.WARN),

    /** A payment request timed out. */
    PAYMENT_TIMEOUT(LogLevel.ERROR),

    /** The payment provider is unavailable. */
    PAYMENT_PROVIDER_UNAVAILABLE(LogLevel.ERROR),

    // ---------------------------------------------------------------------
    // Fraud
    // ---------------------------------------------------------------------

    /** A fraud screening check has started. */
    FRAUD_CHECK_STARTED(LogLevel.INFO),

    /** A fraud screening check has passed. */
    FRAUD_CHECK_PASSED(LogLevel.INFO),

    /** A fraud screening check has failed. */
    FRAUD_CHECK_FAILED(LogLevel.ERROR),

    // ---------------------------------------------------------------------
    // Shipping
    // ---------------------------------------------------------------------

    /** Shipment preparation has started. */
    SHIPMENT_PREPARATION_STARTED(LogLevel.INFO),

    /** A shipment has been created. */
    SHIPMENT_CREATED(LogLevel.INFO),

    /** A shipment has been created but its dispatch is delayed. */
    SHIPMENT_DELAYED(LogLevel.WARN),

    /** The shipping service is unavailable. */
    SHIPPING_SERVICE_UNAVAILABLE(LogLevel.ERROR),

    /** A shipment has been completed. */
    SHIPMENT_COMPLETED(LogLevel.INFO),

    // ---------------------------------------------------------------------
    // Notification
    // ---------------------------------------------------------------------

    /** A notification has been sent successfully. */
    NOTIFICATION_SENT(LogLevel.INFO),

    /** A notification failed to send. */
    NOTIFICATION_FAILED(LogLevel.WARN),

    // ---------------------------------------------------------------------
    // Benchmark
    // ---------------------------------------------------------------------

    /** The active benchmark scenario has changed. */
    SCENARIO_CHANGED(LogLevel.INFO),

    /** The benchmark state has been reset. */
    BENCHMARK_RESET(LogLevel.INFO),

    /** Dataset generation has started. */
    DATASET_GENERATION_STARTED(LogLevel.INFO),

    /** Dataset generation has finished. */
    DATASET_GENERATION_FINISHED(LogLevel.INFO);

    private final LogLevel level;

    LogEvent(LogLevel level) {
        this.level = level;
    }

    /**
     * Returns the default severity associated with this event.
     *
     * @return the default {@link LogLevel} for this event
     */
    public LogLevel getLevel() {
        return level;
    }
}
