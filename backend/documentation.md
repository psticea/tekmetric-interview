# Customer Management API Documentation

## Overview

A RESTful Customer Management API built with Spring Boot 3.3.4 featuring CRUD operations, security, validation, pagination, and comprehensive documentation.

## Architecture Overview

```mermaid
graph TB
    Client[Client Applications] --> LB[Load Balancer]
    LB --> API[Customer Management API]
    API --> Security[Spring Security]
    Security --> Controller[CustomerController]
    Controller --> Service[CustomerService]
    Service --> Repository[CustomerRepository]
    Repository --> DB[(H2 Database)]
    
    API --> Actuator[Spring Boot Actuator]
    API --> Swagger[OpenAPI/Swagger]
    API --> Admin[Spring Boot Admin]
    
    subgraph "Monitoring & Documentation"
        Actuator
        Swagger
        Admin
    end
    
    subgraph "Security Layer"
        Security
        Auth[Basic Authentication]
        RBAC[Role-Based Access Control]
    end
    
    subgraph "Business Logic"
        Controller
        Service
        Repository
    end
```

## API Flow Diagram

```mermaid
sequenceDiagram
    participant C as Client
    participant F as Logging Filter
    participant S as Spring Security
    participant Ctrl as CustomerController
    participant Svc as CustomerService
    participant Repo as CustomerRepository
    participant DB as H2 Database

    C->>F: HTTP Request
    F->>F: Log Request Details
    F->>S: Request
    S->>S: Authenticate & Authorize
    S->>Ctrl: Authorized Request
    Ctrl->>Ctrl: Validate Input Parameters
    Ctrl->>Svc: Business Logic Call
    Svc->>Svc: Business Validation
    Svc->>Repo: Data Access
    Repo->>DB: SQL Query
    DB-->>Repo: Result Set
    Repo-->>Svc: Entity Objects
    Svc->>Svc: Map to DTO
    Svc-->>Ctrl: Response DTO
    Ctrl-->>S: HTTP Response
    S-->>F: Response with Headers
    F->>F: Log Request/Response
    F-->>C: HTTP Response
```

## Database Schema

```mermaid
erDiagram
    CUSTOMERS {
        BIGINT id PK "Auto-generated primary key"
        VARCHAR first_name "Customer first name (max 50 chars)"
        VARCHAR last_name "Customer last name (max 50 chars)"
        VARCHAR email UK "Unique email address"
        VARCHAR phone_number "Phone number (max 15 chars)"
        TIMESTAMP created_at "Record creation timestamp"
        TIMESTAMP last_modified "Last modification timestamp"
        BOOLEAN deleted "Soft delete flag"
    }
```

## Security Architecture

```mermaid
graph TD
    Request[HTTP Request] --> Auth{Authenticated?}
    Auth -->|No| Login[Basic Auth Login]
    Auth -->|Yes| Role{Check Role}
    Login --> Role
    
    Role -->|USER| ReadOnly[Read-Only Access]
    Role -->|ADMIN| FullAccess[Full CRUD Access]
    Role -->|NONE| Denied[Access Denied]
    
    ReadOnly --> GetCustomers[GET /api/v1/customers]
    ReadOnly --> GetCustomer[GET /api/v1/customers/id]
    
    FullAccess --> GetCustomers
    FullAccess --> GetCustomer
    FullAccess --> CreateCustomer[POST /api/v1/customers]
    FullAccess --> UpdateCustomer[PUT /api/v1/customers/id]
    FullAccess --> DeleteCustomer[DELETE /api/v1/customers/id]
    FullAccess --> AdminFeatures[Swagger UI, Admin Dashboard]
    
    Denied --> Error403[403 Forbidden]
```

## API Endpoints

### Customer Management

| Method | Endpoint | Description | Required Role | Request Body | Response |
|--------|----------|-------------|---------------|--------------|----------|
| GET | `/api/v1/customers` | Get paginated customers | USER, ADMIN | None | CustomerPageResponseDto |
| GET | `/api/v1/customers/{id}` | Get customer by ID | USER, ADMIN | None | CustomerResponseDto |
| POST | `/api/v1/customers` | Create new customer | ADMIN | CustomerRequestDto | CustomerResponseDto |
| PUT | `/api/v1/customers/{id}` | Update customer | ADMIN | CustomerRequestDto | CustomerResponseDto |
| DELETE | `/api/v1/customers/{id}` | Soft delete customer | ADMIN | None | 204 No Content |

### System Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/welcome` | Welcome message | Public |
| GET | `/actuator/health` | Health check | Public |
| GET | `/actuator/info` | Application info | Public |
| GET | `/swagger-ui.html` | API documentation | ADMIN |
| GET | `/admin` | Spring Boot Admin | ADMIN |
| GET | `/h2-console` | Database console | Public (dev only) |

## Request/Response Flow

```mermaid
graph LR
    subgraph "Request Processing"
        A[HTTP Request] --> B[Request Logging Filter]
        B --> C[Spring Security]
        C --> D[Input Validation]
        D --> E[Controller Method]
        E --> F[Service Layer]
        F --> G[Repository Layer]
        G --> H[Database]
    end
    
    subgraph "Response Processing"
        H --> I[Entity Mapping]
        I --> J[DTO Creation]
        J --> K[Response Headers]
        K --> L[Response Logging]
        L --> M[HTTP Response]
    end
```

## Monitoring and Observability

```mermaid
graph TD
    App[Customer Management API] --> Metrics[Micrometer Metrics]
    App --> Logs[Structured Logging]
    App --> Health[Health Checks]
    Metrics --> Actuator[Spring Boot Actuator]
    Logs --> LogFile[application.log]
    Health --> HealthEndpoint[Health Endpoint]
    
    Actuator --> AdminUI[Spring Boot Admin UI]
    LogFile --> LogAggregation[Log Aggregation Systems]
    HealthEndpoint --> Monitoring[External Monitoring]
    
    AdminUI --> Dashboard[Admin Dashboard Port 8081]
    Dashboard --> Alerts[Application Monitoring]
```

## Configuration Profiles

```mermaid
graph TD
    Profiles[Configuration Profiles] --> Dev[Development Profile]
    Profiles --> Prod[Production Profile]
    Profiles --> Test[Test Profile]
    
    Dev --> DevFeatures[Debug Logging, H2 Console, Swagger UI, Admin Dashboard]
    Prod --> ProdFeatures[Info Logging, File-based H2, No Swagger, Secure Admin]
    Test --> TestFeatures[Minimal Logging, In-memory H2, No External Services]
    
    DevFeatures --> DevDB[(H2 In-Memory)]
    ProdFeatures --> ProdDB[(H2 File-based)]
    TestFeatures --> TestDB[(H2 Test)]
```

## Error Handling Flow

```mermaid
graph TD
    Request[API Request] --> Validation{Valid Input?}
    Validation -->|No| ValidationError[400 Bad Request]
    Validation -->|Yes| Security{Authorized?}
    
    Security -->|No| AuthError[401/403 Error]
    Security -->|Yes| Business{Business Logic}
    
    Business -->|Customer Not Found| NotFound[404 Not Found]
    Business -->|Duplicate Email| Conflict[409 Conflict]
    Business -->|Success| Success[200/201/204 Success]
    Business -->|Server Error| ServerError[500 Internal Error]
    
    ValidationError --> ErrorResponse[Validation Error Response]
    AuthError --> ErrorResponse
    NotFound --> ErrorResponse
    Conflict --> ErrorResponse
    ServerError --> ErrorResponse
    
    ErrorResponse --> GlobalHandler[Global Exception Handler]
    GlobalHandler --> FormattedError[Formatted Error Response]
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Container Environment"
        Docker[Docker Container] --> App[Customer API :8080]
        Docker --> H2[H2 Database]
    end
    
    subgraph "External Access"
        Client[Client Applications] --> Port8080[Port 8080]
    end
    
    Port8080 --> App
    
    subgraph "Data Persistence"
        App --> H2
    end
```

## Key Features

- **🔐 Security**: Basic Auth with role-based access control (USER/ADMIN)
- **📊 Monitoring**: Health checks, basic metrics, and Spring Boot Admin dashboard
- **📝 Documentation**: OpenAPI 3.0 with Swagger UI and comprehensive Javadoc
- **🔍 Observability**: Structured logging and request/response tracking
- **🚀 Production Ready**: Configuration profiles, Docker support, and audit trails

## Quick Start

```bash
# Start the application
mvn spring-boot:run

# Or with Docker
docker-compose up
```

### Access Points
- **API**: http://localhost:8080/api/v1/customers
- **Swagger UI**: http://localhost:8080/swagger-ui.html (admin required)
- **Admin Dashboard**: http://localhost:8081/admin (admin required)
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Console**: http://localhost:8080/h2-console

### Authentication
- **Admin**: `admin` / `admin` (full access)
- **User**: `user` / `password` (read-only)

## Best Practices Implemented

This section details the 20 Spring Boot best practices implemented in this project, providing comprehensive explanations, importance, and verification methods.

### 1. Modern Java & Spring Boot Stack
- **Implementation**: Java 17 with Spring Boot 3.3.4 for enhanced performance and security features
- **Why Important**: Java 17 provides long-term support and modern language features, while Spring Boot 3 offers better observability
- **How to Verify**: Check `pom.xml` or run `java -version` and check application startup logs

### 2. Data Transfer Objects (DTOs)
- **Implementation**: Separate `CustomerRequestDto`, `CustomerResponseDto`, and `CustomerPageResponseDto` from the `Customer` entity
- **Why Important**: DTOs provide API stability, prevent data leakage, and allow independent evolution of API and database schemas
- **How to Verify**: Examine DTO classes in `src/main/java/com/interview/dto/` and see usage in `CustomerController`

### 3. Input Validation
- **Implementation**: Comprehensive validation using `@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`, and `@Pattern` annotations
- **Why Important**: Prevents malformed data from entering the system, improves security, and provides clear error messages
- **How to Verify**: Test with invalid data via Swagger UI at `http://localhost:8080/swagger-ui.html`

### 4. Lombok Integration
- **Implementation**: Extensive use of `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, and `@Slf4j` annotations
- **Why Important**: Reduces code verbosity, minimizes human error, and improves code readability
- **How to Verify**: Check entity and DTO classes for Lombok annotations and absence of manual getters/setters

### 5. Comprehensive Logging
- **Implementation**: Structured logging with `@Slf4j` in controllers and services, including request/response logging via `RequestLoggingFilter`
- **Why Important**: Provides visibility into application behavior, aids debugging, and enables monitoring
- **How to Verify**: Check application logs during API calls or examine `RequestLoggingFilter` and service classes

### 6. Centralized Error Handling
- **Implementation**: Global exception handling using `@ControllerAdvice` and `@ExceptionHandler` in `GlobalExceptionHandler`
- **Why Important**: Ensures consistent API responses, improves user experience, and simplifies error management
- **How to Verify**: Trigger various error conditions and observe consistent error response format across all endpoints

### 7. Pagination Support
- **Implementation**: Paginated list endpoint with configurable page size, page number, sorting, and default values (page=1, pageSize=10)
- **Why Important**: Improves performance for large datasets, reduces memory usage, and provides better user experience
- **How to Verify**: Call `GET /api/v1/customers?page=1&pageSize=5` and observe pagination metadata in the response

### 8. Role-Based Authorization
- **Implementation**: Custom `@RequireAdmin` annotation and `@PreAuthorize` annotations for method-level security with USER and ADMIN roles
- **Why Important**: Ensures proper access control, protects sensitive operations, and provides fine-grained security
- **How to Verify**: Test API endpoints with different user credentials (user/password for read-only, admin/admin for full access)

### 9. Service Layer Separation
- **Implementation**: Clean separation with `CustomerService` handling business logic, `CustomerController` managing HTTP concerns, and `CustomerRepository` managing data access
- **Why Important**: Improves maintainability, testability, and follows single responsibility principle
- **How to Verify**: Examine the layered architecture in `CustomerController` → `CustomerService` → `CustomerRepository`

### 10. Data Audit Trail
- **Implementation**: `deleted` boolean field and `lastModified` timestamp field in the `Customer` entity with automatic lifecycle management
- **Why Important**: Enables data recovery, compliance tracking, and provides historical context for data changes
- **How to Verify**: Create/update customers and check the `lastModified` field in responses, or examine H2 console at `http://localhost:8080/h2-console`

### 11. Unit Testing
- **Implementation**: Comprehensive unit tests for `CustomerService` and `CustomerController` using Mockito for mocking dependencies
- **Why Important**: Ensures code reliability, catches regressions early, and improves code quality
- **How to Verify**: Run `mvn test` or examine test classes in `src/test/java/com/interview/service/` and `src/test/java/com/interview/controller/`

### 12. Integration Testing
- **Implementation**: End-to-end integration tests using `@SpringBootTest` and `MockMvc` to test complete workflows through the HTTP layer
- **Why Important**: Verifies that all components work together correctly and catches integration issues that unit tests might miss
- **How to Verify**: Run `mvn test -Dtest=CustomerIntegrationTest` or examine `CustomerIntegrationTest` class

### 13. Javadoc Documentation
- **Implementation**: Comprehensive Javadoc comments for all public methods in controllers and services
- **Why Important**: Improves code maintainability, serves as living documentation, and helps new developers understand the codebase
- **How to Verify**: Hover over method names in IDE or generate Javadoc with `mvn javadoc:javadoc`

### 14. OpenAPI Documentation
- **Implementation**: Complete OpenAPI 3.0 specification with Swagger UI integration, including detailed endpoint documentation and examples
- **Why Important**: Enables easy integration, reduces support burden, and provides interactive testing interface
- **How to Verify**: Access Swagger UI at `http://localhost:8080/swagger-ui.html` (admin credentials required)

### 15. Health Check Endpoints
- **Implementation**: Spring Boot Actuator health endpoints at `/actuator/health` providing application status and basic system metrics
- **Why Important**: Enables monitoring systems to detect application issues and provides operational visibility
- **How to Verify**: Access `http://localhost:8080/actuator/health` to see application health status

### 16. Spring Boot Admin Monitoring
- **Implementation**: Spring Boot Admin Server configuration for application monitoring, metrics collection, and centralized management
- **Why Important**: Provides operational insights, enables proactive issue detection, and improves system observability
- **How to Verify**: Access admin dashboard at `http://localhost:8081/admin` (admin credentials required)

### 17. API Versioning
- **Implementation**: All endpoints use `/api/v1/` prefix for versioning, enabling future API evolution without breaking existing clients
- **Why Important**: Allows backward compatibility, enables gradual API evolution, and supports multiple client versions
- **How to Verify**: Check all API endpoints use `/api/v1/customers` pattern and examine `CustomerController` for versioned request mapping

### 18. Configuration Profiles
- **Implementation**: Environment-specific configurations in `application-dev.yml`, `application-prod.yml`, and `application-test.yml`
- **Why Important**: Enables environment-specific deployments, improves security by separating configs, and supports different operational requirements
- **How to Verify**: Check different YAML files for environment-specific settings and run with `--spring.profiles.active=dev|prod|test`

### 19. Docker Containerization
- **Implementation**: `Dockerfile` for application containerization and `docker-compose.yml` for easy deployment with H2 database
- **Why Important**: Ensures consistent deployments, simplifies environment setup, and supports modern DevOps practices
- **How to Verify**: Run `docker-compose up --build` to start the containerized application at `http://localhost:8080`

### 20. Comprehensive Documentation
- **Implementation**: Detailed `documentation.md` file with Mermaid diagrams showing architecture, data flow, security model, and deployment structure
- **Why Important**: Improves project understanding, facilitates onboarding, and provides clear system overview for stakeholders
- **How to Verify**: Review this documentation file with its Mermaid diagrams and detailed explanations of system components
