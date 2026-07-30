package com.logsherlock.benchmark.ecommerce.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Default {@link BenchmarkLogger} implementation backed by SLF4J.
 *
 * <p>For every call this implementation populates the MDC from the supplied
 * {@link BenchmarkLogContext} and the global {@link BenchmarkMetadata}, emits a
 * single log record at the severity carried by the {@link LogEvent}, and then
 * always clears the MDC. The MDC is used to carry structured metadata so that
 * the message itself stays clean.</p>
 */
public class BenchmarkLoggerImpl implements BenchmarkLogger {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkLoggerImpl.class);

    private final BenchmarkMetadata metadata;

    /**
     * Creates a new logger implementation.
     *
     * @param metadata the process-wide metadata attached to every log record
     */
    public BenchmarkLoggerImpl(BenchmarkMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void log(LogEvent event, BenchmarkLogContext context, String message) {
        log(event, context, message, null);
    }

    @Override
    public void log(LogEvent event, BenchmarkLogContext context, String message, Throwable throwable) {
        try {
            populateMdc(context);
            write(event.getLevel(), message, throwable);
        } finally {
            MDC.clear();
        }
    }

    private void populateMdc(BenchmarkLogContext context) {
        put("application", metadata.getApplication());
        put("environment", metadata.getEnvironment());
        put("schemaVersion", String.valueOf(metadata.getSchemaVersion()));

        put("reqId", context.getReqId());
        put("traceId", context.getTraceId());
        put("scenario", context.getScenario());
        put("orderId", context.getOrderId());
        put("customerId", context.getCustomerId());
        put("productId", context.getProductId());
        put("paymentId", context.getPaymentId());
        put("shipmentId", context.getShipmentId());

        if (context.getService() != null) {
            put("service", context.getService().name());
        }
        if (context.getComponent() != null) {
            put("component", context.getComponent().name());
        }
    }

    private void put(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    private void write(LogLevel level, String message, Throwable throwable) {
        if (throwable == null) {
            switch (level) {
                case TRACE -> logger.trace(message);
                case DEBUG -> logger.debug(message);
                case INFO -> logger.info(message);
                case WARN -> logger.warn(message);
                case ERROR -> logger.error(message);
            }
        } else {
            switch (level) {
                case TRACE -> logger.trace(message, throwable);
                case DEBUG -> logger.debug(message, throwable);
                case INFO -> logger.info(message, throwable);
                case WARN -> logger.warn(message, throwable);
                case ERROR -> logger.error(message, throwable);
            }
        }
    }
}
