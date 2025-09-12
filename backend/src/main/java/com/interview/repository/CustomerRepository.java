package com.interview.repository;

import com.interview.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);

    Page<Customer> findByDeletedFalse(Pageable pageable);

}
