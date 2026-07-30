package com.logsherlock.benchmark.ecommerce.state;

import org.springframework.boot.CommandLineRunner;
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
 */
@Component
public class SeedDataLoader implements CommandLineRunner {

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
        seedProducts();
        seedCustomers();
    }

    private void seedProducts() {
        for (int i = 1; i <= 10; i++) {
            String productId = idGenerator.nextProductId();
            Product product = new Product(productId, "Product " + i, 9.99 * i, 100);
            benchmarkState.getProducts().put(productId, product);
        }
    }

    private void seedCustomers() {
        for (int i = 1; i <= 10; i++) {
            String customerId = idGenerator.nextCustomerId();
            Customer customer = new Customer(customerId, "Customer " + i, "customer" + i + "@example.com");
            benchmarkState.getCustomers().put(customerId, customer);
        }
    }
}
