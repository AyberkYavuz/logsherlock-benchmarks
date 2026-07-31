package com.logsherlock.benchmark.ecommerce;

import com.logsherlock.benchmark.ecommerce.model.Customer;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Payment;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.model.Shipment;
import com.logsherlock.benchmark.ecommerce.service.InventoryService;
import com.logsherlock.benchmark.ecommerce.service.OrderService;
import com.logsherlock.benchmark.ecommerce.service.PaymentService;
import com.logsherlock.benchmark.ecommerce.service.ShippingService;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Temporary startup smoke test that exercises the business service layer
 * end-to-end.
 *
 * <p>It runs a single happy-path order workflow — create, validate, reserve
 * inventory, authorize payment, create shipment, complete — against the seeded
 * catalogue, then prints the resulting entities and the {@link BenchmarkState}
 * counts. All benchmark logs are emitted by the services themselves; this class
 * only calls them.</p>
 *
 * <p>This class is intended to be removed once the business layer has been
 * verified.</p>
 */
@Component
public class BenchmarkLoggingSmokeTest implements CommandLineRunner {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    public BenchmarkLoggingSmokeTest(
            OrderService orderService,
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

    @Override
    public void run(String... args) {
        String reqId = idGenerator.nextRequestId();
        String traceId = idGenerator.nextTraceId();

        Customer customer = benchmarkState.getCustomers().values().iterator().next();
        Product product = benchmarkState.getProducts().values().iterator().next();

        Order order = orderService.createOrder(reqId, traceId, customer.getCustomerId(), product.getProductId(), 1);
        orderService.validateOrder(reqId, traceId, order.getOrderId());
        inventoryService.reserveInventory(reqId, traceId, order);
        Payment payment = paymentService.authorizePayment(reqId, traceId, order);
        Shipment shipment = shippingService.createShipment(reqId, traceId, order);
        orderService.completeOrder(reqId, traceId, order.getOrderId());

        System.out.println("========== ORDER ==========");
        System.out.println(order);

        System.out.println("========== PAYMENT ==========");
        System.out.println(payment);

        System.out.println("========== SHIPMENT ==========");
        System.out.println(shipment);

        System.out.println("========== PRODUCT ==========");
        System.out.println(product);

        System.out.println("========== STATE ==========");
        System.out.println("Orders: " + benchmarkState.getOrders().size());
        System.out.println("Payments: " + benchmarkState.getPayments().size());
        System.out.println("Shipments: " + benchmarkState.getShipments().size());
    }
}
