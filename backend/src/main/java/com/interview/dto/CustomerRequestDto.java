package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Customer creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer request data")
public class CustomerRequestDto {
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Schema(description = "Customer's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    @Schema(description = "Customer's phone number", example = "+1234567890")
    private String phoneNumber;
}
