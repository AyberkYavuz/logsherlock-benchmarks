package com.logsherlock.benchmark.ecommerce.logging;

/**
 * Thin logging abstraction used by all benchmark business services.
 *
 * <p>Business code must never talk to SLF4J directly. Instead it emits events
 * through this interface, which guarantees that every benchmark log follows the
 * same structure: a typed {@link LogEvent}, the structured
 * {@link BenchmarkLogContext} metadata and a human-readable message.</p>
 *
 * <p>The concrete implementation owns all logging framework concerns (SLF4J,
 * MDC, Logback); callers remain unaware of them.</p>
 */
public interface BenchmarkLogger {

    /**
     * Logs a benchmark event.
     *
     * <p>The severity is derived from {@link LogEvent#getLevel()}; callers do
     * not choose the log level.</p>
     *
     * @param event   the benchmark event being logged
     * @param context the structured context attached to this event
     * @param message the human-readable message, used exactly as supplied
     */
    void log(LogEvent event, BenchmarkLogContext context, String message);

    /**
     * Logs a benchmark event together with an associated throwable.
     *
     * <p>The severity is derived from {@link LogEvent#getLevel()}; callers do
     * not choose the log level.</p>
     *
     * @param event     the benchmark event being logged
     * @param context   the structured context attached to this event
     * @param message   the human-readable message, used exactly as supplied
     * @param throwable the throwable to include with the log record
     */
    void log(LogEvent event, BenchmarkLogContext context, String message, Throwable throwable);
}
