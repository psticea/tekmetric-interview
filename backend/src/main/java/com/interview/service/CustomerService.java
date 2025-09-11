package com.interview.service;

import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.entity.Customer;
import com.interview.exception.CustomerNotFoundException;
import com.interview.exception.DuplicateEmailException;
import com.interview.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service layer for Customer operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomers(int page, int size, String sortBy, String sortDir,
                                                   String firstName, String lastName, String email) {
        log.info("Fetching active customers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Customer> customers = customerRepository.findCustomersWithFilters(firstName, lastName, email, pageable);
        
        log.info("Found {} active customers", customers.getTotalElements());
        return customers.map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomersIncludingDeleted(int page, int size, String sortBy, String sortDir,
                                                                   String firstName, String lastName, String email, 
                                                                   boolean includeDeleted) {
        log.info("Fetching all customers (includeDeleted: {}) - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                includeDeleted, page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Customer> customers = customerRepository.findAllCustomersWithFilters(firstName, lastName, email, includeDeleted, pageable);
        
        log.info("Found {} customers (includeDeleted: {})", customers.getTotalElements(), includeDeleted);
        return customers.map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        log.info("Fetching active customer with id: {}", id);
        
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        log.info("Found customer: {}", customer.email());
        return mapToResponseDto(customer);
    }

    public CustomerResponseDto createCustomer(CustomerRequestDto request) {
        log.info("Creating new customer with email: {}", request.email());
        
        if (customerRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new DuplicateEmailException("Customer with email " + request.email() + " already exists");
        }

        Customer customer = Customer.createNew(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        );

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Successfully created customer with id: {}", savedCustomer.id());
        
        return mapToResponseDto(savedCustomer);
    }

    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto request) {
        log.info("Updating customer with id: {}", id);
        
        Customer existingCustomer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!existingCustomer.email().equals(request.email()) && 
            customerRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new DuplicateEmailException("Customer with email " + request.email() + " already exists");
        }

        Customer updatedCustomer = existingCustomer
                .withFirstName(request.firstName())
                .withLastName(request.lastName())
                .withEmail(request.email())
                .withPhoneNumber(request.phoneNumber())
                .withLastModified(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(updatedCustomer);
        log.info("Successfully updated customer with id: {}", savedCustomer.id());
        
        return mapToResponseDto(savedCustomer);
    }

    public void deleteCustomer(Long id) {
        log.info("Soft deleting customer with id: {}", id);
        
        Customer existingCustomer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        Customer deletedCustomer = existingCustomer
                .withDeleted(true)
                .withLastModified(LocalDateTime.now());

        customerRepository.save(deletedCustomer);
        log.info("Successfully soft deleted customer with id: {}", id);
    }

    private CustomerResponseDto mapToResponseDto(Customer customer) {
        return new CustomerResponseDto(
                customer.id(),
                customer.firstName(),
                customer.lastName(),
                customer.email(),
                customer.phoneNumber(),
                customer.createdAt(),
                customer.lastModified()
        );
    }
}
