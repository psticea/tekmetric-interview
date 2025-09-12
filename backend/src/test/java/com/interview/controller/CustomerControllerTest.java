package com.interview.controller;

import com.interview.dto.CustomerPageResponseDto;
import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.exception.CustomerNotFoundException;
import com.interview.exception.DuplicateEmailException;
import com.interview.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerRequestDto validRequest;
    private CustomerResponseDto customerResponse;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRequestDto();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhoneNumber("1234567890");

        customerResponse = new CustomerResponseDto();
        customerResponse.setId(1L);
        customerResponse.setFirstName("John");
        customerResponse.setLastName("Doe");
        customerResponse.setEmail("john.doe@example.com");
        customerResponse.setPhoneNumber("1234567890");
        customerResponse.setCreatedAt(LocalDateTime.now());
        customerResponse.setLastModified(LocalDateTime.now());
    }

    // Tests for getAllCustomers endpoint
    @Test
    void getAllCustomersShouldReturnCustomerPageResponseDtoWhenValidParameters() {
        // Given
        Page<CustomerResponseDto> customerPage = new PageImpl<>(List.of(customerResponse));
        when(customerService.getAllCustomers(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(customerPage);

        // When
        ResponseEntity<CustomerPageResponseDto> result = customerController.getAllCustomers(1, 10, "id", "desc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getCustomers().size());
        assertEquals("John", result.getBody().getCustomers().get(0).getFirstName());
        verify(customerService).getAllCustomers(0, 10, "id", "desc"); // Page is 0-based
    }

    @Test
    void getAllCustomersShouldCallServiceWithZeroBasedPageWhenPageParameterProvided() {
        // Given
        Page<CustomerResponseDto> customerPage = new PageImpl<>(List.of(customerResponse));
        when(customerService.getAllCustomers(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(customerPage);

        // When
        customerController.getAllCustomers(3, 20, "firstName", "asc");

        // Then
        verify(customerService).getAllCustomers(2, 20, "firstName", "asc"); // 3-1=2 for 0-based
    }

    // Tests for getCustomerById endpoint
    @Test
    void getCustomerByIdShouldReturnCustomerResponseDtoWhenValidId() {
        // Given
        when(customerService.getCustomerById(1L)).thenReturn(customerResponse);

        // When
        ResponseEntity<CustomerResponseDto> result = customerController.getCustomerById(1L);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("John", result.getBody().getFirstName());
        verify(customerService).getCustomerById(1L);
    }

    @Test
    void getCustomerByIdShouldPropagateNotFoundExceptionWhenCustomerNotFound() {
        // Given
        when(customerService.getCustomerById(999L))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 999"));

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerController.getCustomerById(999L)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerService).getCustomerById(999L);
    }

    // Tests for createCustomer endpoint
    @Test
    void createCustomerShouldReturnCreatedStatusWhenValidRequest() {
        // Given
        when(customerService.createCustomer(any(CustomerRequestDto.class))).thenReturn(customerResponse);

        // When
        ResponseEntity<CustomerResponseDto> result = customerController.createCustomer(validRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        verify(customerService).createCustomer(validRequest);
    }

    @Test
    void createCustomerShouldPropagateDuplicateEmailExceptionWhenEmailExists() {
        // Given
        when(customerService.createCustomer(any(CustomerRequestDto.class)))
                .thenThrow(new DuplicateEmailException("Customer with email john.doe@example.com already exists"));

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerController.createCustomer(validRequest)
        );

        assertEquals("Customer with email john.doe@example.com already exists", exception.getMessage());
        verify(customerService).createCustomer(validRequest);
    }

    // Tests for updateCustomer endpoint
    @Test
    void updateCustomerShouldReturnOkStatusWhenValidRequest() {
        // Given
        when(customerService.updateCustomer(anyLong(), any(CustomerRequestDto.class))).thenReturn(customerResponse);

        // When
        ResponseEntity<CustomerResponseDto> result = customerController.updateCustomer(1L, validRequest);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        verify(customerService).updateCustomer(1L, validRequest);
    }

    @Test
    void updateCustomerShouldPropagateNotFoundExceptionWhenCustomerNotFound() {
        // Given
        when(customerService.updateCustomer(anyLong(), any(CustomerRequestDto.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 999"));

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerController.updateCustomer(999L, validRequest)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerService).updateCustomer(999L, validRequest);
    }

    @Test
    void updateCustomerShouldPropagateDuplicateEmailExceptionWhenEmailExists() {
        // Given
        when(customerService.updateCustomer(anyLong(), any(CustomerRequestDto.class)))
                .thenThrow(new DuplicateEmailException("Customer with email existing@example.com already exists"));

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> customerController.updateCustomer(1L, validRequest)
        );

        assertEquals("Customer with email existing@example.com already exists", exception.getMessage());
        verify(customerService).updateCustomer(1L, validRequest);
    }

    // Tests for deleteCustomer endpoint
    @Test
    void deleteCustomerShouldReturnNoContentStatusWhenValidId() {
        // Given
        doNothing().when(customerService).deleteCustomer(anyLong());

        // When
        ResponseEntity<Void> result = customerController.deleteCustomer(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void deleteCustomerShouldPropagateNotFoundExceptionWhenCustomerNotFound() {
        // Given
        doThrow(new CustomerNotFoundException("Customer not found with id: 999"))
                .when(customerService).deleteCustomer(anyLong());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerController.deleteCustomer(999L)
        );

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerService).deleteCustomer(999L);
    }
}
