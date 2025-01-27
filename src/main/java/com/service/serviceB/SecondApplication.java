package com.service.serviceB;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class SecondApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecondApplication.class, args);
	}

	@PostConstruct
	public void init() {
		Hooks.enableAutomaticContextPropagation();
	}
}
