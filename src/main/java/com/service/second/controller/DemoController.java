package com.service.second.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/hello2")
    public Mono<String> greetings() {
        return Mono.deferContextual(ctx -> {
            // Retrieve the OpenTelemetry context from Reactor's Context
            Context otelContext = ctx.getOrDefault(Context.class, Context.current());
            Span span = Span.fromContext(otelContext);

            // Log span details
            log.info("log-> second service, Trace ID: {}", span.getSpanContext().getTraceId());

            try (Scope scope = otelContext.makeCurrent()) {
                // Set additional attributes to the current span if necessary
                span.setAttribute("custom.attribute", "greetings-endpoint");

                return Mono.just("hello from second service");
            }
        }).contextWrite(ctx -> {
            // Attach the OpenTelemetry context to Reactor's Context
            Context otelContext = Context.current();
            return ctx.put(Context.class, otelContext);
        });
    }
}
