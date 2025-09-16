package com.interview.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for managing correlation IDs across HTTP requests.
 * Generates a unique correlation ID for each request and adds it to the response headers.
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Get correlation ID from request header or generate new one
        String correlationId = getOrGenerateCorrelationId(request);
        
        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        try {
            // Add correlation ID to response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Log request with correlation ID
            log.info("Processing request with correlation ID: {}", correlationId);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
