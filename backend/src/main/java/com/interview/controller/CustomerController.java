package com.interview.controller;

import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.dto.CustomerPageResponseDto;
import com.interview.security.RequireAdmin;
import com.interview.service.CustomerService;
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
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerPageResponseDto> getAllCustomers(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be at least 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int pageSize,
            @RequestParam(defaultValue = "id") @Pattern(regexp = "^(id|firstName|lastName|email|phoneNumber|createdAt|updatedAt)$", message = "Invalid sort field") String sortBy,
            @RequestParam(defaultValue = "desc") @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'") String sortDir) {
        
        log.info("GET /api/customers - page: {}, pageSize: {}, sortBy: {}, sortDir: {}", page, pageSize, sortBy, sortDir);
        
        Page<CustomerResponseDto> customers = customerService.getAllCustomers(
                page - 1, pageSize, sortBy, sortDir);
        
        log.info("Returning {} active customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return ResponseEntity.ok(CustomerPageResponseDto.of(customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id) {
        log.info("GET /api/customers/{}", id);
        
        CustomerResponseDto customer = customerService.getCustomerById(id);
        
        log.info("Returning customer with id: {}", id);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    @RequireAdmin
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto request) {
        log.info("POST /api/customers - creating customer with email: {}", request.getEmail());
        
        CustomerResponseDto customer = customerService.createCustomer(request);
        
        log.info("Successfully created customer with id: {}", customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id, 
            @Valid @RequestBody CustomerRequestDto request) {
        
        log.info("PUT /api/customers/{} - updating customer", id);
        
        CustomerResponseDto customer = customerService.updateCustomer(id, request);
        
        log.info("Successfully updated customer with id: {}", id);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<Void> deleteCustomer(@PathVariable @Min(value = 1, message = "Customer ID must be positive") Long id) {
        log.info("DELETE /api/customers/{}", id);
        
        customerService.deleteCustomer(id);
        
        log.info("Successfully deleted customer with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
