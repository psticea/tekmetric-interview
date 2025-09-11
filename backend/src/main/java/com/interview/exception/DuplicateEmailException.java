package com.interview.exception;

/**
 * Exception thrown when attempting to create or update a customer with an email that already exists.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
}
