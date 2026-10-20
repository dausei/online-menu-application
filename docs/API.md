# Online Menu API — v1

Base URL: `http://localhost:8080` (port is configured by `API_PORT`).

Interactive documentation (Swagger UI) is generated from the code and available while the app runs:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Health check: <http://localhost:8080/actuator/health>

All request and response bodies are JSON (`Content-Type: application/json`), except image upload (`multipart/form-data`) and image download (binary).

## Resources

| Resource | Description |
|---|---|
| **Branch** | A restaurant branch (филиал). Each branch has its own menu. |
| **Menu item** | A dish in the menu of one branch. Can have one image. |

## Endpoints overview

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/branches` | List branches | 200 |
| GET | `/api/v1/branches/{id}` | Get branch by id | 200 |
| POST | `/api/v1/branches` | Create branch | 201 |
| PUT | `/api/v1/branches/{id}` | Update branch | 200 |
| DELETE | `/api/v1/branches/{id}` | Delete branch with its menu items | 204 |
| GET | `/api/v1/menu-items` | List menu items (filters: `branchId`, `category`, `available`) | 200 |
| GET | `/api/v1/menu-items/{id}` | Get menu item by id | 200 |
| POST | `/api/v1/menu-items` | Create menu item | 201 |
| PUT | `/api/v1/menu-items/{id}` | Update menu item | 200 |
| DELETE | `/api/v1/menu-items/{id}` | Delete menu item (and its image) | 204 |
| PUT | `/api/v1/menu-items/{id}/image` | Upload / replace image | 200 |
| GET | `/api/v1/menu-items/{id}/image` | Download image | 200 |
| DELETE | `/api/v1/menu-items/{id}/image` | Delete image | 204 |

---

## Branches

### Branch object

```json
{
  "id": 1,
  "name": "Downtown",
  "slug": "downtown",
  "address": "Abay Ave 10, Almaty",
  "phone": "+7 700 000 00 01",
  "active": true,
  "createdAt": "2026-10-20T05:00:00Z",
  "updatedAt": "2026-10-20T05:00:00Z"
}
```

### Request body (POST and PUT)

| Field | Type | Required | Rules |
|---|---|---|---|
| `name` | string | yes | not blank, max 120 |
| `slug` | string | yes | unique, max 120, `^[a-z0-9]+(-[a-z0-9]+)*$` (e.g. `mega-park`) |
| `address` | string | no | max 255 |
| `phone` | string | no | max 32 |
| `active` | boolean | no | default `true` |

`PUT` is a full replacement: send all fields.

### GET /api/v1/branches

Returns an array of branches sorted by id.

```bash
curl http://localhost:8080/api/v1/branches
```

### GET /api/v1/branches/{id}

`200` with branch object, `404` if not found.

### POST /api/v1/branches

```bash
curl -X POST http://localhost:8080/api/v1/branches \
  -H "Content-Type: application/json" \
  -d '{"name":"Esentai","slug":"esentai","address":"Al-Farabi Ave 77","phone":"+7 700 000 00 03"}'
```

`201 Created` with the created branch and a `Location` header. `400` on validation error, `409` if the slug already exists.

### PUT /api/v1/branches/{id}

`200` with the updated branch. `400`, `404`, `409` as above.

### DELETE /api/v1/branches/{id}

`204 No Content`. Also deletes all menu items of the branch and their images. `404` if not found.

---

## Menu items

### Menu item object

```json
{
  "id": 1,
  "branchId": 1,
  "name": "Beef Burger",
  "description": "Beef patty, cheddar, pickles, house sauce",
  "category": "Burgers",
  "price": 3490.00,
  "available": true,
  "sortOrder": 10,
  "imageUrl": "/api/v1/menu-items/1/image",
  "createdAt": "2026-10-20T05:00:00Z",
  "updatedAt": "2026-10-20T05:00:00Z"
}
```

`imageUrl` is `null` when the item has no image.

### Request body (POST and PUT)

| Field | Type | Required | Rules |
|---|---|---|---|
| `branchId` | number | yes | must reference an existing branch |
| `name` | string | yes | not blank, max 150 |
| `description` | string | no | max 1000 |
| `category` | string | yes | not blank, max 80 (e.g. `Burgers`, `Drinks`) |
| `price` | number | yes | ≥ 0, max 8 integer digits and 2 decimals |
| `available` | boolean | no | default `true` |
| `sortOrder` | number | no | ≥ 0, default `0` |

### GET /api/v1/menu-items

Query parameters (all optional, can be combined):

| Param | Example | Description |
|---|---|---|
| `branchId` | `1` | Only items of this branch |
| `category` | `Burgers` | Case-insensitive category match |
| `available` | `true` | Only available / unavailable items |

Sorted by `category`, `sortOrder`, `id`.

```bash
curl "http://localhost:8080/api/v1/menu-items?branchId=1&available=true"
```

### GET /api/v1/menu-items/{id}

`200` with the item, `404` if not found.

### POST /api/v1/menu-items

```bash
curl -X POST http://localhost:8080/api/v1/menu-items \
  -H "Content-Type: application/json" \
  -d '{"branchId":1,"name":"Cheeseburger","category":"Burgers","price":2490,"sortOrder":30}'
```

`201 Created` + `Location`. `400` on validation error or when `branchId` does not exist.

### PUT /api/v1/menu-items/{id}

Full replacement. `200`, `400`, `404`.

### DELETE /api/v1/menu-items/{id}

`204`. The image file is deleted too. `404` if not found.

---

## Menu item image

Images are stored in a Docker named volume (`online-menu-media-data`). The database stores only the
storage key, so the storage can later be switched to S3/MinIO without changing the API.

### PUT /api/v1/menu-items/{id}/image

`multipart/form-data` with field **`file`**. Allowed types: `image/jpeg`, `image/png`, `image/webp`. Max size 5 MB (`UPLOAD_MAX_FILE_SIZE`).

```bash
curl -X PUT http://localhost:8080/api/v1/menu-items/1/image -F "file=@burger.jpg"
```

`200` with the updated menu item (now `imageUrl` is set). Replaces the previous image.
`400` for a missing file or unsupported type, `404` if item not found, `413` if the file is too large.

### GET /api/v1/menu-items/{id}/image

Returns the binary image with the correct `Content-Type` and `Cache-Control: public, max-age=86400`.
`404` if the item or its image does not exist.

### DELETE /api/v1/menu-items/{id}/image

`204`. `404` if the item has no image.

---

## Errors

All errors use the RFC 7807 *Problem Details* format:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Branch with id 99 not found",
  "instance": "/api/v1/branches/99"
}
```

Validation errors (`400`) additionally contain a map of invalid fields:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/v1/menu-items",
  "errors": {
    "name": "must not be blank",
    "price": "must be greater than or equal to 0.00"
  }
}
```

| Status | When |
|---|---|
| 400 | Invalid body / params, unknown `branchId`, bad image |
| 404 | Resource not found |
| 409 | Duplicate branch slug |
| 413 | Uploaded file too large |
| 500 | Unexpected server error |
