package com.logsherlock.benchmark.ecommerce.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logsherlock.benchmark.ecommerce.service.ShippingService;
import com.logsherlock.benchmark.ecommerce.web.dto.ShipmentResponse;

/**
 * Read-only shipment endpoints.
 *
 * <p>Shipments are created and transitioned by the benchmark workflow; this
 * controller only exposes what is already stored and emits no log records of its
 * own.</p>
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShippingService shippingService;

    /**
     * Creates the controller.
     *
     * @param shippingService the service owning shipments
     */
    public ShipmentController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * Returns every shipment.
     *
     * @return all shipments, ordered by identifier
     */
    @GetMapping
    public List<ShipmentResponse> findAll() {
        return shippingService.findAllShipments().stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    /**
     * Returns a single shipment.
     *
     * @param shipmentId the shipment to look up
     * @return the shipment
     * @throws NotFoundException if no such shipment exists
     */
    @GetMapping("/{shipmentId}")
    public ShipmentResponse findById(@PathVariable String shipmentId) {
        return shippingService.findShipmentById(shipmentId)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new NotFoundException("Shipment not found: " + shipmentId));
    }
}
