package com.interview.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerPageResponseDtoTest {

    private CustomerResponseDto customerResponse1;
    private CustomerResponseDto customerResponse2;
    private Page<CustomerResponseDto> customerPage;

    @BeforeEach
    void setUp() {
        customerResponse1 = new CustomerResponseDto();
        customerResponse1.setId(1L);
        customerResponse1.setFirstName("John");
        customerResponse1.setLastName("Doe");
        customerResponse1.setEmail("john.doe@example.com");
        customerResponse1.setPhoneNumber("1234567890");
        customerResponse1.setCreatedAt(LocalDateTime.now());
        customerResponse1.setLastModified(LocalDateTime.now());

        customerResponse2 = new CustomerResponseDto();
        customerResponse2.setId(2L);
        customerResponse2.setFirstName("Jane");
        customerResponse2.setLastName("Smith");
        customerResponse2.setEmail("jane.smith@example.com");
        customerResponse2.setPhoneNumber("9876543210");
        customerResponse2.setCreatedAt(LocalDateTime.now());
        customerResponse2.setLastModified(LocalDateTime.now());

        List<CustomerResponseDto> customers = List.of(customerResponse1, customerResponse2);
        Pageable pageable = PageRequest.of(0, 10);
        customerPage = new PageImpl<>(customers, pageable, 25L);
    }

    // Tests for of() factory method
    @Test
    void ofShouldReturnCustomerPageResponseDtoWithCorrectContentWhenValidPage() {
        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(customerPage);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCustomers().size());
        assertEquals("John", result.getCustomers().get(0).getFirstName());
        assertEquals("Jane", result.getCustomers().get(1).getFirstName());
    }

    @Test
    void ofShouldReturnCustomerPageResponseDtoWithCorrectPaginationMetadataWhenValidPage() {
        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(customerPage);

        // Then
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 25 elements / 10 size = 3 pages
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.isHasNext());
        assertFalse(result.isHasPrevious());
    }

    @Test
    void ofShouldReturnCustomerPageResponseDtoWithCorrectLastPageMetadataWhenLastPage() {
        // Given
        List<CustomerResponseDto> customers = List.of(customerResponse1);
        Pageable pageable = PageRequest.of(2, 10); // Last page (0-indexed)
        Page<CustomerResponseDto> lastPage = new PageImpl<>(customers, pageable, 21L); // 21 elements = 3 pages (21/10 = 2.1 -> 3)

        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(lastPage);

        // Then
        assertEquals(2, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(21L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertTrue(result.isHasPrevious());
    }

    @Test
    void ofShouldReturnCustomerPageResponseDtoWithCorrectSinglePageMetadataWhenOnlyOnePage() {
        // Given
        List<CustomerResponseDto> customers = List.of(customerResponse1, customerResponse2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerResponseDto> singlePage = new PageImpl<>(customers, pageable, 2L);

        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(singlePage);

        // Then
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertFalse(result.isHasPrevious());
    }

    @Test
    void ofShouldReturnCustomerPageResponseDtoWithEmptyContentWhenEmptyPage() {
        // Given
        List<CustomerResponseDto> emptyCustomers = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerResponseDto> emptyPage = new PageImpl<>(emptyCustomers, pageable, 0L);

        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(emptyPage);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getCustomers().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertFalse(result.isHasPrevious());
    }

    @Test
    void ofShouldReturnCustomerPageResponseDtoWithCorrectMiddlePageMetadataWhenMiddlePage() {
        // Given
        List<CustomerResponseDto> customers = List.of(customerResponse1);
        Pageable pageable = PageRequest.of(1, 10); // Middle page (0-indexed)
        Page<CustomerResponseDto> middlePage = new PageImpl<>(customers, pageable, 25L);

        // When
        CustomerPageResponseDto result = CustomerPageResponseDto.of(middlePage);

        // Then
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.isHasNext());
        assertTrue(result.isHasPrevious());
    }
}
