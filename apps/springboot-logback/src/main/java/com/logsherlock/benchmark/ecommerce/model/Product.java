package com.logsherlock.benchmark.ecommerce.model;

/**
 * Benchmark product model.
 *
 * <p>A deliberately lightweight data container that later phases manipulate while
 * generating logs. It does not model a real product catalogue and holds no
 * business logic.</p>
 */
public class Product {

    private String productId;
    private String name;
    private double price;
    private int availableQuantity;

    public Product() {
    }

    public Product(String productId, String name, double price, int availableQuantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", availableQuantity=" + availableQuantity +
                '}';
    }
}
