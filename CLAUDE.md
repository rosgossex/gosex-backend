# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin backend service built with Ktor framework for a dating application called "GoSex". The service handles user authentication via JWT tokens from an ESIA (Russian government authentication system) and provides user management functionality.

## Architecture

The application follows a layered architecture:

- **API Layer**: Ktor routing and controllers in `Routing.kt`
- **Service Layer**: Repository interfaces in `repo/` package
- **Data Access Layer**: PostgreSQL implementation using Exposed ORM in `db/` package
- **Model Layer**: Data classes in `model/` package
- **Authentication**: JWT-based authentication with ESIA integration in `Authentication.kt`

## Key Components

1. **Authentication**: Uses JWT tokens from ESIA with JWK verification
2. **User Management**: CRUD operations for users with search functionality via service layer
3. **Database**: PostgreSQL with Exposed ORM for data persistence
4. **API**: RESTful endpoints for user registration and search
5. **Testing**: Comprehensive unit and integration tests with JaCoCo coverage
6. **CI/CD**: GitHub Actions workflow for automated testing and coverage reporting

## Common Development Tasks

### Build and Run

```bash
# Continuous build with hot reload
./gradlew -t build -x test -x check -i

# Run application
./gradlew run -Dio.ktor.development=true
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

- `src/main/kotlin/gosex`: Main source code
  - `Application.kt`: Entry point and module configuration
  - `Routing.kt`: API endpoint definitions
  - `Authentication.kt`: JWT authentication configuration
  - `Databases.kt`: Database connection setup
  - `model/`: Data classes (User, Gender)
  - `db/`: Database access layer with Exposed ORM
  - `repo/`: Repository interfaces
  - `service/`: Business logic service layer
- `src/test/kotlin/gosex`: Test source code
  - `service/`: Unit tests for service layer
  - `db/`: Integration tests for database layer
- `src/main/resources/application.yaml`: Configuration file
- `.github/workflows/`: GitHub Actions CI/CD pipeline
