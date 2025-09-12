package com.interview.repository;

import com.interview.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    Optional<Customer> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    Page<Customer> findByDeletedFalse(Pageable pageable);


    // Admin method to access all records including deleted ones
    @Query("SELECT c FROM Customer c WHERE :includeDeleted = true OR c.deleted = false")
    Page<Customer> findAllCustomers(
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );
}
