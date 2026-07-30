package com.logsherlock.benchmark.ecommerce.model;

/**
 * Benchmark payment model.
 *
 * <p>A deliberately lightweight data container that later phases manipulate while
 * generating logs. It does not model a real payment transaction and holds no
 * business logic.</p>
 */
public class Payment {

    private String paymentId;
    private String orderId;
    private PaymentStatus status;
    private double amount;

    public Payment() {
    }

    public Payment(String paymentId, String orderId, PaymentStatus status, double amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                '}';
    }
}
