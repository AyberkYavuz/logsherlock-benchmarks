package com.logsherlock.benchmark.ecommerce.model;

/**
 * Benchmark customer model.
 *
 * <p>A deliberately lightweight data container that later phases manipulate while
 * generating logs. It does not model a real customer account and holds no
 * business logic.</p>
 */
public class Customer {

    private String customerId;
    private String name;
    private String email;

    public Customer() {
    }

    public Customer(String customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
