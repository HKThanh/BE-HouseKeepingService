# Copilot Instructions for house_keeping_service_BE

## Project Overview
This is a Java Spring Boot backend for a house keeping service platform. The codebase is organized by domain features (e.g., Chat, Booking, Employee) with API documentation and test cases in `docs/`.

## Architecture & Key Patterns
- **Modular Structure:**
  - Source code: `src/main/java/`
  - Tests: `src/test/java/`
  - Resources: `src/main/resources/`
- **Domain-Driven:**
  - Each business domain (e.g., Chat, Booking, Employee) is separated for clarity and maintainability.
- **API Documentation:**
  - API test cases and endpoint documentation are in `docs/<Domain>/API-TestCases-*.md`.
- **Database:**
  - PostgreSQL, with initialization scripts in `init_sql/`.
- **Build Tool:**
  - Gradle (`build.gradle`, `gradlew`, `gradlew.bat`).

## Developer Workflows
- **Build:**
  - Windows: `./gradlew.bat build`
  - Linux/macOS: `./gradlew build`
- **Run:**
  - `./gradlew.bat bootRun` (or use Docker, see below)
- **Test:**
  - `./gradlew.bat test`
- **Docker:**
  - Use `docker-compose.yml` for local development (includes database setup).
- **Database Reset:**
  - Drop and re-create using scripts in `init_sql/`.

## Conventions & Patterns
- **API Endpoints:**
  - Follow RESTful conventions, grouped by domain.
- **Documentation:**
  - Always update relevant `docs/<Domain>/API-TestCases-*.md` when changing endpoints.
- **Testing:**
  - Place unit/integration tests in `src/test/java/` mirroring main source structure.
- **Configuration:**
  - Use `src/main/resources/application.properties` for environment settings.

## Integration Points
- **External Services:**
  - Chat and other real-time features may use WebSockets (see `docs/Chat/Socket-Test.md`).
- **Database:**
  - PostgreSQL, configured via Docker and `application.properties`.

## Examples
- To add a new domain (e.g., Payment):
  1. Create a new package in `src/main/java/`
  2. Add API documentation in `docs/Payment/`
  3. Add tests in `src/test/java/`

## References
- `build.gradle`, `docker-compose.yml`, `docs/`, `init_sql/`, `src/`

---

**For AI agents:**
- Prioritize updating documentation in `docs/` when making API changes.
- Follow existing domain separation and naming conventions.
- Use Gradle and Docker for builds and local development.
- Reference `docs/<Domain>/API-TestCases-*.md` for endpoint details and test scenarios.
