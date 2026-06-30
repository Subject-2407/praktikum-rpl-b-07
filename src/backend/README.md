# scapes-backend

REST API for [Scapes](https://scapes.my.id) — a desktop wallpaper browser and setter. Handles authentication, wallpaper upload and moderation, categories, tags, and JWT sessions.

## Tech Stack

- PHP `>= 8.1`
- Composer
- MySQL
- Redis (JWT denylist for logout)
- Predis (`predis/predis`) as Redis client
- PDO for database access
- SMTP for email verification, password reset, and moderation notifications
- Dotenv for environment configuration
- PHPUnit and PHPStan for verification

## Architecture

Clean Architecture with four layers: controllers translate HTTP, use cases own business logic, repositories handle the database, and infrastructure details stay in the `Infrastructure` layer.

```text
.
├── config/                 # Bootstrap and environment
├── public/                 # HTTP entry point
├── src/
│   ├── Core/               # Domain entities and exceptions
│   ├── Application/        # Use cases and application contracts
│   ├── Infrastructure/     # Auth, database, repository, routing, storage
│   └── Interfaces/         # HTTP controllers, request/response, resources
├── storage/                # Runtime files, logs, and wallpapers
├── tests/                  # Unit tests
├── composer.json
├── phpunit.xml
└── router.php              # Router for PHP built-in server
```

## Setup

```bash
composer install
cp .env.example .env
```

Import the database schema (from the [scapes-db](https://github.com/scapes-app/scapes-db) repo):

```bash
mysql -u root -p scapes < scapes_db.sql
```

## Environment Configuration

Fill `.env` based on `.env.example`. Minimum required values:

```env
APP_KEY=<long_random_string>
JWT_SECRET=<long_random_string_different_from_APP_KEY>
JWT_TTL_MINUTES=30
APP_NAME=Scapes

DB_CONNECTION=mysql
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=scapes
DB_USERNAME=root
DB_PASSWORD=

REDIS_SCHEME=tcp
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0
REDIS_PREFIX=scapes:

FRONTEND_URL=http://localhost:4173
EMAIL_VERIFICATION_URL=http://localhost:4173/email-verifications?token={token}
PASSWORD_RESET_URL=http://localhost:4173/password-resets/{token}

SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_ENCRYPTION=tls
SMTP_TIMEOUT=10
SMTP_HELO_DOMAIN=localhost

MAIL_FROM_ADDRESS=no-reply@example.com
MAIL_FROM_NAME=Scapes
```

`EMAIL_VERIFICATION_URL` and `PASSWORD_RESET_URL` should point to the frontend pages that handle those flows. The verification page reads the token from the URL and calls `POST /email-verifications`; the reset page sends the new password to `PUT /password-resets/{token}`.

Generate secrets on Linux/macOS:

```bash
openssl rand -base64 64
```

On Windows (PowerShell):

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

Use different values for `APP_KEY` and `JWT_SECRET`. Never commit `.env`.

## Redis

Redis is required — logout uses a JWT denylist. On logout, the backend reads the `jti` claim from the active token and stores it in Redis until the token's `exp`. Any middleware-guarded endpoint rejects tokens whose `jti` is on the denylist.

If Redis is not running, login still works, but any endpoint that touches the denylist middleware will fail.

## Running the Server

Use `router.php` instead of `-t public` so route fallback works correctly.

```bash
php -S localhost:8000 router.php
```

Quick check:

```bash
curl http://localhost:8000/categories
```

## Verification

```bash
vendor/bin/phpunit
composer run stan
composer run cs-check
composer run cs-fix
```

## Authentication

The API uses JWT Bearer Tokens. The default TTL is 30 minutes, configurable via `JWT_TTL_MINUTES`. Tokens can be sent as:

```http
Authorization: Bearer <token>
```

On successful login, the backend also sets a cookie:

```text
scapes_access_token
```

The cookie is `HttpOnly`, `SameSite=Lax`, and `Secure` when the request is over HTTPS.

### Roles

| Role | Access |
|---|---|
| Public | `GET /wallpapers`, `GET /wallpapers/{id}`, `GET /sources`, `GET /categories`, `GET /tags` |
| `contributor` | All public endpoints, `/me/wallpapers`, logout |
| `admin` | All public endpoints, `/moderation/wallpapers`, logout |

## Response Format

Success:

```json
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": []
}
```

Success with pagination:

```json
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": [],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 0,
    "last_page": 1
  }
}
```

Error:

```json
{
  "success": false,
  "message": "Validation failed.",
  "errors": {
    "title": ["The title field is required."]
  }
}
```

## API Reference

### Auth

#### `POST /registrations`

Register a new contributor. The account is created with `is_verified = false`. The backend creates a token in `email_verifications` and sends a verification email via SMTP.

Request:

```json
{
  "email": "creator@example.com",
  "password": "Secure@1234",
  "password_confirmation": "Secure@1234"
}
```

Response `201`:

```json
{
  "success": true,
  "message": "Account created. Please check your email to verify your account.",
  "data": {
    "id": 12,
    "email": "creator@example.com",
    "role": "contributor",
    "is_verified": false,
    "created_at": "2026-04-23T10:00:00Z"
  }
}
```

#### `POST /email-verifications`

Verify an account using the token from the email.

Request:

```json
{ "token": "<token_from_email>" }
```

Response `200`:

```json
{
  "success": true,
  "message": "Account verified successfully. You can now log in.",
  "data": null
}
```

#### `POST /sessions`

Login and issue a JWT. Account must already be verified.

Request:

```json
{
  "email": "creator@example.com",
  "password": "Secure@1234"
}
```

Response `200`:

```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_at": "2026-04-23T11:00:00Z",
    "user": {
      "id": 12,
      "email": "creator@example.com",
      "role": "contributor"
    }
  }
}
```

Also sets the `scapes_access_token` cookie.

#### `DELETE /sessions/current`

Logout the current token. The token is added to the Redis denylist until its `exp`.

Response `200`:

```json
{
  "success": true,
  "message": "Logged out successfully.",
  "data": null
}
```

#### `POST /password-resets`

Request a password reset email. The response is intentionally generic to prevent user enumeration.

Request:

```json
{ "email": "creator@example.com" }
```

Response `200`:

```json
{
  "success": true,
  "message": "If that email is registered, a password reset link has been sent.",
  "data": null
}
```

#### `PUT /password-resets/{token}`

Set a new password using the reset token.

Request:

```json
{
  "password": "NewSecure@5678",
  "password_confirmation": "NewSecure@5678"
}
```

---

### Public Wallpapers

#### `GET /wallpapers`

Returns `approved` wallpapers that have a `published_at` date.

Query parameters:

| Parameter | Description |
|---|---|
| `q` | Keyword matched against title, description, or tags |
| `category` | Category slug |
| `tag` | Tag slug — can be repeated: `tag=dark&tag=neon` |
| `target_device` | `desktop`, `mobile`, or `tablet` |
| `page` | Default `1` |
| `per_page` | Default `20`, maximum `100` |
| `sort_by` | `published_at` or `title` |
| `order` | `asc` or `desc` |

#### `GET /wallpapers/{id}`

Returns a single approved wallpaper by ID.

---

### Contributor Wallpapers

All endpoints in this section require the `contributor` role. Admin accounts cannot use `/me/wallpapers`.

#### `GET /me/wallpapers`

Returns all wallpapers belonging to the logged-in contributor.

Query parameters:

| Parameter | Description |
|---|---|
| `status` | Optional: `pending`, `approved`, or `rejected` |
| `page` | Default `1` |
| `per_page` | Default `20`, maximum `100` |

`is_review_overdue` appears in the response when a wallpaper has been pending for more than 3 days.

#### `POST /me/wallpapers`

Upload a new wallpaper for admin review.

Request `multipart/form-data`:

| Field | Required | Description |
|---|---|---|
| `file` | Yes | JPG, PNG, or WebP — max 10 MB, minimum resolution per target device |
| `title` | Yes | Max 255 characters |
| `description` | No | Wallpaper description |
| `category_id` | Yes | ID from `GET /categories` |
| `tags` | No | Array of tag IDs, or a JSON/CSV string when using a form client |

`target_device` is not sent by the client — the backend auto-detects it from the aspect ratio:

| Ratio | Target |
|---|---|
| `>= 1.5` | `desktop` |
| `<= 0.6` | `mobile` |
| otherwise | `tablet` |

Minimum resolution by detected target:

| Target | Minimum |
|---|---|
| `desktop` | 1920×1080 |
| `mobile` | 360×800 |
| `tablet` | 768×1024 |

Uploaded files are stored at `storage/wallpapers/pending/{category_slug}/`.

#### `PATCH /me/wallpapers/{id}`

Update metadata for the logged-in contributor's wallpaper. Approved wallpapers cannot be edited.

#### `DELETE /me/wallpapers/{id}`

Permanently delete the wallpaper from both the database and storage.

---

### Admin Moderation

All endpoints in this section require the `admin` role.

#### `GET /moderation/wallpapers`

Returns the wallpaper moderation queue.

Query parameters:

| Parameter | Description |
|---|---|
| `status` | Default `pending`; accepts `pending`, `approved`, `rejected` |
| `contributor_id` | Optional filter |
| `page` | Default `1` |
| `per_page` | Default `20`, maximum `100` |
| `sort_by` | `created_at` or `title` |
| `order` | `asc` or `desc` |

#### `PATCH /moderation/wallpapers/{id}`

Approve or reject a pending wallpaper.

Approve:

```json
{ "decision": "approved" }
```

Reject:

```json
{
  "decision": "rejected",
  "reason": "Image resolution does not meet the content guideline."
}
```

On approval, the file moves from `storage/wallpapers/pending/{category_slug}/` to `storage/wallpapers/approved/{category_slug}/`.

---

### Metadata

#### `GET /sources`

Returns active sources from the `api_sources` table.

#### `GET /categories`

Returns all categories.

#### `GET /tags`

Returns all tags. Supports `?q=<keyword>` filtering.

---

### File Serving

#### `GET /wallpapers/{path}`

Serves a wallpaper image from storage.

- Approved files are publicly accessible.
- Pending files require a valid JWT.
- Path traversal is rejected at the storage layer.

---

## HTTP Error Codes

| Status | Condition |
|---|---|
| `400` | Input validation failed |
| `401` | Token missing, invalid, expired, or revoked |
| `403` | Role lacks access |
| `404` | Resource not found |
| `409` | Data conflict, e.g. email already registered |
| `410` | Verification or reset token expired or already used |
| `422` | Input is valid but business state does not allow the action |
| `429` | Too many failed login attempts |
| `500` | Internal server error |

## Seeders

Run the admin seeder for a default admin account:

```bash
php seeder/seed_admin.php
# or
composer run seed:admin
```

Default credentials:

```text
Email    : admin@scapes.app
Password : admin12345
Role     : admin
Verified : yes
```

Override via `.env`:

```env
ADMIN_SEED_EMAIL=admin@scapes.app
ADMIN_SEED_PASSWORD=admin12345
```

The seeder is idempotent — it won't create duplicates, and will mark an unverified existing admin as verified.
