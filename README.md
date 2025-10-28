Calorie Counter API (Kotlin/Spring Boot)

Overview
- REST API for calorie and exercise tracking designed for AI assistants and apps.
- Tech: Spring Boot (Kotlin), Spring Security + JWT, Spring Data JPA, PostgreSQL, Flyway, Springdoc OpenAPI, Docker.
- Versioned API: /api/v1

Getting started
1) Prerequisites
- Docker + Docker Compose installed

2) Run with Docker Compose
- docker-compose up --build
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

3) Configuration (env vars)
- DB_URL (default jdbc:postgresql://db:5432/calorie_counter)
- DB_USERNAME (default calorie_user)
- DB_PASSWORD (default calorie_pass)
- JWT_SECRET (default please-change-in-prod)

Auth flow
- POST /api/v1/auth/register { username, password, timezone? }
- POST /api/v1/auth/login { username, password } → { token, authUserId, appUserId, isAdmin }
- Use the token as Authorization: Bearer <token> for all other endpoints.

Key endpoints (selection)
- Users
  - GET /api/v1/users/me → profile with BMI and estimated daily intake (Mifflin–St Jeor + activity level)
  - PUT /api/v1/users/me → partial update
  - Weight history: GET/POST/DELETE /api/v1/users/me/weights
- Food diaries
  - CRUD: /api/v1/diaries and /api/v1/diaries/{id}/entries
  - Summary: GET /api/v1/diaries/{id}/summary (kcal/macros; percentages vs goal and estimated intake)
- Exercises (catalog)
  - User-owned: /api/v1/exercises
    - GET supports filters: query (FTS), tag= (repeatable), muscle= (repeatable), group= (repeatable). Filters are OR within each key.
  - Shared (read-only): /api/v1/shared-exercises (same filters)
  - Admin shared CRUD: /api/v1/admin/shared-exercises
- Foods (catalog)
  - User-owned CRUD: /api/v1/foods
  - Shared read-only: /api/v1/shared-foods
  - Admin shared CRUD: /api/v1/admin/shared-foods

Notes on search and filters
- Exercise search uses PostgreSQL full‑text search (FTS) on translated names (no trigram fuzzy for now).
- Multi-value OR filters:
  - tag=strength&tag=cardio matches exercises that have either tag.
  - muscle=biceps%20brachii&muscle=pectoralis%20major matches exercises affecting either muscle.
  - group=chest&group=arms matches exercises assigned to either group.
- Pagination is fixed-size (size=50). Clients can only supply page (0-based).

Units and measurements (foods)
- Quantities are decimals only.
- Supported units: g, ml, cup (240 ml), tablespoon (15 ml), teaspoon (5 ml), pack, item.
- Volumes require density_g_per_ml on food; pack/item require pack_g/item_g respectively.
- Calories are derived from macros per 100 g.

Security
- All endpoints except /api/v1/auth/** require JWT.
- JWT contains auth_user_id and app_user_id; no expiration (for now).
- Admin-only routes under /api/v1/admin/**.

Database
- PostgreSQL with Flyway migrations. SHARED system user is seeded (owner of shared catalog items).

Development tips
- Run tests locally with Gradle: ./gradlew test
- OpenAPI is available at /v3/api-docs and Swagger UI at /swagger-ui.

License
- MIT (or specify your desired license)
