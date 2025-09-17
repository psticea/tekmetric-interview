package com.interview.controller;

import com.interview.dto.CustomerPageResponseDto;
import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI specification interface for Customer Management endpoints.
 * Contains all Swagger/OpenAPI documentation separated from controller implementation.
 */
@Tag(name = "Customer Management", description = "APIs for managing customer information")
public interface CustomerApi {

    /**
     * Retrieves a paginated list of all active customers with optional sorting.
     * 
     * @param page The page number (1-based) to retrieve. Must be >= 1.
     * @param pageSize The number of customers per page. Must be between 1 and 100.
     * @param sortBy The field to sort by. Valid values: id, firstName, lastName, email, phoneNumber, createdAt, updatedAt.
     * @param sortDir The sort direction. Valid values: asc, desc.
     * @return A ResponseEntity containing the paginated customer data and metadata.
     */
    @Operation(summary = "Get all customers", description = "Retrieve a paginated list of all active customers with sorting options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customers",
                    content = @Content(schema = @Schema(implementation = CustomerPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden access")
    })
    ResponseEntity<CustomerPageResponseDto> getAllCustomers(
            @Parameter(description = "Page number (1-based)", example = "1")
            @Min(value = 1, message = "Page number must be at least 1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @Min(value = 1, message = "Page size must be at least 1") 
            @Max(value = 100, message = "Page size must not exceed 100") int pageSize,
            @Parameter(description = "Field to sort by", example = "id")
            @Pattern(regexp = "^(id|firstName|lastName|email|phoneNumber|createdAt|updatedAt)$", 
                    message = "Invalid sort field") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'") String sortDir);

    /**
     * Retrieves a specific customer by their unique identifier.
     * 
     * @param id The unique identifier of the customer. Must be a positive number.
     * @return A ResponseEntity containing the customer data if found.
     */
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customer",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden access")
    })
    ResponseEntity<CustomerResponseDto> getCustomerById(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @Min(value = 1, message = "Customer ID must be positive") Long id);

    /**
     * Creates a new customer with the provided information.
     * 
     * @param request The customer data for creation. Must contain valid firstName, lastName, 
     *                email, and optional phoneNumber. Email must be unique among active customers.
     * @return A ResponseEntity containing the created customer data with generated ID and timestamps.
     */
    @Operation(summary = "Create new customer", description = "Create a new customer with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer data or validation errors"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    ResponseEntity<CustomerResponseDto> createCustomer(
            @Parameter(description = "Customer information", required = true)
            @Valid CustomerRequestDto request);

    /**
     * Updates an existing customer's information.
     * 
     * @param id The unique identifier of the customer to update. Must be a positive number.
     * @param request The updated customer data. Must contain valid firstName, lastName, 
     *                email, and optional phoneNumber. Email must be unique among active customers
     *                (excluding the customer being updated).
     * @return A ResponseEntity containing the updated customer data with new lastModified timestamp.
     */
    @Operation(summary = "Update customer", description = "Update an existing customer's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data or validation errors"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    ResponseEntity<CustomerResponseDto> updateCustomer(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @Min(value = 1, message = "Customer ID must be positive") Long id,
            @Parameter(description = "Updated customer information", required = true)
            @Valid CustomerRequestDto request);

    /**
     * Soft deletes a customer by setting the deleted flag to true.
     * 
     * @param id The unique identifier of the customer to delete. Must be a positive number.
     * @return A ResponseEntity with no content on successful deletion.
     */
    @Operation(summary = "Delete customer", description = "Soft delete a customer by setting the deleted flag to true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @Min(value = 1, message = "Customer ID must be positive") Long id);
}
