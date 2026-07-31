package com.logsherlock.benchmark.ecommerce.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logsherlock.benchmark.ecommerce.service.InventoryService;
import com.logsherlock.benchmark.ecommerce.web.dto.ProductResponse;

/**
 * Read-only product endpoints.
 *
 * <p>Products and their stock levels are owned by {@link InventoryService}; this
 * controller only maps them onto {@link ProductResponse} payloads and emits no log
 * records of its own.</p>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventoryService;

    /**
     * Creates the controller.
     *
     * @param inventoryService the service owning products and stock
     */
    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Returns every product.
     *
     * @return all products, ordered by identifier
     */
    @GetMapping
    public List<ProductResponse> findAll() {
        return inventoryService.findAllProducts().stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Returns a single product.
     *
     * @param productId the product to look up
     * @return the product
     * @throws NotFoundException if no such product exists
     */
    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable String productId) {
        return inventoryService.findProductById(productId)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }
}
