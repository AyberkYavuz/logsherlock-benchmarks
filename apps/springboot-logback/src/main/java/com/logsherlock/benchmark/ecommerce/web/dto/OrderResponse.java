package com.logsherlock.benchmark.ecommerce.web.dto;

import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.OrderStatus;

/**
 * API representation of an {@link Order}.
 *
 * @param orderId    the order identifier
 * @param customerId the ordering customer
 * @param productId  the ordered product
 * @param quantity   the ordered quantity
 * @param status     the current lifecycle status
 * @param paymentId  the linked payment, or {@code null} if none was opened
 * @param shipmentId the linked shipment, or {@code null} if none was created
 * @param createdAt  the creation timestamp
 */
public record OrderResponse(String orderId,
                            String customerId,
                            String productId,
                            int quantity,
                            OrderStatus status,
                            String paymentId,
                            String shipmentId,
                            String createdAt) {

    /**
     * Maps a domain order onto its API representation.
     *
     * @param order the order to map
     * @return the response payload
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getStatus(),
                order.getPaymentId(),
                order.getShipmentId(),
                order.getCreatedAt());
    }
}
