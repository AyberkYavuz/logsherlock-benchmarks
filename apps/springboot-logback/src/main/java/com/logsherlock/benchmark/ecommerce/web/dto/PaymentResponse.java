package com.logsherlock.benchmark.ecommerce.web.dto;

import com.logsherlock.benchmark.ecommerce.model.Payment;
import com.logsherlock.benchmark.ecommerce.model.PaymentStatus;

/**
 * API representation of a {@link Payment}.
 *
 * @param paymentId the payment identifier
 * @param orderId   the order this payment belongs to
 * @param status    the current payment status
 * @param amount    the authorized amount
 */
public record PaymentResponse(String paymentId, String orderId, PaymentStatus status, double amount) {

    /**
     * Maps a domain payment onto its API representation.
     *
     * @param payment the payment to map
     * @return the response payload
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount());
    }
}
