# apps/web — public menu (planned)

Public website where guests open the menu of a branch (e.g. by QR code: `/menu/{branchSlug}`).

Not part of Part 1. Will consume the API from `apps/api`:

- `GET /api/v1/branches` — branch list
- `GET /api/v1/menu-items?branchId={id}&available=true` — menu of a branch
- `GET /api/v1/menu-items/{id}/image` — dish photo
