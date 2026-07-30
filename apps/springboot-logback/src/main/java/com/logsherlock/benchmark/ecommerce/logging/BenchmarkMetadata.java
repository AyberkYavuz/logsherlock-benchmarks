package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Static metadata that rarely changes during the lifetime of the benchmark
 * application.
 *
 * <p>Where {@link BenchmarkLogContext} captures per-event metadata, this class
 * captures process-wide constants such as the application name, the environment
 * it runs in and the version of the log schema being produced. It is an
 * immutable data container and performs no logging.</p>
 */
public final class BenchmarkMetadata {

    private final String application;
    private final String environment;
    private final int schemaVersion;

    /**
     * Creates a new immutable metadata instance.
     *
     * @param application   the logical application name
     * @param environment   the environment the application runs in
     * @param schemaVersion the version of the emitted log schema
     */
    public BenchmarkMetadata(String application, String environment, int schemaVersion) {
        this.application = application;
        this.environment = environment;
        this.schemaVersion = schemaVersion;
    }

    /**
     * Returns the logical application name.
     *
     * @return the application name
     */
    public String getApplication() {
        return application;
    }

    /**
     * Returns the environment the application runs in.
     *
     * @return the environment name
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Returns the version of the emitted log schema.
     *
     * @return the schema version
     */
    public int getSchemaVersion() {
        return schemaVersion;
    }
}
