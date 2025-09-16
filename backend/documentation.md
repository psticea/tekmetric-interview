# Customer Management API Documentation

## Overview

The Customer Management API is a RESTful service built with Spring Boot 3.3.4 that provides CRUD operations for customer data management. It features security, validation, pagination, monitoring, and comprehensive documentation.

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
    ReadOnly --> GetCustomer[GET /api/v1/customers/{id}]
    
    FullAccess --> GetCustomers
    FullAccess --> GetCustomer
    FullAccess --> CreateCustomer[POST /api/v1/customers]
    FullAccess --> UpdateCustomer[PUT /api/v1/customers/{id}]
    FullAccess --> DeleteCustomer[DELETE /api/v1/customers/{id}]
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
    Health --> HealthEndpoint[/actuator/health]
    
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

### 🔐 Security
- **Authentication**: Basic Auth with username/password
- **Authorization**: Role-based access control (USER, ADMIN)
- **Endpoint Security**: Different access levels for different operations
- **Password Encoding**: BCrypt password encryption

### 📊 Monitoring
- **Health Checks**: Built-in Spring Boot Actuator health indicators
- **Metrics**: Application metrics via Micrometer
- **Admin Dashboard**: Spring Boot Admin for application monitoring
- **Request Logging**: Detailed request/response logging

### 📝 Documentation
- **OpenAPI 3.0**: Complete API specification
- **Swagger UI**: Interactive API documentation
- **Javadoc**: Comprehensive code documentation
- **Architecture Diagrams**: Visual system documentation

### 🔍 Observability
- **Structured Logging**: Detailed application logging
- **Request/Response Logging**: HTTP request/response logging
- **Performance Metrics**: Response time and throughput monitoring
- **Error Tracking**: Comprehensive error logging and handling

### 🚀 Production Ready
- **Configuration Profiles**: Environment-specific configurations
- **Docker Support**: Simple containerization
- **Database Migration**: SQL scripts for database initialization
- **Soft Deletes**: Data preservation with logical deletion

## Quick Start

### Development Environment
```bash
# Start the application
mvn spring-boot:run

# Or with Docker
docker-compose up
```

### Access Points
- **API**: http://localhost:8080/api/v1/customers
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Admin Dashboard**: http://localhost:8081/admin
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Console**: http://localhost:8080/h2-console

### Authentication
- **Username**: `admin`
- **Password**: `admin`
- **Roles**: ADMIN (full access), USER (read-only)

## Best Practices Implemented

### Code Quality
- ✅ SOLID principles
- ✅ Clean architecture with separated concerns
- ✅ Comprehensive error handling
- ✅ Input validation and sanitization
- ✅ Proper logging and monitoring

### Security
- ✅ Role-based access control
- ✅ Input validation
- ✅ Secure password storage
- ✅ Protected admin endpoints
- ✅ CORS configuration

### Performance
- ✅ Database indexing
- ✅ Pagination for large datasets
- ✅ Efficient queries with JPA
- ✅ Connection pooling
- ✅ Response caching headers

### Maintainability
- ✅ Comprehensive documentation
- ✅ Unit and integration tests
- ✅ Clear code structure
- ✅ Environment-specific configurations
- ✅ Monitoring and observability
