# Online Menu Application

Online menu for a restaurant with several branches + (planned) admin panel to edit each branch's menu.

**Part 1** (this submission): containerized backend with REST API, PostgreSQL database and image storage.

## Tech stack

| Part | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Maven |
| Database | PostgreSQL 16, Flyway migrations, Spring Data JPA |
| Image storage | Docker named volume behind a `StorageService` interface (S3/MinIO later) |
| API docs | `docs/API.md` + Swagger UI (springdoc-openapi) |
| Containers | Dockerfile (multi-stage) + Docker Compose |

## Quick start

Requirements: Docker Desktop (Docker Compose v2).

```bash
cp .env.example .env        # optional — all variables have defaults
docker compose up --build
```

After start:

- API: <http://localhost:8080/api/v1/branches>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>
- PostgreSQL from the host: `localhost:5433` (user/password from `.env`)

Demo data (2 branches, 6 dishes) is inserted by the migration `V2__seed_demo_data.sql`.

Stop: `docker compose down` (data stays in volumes) · full reset: `docker compose down -v`.

## Repository structure (monorepo)

```
online-menu-application/
├── apps/
│   ├── api/                  # Backend — Spring Boot modular monolith
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/java/com/onlinemenu/api/
│   │       ├── branch/           # module: branches (филиалы)
│   │       │   ├── controller/   #   REST endpoints
│   │       │   ├── service/      #   interface = public API of the module
│   │       │   │   └── impl/     #   implementation (BranchServiceImpl)
│   │       │   ├── repository/   #   Spring Data JPA (internal)
│   │       │   ├── entity/       #   JPA entities (internal)
│   │       │   ├── mapper/       #   entity <-> DTO (internal)
│   │       │   ├── dto/request/  #   request bodies + validation
│   │       │   ├── dto/response/ #   response bodies
│   │       │   └── event/        #   BranchDeletedEvent
│   │       ├── menu/             # module: menu items + image sub-resource (same layers)
│   │       │   └── listener/     #   BranchEventsListener — reacts to BranchDeletedEvent
│   │       ├── media/            # module: images
│   │       │   ├── service/      #   ImageService (interface) + impl/ImageServiceImpl
│   │       │   ├── storage/      #   StorageService (interface) + impl/LocalFileStorageService
│   │       │   ├── config/       #   StorageProperties
│   │       │   └── model/        #   ImageType, StoredImage
│   │       └── common/
│   │           ├── exception/    # NotFound / Conflict / BadRequest
│   │           └── handler/      # GlobalExceptionHandler -> ProblemDetail JSON
│   ├── web/                  # (planned) public menu frontend
│   └── admin/                # (planned) admin panel frontend
├── docs/
│   ├── API.md                # API documentation
│   └── ARCHITECTURE.md       # architecture decisions
├── postman/
│   └── online-menu-api.postman_collection.json
├── CLAUDE.md                 # project context & status for Claude Code
├── docker-compose.yml
├── .env.example
└── README.md
```

## Docker setup

| Requirement | Where |
|---|---|
| Custom Dockerfile for the backend | `apps/api/Dockerfile` — multi-stage: Maven build → JRE 21 Alpine, non-root user, healthcheck |
| Database resource | `db` service, `postgres:16-alpine`; tables `branches`, `menu_items` |
| Compose for all services | `docker-compose.yml` — `db` + `api` |
| Networking / service-to-service | custom bridge network `online-menu-backend`; API connects to `jdbc:postgresql://db:5432/...` by service name; `depends_on: condition: service_healthy` |
| Persistent storage (named volume) | `online-menu-postgres-data` (DB), `online-menu-media-data` (images) |
| Environment variables | `.env` / `.env.example` → `POSTGRES_*`, `API_PORT`, `DB_EXTERNAL_PORT`, `UPLOAD_MAX_FILE_SIZE`, `JAVA_OPTS` |
| Starts with `docker compose up` | ✔ |

## API

Full documentation: [docs/API.md](docs/API.md). Summary:

| Method | Path |
|---|---|
| GET / POST | `/api/v1/branches` |
| GET / PUT / DELETE | `/api/v1/branches/{id}` |
| GET / POST | `/api/v1/menu-items` (`?branchId=&category=&available=`) |
| GET / PUT / DELETE | `/api/v1/menu-items/{id}` |
| PUT / GET / DELETE | `/api/v1/menu-items/{id}/image` |

## Postman

Import `postman/online-menu-api.postman_collection.json`. Run it top-to-bottom with the Collection Runner:
requests save created ids into collection variables and contain tests for status codes.
For **Upload image**, choose a file in *Body → form-data → file* first.

## Running the backend without Docker (for development)

```bash
docker compose up db            # only the database
cd apps/api
DB_URL=jdbc:postgresql://localhost:5433/online_menu mvn spring-boot:run
mvn test                        # unit tests + ArchitectureTest (layer/module rules)
```
