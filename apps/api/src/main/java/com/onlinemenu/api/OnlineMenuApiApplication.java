package com.onlinemenu.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the backend.
 *
 * <p>The application is a modular monolith: every top-level package
 * ({@code branch}, {@code menu}, {@code media}) is a module with its own
 * entities, service and controller. Modules talk to each other only through
 * public services and events, so any of them can later be extracted into a
 * separate service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class OnlineMenuApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineMenuApiApplication.class, args);
    }
}
