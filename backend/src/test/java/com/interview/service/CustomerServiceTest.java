package com.interview.service;

import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.entity.Customer;
import com.interview.exception.CustomerNotFoundException;
import com.interview.exception.DuplicateEmailException;
import com.interview.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDto validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRequestDto();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhoneNumber("1234567890");

        savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setEmail("john.doe@example.com");
        savedCustomer.setPhoneNumber("1234567890");
        savedCustomer.setCreatedAt(LocalDateTime.now());
        savedCustomer.setLastModified(LocalDateTime.now());
        savedCustomer.setDeleted(false);
    }

    @Test
    void createCustomerShouldReturnCustomerResponseDtoWhenValidRequestAndUniqueEmail() {
        // Given
        when(customerRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerResponseDto result = customerService.createCustomer(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getLastModified());

        verify(customerRepository).existsByEmailAndDeletedFalse("john.doe@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomerShouldThrowDuplicateEmailExceptionWhenEmailAlreadyExists() {
        // Given
        when(customerRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerService.createCustomer(validRequest)
        );

        assertEquals("Customer with email john.doe@example.com already exists", exception.getMessage());
        
        verify(customerRepository).existsByEmailAndDeletedFalse("john.doe@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // Tests for getAllCustomers method
    @Test
    void getAllCustomersShouldReturnPagedResultsWhenValidParameters() {
        // Given
        List<Customer> customerList = List.of(savedCustomer);
        Page<Customer> customerPage = new PageImpl<>(customerList);
        when(customerRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(customerPage);

        // When
        Page<CustomerResponseDto> result = customerService.getAllCustomers(0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(customerRepository).findByDeletedFalse(any(Pageable.class));
    }

    @Test
    void getAllCustomersShouldReturnDescendingSortedResultsWhenDescSortDirection() {
        // Given
        List<Customer> customerList = List.of(savedCustomer);
        Page<Customer> customerPage = new PageImpl<>(customerList);
        when(customerRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(customerPage);

        // When
        customerService.getAllCustomers(0, 10, "firstName", "desc");

        // Then
        verify(customerRepository).findByDeletedFalse(argThat(pageable -> 
            pageable.getSort().isSorted() && 
            pageable.getSort().getOrderFor("firstName").getDirection() == Sort.Direction.DESC
        ));
    }

    // Tests for getCustomerById method
    @Test
    void getCustomerByIdShouldReturnCustomerDtoWhenValidId() {
        // Given
        when(customerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(savedCustomer));

        // When
        CustomerResponseDto result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(customerRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void getCustomerByIdShouldThrowNotFoundExceptionWhenInvalidId() {
        // Given
        when(customerRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(999L);
    }

    // Tests for updateCustomer method
    @Test
    void updateCustomerShouldReturnUpdatedDtoWhenValidData() {
        // Given
        CustomerRequestDto updateRequest = new CustomerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setPhoneNumber("9876543210");

        when(customerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByEmailAndDeletedFalse("jane.smith@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(1L);
            return customer;
        });

        // When
        CustomerResponseDto result = customerService.updateCustomer(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhoneNumber());
        verify(customerRepository).findByIdAndDeletedFalse(1L);
        verify(customerRepository).existsByEmailAndDeletedFalse("jane.smith@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomerShouldThrowNotFoundExceptionWhenInvalidId() {
        // Given
        CustomerRequestDto updateRequest = new CustomerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setPhoneNumber("9876543210");

        when(customerRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.updateCustomer(999L, updateRequest)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomerShouldThrowExceptionWhenEmailExistsOnOtherCustomer() {
        // Given
        CustomerRequestDto updateRequest = new CustomerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("existing@example.com");
        updateRequest.setPhoneNumber("9876543210");

        when(customerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByEmailAndDeletedFalse("existing@example.com")).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerService.updateCustomer(1L, updateRequest)
        );

        assertEquals("Customer with email existing@example.com already exists", exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(1L);
        verify(customerRepository).existsByEmailAndDeletedFalse("existing@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomerShouldAllowSameEmailWhenEmailNotChanged() {
        // Given
        CustomerRequestDto updateRequest = new CustomerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("john.doe@example.com"); // Same email
        updateRequest.setPhoneNumber("9876543210");

        when(customerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(1L);
            return customer;
        });

        // When
        CustomerResponseDto result = customerService.updateCustomer(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(customerRepository).findByIdAndDeletedFalse(1L);
        verify(customerRepository, never()).existsByEmailAndDeletedFalse(anyString());
        verify(customerRepository).save(any(Customer.class));
    }

    // Tests for deleteCustomer method
    @Test
    void deleteCustomerShouldSetDeletedFlagWhenCustomerExists() {
        // Given
        when(customerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        customerService.deleteCustomer(1L);

        // Then
        assertTrue(savedCustomer.getDeleted());
        assertNotNull(savedCustomer.getLastModified());
        verify(customerRepository).findByIdAndDeletedFalse(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void deleteCustomerShouldThrowNotFoundExceptionWhenInvalidId() {
        // Given
        when(customerRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(999L)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
