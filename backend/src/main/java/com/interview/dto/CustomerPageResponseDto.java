package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO for paginated customer responses to ensure stable JSON structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPageResponseDto {
    
    private List<CustomerResponseDto> customers;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    /**
     * Creates a customer page response from a Spring Data Page object.
     */
    public static CustomerPageResponseDto of(Page<CustomerResponseDto> page) {
        return new CustomerPageResponseDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
