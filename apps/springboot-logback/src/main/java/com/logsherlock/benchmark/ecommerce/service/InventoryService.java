package com.logsherlock.benchmark.ecommerce.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogContext;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogger;
import com.logsherlock.benchmark.ecommerce.logging.ComponentName;
import com.logsherlock.benchmark.ecommerce.logging.LogEvent;
import com.logsherlock.benchmark.ecommerce.logging.ServiceName;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;

/**
 * Stock reservation and release operations of the benchmark.
 *
 * <p>Owns the {@code availableQuantity} of the seeded {@link Product} entities in
 * {@link BenchmarkState}. Reservation is plain arithmetic against the stored
 * stock level, so a shortage is a real consequence of the current state rather
 * than an injected failure — a scenario that wants a shortage orders more units
 * than the seeded catalogue holds.</p>
 */
@Service
public class InventoryService {

    private final BenchmarkLogger benchmarkLogger;
    private final BenchmarkState benchmarkState;

    /**
     * Creates the inventory service.
     *
     * @param benchmarkLogger the structured logging abstraction
     * @param benchmarkState  the in-memory store holding all benchmark entities
     */
    public InventoryService(BenchmarkLogger benchmarkLogger, BenchmarkState benchmarkState) {
        this.benchmarkLogger = benchmarkLogger;
        this.benchmarkState = benchmarkState;
    }

    /**
     * Reserves the order's quantity from the product's available stock.
     *
     * <p>Emits {@link LogEvent#INVENTORY_CHECK_STARTED} first, then either
     * {@link LogEvent#INVENTORY_RESERVED} after decrementing the stock or
     * {@link LogEvent#INVENTORY_SHORTAGE} when the available quantity is lower
     * than the ordered quantity. The product's stock is only touched on
     * success.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order whose product and quantity are reserved
     * @return {@code true} if the stock was reserved
     * @throws IllegalArgumentException if the order references an unknown product
     */
    public boolean reserveInventory(String reqId, String traceId, Order order) {
        benchmarkLogger.log(LogEvent.INVENTORY_CHECK_STARTED, context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Checking stock for " + order.getQuantity() + " x " + order.getProductId());

        Product product = requireProduct(order);
        if (product.getAvailableQuantity() < order.getQuantity()) {
            benchmarkLogger.log(LogEvent.INVENTORY_SHORTAGE, context(reqId, traceId, order, ComponentName.STORE),
                    "Insufficient stock for " + product.getProductId()
                            + ": requested " + order.getQuantity()
                            + ", available " + product.getAvailableQuantity());
            return false;
        }

        product.setAvailableQuantity(product.getAvailableQuantity() - order.getQuantity());
        benchmarkLogger.log(LogEvent.INVENTORY_RESERVED, context(reqId, traceId, order, ComponentName.STORE),
                "Reserved " + order.getQuantity() + " x " + product.getProductId()
                        + " for order " + order.getOrderId()
                        + ", " + product.getAvailableQuantity() + " remaining");
        return true;
    }

    /**
     * Returns the order's quantity to the product's available stock and emits
     * {@link LogEvent#INVENTORY_RELEASED}.
     *
     * <p>Called by orchestrating code when a later step of the order flow fails
     * after a successful reservation.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order whose reservation is released
     * @return the updated product
     * @throws IllegalArgumentException if the order references an unknown product
     */
    public Product releaseInventory(String reqId, String traceId, Order order) {
        Product product = requireProduct(order);
        product.setAvailableQuantity(product.getAvailableQuantity() + order.getQuantity());
        benchmarkLogger.log(LogEvent.INVENTORY_RELEASED, context(reqId, traceId, order, ComponentName.STORE),
                "Released " + order.getQuantity() + " x " + product.getProductId()
                        + " from order " + order.getOrderId()
                        + ", " + product.getAvailableQuantity() + " available");
        return product;
    }

    /**
     * Returns every seeded product, ordered by identifier.
     *
     * <p>The returned list is an immutable copy: callers cannot reach the store
     * through it.</p>
     *
     * @return all products
     */
    public List<Product> findAllProducts() {
        return benchmarkState.getProducts().values().stream()
                .sorted(Comparator.comparing(Product::getProductId))
                .toList();
    }

    /**
     * Looks up a single product.
     *
     * @param productId the product to look up
     * @return the product, or {@link Optional#empty()} if there is none
     */
    public Optional<Product> findProductById(String productId) {
        return productId == null
                ? Optional.empty()
                : Optional.ofNullable(benchmarkState.getProducts().get(productId));
    }

    private Product requireProduct(Order order) {
        String productId = order.getProductId();
        Product product = productId == null ? null : benchmarkState.getProducts().get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Unknown product: " + productId);
        }
        return product;
    }

    private BenchmarkLogContext context(String reqId, String traceId, Order order, ComponentName component) {
        return BenchmarkLogContext.builder()
                .reqId(reqId)
                .traceId(traceId)
                .scenario(benchmarkState.getCurrentScenario().name())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .paymentId(order.getPaymentId())
                .shipmentId(order.getShipmentId())
                .service(ServiceName.INVENTORY)
                .component(component)
                .build();
    }
}
