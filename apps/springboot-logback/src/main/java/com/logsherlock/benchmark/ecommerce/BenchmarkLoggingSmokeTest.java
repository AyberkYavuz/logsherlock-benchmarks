package com.logsherlock.benchmark.ecommerce;

import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogContext;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogger;
import com.logsherlock.benchmark.ecommerce.logging.ComponentName;
import com.logsherlock.benchmark.ecommerce.logging.LogEvent;
import com.logsherlock.benchmark.ecommerce.logging.ServiceName;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Temporary startup smoke test that exercises the {@link BenchmarkLogger}
 * end-to-end (dependency injection, MDC population, log levels and emission).
 *
 * <p>This class is intended to be removed once the logging pipeline has been
 * verified.</p>
 */
@Component
public class BenchmarkLoggingSmokeTest implements CommandLineRunner {

    private final BenchmarkLogger benchmarkLogger;
    private final BenchmarkState benchmarkState;

    public BenchmarkLoggingSmokeTest(
        BenchmarkLogger benchmarkLogger,
        BenchmarkState benchmarkState) {
        this.benchmarkLogger = benchmarkLogger;
        this.benchmarkState = benchmarkState;
    }

    @Override
    public void run(String... args) {
        BenchmarkLogContext applicationStarted = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.BENCHMARK)
                .component(ComponentName.APPLICATION)
                .build();
        benchmarkLogger.log(LogEvent.APPLICATION_STARTED, applicationStarted, "Application started successfully");

        BenchmarkLogContext orderReceived = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.ORDER)
                .component(ComponentName.API)
                .build();
        benchmarkLogger.log(LogEvent.ORDER_RECEIVED, orderReceived, "Received new customer order");

        BenchmarkLogContext orderValidated = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.ORDER)
                .component(ComponentName.VALIDATOR)
                .build();
        benchmarkLogger.log(LogEvent.ORDER_VALIDATED, orderValidated, "Order validation completed");

        BenchmarkLogContext inventoryCheckStarted = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.INVENTORY)
                .component(ComponentName.WORKFLOW)
                .build();
        benchmarkLogger.log(LogEvent.INVENTORY_CHECK_STARTED, inventoryCheckStarted, "Checking product inventory");

        BenchmarkLogContext inventoryReserved = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.INVENTORY)
                .component(ComponentName.STORE)
                .build();
        benchmarkLogger.log(LogEvent.INVENTORY_RESERVED, inventoryReserved, "Inventory reserved successfully");

        BenchmarkLogContext paymentRequested = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.PAYMENT)
                .component(ComponentName.PROVIDER)
                .build();
        benchmarkLogger.log(LogEvent.PAYMENT_REQUESTED, paymentRequested, "Submitting payment authorization request");

        BenchmarkLogContext paymentAuthorized = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.PAYMENT)
                .component(ComponentName.PROVIDER)
                .build();
        benchmarkLogger.log(LogEvent.PAYMENT_AUTHORIZED, paymentAuthorized, "Payment authorized successfully");

        BenchmarkLogContext shipmentCreated = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.SHIPPING)
                .component(ComponentName.WORKFLOW)
                .build();
        benchmarkLogger.log(LogEvent.SHIPMENT_CREATED, shipmentCreated, "Shipment created successfully");

        BenchmarkLogContext orderCompleted = BenchmarkLogContext.builder()
                .scenario("normal")
                .reqId("REQ-1001")
                .traceId("TRACE-1001")
                .orderId("ORDER-5001")
                .customerId("CUSTOMER-42")
                .productId("PRODUCT-10")
                .paymentId("PAYMENT-7001")
                .shipmentId("SHIPMENT-9001")
                .service(ServiceName.ORDER)
                .component(ComponentName.WORKFLOW)
                .build();
        benchmarkLogger.log(LogEvent.ORDER_COMPLETED, orderCompleted, "Order completed successfully");

        System.out.println("========== PRODUCTS ==========");
        benchmarkState.getProducts().values().forEach(System.out::println);

        System.out.println("========== CUSTOMERS ==========");
        benchmarkState.getCustomers().values().forEach(System.out::println);
    }
}
