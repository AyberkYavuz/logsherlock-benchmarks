package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Structured metadata attached to a single business event.
 *
 * <p>This is an immutable data container only; it does not perform any logging.
 * Instances are created through the {@link Builder} obtained via
 * {@link #builder()}. Any field that is not set remains {@code null}.</p>
 *
 * <pre>{@code
 * BenchmarkLogContext context =
 *     BenchmarkLogContext.builder()
 *         .reqId("REQ-001")
 *         .scenario("normal")
 *         .orderId("ORDER-100")
 *         .customerId("CUSTOMER-42")
 *         .service(ServiceName.ORDER)
 *         .component(ComponentName.WORKFLOW)
 *         .build();
 * }</pre>
 */
public final class BenchmarkLogContext {

    private final String reqId;
    private final String traceId;
    private final String scenario;
    private final String orderId;
    private final String customerId;
    private final String productId;
    private final String paymentId;
    private final String shipmentId;
    private final ServiceName service;
    private final ComponentName component;

    private BenchmarkLogContext(Builder builder) {
        this.reqId = builder.reqId;
        this.traceId = builder.traceId;
        this.scenario = builder.scenario;
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.productId = builder.productId;
        this.paymentId = builder.paymentId;
        this.shipmentId = builder.shipmentId;
        this.service = builder.service;
        this.component = builder.component;
    }

    /**
     * Creates a new builder for {@link BenchmarkLogContext}.
     *
     * @return a fresh builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the request identifier.
     *
     * @return the request id, or {@code null} if unset
     */
    public String getReqId() {
        return reqId;
    }

    /**
     * Returns the trace identifier.
     *
     * @return the trace id, or {@code null} if unset
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * Returns the scenario name.
     *
     * @return the scenario, or {@code null} if unset
     */
    public String getScenario() {
        return scenario;
    }

    /**
     * Returns the order identifier.
     *
     * @return the order id, or {@code null} if unset
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Returns the customer identifier.
     *
     * @return the customer id, or {@code null} if unset
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Returns the product identifier.
     *
     * @return the product id, or {@code null} if unset
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Returns the payment identifier.
     *
     * @return the payment id, or {@code null} if unset
     */
    public String getPaymentId() {
        return paymentId;
    }

    /**
     * Returns the shipment identifier.
     *
     * @return the shipment id, or {@code null} if unset
     */
    public String getShipmentId() {
        return shipmentId;
    }

    /**
     * Returns the business service this event belongs to.
     *
     * @return the service, or {@code null} if unset
     */
    public ServiceName getService() {
        return service;
    }

    /**
     * Returns the technical component this event originates from.
     *
     * @return the component, or {@code null} if unset
     */
    public ComponentName getComponent() {
        return component;
    }

    /**
     * Fluent builder for {@link BenchmarkLogContext}.
     */
    public static final class Builder {

        private String reqId;
        private String traceId;
        private String scenario;
        private String orderId;
        private String customerId;
        private String productId;
        private String paymentId;
        private String shipmentId;
        private ServiceName service;
        private ComponentName component;

        private Builder() {
        }

        /**
         * Sets the request identifier.
         *
         * @param reqId the request id
         * @return this builder
         */
        public Builder reqId(String reqId) {
            this.reqId = reqId;
            return this;
        }

        /**
         * Sets the trace identifier.
         *
         * @param traceId the trace id
         * @return this builder
         */
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * Sets the scenario name.
         *
         * @param scenario the scenario
         * @return this builder
         */
        public Builder scenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        /**
         * Sets the order identifier.
         *
         * @param orderId the order id
         * @return this builder
         */
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        /**
         * Sets the customer identifier.
         *
         * @param customerId the customer id
         * @return this builder
         */
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        /**
         * Sets the product identifier.
         *
         * @param productId the product id
         * @return this builder
         */
        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        /**
         * Sets the payment identifier.
         *
         * @param paymentId the payment id
         * @return this builder
         */
        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        /**
         * Sets the shipment identifier.
         *
         * @param shipmentId the shipment id
         * @return this builder
         */
        public Builder shipmentId(String shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        /**
         * Sets the business service.
         *
         * @param service the service
         * @return this builder
         */
        public Builder service(ServiceName service) {
            this.service = service;
            return this;
        }

        /**
         * Sets the technical component.
         *
         * @param component the component
         * @return this builder
         */
        public Builder component(ComponentName component) {
            this.component = component;
            return this;
        }

        /**
         * Builds an immutable {@link BenchmarkLogContext} from the current state.
         *
         * @return a new context instance
         */
        public BenchmarkLogContext build() {
            return new BenchmarkLogContext(this);
        }
    }
}
