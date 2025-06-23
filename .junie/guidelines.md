# Project Guidelines for GlitchDex

## Project Overview

GlitchDex is a bug tracking and test management system built with Spring Boot. The application allows testers to create
and manage test sessions using different testing strategies, track bugs found during testing, and organize them within
projects.

### Core Domain Model

- **User**: Represents system users with roles (likely testers, managers, admins)
- **Project**: Represents a software project being tested
- **Strategy**: Represents a testing strategy with description, examples, and tips
- **TestSession**: Represents a testing session conducted by a tester using a specific strategy on a project
- **Bug**: Represents a bug identified during a test session

## Project Structure

The project follows a standard Spring Boot application structure:

```
src/
├── main/
│   ├── java/br/ufscar/glitchdex/
│   │   ├── config/          # Configuration classes (e.g., SecurityConfig)
│   │   ├── controller/      # MVC controllers
│   │   ├── domain/          # Domain model entities
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── service/         # Business logic services
│   │   └── GlitchDexApplication.java  # Main application class
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       └── application.properties  # Application configuration
└── test/
    └── java/br/ufscar/glitchdex/  # Test classes
```

## Technology Stack

- **Java 21**: Programming language
- **Spring Boot 3.5.0**: Application framework
- **Spring Data JPA**: Database access
- **Spring Security**: Authentication and authorization
- **Thymeleaf**: Server-side templating
- **PostgreSQL/H2**: Database options
- **Lombok**: Reduces boilerplate code
- **Maven**: Build and dependency management

## Development Guidelines

### Code Style

- Use Lombok annotations to reduce boilerplate code
- Follow standard Java naming conventions
- Use JPA annotations for entity mapping
- Implement validation using Jakarta Validation annotations
- Use proper encapsulation and access modifiers

### Testing

- Write unit tests for services and controllers
- Use Spring Boot Test for integration testing
- Test security configurations with Spring Security Test
- Run tests before submitting changes

### Build Process

- The project uses Maven for building
- Build the project using `./mvnw clean install`
- Run the application using `./mvnw spring-boot:run`

### Docker Support

- The project includes Docker Compose support
- Use `docker-compose up` to start the application with its dependencies

## Junie Guidelines

When working with this project, Junie should:

1. Run tests to verify changes using the `run_test` command
2. Follow the existing code style and patterns
3. Ensure all validation annotations are properly used
4. Maintain the existing architecture and separation of concerns
5. Build the project before submitting to verify compilation
