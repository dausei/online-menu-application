# CLAUDE.md — контекст проекта для Claude Code

Этот файл описывает, что за проект, что уже сделано, что не проверено и что делать дальше.
Отвечай пользователю на русском. Код, комментарии в коде и документацию (`docs/`) пиши на английском.

## Что за проект

**Online Menu Application** — онлайн-меню для заведения с несколькими филиалами + админ-панель,
где сотрудник редактирует меню своего филиала (блюда, цены, наличие, фото).

Сейчас проект делается как учебное задание **«Project Evaluation — Part 1»**:

> Develop and containerize a web application with backend and database services.

Требования задания (всё должно выполняться):

| # | Требование | Где реализовано |
|---|---|---|
| 1 | Custom Dockerfile for the backend | `apps/api/Dockerfile` |
| 2 | At least one database resource | PostgreSQL, таблицы `branches`, `menu_items` |
| 3 | REST: list, get by ID, create, update, delete | `/api/v1/branches`, `/api/v1/menu-items` |
| 4 | Docker Compose for all services | `docker-compose.yml` |
| 5 | Docker networking, service-to-service | сеть `online-menu-backend`, API → `db:5432` |
| 6 | Persistent DB storage via named volume | `online-menu-postgres-data` |
| 7 | Environment variables for config | `.env.example`, `application.yml` |
| 8 | Starts with `docker compose up` | всё имеет значения по умолчанию |
| 9 | API documentation | `docs/API.md` + Swagger UI |
| 10 | Postman Collection for all endpoints | `postman/online-menu-api.postman_collection.json` |

Оценка: рабочий бэкенд (2), REST + БД (2), Docker (2), документация API (1),
Postman (2), качество кода и структура репозитория (1). Потом устная защита.

## Архитектура (решения уже приняты — придерживаться)

- **Монорепозиторий**: `apps/api` (бэкенд), `apps/web` и `apps/admin` — заглушки под будущие фронты.
- **Модульный монолит**: один Spring Boot процесс, внутри модули `branch`, `menu`, `media` + `common`.
  Цель — простота монолита сейчас и возможность вынести модуль в отдельный сервис потом.
- **Слои внутри каждого модуля** (отдельные пакеты):
  `controller/`, `service/` (интерфейс) + `service/impl/` (реализация), `repository/`, `entity/`,
  `mapper/`, `dto/request/`, `dto/response/`, `event/`, `listener/`.
- **Всё через интерфейсы**: зависят от `BranchService`, `MenuItemService`, `ImageService`, `StorageService`;
  `*Impl` никто напрямую не импортирует. Репозитории — интерфейсы Spring Data (реализация генерируется).
- **Границы модулей**:
  - другой модуль может использовать только `service`, `dto`, `event` чужого модуля;
  - `entity`, `repository`, `mapper`, `media.storage` — внутренние;
  - нет общих JPA-связей между модулями: `MenuItem` хранит `branchId` (число), а не `@ManyToOne Branch`;
  - зависимости в одну сторону: `menu → branch`, `menu → media`. `branch` не знает про `menu`:
    при удалении публикует `BranchDeletedEvent`, `menu/listener/BranchEventsListener` удаляет блюда.
- **Правила проверяются тестом** `apps/api/src/test/java/com/onlinemenu/api/ArchitectureTest.java` (ArchUnit).
  Если тест падает — исправлять код, а не ослаблять правило.
- **Изображения**: в БД хранится только ключ (`menu-items/<uuid>.jpg`), URL строит mapper
  (`/api/v1/menu-items/{id}/image`). Сейчас файлы лежат в named volume `online-menu-media-data`
  через `LocalFileStorageService`; позже — MinIO/S3 новой реализацией `StorageService`.
- **Схема БД** только через Flyway (`src/main/resources/db/migration`), `ddl-auto: none`.
  Не менять старые миграции — добавлять новые `V3__...sql`.
- **Ошибки** — RFC 7807 `ProblemDetail` через `common/handler/GlobalExceptionHandler`.

## Стек

Java 21 · Spring Boot 3.5.6 · Maven · Spring Data JPA · PostgreSQL 16 · Flyway ·
springdoc-openapi 2.8.9 · ArchUnit 1.4.1 · Docker (multi-stage: maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre-alpine).

Maven Wrapper (`mvnw`) пока нет — сборка через Docker или установленный `mvn`.

## Текущее состояние

### Сделано
- Структура монорепозитория, README, `docs/API.md`, `docs/ARCHITECTURE.md`.
- Модуль `branch`: CRUD филиалов, уникальный `slug`, событие удаления.
- Модуль `menu`: CRUD блюд, фильтры `branchId`/`category`/`available`, сортировка
  `category, sortOrder, id`, загрузка/получение/удаление фото (`/api/v1/menu-items/{id}/image`).
- Модуль `media`: проверка типа (jpeg/png/webp), лимит 5 MB, хранение в volume, защита от path traversal.
- Миграции `V1__init_schema.sql`, `V2__seed_demo_data.sql` (2 филиала, 6 блюд).
- Dockerfile (non-root, healthcheck через `/actuator/health`), `docker-compose.yml`
  (db + api, healthcheck, `depends_on: service_healthy`, сеть, 2 named volume), `.env.example`.
- Postman-коллекция: 20 запросов с тестами, id сохраняются в переменные коллекции.
- Тесты: `BranchServiceImplTest` (Mockito), `ArchitectureTest` (ArchUnit).

### ⚠️ НЕ ПРОВЕРЕНО
Код писался без доступа к Maven Central и Docker Hub — **ни разу не компилировался и не запускался**.
Версии зависимостей (Spring Boot 3.5.6, springdoc 2.8.9, ArchUnit 1.4.1) взяты по памяти —
если какая-то не резолвится, заменить на ближайшую существующую.

## Что сделать первым делом (по порядку)

1. `cd apps/api && mvn -q test` — собрать и прогнать тесты, исправить ошибки компиляции.
2. `docker compose up --build` из корня — убедиться, что `db` и `api` становятся healthy.
3. Проверить вручную или через Postman (Collection Runner) все эндпоинты из `docs/API.md`,
   включая коды ошибок 400/404/409/413 и загрузку картинки.
4. Проверить, что данные переживают `docker compose down && docker compose up` (volume).
5. Сверить `docs/API.md` и Postman с реальным поведением, поправить расхождения.
6. `git init` + первый коммит (в `.gitignore` уже есть `.env`, `target/`).
7. Удалить пустую папку `apps/api/src/main/java/com/onlinemenu/api/common/web/`, если осталась.

## Планы (после Part 1, сейчас НЕ делать без запроса)

- `apps/web` — публичное меню (`/menu/{branchSlug}`, QR-код), SSR/ISR.
- `apps/admin` — админка: CRUD блюд, фото, цены/наличие.
- Модуль `auth`: пользователи, роли по филиалам (`UserBranchRole`), JWT.
- MinIO/S3 вместо volume (`S3StorageService implements StorageService`), CDN для картинок, ресайз.
- Возможно общий каталог блюд + цены/наличие по филиалам (`BranchMenuItem`).
- Кэш (Redis) — только когда появится реальная нагрузка; сначала HTTP-кэш/ETag.

## Команды

```bash
docker compose up --build          # всё приложение: http://localhost:8080
docker compose up db               # только БД (localhost:5433)
docker compose down                # остановить (данные в volume остаются)
docker compose down -v             # полный сброс БД и картинок

cd apps/api
mvn test                           # unit + architecture tests
DB_URL=jdbc:postgresql://localhost:5433/online_menu mvn spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health
