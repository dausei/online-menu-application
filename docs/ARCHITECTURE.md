# Architecture

## Monorepo + modular monolith

One repository holds all applications (`apps/api`, later `apps/web`, `apps/admin`).
The backend is deployed as **one** service (simple to run and debug), but internally it is split into
**modules** with clear boundaries, so a module can be extracted into a separate service later.

```
            ┌──────────────── apps/api (one Spring Boot process) ────────────────┐
 HTTP ──►   │  branch  ◄──── menu ────►  media ──► StorageService ──► volume     │
            │    │  publishes      listens to                (later: S3/MinIO)   │
            │    └── BranchDeletedEvent ──►┘                                     │
            └──────────────────────────────┬─────────────────────────────────────┘
                                           │ JDBC (Docker network "backend")
                                       PostgreSQL
```

### Layers inside a module

Every module has the same package layout:

```
<module>/
├── controller/     REST endpoints, HTTP only (status codes, headers)
├── service/        interface — the ONLY entry point for other modules
│   └── impl/       its implementation (*ServiceImpl), never referenced directly
├── repository/     Spring Data JPA interfaces
├── entity/         JPA entities
├── mapper/         entity <-> DTO conversion
├── dto/request/    request bodies with Bean Validation
├── dto/response/   response bodies (records)
├── event/          domain events published by the module
└── listener/       reactions to events of other modules
```

Everything goes through interfaces: controllers and other modules depend on `BranchService`,
`MenuItemService`, `ImageService`, `StorageService`; Spring injects the `impl` class.
Repositories are interfaces too — Spring Data JPA generates their implementation at runtime.

Call direction: `controller → service → repository → entity`. Controllers never see entities or repositories.

### Module rules

1. **Public API of a module** = `service`, `dto`, `event`. `entity`, `repository`, `mapper` are internal.
2. **No shared entities.** `MenuItem` stores `branchId` (a number), not a JPA relation to `Branch`.
3. **Dependencies go one way:** `menu → branch`, `menu → media`. `branch` knows nothing about `menu`;
   when a branch is deleted it publishes `BranchDeletedEvent`, and `menu.listener.BranchEventsListener`
   removes its items.
4. **Storage is behind an interface.** Other modules use `ImageService`; `media.storage` is internal.
   The DB keeps only an image *key* (e.g. `menu-items/<uuid>.jpg`); the URL is built by the mapper.
   Switching to S3/MinIO = one new `StorageService` implementation.
5. **Rules are enforced automatically** by `ArchitectureTest` (ArchUnit): `mvn test` fails if a
   controller uses a repository, someone depends on an `impl` class instead of the interface,
   or a module reaches into another module's internals.

### How to extract a module later

Replace the in-process call (`branches.exists(id)`) with an HTTP client implementing the same method,
and the Spring event with a message broker. Other code does not change.

## Data model

| Table | Module | Key columns |
|---|---|---|
| `branches` | branch | `id`, `name`, `slug` (unique), `address`, `phone`, `active` |
| `menu_items` | menu | `id`, `branch_id` → branches, `name`, `category`, `price`, `available`, `sort_order`, `image_key` |

Schema is versioned with Flyway (`apps/api/src/main/resources/db/migration`).

## Roadmap

- `apps/web` — public menu (`/menu/{slug}`), `apps/admin` — admin panel
- `auth` module: users and per-branch roles for the admin panel
- MinIO/S3 instead of the local volume, CDN for images
- Redis cache for public menu reads when needed
