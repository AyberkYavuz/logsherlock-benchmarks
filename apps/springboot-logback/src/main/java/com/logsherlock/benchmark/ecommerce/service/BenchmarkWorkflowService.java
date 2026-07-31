package com.logsherlock.benchmark.ecommerce.service;

import org.springframework.stereotype.Service;

import com.logsherlock.benchmark.ecommerce.model.Customer;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.scenario.BenchmarkScenario;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Executes complete benchmark workflows by orchestrating the business services.
 *
 * <p>This is the only workflow orchestrator in the application. It owns the
 * correlation identifiers of a run (request id and trace id), the inputs a
 * scenario is run with, and the order in which the business services are invoked.
 * It holds no business logic of its own and emits no log records — every log line
 * of a workflow comes from the service that performed the step.</p>
 *
 * <p>{@link #runScenario(BenchmarkScenario)} branches once on the requested
 * scenario; each branch is a straight-line sequence of service calls. Scenario
 * outcomes are produced only by the inputs each branch supplies (for example an
 * order quantity that exceeds the available stock), never by randomness, timing
 * or failure injection, so a given scenario always yields the same log
 * sequence.</p>
 *
 * <p>Later phases reuse this service from REST endpoints, the scenario controller
 * and dataset generation.</p>
 */
@Service
public class BenchmarkWorkflowService {

    private static final int DEFAULT_QUANTITY = 1;
    private static final int INVALID_QUANTITY = 0;
    private static final int SHIPPING_DELAY_HOURS = 48;

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    /**
     * Creates the workflow service.
     *
     * @param orderService     the order lifecycle service
     * @param inventoryService the stock reservation service
     * @param paymentService   the payment authorization service
     * @param shippingService  the shipment service
     * @param benchmarkState   the in-memory store holding all benchmark entities
     * @param idGenerator      the deterministic identifier source
     */
    public BenchmarkWorkflowService(OrderService orderService,
                                    InventoryService inventoryService,
                                    PaymentService paymentService,
                                    ShippingService shippingService,
                                    BenchmarkState benchmarkState,
                                    IdGenerator idGenerator) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.benchmarkState = benchmarkState;
        this.idGenerator = idGenerator;
    }

    /**
     * Runs one order workflow for the requested scenario.
     *
     * <p>The scenario becomes the active one in {@link BenchmarkState}, so every
     * log record of the run carries it, and a fresh request id and trace id are
     * threaded through every step to correlate the records. The customer and
     * product are taken from the seeded catalogue, keeping the run
     * deterministic.</p>
     *
     * @param scenario the scenario to execute
     * @return the order the workflow ended on, in its final state
     * @throws IllegalStateException if the benchmark state holds no seeded
     *                               customers or products
     */
    public Order runScenario(BenchmarkScenario scenario) {
        benchmarkState.setCurrentScenario(scenario);

        return switch (scenario) {
            case NORMAL -> runNormal();
            case INVALID_ORDER -> runInvalidOrder();
            case OUT_OF_STOCK -> runOutOfStock();
            case PAYMENT_DECLINED -> runPaymentDeclined();
            case SHIPPING_DELAY -> runShippingDelay();
        };
    }

    /**
     * Happy path: the order is validated, paid, shipped and completed.
     */
    private Order runNormal() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Order order = createOrder(reqId, traceId, DEFAULT_QUANTITY);

        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        paymentService.authorizePayment(reqId, traceId, order);
        shippingService.createShipment(reqId, traceId, order);
        return orderService.completeOrder(reqId, traceId, order.getOrderId());
    }

    /**
     * The order is created with a quantity of zero, so validation rejects it and
     * the workflow stops before any fulfilment step.
     */
    private Order runInvalidOrder() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Order order = createOrder(reqId, traceId, INVALID_QUANTITY);

        orderService.validateOrder(reqId, traceId, order.getOrderId());
        return order;
    }

    /**
     * The order asks for more units than the catalogue holds, so the reservation
     * reports a shortage and the order is cancelled without being paid or shipped.
     */
    private Order runOutOfStock() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Order order = createOrder(reqId, traceId, firstProduct().getAvailableQuantity() + 1);

        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        return orderService.cancelOrder(reqId, traceId, order.getOrderId(), "requested stock is not available");
    }

    /**
     * Stock is reserved, the payment is then declined, the reservation is rolled
     * back and the order is cancelled without a shipment.
     */
    private Order runPaymentDeclined() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Order order = createOrder(reqId, traceId, DEFAULT_QUANTITY);

        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        paymentService.declinePayment(reqId, traceId, order, "insufficient funds");
        inventoryService.releaseInventory(reqId, traceId, order);
        return orderService.cancelOrder(reqId, traceId, order.getOrderId(), "payment was declined");
    }

    /**
     * The order is paid and shipped, the shipment is then delayed, and the order
     * still completes.
     */
    private Order runShippingDelay() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Order order = createOrder(reqId, traceId, DEFAULT_QUANTITY);

        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        paymentService.authorizePayment(reqId, traceId, order);
        shippingService.createShipment(reqId, traceId, order);
        shippingService.markShipmentDelayed(
                reqId, traceId, order, SHIPPING_DELAY_HOURS, "carrier capacity exceeded");
        return orderService.completeOrder(reqId, traceId, order.getOrderId());
    }

    private Order createOrder(String reqId, String traceId, int quantity) {
        return orderService.createOrder(
                reqId, traceId, firstCustomer().getCustomerId(), firstProduct().getProductId(), quantity);
    }

    private Customer firstCustomer() {
        return benchmarkState.getCustomers().values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded customers available"));
    }

    private Product firstProduct() {
        return benchmarkState.getProducts().values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded products available"));
    }
}
