package com.interview.controller;

import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.dto.CustomerPageResponseDto;
import com.interview.security.RequireAdmin;
import com.interview.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

/**
 * REST controller for Customer operations.
 * Provides CRUD operations for customer management with API versioning.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Customer Management", description = "APIs for managing customer information")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Retrieves a paginated list of all active customers with optional sorting.
     * 
     * @param page The page number (1-based) to retrieve. Must be >= 1.
     * @param pageSize The number of customers per page. Must be between 1 and 100.
     * @param sortBy The field to sort by. Valid values: id, firstName, lastName, email, phoneNumber, createdAt, updatedAt.
     * @param sortDir The sort direction. Valid values: asc, desc.
     * @return A ResponseEntity containing the paginated customer data and metadata.
     * @throws AccessDeniedException if the user lacks USER or ADMIN role.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get all customers", description = "Retrieve a paginated list of all active customers with sorting options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customers",
                    content = @Content(schema = @Schema(implementation = CustomerPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden access")
    })
    public ResponseEntity<CustomerPageResponseDto> getAllCustomers(
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be at least 1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int pageSize,
            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(defaultValue = "id") @Pattern(regexp = "^(id|firstName|lastName|email|phoneNumber|createdAt|updatedAt)$", message = "Invalid sort field") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'") String sortDir) {
        
        log.info("GET /api/v1/customers - page: {}, pageSize: {}, sortBy: {}, sortDir: {}", page, pageSize, sortBy, sortDir);
        
        Page<CustomerResponseDto> customers = customerService.getAllCustomers(
                page - 1, pageSize, sortBy, sortDir);
        
        log.info("Returning {} active customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return ResponseEntity.ok(CustomerPageResponseDto.of(customers));
    }

    /**
     * Retrieves a specific customer by their unique identifier.
     * 
     * @param id The unique identifier of the customer. Must be a positive number.
     * @return A ResponseEntity containing the customer data if found.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     * @throws AccessDeniedException if the user lacks USER or ADMIN role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customer",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Forbidden access")
    })
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id) {
        log.info("GET /api/v1/customers/{}", id);
        
        CustomerResponseDto customer = customerService.getCustomerById(id);
        
        log.info("Returning customer with id: {}", id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Creates a new customer with the provided information.
     * 
     * @param request The customer data for creation. Must contain valid firstName, lastName, 
     *                email, and optional phoneNumber. Email must be unique among active customers.
     * @return A ResponseEntity containing the created customer data with generated ID and timestamps.
     * @throws DuplicateEmailException if a customer with the same email already exists.
     * @throws AccessDeniedException if the user lacks ADMIN role.
     */
    @PostMapping
    @RequireAdmin
    @Operation(summary = "Create new customer", description = "Create a new customer with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer data or validation errors"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @Parameter(description = "Customer information", required = true)
            @Valid @RequestBody CustomerRequestDto request) {
        log.info("POST /api/v1/customers - creating customer with email: {}", request.getEmail());
        
        CustomerResponseDto customer = customerService.createCustomer(request);
        
        log.info("Successfully created customer with id: {}", customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    /**
     * Updates an existing customer's information.
     * 
     * @param id The unique identifier of the customer to update. Must be a positive number.
     * @param request The updated customer data. Must contain valid firstName, lastName, 
     *                email, and optional phoneNumber. Email must be unique among active customers
     *                (excluding the customer being updated).
     * @return A ResponseEntity containing the updated customer data with new lastModified timestamp.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     * @throws DuplicateEmailException if another customer already uses the provided email.
     * @throws AccessDeniedException if the user lacks ADMIN role.
     */
    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "Update customer", description = "Update an existing customer's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer data or validation errors"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id,
            @Parameter(description = "Updated customer information", required = true)
            @Valid @RequestBody CustomerRequestDto request) {
        
        log.info("PUT /api/v1/customers/{} - updating customer", id);
        
        CustomerResponseDto customer = customerService.updateCustomer(id, request);
        
        log.info("Successfully updated customer with id: {}", id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Performs a soft delete of a customer by marking them as deleted.
     * The customer record is preserved in the database but excluded from normal queries.
     * 
     * @param id The unique identifier of the customer to delete. Must be a positive number.
     * @return A ResponseEntity with no content (204 No Content) if deletion is successful.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     * @throws AccessDeniedException if the user lacks ADMIN role.
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "Delete customer", description = "Soft delete a customer by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id) {
        log.info("DELETE /api/v1/customers/{}", id);
        
        customerService.deleteCustomer(id);
        
        log.info("Successfully deleted customer with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
