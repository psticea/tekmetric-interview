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
        
        log.info("Found customer: {}", customer.getEmail());
        return mapToResponseDto(customer);
    }

    public CustomerResponseDto createCustomer(CustomerRequestDto request) {
        log.info("Creating new customer with email: {}", request.getEmail());
        
        if (customerRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new DuplicateEmailException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = Customer.createNew(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Successfully created customer with id: {}", savedCustomer.getId());
        
        return mapToResponseDto(savedCustomer);
    }

    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto request) {
        log.info("Updating customer with id: {}", id);
        
        Customer existingCustomer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!existingCustomer.getEmail().equals(request.getEmail()) && 
            customerRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new DuplicateEmailException("Customer with email " + request.getEmail() + " already exists");
        }

        existingCustomer.setFirstName(request.getFirstName());
        existingCustomer.setLastName(request.getLastName());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setPhoneNumber(request.getPhoneNumber());
        existingCustomer.setLastModified(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(existingCustomer);
        log.info("Successfully updated customer with id: {}", savedCustomer.getId());
        
        return mapToResponseDto(savedCustomer);
    }

    public void deleteCustomer(Long id) {
        log.info("Soft deleting customer with id: {}", id);
        
        Customer existingCustomer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        existingCustomer.setDeleted(true);
        existingCustomer.setLastModified(LocalDateTime.now());

        customerRepository.save(existingCustomer);
        log.info("Successfully soft deleted customer with id: {}", id);
    }

    private CustomerResponseDto mapToResponseDto(Customer customer) {
        CustomerResponseDto dto = new CustomerResponseDto();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setLastModified(customer.getLastModified());
        return dto;
    }
}
