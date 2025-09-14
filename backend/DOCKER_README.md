# Docker Setup for Customer Management API

This document provides instructions for running the Customer Management API using Docker with H2 database.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Quick Start

```bash
# Build and run the application with H2 database
docker-compose up

# The API will be available at:
# - API: http://localhost:8080/api/v1
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - H2 Console: http://localhost:8080/h2-console
# - Health Check: http://localhost:8080/actuator/health
```

## Available Services

| Service | Port | Description |
|---------|------|-------------|
| app | 8080 | Application with H2 database |

## Environment Variables

- `SPRING_PROFILES_ACTIVE=dev` (default)

## API Endpoints

### Customer Management
- `GET /api/v1/customers` - Get all customers (paginated)
- `GET /api/v1/customers/{id}` - Get customer by ID
- `POST /api/v1/customers` - Create new customer (Admin only)
- `PUT /api/v1/customers/{id}` - Update customer (Admin only)
- `DELETE /api/v1/customers/{id}` - Delete customer (Admin only)

### System
- `GET /api/v1/welcome` - Welcome message
- `GET /api/v1/info` - API information
- `GET /actuator/health` - Health check

## Authentication

Default credentials:
- Username: `admin`
- Password: `admin`

## Database Access

### H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Useful Commands

```bash
# Build the application
docker-compose build

# Run in background
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up --build

# Check service status
docker-compose ps
```

## Troubleshooting

### Port Conflicts
If port 8080 is already in use, modify the `docker-compose.yml` file to use a different port.

### Application Won't Start
- Check logs: `docker-compose logs app`
- Verify the application builds successfully: `docker-compose build`

## Health Checks

The application includes health checks that verify:
- Application is running
- H2 database connectivity
- Custom health indicators

Access health status at: `http://localhost:8080/actuator/health`
