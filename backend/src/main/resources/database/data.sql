-- Customer table creation and sample data
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Insert sample customer data
INSERT INTO customers (first_name, last_name, email, phone_number, created_at, last_modified, deleted) VALUES
('John', 'Doe', 'john.doe@example.com', '555-1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
('Jane', 'Smith', 'jane.smith@example.com', '555-5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
('Bob', 'Johnson', 'bob.johnson@example.com', '555-9012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
('Alice', 'Williams', 'alice.williams@example.com', '555-3456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
('Charlie', 'Brown', 'charlie.brown@example.com', '555-7890', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
('Test', 'Deleted', 'deleted.user@example.com', '555-0000', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);