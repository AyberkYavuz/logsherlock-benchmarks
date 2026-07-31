package com.logsherlock.benchmark.ecommerce.web.dto;

import com.logsherlock.benchmark.ecommerce.model.Shipment;
import com.logsherlock.benchmark.ecommerce.model.ShipmentStatus;

/**
 * API representation of a {@link Shipment}.
 *
 * @param shipmentId the shipment identifier
 * @param orderId    the order this shipment belongs to
 * @param status     the current shipment status
 * @param carrier    the carrier handling the shipment
 */
public record ShipmentResponse(String shipmentId, String orderId, ShipmentStatus status, String carrier) {

    /**
     * Maps a domain shipment onto its API representation.
     *
     * @param shipment the shipment to map
     * @return the response payload
     */
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getOrderId(),
                shipment.getStatus(),
                shipment.getCarrier());
    }
}
