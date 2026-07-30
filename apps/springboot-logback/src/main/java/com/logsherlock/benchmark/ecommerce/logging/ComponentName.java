package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Enumerates the technical components that can exist within a
 * {@link ServiceName business service}.
 *
 * <p>Where {@link ServiceName} answers "which business capability", a
 * {@code ComponentName} answers "which technical layer" produced a given log
 * record (for example an API entry point, a workflow orchestrator or a data
 * store adapter). Combining service and component yields fine-grained,
 * production-like log attribution.</p>
 *
 * <p>These values form part of the canonical logging vocabulary and are consumed
 * by later phases of the benchmark.</p>
 */
public enum ComponentName {

    /** Application bootstrap and lifecycle. */
    APPLICATION,

    /** Inbound API / request entry points. */
    API,

    /** Multi-step business workflow orchestration. */
    WORKFLOW,

    /** Input and business-rule validation. */
    VALIDATOR,

    /** Integration with an external provider. */
    PROVIDER,

    /** Outbound client to a downstream dependency. */
    CLIENT,

    /** Core domain processing engine. */
    ENGINE,

    /** Unit-of-work processor. */
    PROCESSOR,

    /** Persistence / data store adapter. */
    STORE,

    /** Configuration loading and wiring. */
    CONFIGURATION,

    /** Benchmark scenario control. */
    SCENARIO
}
