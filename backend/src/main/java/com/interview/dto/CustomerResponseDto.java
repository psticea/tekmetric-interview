package com.interview.dto;

import java.time.LocalDateTime;

/**
 * DTO for Customer API responses.
 */
public record CustomerResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime lastModified
) {}