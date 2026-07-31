package com.logsherlock.benchmark.ecommerce.state;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.logsherlock.benchmark.ecommerce.model.Customer;
import com.logsherlock.benchmark.ecommerce.model.Product;
import com.logsherlock.benchmark.ecommerce.util.IdGenerator;

/**
 * Populates {@link BenchmarkState} with a fixed set of seed entities at startup.
 *
 * <p>On application boot this runner creates ten products and ten customers so
 * that later phases always start from the same, deterministic catalogue. It
 * deliberately creates no orders, performs no business logic and emits no log
 * output — it is purely initial data.</p>
 *
 * <p>Ordered first among the {@link CommandLineRunner} beans: every other runner
 * expects the catalogue to already exist, so the seed data must be in place
 * before any of them execute.</p>
 */
@Component
@Order(SeedDataLoader.ORDER)
public class SeedDataLoader implements CommandLineRunner {

    /** Execution order of this runner; the lowest value in the application. */
    public static final int ORDER = 0;

    private final BenchmarkState benchmarkState;
    private final IdGenerator idGenerator;

    /**
     * Creates the seed loader.
     *
     * @param benchmarkState the in-memory store to populate
     * @param idGenerator    the deterministic identifier source
     */
    public SeedDataLoader(BenchmarkState benchmarkState, IdGenerator idGenerator) {
        this.benchmarkState = benchmarkState;
        this.idGenerator = idGenerator;
    }

    @Override
    public void run(String... args) {
        String[] productNames = {
                "Apple MacBook Pro 14\"",
                "Apple iPhone 17 Pro",
                "Samsung Galaxy S26 Ultra",
                "Dell UltraSharp 27 Monitor",
                "Logitech MX Master 3S Mouse",
                "Sony WH-1000XM6 Headphones",
                "Samsung 990 PRO 2TB SSD",
                "Keychron K8 Mechanical Keyboard",
                "Anker 575 USB-C Dock",
                "Apple Magic Mouse"
        };
        double[] productPrices = {
                2499.99,
                1199.99,
                1399.99,
                549.99,
                99.99,
                349.99,
                179.99,
                109.99,
                249.99,
                79.99
        };
        for (int i = 0; i < 10; i++) {
            String productId = idGenerator.nextProductId();
            Product product = new Product(productId, productNames[i], productPrices[i], 100);
            benchmarkState.getProducts().put(productId, product);
        }

        String[] customerNames = {
                "John Smith",
                "Emma Johnson",
                "Michael Brown",
                "Sophia Davis",
                "Daniel Wilson",
                "Olivia Miller",
                "James Taylor",
                "Charlotte Anderson",
                "William Thomas",
                "Isabella Moore"
        };
        String[] customerEmails = {
                "john.smith@example.com",
                "emma.johnson@example.com",
                "michael.brown@example.com",
                "sophia.davis@example.com",
                "daniel.wilson@example.com",
                "olivia.miller@example.com",
                "james.taylor@example.com",
                "charlotte.anderson@example.com",
                "william.thomas@example.com",
                "isabella.moore@example.com"
        };
        for (int i = 0; i < 10; i++) {
            String customerId = idGenerator.nextCustomerId();
            Customer customer = new Customer(customerId, customerNames[i], customerEmails[i]);
            benchmarkState.getCustomers().put(customerId, customer);
        }
    }
}
