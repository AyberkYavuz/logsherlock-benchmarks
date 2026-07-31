package com.logsherlock.benchmark.ecommerce.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logsherlock.benchmark.ecommerce.service.OrderService;
import com.logsherlock.benchmark.ecommerce.web.dto.OrderResponse;

/**
 * Read-only order endpoints.
 *
 * <p>Orders are created and transitioned by the benchmark workflow; this controller
 * only exposes what is already stored and emits no log records of its own.</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates the controller.
     *
     * @param orderService the service owning orders
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Returns every order.
     *
     * @return all orders, ordered by identifier
     */
    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAllOrders().stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * Returns a single order.
     *
     * @param orderId the order to look up
     * @return the order
     * @throws NotFoundException if no such order exists
     */
    @GetMapping("/{orderId}")
    public OrderResponse findById(@PathVariable String orderId) {
        return orderService.findOrderById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }
}
