package com.logsherlock.benchmark.ecommerce.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogContext;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogger;
import com.logsherlock.benchmark.ecommerce.logging.ComponentName;
import com.logsherlock.benchmark.ecommerce.logging.LogEvent;
import com.logsherlock.benchmark.ecommerce.logging.ServiceName;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.OrderStatus;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Order lifecycle operations of the benchmark.
 *
 * <p>Owns the {@link Order} entity: intake, validation and the terminal states
 * (completed, cancelled, failed). Every state transition writes the new status to
 * the shared {@link BenchmarkState} and emits the matching {@link LogEvent}.</p>
 *
 * <p>The methods are deliberately small, synchronous and free of any decision
 * making: they never choose whether an order should succeed or fail. Callers
 * (REST controllers and, later, benchmark scenarios) orchestrate them and decide
 * which path an order takes.</p>
 */
@Service
public class OrderService {

    private final BenchmarkLogger benchmarkLogger;
    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    /**
     * Creates the order service.
     *
     * @param benchmarkLogger the structured logging abstraction
     * @param benchmarkState  the in-memory store holding all benchmark entities
     * @param idGenerator     the deterministic identifier source
     */
    public OrderService(BenchmarkLogger benchmarkLogger,
                        BenchmarkState benchmarkState,
                        IdGenerator idGenerator) {
        this.benchmarkLogger = benchmarkLogger;
        this.benchmarkState = benchmarkState;
        this.idGenerator = idGenerator;
    }

    /**
     * Records an inbound order and stores it in {@link OrderStatus#CREATED}.
     *
     * <p>Emits {@link LogEvent#ORDER_RECEIVED} for the intake itself and
     * {@link LogEvent#ORDER_CREATED} once the order has been stored. The order is
     * accepted as supplied; business rules are checked later by
     * {@link #validateOrder(String, String, String)}.</p>
     *
     * @param reqId      the correlating request id
     * @param traceId    the correlating trace id
     * @param customerId the ordering customer
     * @param productId  the ordered product
     * @param quantity   the ordered quantity
     * @return the newly created order
     */
    public Order createOrder(String reqId, String traceId, String customerId, String productId, int quantity) {
        BenchmarkLogContext intake = BenchmarkLogContext.builder()
                .reqId(reqId)
                .traceId(traceId)
                .scenario(scenarioName())
                .customerId(customerId)
                .productId(productId)
                .service(ServiceName.ORDER)
                .component(ComponentName.API)
                .build();
        benchmarkLogger.log(LogEvent.ORDER_RECEIVED, intake,
                "Received order request for " + quantity + " x " + productId + " from customer " + customerId);

        Order order = new Order(
                idGenerator.nextOrderId(),
                customerId,
                productId,
                quantity,
                OrderStatus.CREATED,
                null,
                null,
                Instant.now().toString());
        benchmarkState.getOrders().put(order.getOrderId(), order);

        benchmarkLogger.log(LogEvent.ORDER_CREATED, context(reqId, traceId, order, ComponentName.STORE),
                "Order " + order.getOrderId() + " created with status " + order.getStatus());
        return order;
    }

    /**
     * Validates a stored order against the seeded catalogue.
     *
     * <p>An order passes when the quantity is positive and both the customer and
     * the product are known. On success the order moves to
     * {@link OrderStatus#VALIDATED} and {@link LogEvent#ORDER_VALIDATED} is
     * emitted; on rejection it is handed to
     * {@link #invalidateOrder(String, String, String, String)} with the concrete
     * reason. The outcome depends only on the order's own data, never on the
     * active scenario.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param orderId the order to validate
     * @return {@code true} if the order passed validation
     * @throws IllegalArgumentException if no such order exists
     */
    public boolean validateOrder(String reqId, String traceId, String orderId) {
        Order order = requireOrder(orderId);
        String rejectionReason = findRejectionReason(order);

        if (rejectionReason != null) {
            invalidateOrder(reqId, traceId, orderId, rejectionReason);
            return false;
        }

        order.setStatus(OrderStatus.VALIDATED);
        benchmarkLogger.log(LogEvent.ORDER_VALIDATED, context(reqId, traceId, order, ComponentName.VALIDATOR),
                "Order " + orderId + " passed validation");
        return true;
    }

    /**
     * Moves an order to {@link OrderStatus#INVALID} and emits
     * {@link LogEvent#ORDER_VALIDATION_FAILED}.
     *
     * <p>An invalid order is never fulfilled: no stock is reserved, no payment is
     * taken and no shipment is created.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param orderId the order to reject
     * @param reason  the rejection reason, included in the message
     * @return the updated order
     * @throws IllegalArgumentException if no such order exists
     */
    public Order invalidateOrder(String reqId, String traceId, String orderId, String reason) {
        Order order = requireOrder(orderId);
        order.setStatus(OrderStatus.INVALID);
        benchmarkLogger.log(LogEvent.ORDER_VALIDATION_FAILED,
                context(reqId, traceId, order, ComponentName.VALIDATOR),
                "Order " + orderId + " rejected during validation: " + reason);
        return order;
    }

    /**
     * Moves an order to {@link OrderStatus#COMPLETED} and emits
     * {@link LogEvent#ORDER_COMPLETED}.
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param orderId the order to complete
     * @return the updated order
     * @throws IllegalArgumentException if no such order exists
     */
    public Order completeOrder(String reqId, String traceId, String orderId) {
        Order order = requireOrder(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        benchmarkLogger.log(LogEvent.ORDER_COMPLETED, context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Order " + orderId + " completed successfully");
        return order;
    }

    /**
     * Moves an order to {@link OrderStatus#CANCELLED} and emits
     * {@link LogEvent#ORDER_CANCELLED}.
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param orderId the order to cancel
     * @param reason  the human-readable cancellation reason, included in the message
     * @return the updated order
     * @throws IllegalArgumentException if no such order exists
     */
    public Order cancelOrder(String reqId, String traceId, String orderId, String reason) {
        Order order = requireOrder(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        benchmarkLogger.log(LogEvent.ORDER_CANCELLED, context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Order " + orderId + " cancelled: " + reason);
        return order;
    }

    private String findRejectionReason(Order order) {
        if (order.getQuantity() <= 0) {
            return "quantity must be greater than zero but was " + order.getQuantity();
        }
        if (!benchmarkState.getCustomers().containsKey(order.getCustomerId())) {
            return "unknown customer " + order.getCustomerId();
        }
        if (!benchmarkState.getProducts().containsKey(order.getProductId())) {
            return "unknown product " + order.getProductId();
        }
        return null;
    }

    private Order requireOrder(String orderId) {
        Order order = orderId == null ? null : benchmarkState.getOrders().get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Unknown order: " + orderId);
        }
        return order;
    }

    private String scenarioName() {
        return benchmarkState.getCurrentScenario().name();
    }

    private BenchmarkLogContext context(String reqId, String traceId, Order order, ComponentName component) {
        return BenchmarkLogContext.builder()
                .reqId(reqId)
                .traceId(traceId)
                .scenario(scenarioName())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .paymentId(order.getPaymentId())
                .shipmentId(order.getShipmentId())
                .service(ServiceName.ORDER)
                .component(component)
                .build();
    }
}
