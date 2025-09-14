package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Paginated customer response")
public class CustomerPageResponseDto {
    
    @Schema(description = "List of customers")
    private List<CustomerResponseDto> customers;
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;
    
    @Schema(description = "Page size", example = "10")
    private int size;
    
    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Whether there is a next page", example = "true")
    private boolean hasNext;
    
    @Schema(description = "Whether there is a previous page", example = "false")
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
