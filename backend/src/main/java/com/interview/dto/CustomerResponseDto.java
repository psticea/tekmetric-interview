package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Customer API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer response data")
public class CustomerResponseDto {
    @Schema(description = "Customer ID", example = "1")
    private Long id;
    
    @Schema(description = "Customer's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Customer's phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Customer creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Customer last modification timestamp")
    private LocalDateTime lastModified;
}