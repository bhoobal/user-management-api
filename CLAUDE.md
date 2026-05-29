# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Build
mvn clean package
mvn clean package -DskipTests

# Run locally
mvn spring-boot:run

# Tests
mvn test
mvn test -Dtest=ClassName                  # single test class
mvn test -Dtest=ClassName#methodName       # single test method

# Docker
docker build -t user-management-api:1.0.0 .
docker run -p 8080:8080 user-management-api:1.0.0

# Kubernetes (Docker Desktop)
./deploy.sh
```

App runs on port 8080 locally; Kubernetes NodePort is 30080.

## Architecture

Spring Boot 3.2.3 / Java 17 / Maven. Standard layered architecture:

- **Controllers** (`UserController`, `GroupController`) — REST endpoints under `/api/v1/`, use `@Valid` on request bodies
- **Services** (`UserService`, `GroupService`) — business logic, `@Transactional`, read-only for GET operations
- **Repositories** (`UserRepository`, `GroupRepository`) — Spring Data JPA
- **Models** (`User`, `ProjectGroup`) — JPA entities; `User`↔`ProjectGroup` is `@ManyToMany` with join table `user_groups`
- **DTOs** (`UserDto`, `GroupDto`, `ApiResponse<T>`) — `ApiResponse<T>` wraps every response
- **Exception handling** — `GlobalExceptionHandler` (`@RestControllerAdvice`), custom exceptions `ResourceNotFoundException` (404) and `DuplicateResourceException` (409)

All source files live flat under `src/` (not split into `src/main/java` and `src/test/java` per normal Maven convention — the `pom.xml` uses `<sourceDirectory>src</sourceDirectory>`).

## Data Layer

Uses H2 in-memory database (`jdbc:h2:mem:userdb`) with `ddl-auto=create-drop` — schema is recreated on every restart. H2 console available at `/h2-console` in dev. No persistent storage.

## Key Patterns

- All endpoints return `ApiResponse<T>` with success/error flag and timestamp
- `User` and `ProjectGroup` entities use `createdAt` (`@Column(updatable=false)`) and `updatedAt` (`@PreUpdate`)
- Lombok is used throughout (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.)
- Jakarta validation annotations (`@NotBlank`, `@Email`, `@Size`) on DTOs
- Actuator exposes `/actuator/health` for Kubernetes liveness/readiness probes

## Kubernetes Deployment

Three manifest files (`00-namespace.yaml`, `01-deployment.yaml`, `02-service.yaml`) deploy to the `development` namespace with 2 replicas. Image pull policy is `Never` (expects a locally built Docker image). `deploy.sh` automates the full build-and-deploy sequence.
