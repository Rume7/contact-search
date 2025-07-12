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
- **Testing**: JUnit 5, Testcontainers, AssertJ
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
   - Health Check: http://localhost:8080/actuator/health
   - PostgreSQL: localhost:5432
   - Elasticsearch: http://localhost:9200

### API Endpoints

#### Contact Management
- `GET /api/contacts` - Get all contacts (paginated)
- `POST /api/contacts` - Create a new contact
- `GET /api/contacts/{id}` - Get contact by ID
- `PUT /api/contacts/{id}` - Update contact
- `DELETE /api/contacts/{id}` - Delete contact

#### Search Operations
- `GET /api/search/contacts?query={searchTerm}` - Full-text search
- `GET /api/search/contacts/autocomplete?query={term}` - Autocomplete search
- `GET /api/search/contacts/city?city={cityName}` - Search by city
- `POST /api/contacts/sync` - Sync data to Elasticsearch

### Example Usage

```bash
# Create a contact
curl -X POST "http://localhost:8080/api/contacts" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "city": "New York"
  }'

# Search contacts
curl -X GET "http://localhost:8080/api/search/contacts?query=john"

# Get all contacts
curl -X GET "http://localhost:8080/api/contacts"
```

## Testing

### Test Setup

The application uses **Testcontainers** for integration testing, providing isolated, containerized test environments for both PostgreSQL and Elasticsearch.

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dspring.profiles.active=test

# Run integration tests
mvn test -Dtest=*IntegrationTest,*ServiceTest -Dspring.profiles.active=integration

# Run with coverage
mvn clean test jacoco:report
```

### Test Structure

- **Unit Tests**: `ContactSearchApplicationTests.java` - Basic context loading
- **Integration Tests**: `ContactIntegrationTest.java` - Full API testing with Testcontainers
- **Service Tests**: `ContactServiceTest.java` - Service layer testing with Testcontainers

### Test Profiles

- **test**: Uses H2 in-memory database for fast unit tests
- **integration**: Uses Testcontainers for PostgreSQL and Elasticsearch

## CI/CD Pipeline

The GitHub Actions workflow includes:

1. **Unit Tests**: Fast tests with H2 database
2. **Integration Tests**: Full tests with Testcontainers
3. **Security Scan**: OWASP Dependency Check
4. **Docker Build**: Build and cache Docker images
5. **Deployment**: Push to Docker Hub (main branch only)

### Pipeline Features

- ✅ **Testcontainers**: Isolated test environments
- ✅ **Code Coverage**: JaCoCo reports
- ✅ **Security Scanning**: OWASP dependency check
- ✅ **Docker Caching**: Optimized build times
- ✅ **Artifact Upload**: Test results and reports

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

### Elasticsearch Mapping

Custom mapping with autocomplete analyzers for enhanced search capabilities:

- **Text Analysis**: Standard analyzer for general search
- **Autocomplete**: Edge n-gram analyzer for suggestions
- **Keyword Fields**: Exact match capabilities
- **Date Fields**: Proper date formatting

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

This project is licensed under the MIT License.
