package com.logsherlock.benchmark.ecommerce.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the benchmark logging abstraction.
 *
 * <p>It exposes the process-wide {@link BenchmarkMetadata} and the
 * {@link BenchmarkLogger} that business services depend on.</p>
 */
@Configuration
public class LoggingConfiguration {

    /**
     * Creates the singleton {@link BenchmarkMetadata} attached to every log record.
     *
     * @return the benchmark metadata
     */
    @Bean
    public BenchmarkMetadata benchmarkMetadata() {
        return new BenchmarkMetadata("logsherlock-order-service", "benchmark", 1);
    }

    /**
     * Creates the {@link BenchmarkLogger} used by all business services.
     *
     * @param metadata the process-wide metadata
     * @return the benchmark logger
     */
    @Bean
    public BenchmarkLogger benchmarkLogger(BenchmarkMetadata metadata) {
        return new BenchmarkLoggerImpl(metadata);
    }
}
