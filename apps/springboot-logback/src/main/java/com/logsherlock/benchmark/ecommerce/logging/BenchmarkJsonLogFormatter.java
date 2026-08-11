package com.logsherlock.benchmark.ecommerce.logging;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Renders every log record as a single JSON object on one line.
 *
 * <p>This is the JSON counterpart of the plaintext pattern layout declared in
 * {@code logback-spring.xml}: it emits exactly the same vocabulary, with the same
 * values, and adds nothing of its own. The record fields ({@code timestamp},
 * {@code level}, {@code thread}, {@code logger}, {@code message}) mirror the
 * corresponding pattern conversion words, and the structured fields
 * ({@code application} … {@code component}) are read straight from the MDC that
 * {@link BenchmarkLoggerImpl} populates. A structured field that is absent from
 * the MDC — for example {@code orderId} on a framework startup record — is
 * omitted rather than written as an empty string.</p>
 *
 * <p>The formatter is selected declaratively from {@code logback-spring.xml} and
 * is never referenced from Java code, so the log format can be switched at
 * runtime without touching any source file.</p>
 *
 * @see StructuredLogFormatter
 */
public class BenchmarkJsonLogFormatter implements StructuredLogFormatter<ILoggingEvent> {

    /** Same rendering as the {@code %d{...}} conversion word of the plaintext pattern. */
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    /** MDC keys of the benchmark vocabulary, in the order the plaintext pattern prints them. */
    private static final List<String> STRUCTURED_KEYS = List.of(
            "application",
            "environment",
            "schemaVersion",
            "scenario",
            "reqId",
            "traceId",
            "orderId",
            "customerId",
            "productId",
            "paymentId",
            "shipmentId",
            "service",
            "component");

    private final ThrowableProxyConverter throwableProxyConverter = new ThrowableProxyConverter();

    private final JsonWriter<ILoggingEvent> jsonWriter;

    /**
     * Creates the formatter.
     *
     * <p>Instantiated reflectively by Logback through
     * {@code org.springframework.boot.logging.logback.StructuredLogEncoder}.</p>
     */
    public BenchmarkJsonLogFormatter() {
        this.throwableProxyConverter.setOptionList(List.of("full"));
        this.throwableProxyConverter.start();
        this.jsonWriter = JsonWriter.<ILoggingEvent>of(members -> {
            members.add("timestamp", event -> TIMESTAMP.format(event.getInstant()));
            members.add("level", event -> event.getLevel().toString());
            members.add("thread", ILoggingEvent::getThreadName);
            members.add("logger", event -> simpleLoggerName(event.getLoggerName()));
            members.add("message", ILoggingEvent::getFormattedMessage);
            for (String key : STRUCTURED_KEYS) {
                members.add(key, event -> event.getMDCPropertyMap().get(key)).whenNotNull();
            }
            members.add("stackTrace", this::stackTrace).whenNotNull();
        }).withNewLineAtEnd();
    }

    @Override
    public String format(ILoggingEvent event) {
        return this.jsonWriter.writeToString(event);
    }

    /**
     * Returns the logger name the way {@code %logger{0}} prints it, so a record
     * carries the same logger value in both formats.
     */
    private static String simpleLoggerName(String loggerName) {
        if (loggerName == null) {
            return null;
        }
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot < 0 ? loggerName : loggerName.substring(lastDot + 1);
    }

    private String stackTrace(ILoggingEvent event) {
        return event.getThrowableProxy() == null ? null : this.throwableProxyConverter.convert(event);
    }
}
