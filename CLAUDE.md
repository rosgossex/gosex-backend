# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin backend service built with Spring Boot framework for a dating application called "GoSex". The service handles user authentication via JWT tokens from an ESIA (Russian government authentication system) and provides user management functionality.

## Architecture

The application follows a layered architecture:

- **API Layer**: Spring MVC controllers in `controller/` package
- **Service Layer**: Spring services with dependency injection in `service/` package
- **Data Access Layer**: Spring Data JPA repositories in `repository/` package
- **Model Layer**: JPA entities in `model/` package
- **Authentication**: Spring Security with JWT resource server configuration

## Key Components

1. **Authentication**: Spring Security with JWT resource server and JWK verification
2. **User Management**: CRUD operations for users with search functionality via Spring Data JPA
3. **Database**: PostgreSQL with Spring Data JPA for data persistence
4. **API**: RESTful endpoints using Spring MVC for user registration and search
5. **Testing**: Comprehensive unit and integration tests using Spring Boot Test framework with JaCoCo coverage
6. **CI/CD**: GitHub Actions workflow for automated testing and coverage reporting

## Common Development Tasks

### Build and Run

```bash
# Continuous build with hot reload
./gradlew -t build -x test -x check -i

# Run application
./gradlew bootRun
```

### Code Formatting

```bash
# Format code
./gradlew ktfmtFormat

# Check code formatting
./gradlew ktfmtCheck
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run only unit tests
./gradlew test --tests "*.service.*"

# Run only integration tests
./gradlew test --tests "*.db.*"
```

### Code Style Rules

- **No inline comments**: Code must not contain inline comments (e.g., `// something...`). Code should be self-documenting through clear naming and structure.

### Git Workflow Rules

- **Explicit file staging**: Never use `git add .` - always explicitly specify which files to stage (e.g., `git add file1.kt file2.kt`). This ensures intentional commits and prevents accidental inclusion of unwanted files.

### Deployment

```bash
docker compose --env-file .env.dev up -d --wait --build
```

## CI/CD Pipeline

The project uses GitHub Actions for automated testing and quality assurance:

- **Workflow**: `.github/workflows/test.yml`
- **Triggers**: Push and pull requests to `master` branch
- **Environment**: Ubuntu with Java 21 and PostgreSQL 17
- **Steps**:
  1. Code formatting validation with `ktfmtCheck`
  2. Unit and integration test execution
  3. JaCoCo coverage report generation
  4. Coverage reporting to pull request comments
- **Coverage Requirements**: 50% minimum for overall project and changed files

## Project Structure

- `src/main/kotlin/gosex/backend`: Main source code
  - `Application.kt`: Spring Boot main application class
  - `Authentication.kt`: Spring Security configuration
  - `controller/`: Spring MVC REST controllers
  - `model/`: JPA entities (User, Gender)
  - `repository/`: Spring Data JPA repositories
  - `service/`: Spring service layer with business logic
- `src/test/kotlin/gosex/backend`: Test source code
  - `service/`: Unit tests for service layer
  - `repository/`: Integration tests for repository layer using Spring Boot Test
- `src/main/resources/application.yaml`: Spring Boot configuration file
- `.github/workflows/`: GitHub Actions CI/CD pipeline
