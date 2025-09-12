package com.interview.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
    }

    // Tests for createNew factory method
    @Test
    void createNewShouldReturnCustomerWithCorrectFieldsWhenValidParameters() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String phoneNumber = "1234567890";

        // When
        Customer result = Customer.createNew(firstName, lastName, email, phoneNumber);

        // Then
        assertNotNull(result);
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(email, result.getEmail());
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals(false, result.getDeleted());
        assertNull(result.getId()); // ID should be null before persistence
        assertNull(result.getCreatedAt()); // Will be set by @PrePersist
        assertNull(result.getLastModified()); // Will be set by @PrePersist
    }

    @Test
    void createNewShouldReturnCustomerWithDeletedFalseWhenValidParameters() {
        // Given
        String firstName = "Jane";
        String lastName = "Smith";
        String email = "jane.smith@example.com";
        String phoneNumber = "9876543210";

        // When
        Customer result = Customer.createNew(firstName, lastName, email, phoneNumber);

        // Then
        assertFalse(result.getDeleted());
    }

    @Test
    void createNewShouldReturnCustomerWithNullPhoneNumberWhenPhoneNumberNotProvided() {
        // Given
        String firstName = "Bob";
        String lastName = "Johnson";
        String email = "bob.johnson@example.com";
        String phoneNumber = null;

        // When
        Customer result = Customer.createNew(firstName, lastName, email, phoneNumber);

        // Then
        assertNull(result.getPhoneNumber());
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(email, result.getEmail());
    }

    // Tests for onCreate method (PrePersist)
    @Test
    void onCreateShouldSetTimestampsAndDeletedFlagWhenCalled() {
        // Given
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");

        // When
        customer.onCreate();

        // Then
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getLastModified());
        assertEquals(false, customer.getDeleted());
    }

    @Test
    void onCreateShouldNotOverrideExistingTimestampsWhenTimestampsAlreadySet() {
        // Given
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime existingLastModified = LocalDateTime.now().minusHours(1);
        
        customer.setCreatedAt(existingCreatedAt);
        customer.setLastModified(existingLastModified);
        customer.setDeleted(true);

        // When
        customer.onCreate();

        // Then
        assertEquals(existingCreatedAt, customer.getCreatedAt());
        assertEquals(existingLastModified, customer.getLastModified());
        assertEquals(true, customer.getDeleted()); // Should preserve existing deleted value
    }

    @Test
    void onCreateShouldSetDeletedToFalseWhenDeletedIsNull() {
        // Given
        customer.setDeleted(null);

        // When
        customer.onCreate();

        // Then
        assertEquals(false, customer.getDeleted());
    }

    // Tests for onUpdate method (PreUpdate)
    @Test
    void onUpdateShouldUpdateLastModifiedTimestampWhenCalled() {
        // Given
        LocalDateTime originalLastModified = LocalDateTime.now().minusHours(1);
        customer.setLastModified(originalLastModified);

        // When
        customer.onUpdate();

        // Then
        assertNotNull(customer.getLastModified());
        assertTrue(customer.getLastModified().isAfter(originalLastModified));
    }

    @Test
    void onUpdateShouldSetLastModifiedWhenLastModifiedIsNull() {
        // Given
        customer.setLastModified(null);

        // When
        customer.onUpdate();

        // Then
        assertNotNull(customer.getLastModified());
    }
}
