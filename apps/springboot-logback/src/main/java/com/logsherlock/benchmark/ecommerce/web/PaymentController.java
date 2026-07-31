package com.logsherlock.benchmark.ecommerce.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logsherlock.benchmark.ecommerce.service.PaymentService;
import com.logsherlock.benchmark.ecommerce.web.dto.PaymentResponse;

/**
 * Read-only payment endpoints.
 *
 * <p>Payments are opened and settled by the benchmark workflow; this controller only
 * exposes what is already stored and emits no log records of its own.</p>
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates the controller.
     *
     * @param paymentService the service owning payments
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Returns every payment.
     *
     * @return all payments, ordered by identifier
     */
    @GetMapping
    public List<PaymentResponse> findAll() {
        return paymentService.findAllPayments().stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * Returns a single payment.
     *
     * @param paymentId the payment to look up
     * @return the payment
     * @throws NotFoundException if no such payment exists
     */
    @GetMapping("/{paymentId}")
    public PaymentResponse findById(@PathVariable String paymentId) {
        return paymentService.findPaymentById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
    }
}
