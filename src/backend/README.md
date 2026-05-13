# Scapes Backend

REST API untuk aplikasi Scapes.

## Prasyarat

- PHP 8.1+
- MySQL 5.7+
- Composer

## Setup Awal

1. Install dependencies:
```bash
composer install
```

2. Buat database MySQL dan import schema:
```bash
mysql -u root -p < ../sql/scapes_db.sql
```

3. Konfigurasi database di `config/bootstrap.php` (sesuaikan HOST, USER, PASSWORD jika diperlukan)

## Menjalankan Server

Jalankan development server dengan:
```bash
php -S localhost:8000 -t public/
```

Server akan berjalan di `http://localhost:8000`

**Test endpoint welcome:**
```bash
curl http://localhost:8000/
```

## Menjalankan Tests

Jalankan semua unit tests:
```bash
vendor/bin/phpunit
```

**Options:**
```bash
# Jalankan test file spesifik
vendor/bin/phpunit tests/Unit/Application/UseCases/RegisterContributorUseCaseTest.php

# Jalankan dengan verbose output
vendor/bin/phpunit --verbose

# Jalankan dengan code coverage
vendor/bin/phpunit --coverage-html coverage/
```

## API Endpoints

### Authentication

#### POST /auth/register
Registers a new contributor account.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Response (201 - Success):**
```json
{
  "success": true,
  "status_code": 201,
  "message": "Account registered successfully. Please login.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "role": "contributor",
    "is_verified": false
  }
}
```

**Error Responses:**
- `400 Bad Request`: Email and password are required
- `400 Bad Request`: Email already exists or invalid password format
- `500 Internal Server Error`: Server error

---

#### POST /auth/login
Authenticate a user and create a session.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Login successful",
  "data": {
    "token": "session_token_xyz123"
  }
}
```

**Error Responses:**
- `400 Bad Request`: Email and password are required
- `401 Unauthorized`: Invalid email or password
- `500 Internal Server Error`: Server error

---

#### POST /auth/logout
Logout a user and invalidate the session.

**Request:**
```json
{
  "token": "session_token_xyz123"
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Logout successful",
  "data": {}
}
```

**Error Responses:**
- `401 Unauthorized`: Token not found or invalid
- `500 Internal Server Error`: Server error

---

### Wallpaper

#### POST /wallpaper/upload
Upload a new wallpaper for moderation.

**Request (FormData):**
```
- title: "Beautiful Landscape" (required)
- description: "A scenic mountain view" (optional)
- category_id: 1 (required)
- contributor_id: 5 (required)
- file: <binary image file> (required, .jpg/.png/.webp, max 10MB)
```

**Response (201 - Success):**
```json
{
  "success": true,
  "status_code": 201,
  "message": "Wallpaper uploaded successfully. Waiting for moderation.",
  "data": {
    "id": 42,
    "title": "Beautiful Landscape",
    "status": "pending",
    "category_id": 1,
    "contributor_id": 5,
    "uploaded_at": "2024-05-13 10:30:45"
  }
}
```

**Error Responses:**
- `400 Bad Request`: Wallpaper title is required
- `400 Bad Request`: Wallpaper category is required
- `400 Bad Request`: Wallpaper file must be uploaded
- `401 Unauthorized`: Contributor ID not found
- `400 Bad Request`: Invalid file format or file size exceeds limit
- `500 Internal Server Error`: Server error

---

#### GET /wallpaper/{id}
Retrieve wallpaper details and status.

**Request:**
```json
{
  "wallpaper_id": 42
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper details retrieved successfully",
  "data": {
    "id": 42,
    "title": "Beautiful Landscape",
    "status": "pending",
    "width": 1920,
    "height": 1080,
    "size_kb": 2048,
    "category_id": 1,
    "contributor_id": 5,
    "description": "A scenic mountain view",
    "uploaded_at": "2024-05-13 10:30:45",
    "updated_at": "2024-05-13 10:30:45"
  }
}
```

**Error Responses:**
- `400 Bad Request`: Wallpaper ID is required
- `404 Not Found`: Wallpaper not found
- `500 Internal Server Error`: Server error

---

#### DELETE /wallpaper/{id}
Delete a wallpaper (only by owner or admin).

**Request:**
```json
{
  "wallpaper_id": 42,
  "user_id": 5
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper deleted successfully",
  "data": {}
}
```

**Error Responses:**
- `400 Bad Request`: Wallpaper ID is required
- `401 Unauthorized`: User not authenticated
- `403 Forbidden`: You do not have permission to delete this wallpaper
- `404 Not Found`: Wallpaper not found
- `500 Internal Server Error`: Server error

---

#### GET /wallpaper/contributor/{contributor_id}
Retrieve all wallpapers uploaded by a specific contributor.

**Request:**
```json
{
  "contributor_id": 5
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Contributor wallpapers retrieved successfully",
  "data": {
    "wallpapers": [
      {
        "id": 42,
        "title": "Beautiful Landscape",
        "status": "pending",
        "category_id": 1,
        "uploaded_at": "2024-05-13 10:30:45"
      },
      {
        "id": 43,
        "title": "Urban City",
        "status": "approved",
        "category_id": 2,
        "uploaded_at": "2024-05-12 15:22:10"
      }
    ]
  }
}
```

**Error Responses:**
- `400 Bad Request`: Contributor ID is required
- `500 Internal Server Error`: Server error

---

### Moderation

#### POST /moderation/moderate
Approve or reject a pending wallpaper (admin only).

**Request:**
```json
{
  "admin_id": 1,
  "wallpaper_id": 42,
  "decision": "approved",
  "reason": "Image quality is excellent"
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper moderation processed successfully",
  "data": {
    "id": 1,
    "wallpaper_id": 42,
    "admin_id": 1,
    "decision": "approved",
    "reason": null,
    "reviewed_at": "2024-05-13 11:45:30"
  }
}
```

**Error Responses:**
- `400 Bad Request`: Moderation decision (approved/rejected) is required
- `400 Bad Request`: Reason is required when rejecting
- `401 Unauthorized`: Admin not authenticated
- `404 Not Found`: Wallpaper not found
- `500 Internal Server Error`: Server error

---

#### GET /moderation/pending
Retrieve list of wallpapers pending moderation (admin only).

**Request:**
```json
{
  "admin_id": 1,
  "page": 1,
  "limit": 20
}
```

**Response (200 - Success):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Pending wallpapers list retrieved successfully",
  "data": {
    "wallpapers": [
      {
        "id": 42,
        "title": "Beautiful Landscape",
        "status": "pending",
        "contributor_id": 5,
        "category_id": 1,
        "width": 1920,
        "height": 1080,
        "size_kb": 2048,
        "uploaded_at": "2024-05-13 10:30:45"
      }
    ],
    "page": 1,
    "limit": 20,
    "total": 5
  }
}
```

**Error Responses:**
- `401 Unauthorized`: Admin not authenticated
- `500 Internal Server Error`: Server error

---

### Authentication Workflow

1. **Register** → POST `/auth/register` with email and password
2. **Login** → POST `/auth/login` with credentials to get a token
3. **Use Token** → Include token in subsequent requests (for protected endpoints)
4. **Logout** → POST `/auth/logout` with token to end session

### Authorization

- **Contributor**: Can upload wallpapers, view own wallpapers, delete own wallpapers
- **Admin**: Can view all pending wallpapers, approve/reject wallpapers

### Response Format

All endpoints return responses in the following format:

```json
{
  "success": boolean,
  "status_code": integer,
  "message": string,
  "data": object
}
```

### Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication failed or token invalid
- `403 Forbidden`: Authorization failed
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### Wallpaper Status Values

- `pending`: Awaiting moderation
- `approved`: Approved by admin
- `rejected`: Rejected by admin
- `scheduled`: Scheduled for publication (future feature)

## Project Structure

```
src/backend/
├── config/          - Konfigurasi database
├── public/          - Entry point (index.php)
├── src/
│   ├── Core/        - Domain entities & exceptions
│   ├── Application/ - Use cases
│   ├── Infrastructure/ - Repositories, routing, database
│   └── Interfaces/  - HTTP controllers
├── tests/           - Unit tests
└── vendor/          - Dependencies
```

## Catatan

- Semua endpoint mengembalikan JSON responses
- Error responses menggunakan HTTP status codes yang sesuai
- Semua tests menggunakan mocking dan pattern AAA (Arrange, Act, Assert)
