package com.interview.config;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for Spring Boot Admin Server.
 * This enables the admin dashboard for monitoring the application.
 * Only active in development profile for demonstration purposes.
 */
@Configuration
@EnableAdminServer
@Profile("dev")
public class SpringBootAdminConfig {
    // Spring Boot Admin Server configuration
    // The admin UI will be available at /admin
}
