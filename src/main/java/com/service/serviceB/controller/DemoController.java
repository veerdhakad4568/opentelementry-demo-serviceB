package com.service.serviceB.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/helloServiceB")
    public Mono<String> greetings() {
                return Mono.just("hello from serviceB");
    }
}
