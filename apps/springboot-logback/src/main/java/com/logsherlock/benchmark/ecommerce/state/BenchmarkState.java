package com.logsherlock.benchmark.ecommerce.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.logsherlock.benchmark.ecommerce.model.Customer;
import com.logsherlock.benchmark.ecommerce.model.Order;
import com.logsherlock.benchmark.ecommerce.model.Payment;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.model.Shipment;
import com.logsherlock.benchmark.ecommerce.scenario.BenchmarkScenario;

/**
 * In-memory store for the whole benchmark.
 *
 * <p>This singleton bean holds the currently active {@link BenchmarkScenario}
 * together with every domain entity created during a run. All state lives in
 * memory; there is no persistence. The class deliberately contains no business
 * logic and only exposes accessors so later phases can read and mutate the
 * shared maps.</p>
 *
 * <p>The maps are {@link ConcurrentHashMap} instances keyed by the entity's
 * identifier, allowing concurrent access from request-handling threads.</p>
 */
@Component
public class BenchmarkState {

    private BenchmarkScenario currentScenario = BenchmarkScenario.NORMAL;

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final Map<String, Shipment> shipments = new ConcurrentHashMap<>();

    /**
     * Returns the currently active benchmark scenario.
     *
     * @return the active scenario, never {@code null}
     */
    public BenchmarkScenario getCurrentScenario() {
        return currentScenario;
    }

    /**
     * Sets the active benchmark scenario.
     *
     * @param currentScenario the scenario to activate
     */
    public void setCurrentScenario(BenchmarkScenario currentScenario) {
        this.currentScenario = currentScenario;
    }

    /**
     * Returns the live map of orders keyed by order identifier.
     *
     * @return the orders map
     */
    public Map<String, Order> getOrders() {
        return orders;
    }

    /**
     * Returns the live map of products keyed by product identifier.
     *
     * @return the products map
     */
    public Map<String, Product> getProducts() {
        return products;
    }

    /**
     * Returns the live map of customers keyed by customer identifier.
     *
     * @return the customers map
     */
    public Map<String, Customer> getCustomers() {
        return customers;
    }

    /**
     * Returns the live map of payments keyed by payment identifier.
     *
     * @return the payments map
     */
    public Map<String, Payment> getPayments() {
        return payments;
    }

    /**
     * Returns the live map of shipments keyed by shipment identifier.
     *
     * @return the shipments map
     */
    public Map<String, Shipment> getShipments() {
        return shipments;
    }
}
