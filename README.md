# Contact Search Application

A Spring Boot application for contact management with advanced search capabilities using PostgreSQL and Elasticsearch.

## Features

- **Contact Management**: CRUD operations for contacts
- **Advanced Search**: Full-text search with Elasticsearch
- **Autocomplete**: Real-time search suggestions
- **City-based Search**: Filter contacts by city
- **RESTful API**: Complete REST API for all operations
- **Docker Support**: Containerized with Docker Compose
- **CI/CD Pipeline**: Automated testing and deployment

## Technology Stack

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: PostgreSQL 16.8
- **Search Engine**: Elasticsearch 8.8.0
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17 (for local development)
- Maven (for local development)

### Running with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd contact-search
   ```

2. **Start the application**
   ```bash
   docker compose up --build
   ```

3. **Access the application**
   - Application: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### API Endpoints

#### Contact Management
- `GET /api/v1/contacts` - Get all contacts (paginated)
- `POST /api/v1/contacts` - Create a new contact
- `GET /api/v1/contacts/{id}` - Get contact by ID
- `PUT /api/v1/contacts/{id}` - Update contact
- `DELETE /api/v1/contacts/{id}` - Delete contact
- `POST /api/v1/contacts/sync` - Sync data to Elasticsearch

#### Search Operations
- `GET /api/v1/search/contacts?query={searchTerm}` - Full-text search
- `GET /api/v1/search/contacts/autocomplete?query={term}` - Autocomplete search
- `GET /api/v1/search/contacts/fuzzy?query={term}` - Fuzzy search with typo tolerance
- `GET /api/v1/search/contacts/city?city={cityName}` - Search by city

### Example Usage

```bash
# Create a contact
curl -X POST "http://localhost:8080/api/v1/contacts" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "city": "New York"
  }'

# Search contacts
curl -X GET "http://localhost:8080/api/v1/search/contacts?query=john"

# Get all contacts
curl -X GET "http://localhost:8080/api/v1/contacts"
```

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## CI/CD Pipeline

The GitHub Actions workflow includes:

1. **Unit Tests**: Fast tests with H2 database
2. **Integration Tests**: Full tests with Testcontainers
3. **Security Scan**: OWASP Dependency Check
4. **Docker Build**: Build and cache Docker images
5. **Deployment**: Push to Docker Hub (main branch only)

## Development

### Local Development

1. **Start dependencies only**
   ```bash
   docker compose up postgres elasticsearch
   ```

2. **Run application locally**
   ```bash
   mvn spring-boot:run
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

### Database Schema

The application automatically creates the database schema on startup. The `contacts` table includes:

- `id` (Primary Key)
- `first_name` (Indexed)
- `last_name` (Indexed)
- `email` (Unique, Indexed)
- `city` (Indexed)
- `created_at`
- `updated_at`

## Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/contacts_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Elasticsearch
SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200

# Application
SERVER_PORT=8080
```

### Docker Secrets

For production, set these GitHub secrets:
- `DOCKERHUB_USERNAME`: Docker Hub username
- `DOCKERHUB_TOKEN`: Docker Hub access token

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under a custom license that allows use but restricts redistribution.

Copyright (c) 2025 Contact Search Team. All rights reserved.

**Permission is granted to:**
- Use this software for personal, educational, or commercial purposes
- Modify the source code for internal use
- Run the application in any environment

**Permission is NOT granted to:**
- Redistribute this software or its derivatives without explicit written permission from the author
- Include this software in other projects without permission
- Create derivative works for public distribution without permission
- Use this software in a way that violates applicable laws

**For redistribution or commercial use, please contact:**
- Email: support@contactsearch.com
- GitHub: https://github.com/Rume7/contact-search

This software is provided "as is" without warranty of any kind, express or implied.
