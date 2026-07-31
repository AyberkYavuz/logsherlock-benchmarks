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
import com.logsherlock.benchmark.ecommerce.model.Shipment;
import com.logsherlock.benchmark.ecommerce.model.ShipmentStatus;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Shipment operations of the benchmark.
 *
 * <p>Owns the {@link Shipment} entity: creation and the delayed state. Created
 * shipments are stored in {@link BenchmarkState} and linked back onto the order via
 * its shipment id.</p>
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
     * Moves the order's shipment to {@link ShipmentStatus#DELAYED} and emits
     * {@link LogEvent#SHIPMENT_DELAYED}.
     *
     * <p>A delayed shipment still belongs to a fulfillable order: only the
     * dispatch is held up, so the order itself can still be completed. The
     * reported delay is a value used in the message only; no waiting takes
     * place.</p>
     *
     * @param reqId       the correlating request id
     * @param traceId     the correlating trace id
     * @param order       the order whose shipment is delayed
     * @param delayHours  the delay to report in the message
     * @param reason      the delay reason, included in the message
     * @return the updated shipment
     * @throws IllegalArgumentException if the order has no stored shipment
     */
    public Shipment markShipmentDelayed(String reqId, String traceId, Order order, int delayHours, String reason) {
        Shipment shipment = requireShipment(order);
        shipment.setStatus(ShipmentStatus.DELAYED);
        benchmarkLogger.log(LogEvent.SHIPMENT_DELAYED, context(reqId, traceId, order, ComponentName.CLIENT),
                "Shipment " + shipment.getShipmentId() + " for order " + order.getOrderId()
                        + " delayed by " + delayHours + "h at carrier " + shipment.getCarrier()
                        + ": " + reason);
        return shipment;
    }

    /**
     * Returns every stored shipment, ordered by identifier.
     *
     * <p>The returned list is an immutable copy: callers cannot reach the store
     * through it.</p>
     *
     * @return all shipments
     */
    public List<Shipment> findAllShipments() {
        return benchmarkState.getShipments().values().stream()
                .sorted(Comparator.comparing(Shipment::getShipmentId))
                .toList();
    }

    /**
     * Looks up a single shipment.
     *
     * @param shipmentId the shipment to look up
     * @return the shipment, or {@link Optional#empty()} if there is none
     */
    public Optional<Shipment> findShipmentById(String shipmentId) {
        return shipmentId == null
                ? Optional.empty()
                : Optional.ofNullable(benchmarkState.getShipments().get(shipmentId));
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
