package com.interview.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/customers");
    }

    // Tests for CustomerNotFoundException handler
    @Test
    void handleCustomerNotFoundExceptionShouldReturnNotFoundResponseWhenCustomerNotFound() {
        // Given
        CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found with id: 999");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/customers/999");

        // When
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleCustomerNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("Not Found", result.getBody().getError());
        assertEquals("Customer not found with id: 999", result.getBody().getMessage());
        assertEquals("/api/customers/999", result.getBody().getPath());
        assertNotNull(result.getBody().getTimestamp());
    }

    // Tests for DuplicateEmailException handler
    @Test
    void handleDuplicateEmailExceptionShouldReturnConflictResponseWhenEmailExists() {
        // Given
        DuplicateEmailException exception = new DuplicateEmailException("Customer with email test@example.com already exists");

        // When
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleDuplicateEmailException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), result.getBody().getStatus());
        assertEquals("Conflict", result.getBody().getError());
        assertEquals("Customer with email test@example.com already exists", result.getBody().getMessage());
        assertEquals("/api/customers", result.getBody().getPath());
    }

    // Tests for ConstraintViolationException handler
    @Test
    void handleConstraintViolationExceptionShouldReturnBadRequestResponseWhenConstraintViolationOccurs() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("Email is required");
        when(path1.toString()).thenReturn("email");
        
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("Phone number is invalid");
        when(path2.toString()).thenReturn("phoneNumber");
        
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation1, violation2));

        // When
        ResponseEntity<ValidationErrorResponse> result = globalExceptionHandler.handleConstraintViolationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getBody().getStatus());
        assertEquals("Validation Failed", result.getBody().getError());
        assertEquals("Input validation failed", result.getBody().getMessage());
        assertEquals("/api/customers", result.getBody().getPath());
        assertEquals(2, result.getBody().getValidationErrors().size());
        assertEquals("Email is required", result.getBody().getValidationErrors().get("email"));
        assertEquals("Phone number is invalid", result.getBody().getValidationErrors().get("phoneNumber"));
    }

    // Tests for global Exception handler
    @Test
    void handleGlobalExceptionShouldReturnInternalServerErrorResponseWhenUnexpectedErrorOccurs() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getBody().getStatus());
        assertEquals("Internal Server Error", result.getBody().getError());
        assertEquals("An unexpected error occurred", result.getBody().getMessage());
        assertEquals("/api/customers", result.getBody().getPath());
    }

    @Test
    void handleGlobalExceptionShouldReturnGenericMessageWhenUnexpectedErrorOccurs() {
        // Given
        Exception exception = new Exception("Database connection failed");

        // When
        ResponseEntity<ErrorResponse> result = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("An unexpected error occurred", result.getBody().getMessage());
        // Should not expose the actual exception message for security
    }
}
