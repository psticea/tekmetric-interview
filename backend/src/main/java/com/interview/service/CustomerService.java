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

    /**
     * Retrieves a paginated list of active customers with optional sorting.
     * 
     * @param page The page number (0-based) to retrieve.
     * @param size The number of customers per page.
     * @param sortBy The field to sort by (id, firstName, lastName, email, phoneNumber, createdAt, updatedAt).
     * @param sortDir The sort direction (asc or desc).
     * @return A Page containing CustomerResponseDto objects for active customers.
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomers(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching active customers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Customer> customers = customerRepository.findByDeletedFalse(pageable);
        
        log.info("Found {} active customers", customers.getTotalElements());
        return customers.map(this::mapToResponseDto);
    }


    /**
     * Retrieves a specific active customer by their unique identifier.
     * 
     * @param id The unique identifier of the customer.
     * @return A CustomerResponseDto containing the customer's information.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     */
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        log.info("Fetching active customer with id: {}", id);
        
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        log.info("Found customer: {}", customer.getEmail());
        return mapToResponseDto(customer);
    }

    /**
     * Creates a new customer with the provided information.
     * 
     * @param request The customer data containing firstName, lastName, email, and optional phoneNumber.
     * @return A CustomerResponseDto containing the created customer's information with generated ID and timestamps.
     * @throws DuplicateEmailException if a customer with the same email already exists and is active.
     */
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

    /**
     * Updates an existing customer's information.
     * 
     * @param id The unique identifier of the customer to update.
     * @param request The updated customer data containing firstName, lastName, email, and optional phoneNumber.
     * @return A CustomerResponseDto containing the updated customer's information with new lastModified timestamp.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     * @throws DuplicateEmailException if another active customer already uses the provided email address.
     */
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

    /**
     * Performs a soft delete of a customer by marking them as deleted.
     * The customer record is preserved in the database but will be excluded from normal queries.
     * 
     * @param id The unique identifier of the customer to delete.
     * @throws CustomerNotFoundException if no active customer exists with the given ID.
     */
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
