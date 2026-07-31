package com.logsherlock.benchmark.ecommerce.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogContext;
import com.logsherlock.benchmark.ecommerce.logging.BenchmarkLogger;
import com.logsherlock.benchmark.ecommerce.logging.ComponentName;
import com.logsherlock.benchmark.ecommerce.logging.LogEvent;
import com.logsherlock.benchmark.ecommerce.logging.ServiceName;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Payment;
import com.logsherlock.benchmark.ecommerce.model.PaymentStatus;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.state.BenchmarkState;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Payment authorization operations of the benchmark.
 *
 * <p>Owns the {@link Payment} entity. Every operation first opens a
 * {@link PaymentStatus#PENDING} payment for the order — emitting
 * {@link LogEvent#PAYMENT_REQUESTED} and linking the payment id back onto the
 * order — and then applies exactly one deterministic outcome chosen by the
 * caller.</p>
 *
 * <p>{@link #declinePayment} exists so that a scenario can reproduce a declined
 * provider response without this service ever deciding to fail on its own.
 * Nothing here calls out to a network, sleeps or retries; the "provider" is purely
 * a log-level narrative.</p>
 */
@Service
public class PaymentService {

    private final BenchmarkLogger benchmarkLogger;
    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    /**
     * Creates the payment service.
     *
     * @param benchmarkLogger the structured logging abstraction
     * @param benchmarkState  the in-memory store holding all benchmark entities
     * @param idGenerator     the deterministic identifier source
     */
    public PaymentService(BenchmarkLogger benchmarkLogger,
                          BenchmarkState benchmarkState,
                          IdGenerator idGenerator) {
        this.benchmarkLogger = benchmarkLogger;
        this.benchmarkState = benchmarkState;
        this.idGenerator = idGenerator;
    }

    /**
     * Opens a payment for the order and authorizes it.
     *
     * <p>Emits {@link LogEvent#PAYMENT_REQUESTED} followed by
     * {@link LogEvent#PAYMENT_AUTHORIZED}, leaving the payment in
     * {@link PaymentStatus#AUTHORIZED}.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order being paid for
     * @return the authorized payment
     * @throws IllegalArgumentException if the order references an unknown product
     */
    public Payment authorizePayment(String reqId, String traceId, Order order) {
        Payment payment = requestPayment(reqId, traceId, order);
        payment.setStatus(PaymentStatus.AUTHORIZED);
        benchmarkLogger.log(LogEvent.PAYMENT_AUTHORIZED, context(reqId, traceId, order, ComponentName.PROVIDER),
                "Payment " + payment.getPaymentId() + " authorized for order " + order.getOrderId()
                        + " (amount " + format(payment.getAmount()) + ")");
        return payment;
    }

    /**
     * Opens a payment for the order and marks it declined by the provider.
     *
     * <p>Emits {@link LogEvent#PAYMENT_REQUESTED} followed by
     * {@link LogEvent#PAYMENT_DECLINED}, leaving the payment in
     * {@link PaymentStatus#DECLINED}.</p>
     *
     * @param reqId   the correlating request id
     * @param traceId the correlating trace id
     * @param order   the order being paid for
     * @param reason  the decline reason, included in the message
     * @return the declined payment
     * @throws IllegalArgumentException if the order references an unknown product
     */
    public Payment declinePayment(String reqId, String traceId, Order order, String reason) {
        Payment payment = requestPayment(reqId, traceId, order);
        payment.setStatus(PaymentStatus.DECLINED);
        benchmarkLogger.log(LogEvent.PAYMENT_DECLINED, context(reqId, traceId, order, ComponentName.PROVIDER),
                "Payment " + payment.getPaymentId() + " declined for order " + order.getOrderId() + ": " + reason);
        return payment;
    }

    private Payment requestPayment(String reqId, String traceId, Order order) {
        Payment payment = new Payment(
                idGenerator.nextPaymentId(),
                order.getOrderId(),
                PaymentStatus.PENDING,
                amountOf(order));
        benchmarkState.getPayments().put(payment.getPaymentId(), payment);
        order.setPaymentId(payment.getPaymentId());

        benchmarkLogger.log(LogEvent.PAYMENT_REQUESTED, context(reqId, traceId, order, ComponentName.PROVIDER),
                "Requesting authorization of " + format(payment.getAmount())
                        + " for order " + order.getOrderId());
        return payment;
    }

    private double amountOf(Order order) {
        String productId = order.getProductId();
        Product product = productId == null ? null : benchmarkState.getProducts().get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Unknown product: " + productId);
        }
        return Math.round(product.getPrice() * order.getQuantity() * 100.0) / 100.0;
    }

    private String format(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
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
                .service(ServiceName.PAYMENT)
                .component(component)
                .build();
    }
}
