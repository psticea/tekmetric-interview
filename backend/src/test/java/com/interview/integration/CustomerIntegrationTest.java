package com.interview.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.CustomerPageResponseDto;
import com.interview.dto.CustomerRequestDto;
import com.interview.dto.CustomerResponseDto;
import com.interview.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Customer REST API endpoints.
 * Tests full end-to-end workflows through HTTP layer with security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequestDto validCustomerRequest;

    @BeforeEach
    void setUp() {
        validCustomerRequest = new CustomerRequestDto();
        validCustomerRequest.setFirstName("John");
        validCustomerRequest.setLastName("Doe");
        validCustomerRequest.setEmail("john.doe@integration-test.com");
        validCustomerRequest.setPhoneNumber("1234567890");
    }

    private String createAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    // Test complete customer lifecycle workflow
    @Test
    void customerLifecycleShouldWorkEndToEndThroughRestApi() throws Exception {
        // 1. Create customer (requires admin)
        MvcResult createResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", createAuthHeader("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@integration-test.com"))
                .andReturn();

        // Extract created customer ID
        String responseBody = createResult.getResponse().getContentAsString();
        CustomerResponseDto createdCustomer = objectMapper.readValue(responseBody, CustomerResponseDto.class);
        Long customerId = createdCustomer.getId();
        assertNotNull(customerId);

        // 2. Retrieve customer (user can access)
        mockMvc.perform(get("/api/customers/{id}", customerId)
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@integration-test.com"));

        // 3. Update customer (requires admin)
        CustomerRequestDto updateRequest = new CustomerRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@integration-test.com");
        updateRequest.setPhoneNumber("9876543210");

        mockMvc.perform(put("/api/customers/{id}", customerId)
                .header("Authorization", createAuthHeader("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane.smith@integration-test.com"));

        // 4. Delete customer (requires admin)
        mockMvc.perform(delete("/api/customers/{id}", customerId)
                .header("Authorization", createAuthHeader("admin", "admin")))
                .andExpect(status().isNoContent());

        // 5. Verify customer is deleted (should return 404)
        mockMvc.perform(get("/api/customers/{id}", customerId)
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isNotFound());
    }

    // Test business rule validation - duplicate email
    @Test
    void duplicateEmailValidationShouldWorkAcrossFullStack() throws Exception {
        // Create first customer
        mockMvc.perform(post("/api/customers")
                .header("Authorization", createAuthHeader("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isCreated());

        // Try to create second customer with same email
        CustomerRequestDto duplicateRequest = new CustomerRequestDto();
        duplicateRequest.setFirstName("Jane");
        duplicateRequest.setLastName("Smith");
        duplicateRequest.setEmail("john.doe@integration-test.com"); // Same email
        duplicateRequest.setPhoneNumber("9876543210");

        MvcResult errorResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", createAuthHeader("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andReturn();

        // Verify error response structure
        String errorResponseBody = errorResult.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(errorResponseBody, ErrorResponse.class);
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Conflict", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("john.doe@integration-test.com"));
    }

    // Test pagination workflow
    @Test
    void paginationShouldWorkWithCorrectMetadata() throws Exception {
        // Create multiple customers for pagination testing
        for (int i = 1; i <= 15; i++) {
            CustomerRequestDto request = new CustomerRequestDto();
            request.setFirstName("Customer" + i);
            request.setLastName("Test");
            request.setEmail("customer" + i + "@pagination-test.com");
            request.setPhoneNumber("123456789" + (i % 10));

            mockMvc.perform(post("/api/customers")
                    .header("Authorization", createAuthHeader("admin", "admin"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Test first page with pagination
        MvcResult firstPageResult = mockMvc.perform(get("/api/customers")
                .param("page", "1")
                .param("pageSize", "5")
                .param("sortBy", "firstName")
                .param("sortDir", "asc")
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0)) // 0-based in response
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers.length()").value(5))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andReturn();

        // Verify pagination metadata
        String responseBody = firstPageResult.getResponse().getContentAsString();
        CustomerPageResponseDto pageResponse = objectMapper.readValue(responseBody, CustomerPageResponseDto.class);
        assertTrue(pageResponse.getTotalElements() >= 15);
        assertTrue(pageResponse.getTotalPages() >= 3);
    }

    // Test security authentication and authorization
    @Test
    void securityRulesShouldBeEnforcedAcrossAllEndpoints() throws Exception {
        // Test unauthorized access
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());

        // Test user can read but not create/update/delete
        mockMvc.perform(get("/api/customers")
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                .header("Authorization", createAuthHeader("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isForbidden());

        // Create a customer with admin for further security tests
        MvcResult createResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", createAuthHeader("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        CustomerResponseDto customer = objectMapper.readValue(responseBody, CustomerResponseDto.class);

        // Test user cannot update or delete
        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                .header("Authorization", createAuthHeader("user", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/customers/{id}", customer.getId())
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isForbidden());
    }

    // Test error handling for edge cases
    @Test
    void edgeCasesShouldReturnProperErrors() throws Exception {
        // Test non-existent customer
        mockMvc.perform(get("/api/customers/99999")
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99999"));

        // Test invalid pagination parameters
        mockMvc.perform(get("/api/customers")
                .param("page", "0") // Should be at least 1
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "invalidField") // Invalid sort field
                .header("Authorization", createAuthHeader("user", "password")))
                .andExpect(status().isBadRequest());
    }
}
