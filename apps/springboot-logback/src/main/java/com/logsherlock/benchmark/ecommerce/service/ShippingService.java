package com.logsherlock.benchmark.ecommerce.service;

import org.springframework.stereotype.Service;

import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogContext;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogger;
import com.logsherlock.benchmark.ecommerce.logging.ComponentName;
import com.logsherlock.benchmark.ecommerce.logging.LogEvent;
import com.logsherlock.benchmark.ecommerce.logging.ServiceName;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Shipment;
import com.logsherlock.benchmark.ecommerce.model.ShipmentStatus;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Shipment operations of the benchmark.
 *
 * <p>Owns the {@link Shipment} entity: creation, delivery and the failure state
 * used when the shipping dependency is reported as unavailable. Created shipments
 * are stored in {@link BenchmarkState} and linked back onto the order via its
 * shipment id.</p>
 *
 * <p>As with the other services the outcome is always chosen by the caller; no
 * network call, wait or retry happens here.</p>
 */
@Service
public class ShippingService {

    private static final String DEFAULT_CARRIER = "DHL Express";

    private final BenchmarkLogger benchmarkLogger;
    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    /**
     * Creates the shipping service.
     *
     * @param benchmarkLogger the structured logging abstraction
     * @param benchmarkState  the in-memory store holding all benchmark entities
     * @param idGenerator     the deterministic identifier source
     */
    public ShippingService(BenchmarkLogger benchmarkLogger,
                           BenchmarkState benchmarkState,
                           IdGenerator idGenerator) {
        this.benchmarkLogger = benchmarkLogger;
        this.benchmarkState = benchmarkState;
        this.idGenerator = idGenerator;
    }

    /**
     * Creates a {@link ShipmentStatus#PENDING} shipment for the order.
     *
     * <p>Emits {@link LogEvent#SHIPMENT_PREPARATION_STARTED} before the shipment
     * exists and {@link LogEvent#SHIPMENT_CREATED} once it has been stored and
     * linked to the order.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order being shipped
     * @return the newly created shipment
     */
    public Shipment createShipment(String reqId, String traceId, Order order) {
        benchmarkLogger.log(LogEvent.SHIPMENT_PREPARATION_STARTED,
                context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Preparing shipment for order " + order.getOrderId());

        Shipment shipment = storeShipment(order, ShipmentStatus.PENDING);
        benchmarkLogger.log(LogEvent.SHIPMENT_CREATED, context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Shipment " + shipment.getShipmentId() + " created for order " + order.getOrderId()
                        + " with carrier " + shipment.getCarrier());
        return shipment;
    }

    /**
     * Moves the order's shipment to {@link ShipmentStatus#DELIVERED} and emits
     * {@link LogEvent#SHIPMENT_COMPLETED}.
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order whose shipment is completed
     * @return the updated shipment
     * @throws IllegalArgumentException if the order has no stored shipment
     */
    public Shipment completeShipment(String reqId, String traceId, Order order) {
        Shipment shipment = requireShipment(order);
        shipment.setStatus(ShipmentStatus.DELIVERED);
        benchmarkLogger.log(LogEvent.SHIPMENT_COMPLETED, context(reqId, traceId, order, ComponentName.WORKFLOW),
                "Shipment " + shipment.getShipmentId() + " for order " + order.getOrderId()
                        + " delivered by " + shipment.getCarrier());
        return shipment;
    }

    /**
     * Marks the order's shipment as {@link ShipmentStatus#FAILED} and emits
     * {@link LogEvent#SHIPPING_SERVICE_UNAVAILABLE}.
     *
     * <p>When the order has no shipment yet — the failure happened before the
     * shipment could be created — a failed shipment record is stored so the
     * attempt remains visible in the benchmark state.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order whose shipment failed
     * @param reason  the failure reason, included in the message
     * @return the failed shipment
     */
    public Shipment failShipment(String reqId, String traceId, Order order, String reason) {
        Shipment shipment = findShipment(order);
        if (shipment == null) {
            shipment = storeShipment(order, ShipmentStatus.FAILED);
        } else {
            shipment.setStatus(ShipmentStatus.FAILED);
        }

        benchmarkLogger.log(LogEvent.SHIPPING_SERVICE_UNAVAILABLE,
                context(reqId, traceId, order, ComponentName.CLIENT),
                "Shipment " + shipment.getShipmentId() + " for order " + order.getOrderId()
                        + " failed: " + reason);
        return shipment;
    }

    private Shipment storeShipment(Order order, ShipmentStatus status) {
        Shipment shipment = new Shipment(
                idGenerator.nextShipmentId(),
                order.getOrderId(),
                status,
                DEFAULT_CARRIER);
        benchmarkState.getShipments().put(shipment.getShipmentId(), shipment);
        order.setShipmentId(shipment.getShipmentId());
        return shipment;
    }

    private Shipment findShipment(Order order) {
        String shipmentId = order.getShipmentId();
        return shipmentId == null ? null : benchmarkState.getShipments().get(shipmentId);
    }

    private Shipment requireShipment(Order order) {
        Shipment shipment = findShipment(order);
        if (shipment == null) {
            throw new IllegalArgumentException("Order has no shipment: " + order.getOrderId());
        }
        return shipment;
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
                .service(ServiceName.SHIPPING)
                .component(component)
                .build();
    }
}
