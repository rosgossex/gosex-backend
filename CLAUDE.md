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
2. **User Management**: CRUD operations for users with search functionality
3. **Database**: PostgreSQL with Exposed ORM for data persistence
4. **API**: RESTful endpoints for user registration and search

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
./gradlew ktfmtFormat
```

### Deployment
```bash
docker compose --env-file .env.dev up -d --wait --build
```

## Project Structure
- `src/main/kotlin/`: Main source code
- `Application.kt`: Entry point and module configuration
- `Routing.kt`: API endpoint definitions
- `Authentication.kt`: JWT authentication configuration
- `Databases.kt`: Database connection setup
- `model/`: Data classes (User, Gender)
- `db/`: Database access layer with Exposed ORM
- `repo/`: Repository interfaces
- `src/main/resources/application.yaml`: Configuration file