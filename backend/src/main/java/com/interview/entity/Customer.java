package com.interview.entity;

import lombok.With;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Customer entity representing a customer in the system.
 */
@Entity
@Table(name = "customers")
@With
public record Customer(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        @Column(name = "first_name", nullable = false, length = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        @Column(name = "last_name", nullable = false, length = 50)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Column(name = "email", nullable = false, unique = true)
        String email,

        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        @Column(name = "phone_number", length = 15)
        String phoneNumber,

        @Column(name = "created_at", nullable = false, updatable = false)
        LocalDateTime createdAt,

        @Column(name = "last_modified", nullable = false)
        LocalDateTime lastModified,

        @Column(name = "deleted", nullable = false)
        Boolean deleted
) {
    public Customer {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastModified == null) {
            lastModified = LocalDateTime.now();
        }
        if (deleted == null) {
            deleted = false;
        }
    }

    // Factory method for creating new customers
    public static Customer createNew(String firstName, String lastName, String email, String phoneNumber) {
        LocalDateTime now = LocalDateTime.now();
        return new Customer(null, firstName, lastName, email, phoneNumber, now, now, false);
    }
}
