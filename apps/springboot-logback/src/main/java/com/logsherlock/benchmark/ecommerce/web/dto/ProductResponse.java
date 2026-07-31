package com.logsherlock.benchmark.ecommerce.web.dto;

import com.logsherlock.benchmark.ecommerce.model.Product;

/**
 * API representation of a {@link Product}.
 *
 * @param productId         the product identifier
 * @param name              the product name
 * @param price             the unit price
 * @param availableQuantity the stock currently available
 */
public record ProductResponse(String productId, String name, double price, int availableQuantity) {

    /**
     * Maps a domain product onto its API representation.
     *
     * @param product the product to map
     * @return the response payload
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getAvailableQuantity());
    }
}
