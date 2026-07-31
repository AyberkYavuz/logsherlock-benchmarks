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
 * <p>This is the single entry point for "run one order through the system". It
 * owns the correlation identifiers of a run (request id and trace id) and the
 * order in which the business services are invoked; it holds no business logic of
 * its own and emits no log records — every log line of a workflow comes from the
 * service that performed the step.</p>
 *
 * <p>Phase 5 implements the {@link BenchmarkScenario#NORMAL} happy path only. The
 * failure scenarios, and with them the handling of the boolean outcomes returned
 * by validation and inventory reservation, arrive in a later phase.</p>
 *
 * <p>Later phases reuse this service from REST endpoints, the scenario controller
 * and dataset generation.</p>
 */
@Service
public class BenchmarkWorkflowService {

    private static final int DEFAULT_QUANTITY = 1;

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
     * Runs one successful order workflow end to end.
     *
     * <p>A fresh request id and trace id are generated and threaded through every
     * step, so all log records of the run are correlated. The customer and product
     * are taken from the seeded catalogue in {@link BenchmarkState} and the
     * quantity is fixed, keeping the run deterministic.</p>
     *
     * <p>The steps are: create the order, validate it, reserve inventory,
     * authorize payment, create the shipment and complete the order.</p>
     *
     * @return the completed order
     * @throws IllegalStateException if the benchmark state holds no seeded
     *                               customers or products
     */
    public Order runNormalScenario() {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();
        Customer customer = firstCustomer();
        Product product = firstProduct();

        Order order = orderService.createOrder(
                reqId, traceId, customer.getCustomerId(), product.getProductId(), DEFAULT_QUANTITY);
        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        paymentService.authorizePayment(reqId, traceId, order);
        shippingService.createShipment(reqId, traceId, order);
        return orderService.completeOrder(reqId, traceId, order.getOrderId());
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
