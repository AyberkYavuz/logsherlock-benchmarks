package com.logsherlock.benchmark.ecommerce.model;

/**
 * Benchmark order model.
 *
 * <p>A deliberately lightweight data container that later phases manipulate while
 * generating logs. It does not model a real e-commerce order and holds no
 * business logic.</p>
 */
public class Order {

    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private OrderStatus status;
    private String paymentId;
    private String shipmentId;
    private String createdAt;

    public Order() {
    }

    public Order(String orderId, String customerId, String productId, int quantity,
                 OrderStatus status, String paymentId, String shipmentId, String createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.paymentId = paymentId;
        this.shipmentId = shipmentId;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", status=" + status +
                ", paymentId='" + paymentId + '\'' +
                ", shipmentId='" + shipmentId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
