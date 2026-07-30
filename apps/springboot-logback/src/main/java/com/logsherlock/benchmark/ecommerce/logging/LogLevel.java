package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Benchmark-specific severity levels for emitted log records.
 *
 * <p>These levels mirror the conventional SLF4J / Logback hierarchy but are
 * defined independently so that the benchmark vocabulary does not leak a logging
 * framework dependency into the domain. Constants are ordered from least to most
 * severe.</p>
 */
public enum LogLevel {

    /** Fine-grained diagnostic detail, typically disabled in production. */
    TRACE,

    /** Developer-oriented debugging information. */
    DEBUG,

    /** Normal, expected application progress. */
    INFO,

    /** An unexpected but recoverable situation. */
    WARN,

    /** A failure that prevented an operation from completing. */
    ERROR
}
