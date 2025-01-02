package com.service.second.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Configuration
public class TracingWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(TracingWebFilter.class);
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("example-tracer");
    private static final TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        logger.info("Incoming Headers: {}", headers);

        // Extract the OpenTelemetry Context from headers
        Context extractedContext = propagator.extract(Context.current(), headers, HttpHeadersGetter.INSTANCE);
        if (extractedContext == Context.current()) {
            logger.warn("Context extraction failed: Default context returned.");
        } else {
            logger.info("Extracted Context: {}", extractedContext);
        }

        // Start a span with the extracted context as the parent
        Span span = tracer.spanBuilder(exchange.getRequest().getPath().value())
                .setParent(extractedContext)
                .startSpan();

        logger.info("Span created with trace ID: {}", span.getSpanContext().getTraceId());

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(Context.class, extractedContext)) // Attach context to Reactor Context
                .doOnError(error -> {
                    span.recordException(error);
                    logger.error("Error in filter chain: {}", error.getMessage(), error);
                })
                .doFinally(signalType -> {
                    span.end();
                    logger.info("Span ended");
                });
    }

    private enum HttpHeadersGetter implements TextMapGetter<HttpHeaders> {
        INSTANCE;

        @Override
        public String get(HttpHeaders carrier, String key) {
            if (carrier == null) {
                return null;
            }
            return carrier.getFirst(key);
        }

        @Override
        public Iterable<String> keys(HttpHeaders carrier) {
            if (carrier == null) {
                return Collections.emptyList();
            }
            return carrier.keySet();
        }
    }
}