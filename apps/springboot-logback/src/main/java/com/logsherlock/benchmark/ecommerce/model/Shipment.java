package com.logsherlock.benchmark.ecommerce.model;

/**
 * Benchmark shipment model.
 *
 * <p>A deliberately lightweight data container that later phases manipulate while
 * generating logs. It does not model a real logistics shipment and holds no
 * business logic.</p>
 */
public class Shipment {

    private String shipmentId;
    private String orderId;
    private ShipmentStatus status;
    private String carrier;

    public Shipment() {
    }

    public Shipment(String shipmentId, String orderId, ShipmentStatus status, String carrier) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.status = status;
        this.carrier = carrier;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "shipmentId='" + shipmentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status=" + status +
                ", carrier='" + carrier + '\'' +
                '}';
    }
}
