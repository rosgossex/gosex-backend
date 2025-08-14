# GoSex Backend

A Kotlin backend service built with Spring Boot for a dating application. The service handles user authentication via JWT tokens from ESIA (Russian government authentication system) and provides user management functionality.

## Architecture

The application follows a layered architecture:

- **API Layer**: Spring MVC controllers for RESTful endpoints with OpenAPI documentation
- **Service Layer**: Spring services with dependency injection for business logic
- **Data Access Layer**: Spring Data JPA repositories for data persistence
- **Model Layer**: JPA entities for domain objects
- **DTO Layer**: Data transfer objects and standardized error handling
- **Authentication**: Spring Security with JWT resource server configuration

## Development

**Build and run with hot reload:**

In first shell run gradle to continuous build your project

```shell
./gradlew -t build -x test -x check -i
```

In another terminal run the application

```shell
./gradlew bootRun
```

**Format:**

```shell
./gradlew ktfmtFormat
```

**Pre-commit Hooks:**

Install pre-commit to automatically format code before commits:

```shell
pip install pre-commit
pre-commit install
```

The hooks will automatically run `ktfmtFormat` before each commit. To run manually:

```shell
pre-commit run --all-files
```

To bypass hooks (not recommended):

```shell
git commit --no-verify
```

## Testing

**Run all tests:**

```shell
./gradlew test
```

**Run only unit tests:**

```shell
./gradlew test --tests "*.service.*"
```

**Run only integration tests:**

```shell
./gradlew test --tests "*.db.*"
```

**Generate coverage report:**

```shell
./gradlew test jacocoTestReport
```

**View coverage report:**

Open `build/reports/jacoco/test/html/index.html` in your browser.

**Coverage verification (50% minimum):**

```shell
./gradlew jacocoTestCoverageVerification
```

## API Documentation

The application provides interactive API documentation via Swagger UI and OpenAPI specification.

**Access Swagger UI (when running locally):**

```shell
open http://localhost:6969/swagger-ui/index.html
```

**Access OpenAPI spec:**

```shell
curl http://localhost:6969/v3/api-docs
```

## Deployment

```shell
docker compose up -d --wait --build
```
