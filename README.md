# Calorie Counter API

REST API for managing calorie intake, food diaries, exercises, and user metrics. Built for health-tracking assistants that need a pragmatic Kotlin/Spring Boot backend with JWT authentication and PostgreSQL.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
  - [Prerequisites](#prerequisites)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Local Development](#local-development)
- [Configuration](#configuration)
- [Usage Overview](#usage-overview)
  - [Authentication Flow](#authentication-flow)
  - [Sample Requests](#sample-requests)
- [Endpoint Highlights](#endpoint-highlights)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## Features
- Versioned REST API exposed under `/api/v1`.
- User management with secure JWT-based authentication.
- Food diary tracking with macros summarization and calorie goal comparisons.
- Personal and shared catalogs for foods and exercises, with rich filtering.
- PostgreSQL persistence, Flyway migrations, and seeded shared content.
- OpenAPI/Swagger UI for interactive exploration.

## Tech Stack
- Kotlin, Spring Boot, Spring Web, Spring Validation.
- Spring Security with JWT authentication.
- Spring Data JPA backed by PostgreSQL.
- Flyway for schema migrations.
- Springdoc OpenAPI for documentation.
- Docker + Docker Compose for local orchestration.

## Quick Start

### Prerequisites
- Docker and Docker Compose.
- (Optional) Java 21+ and Gradle if running without Docker.

### Run with Docker Compose
```bash
docker-compose up --build
```
- Application: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui/index.html  
- Health check: http://localhost:8080/actuator/health

### Local Development
Run against a local PostgreSQL instance (see `docker/postgres` for reference env):
```bash
./gradlew bootRun
```
Run the automated test suite:
```bash
./gradlew test
```

## Configuration
Set environment variables (or Docker Compose overrides) to customize runtime behavior.

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | JDBC connection string | `jdbc:postgresql://db:5432/calorie_counter` |
| `DB_USERNAME` | Database username | `calorie_user` |
| `DB_PASSWORD` | Database password | `calorie_pass` |
| `JWT_SECRET` | Symmetric key for signing tokens | `please-change-in-prod` |

## Usage Overview

### Authentication Flow
1. `POST /api/v1/auth/register` with `username`, `password`, optional `timezone`.
2. `POST /api/v1/auth/login` with credentials to receive `{ token, authUserId, appUserId, isAdmin }`.
3. Send `Authorization: Bearer <token>` header on subsequent requests.

### Sample Requests
Register a user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"changeme","timezone":"UTC"}'
```

Retrieve the current user profile (BMI, estimated intake, preferences):
```bash
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <token>"
```

Create a food diary entry:
```bash
curl -X POST http://localhost:8080/api/v1/diaries/{diaryId}/entries \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"foodId":"...", "portion":{"amount":150,"unit":"g"}}'
```

## Endpoint Highlights
- **Users**: manage profile and weight history (`/api/v1/users/me`, `/api/v1/users/me/weights`).
- **Food Diaries**: CRUD diaries and entries; summaries via `/api/v1/diaries/{id}/summary`.
- **Exercises**: personal (`/api/v1/exercises`) and shared catalogs (`/api/v1/shared-exercises`) with FTS and multi-value filters.
- **Foods**: personal (`/api/v1/foods`) and shared catalogs (`/api/v1/shared-foods`); admin CRUD under `/api/v1/admin/**`.
- Pagination uses fixed size (50 items) with page-based navigation.

## API Documentation
- OpenAPI spec: `GET /v3/api-docs`
- Interactive UI: `/swagger-ui/index.html`

## Project Structure
```
project-root/
|- docker/               # Helper files for local infrastructure
|- docker-compose.yml    # Orchestration for app + PostgreSQL
|- src/
|  |- main/kotlin/       # Application source code
|  `- main/resources/    # Configuration, SQL migrations, etc.
|- build.gradle          # Gradle build script
`- HELP.md               # Spring Boot starter help
```

## Development
- Execute unit tests: `./gradlew test`.
- Inspect database migrations: `src/main/resources/db/migration`.
- Update API docs automatically via Springdoc when modifying controllers.

## Contributing
Pull requests and feature ideas are welcome. Please open an issue to discuss large changes before submitting a PR.

## License
Released under the [MIT License](LICENSE).
