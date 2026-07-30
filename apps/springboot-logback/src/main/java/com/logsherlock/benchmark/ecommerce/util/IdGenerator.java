package com.logsherlock.benchmark.ecommerce.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Generates deterministic, human-readable benchmark identifiers.
 *
 * <p>Each identifier family has its own monotonic counter seeded at a fixed
 * value, so a given run always produces the same sequence of identifiers. No
 * randomness is involved, which keeps generated log datasets reproducible.</p>
 */
@Component
public class IdGenerator {

    private final AtomicInteger requestCounter = new AtomicInteger(1000);
    private final AtomicInteger traceCounter = new AtomicInteger(1000);
    private final AtomicInteger orderCounter = new AtomicInteger(5000);
    private final AtomicInteger customerCounter = new AtomicInteger(41);
    private final AtomicInteger productCounter = new AtomicInteger(9);
    private final AtomicInteger paymentCounter = new AtomicInteger(7000);
    private final AtomicInteger shipmentCounter = new AtomicInteger(9000);

    /**
     * Returns the next request identifier, for example {@code REQ-1001}.
     *
     * @return a unique request identifier
     */
    public String nextRequestId() {
        return "REQ-" + requestCounter.incrementAndGet();
    }

    /**
     * Returns the next trace identifier, for example {@code TRACE-1001}.
     *
     * @return a unique trace identifier
     */
    public String nextTraceId() {
        return "TRACE-" + traceCounter.incrementAndGet();
    }

    /**
     * Returns the next order identifier, for example {@code ORDER-5001}.
     *
     * @return a unique order identifier
     */
    public String nextOrderId() {
        return "ORDER-" + orderCounter.incrementAndGet();
    }

    /**
     * Returns the next customer identifier, for example {@code CUSTOMER-42}.
     *
     * @return a unique customer identifier
     */
    public String nextCustomerId() {
        return "CUSTOMER-" + customerCounter.incrementAndGet();
    }

    /**
     * Returns the next product identifier, for example {@code PRODUCT-10}.
     *
     * @return a unique product identifier
     */
    public String nextProductId() {
        return "PRODUCT-" + productCounter.incrementAndGet();
    }

    /**
     * Returns the next payment identifier, for example {@code PAYMENT-7001}.
     *
     * @return a unique payment identifier
     */
    public String nextPaymentId() {
        return "PAYMENT-" + paymentCounter.incrementAndGet();
    }

    /**
     * Returns the next shipment identifier, for example {@code SHIPMENT-9001}.
     *
     * @return a unique shipment identifier
     */
    public String nextShipmentId() {
        return "SHIPMENT-" + shipmentCounter.incrementAndGet();
    }
}
