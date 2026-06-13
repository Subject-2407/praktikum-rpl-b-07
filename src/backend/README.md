# Scapes Backend

Scapes API.

Dokumentasi kontrak lengkap ada di `../../docs/api-contract.md`.
README ini berisi panduan operasional backend dan ringkasan endpoint
yang sudah diimplementasikan.

## Tech Stack

- PHP `>= 8.1`
- Composer
- MySQL
- Redis untuk denylist JWT
- Predis (`predis/predis`) sebagai Redis client
- PDO untuk akses database
- SMTP untuk email verification, reset password, dan notifikasi moderasi
- Dotenv untuk konfigurasi environment
- PHPUnit dan PHPStan untuk verifikasi

## Struktur Backend

```text
src/backend/
|-- config/                 # Bootstrap dan environment
|-- public/                 # Entry point HTTP
|-- src/
|   |-- Core/               # Domain entity dan exception
|   |-- Application/        # Use case dan kontrak aplikasi
|   |-- Infrastructure/     # Auth, database, repository, routing, storage
|   `-- Interfaces/         # Controller HTTP, request/response, resource
|-- storage/                # File runtime, log, dan wallpaper
|-- tests/                  # Unit test
|-- composer.json
|-- phpunit.xml
`-- router.php              # Router PHP built-in server
```

Arsitektur mengikuti Clean Architecture: controller hanya menerjemahkan
request/response, use case memegang alur bisnis, repository menangani
database, dan detail teknis berada di layer `Infrastructure`.

## Setup

Jalankan semua command dari folder `src/backend`.

```powershell
cd "E:\Semester 4\Rekayasa Perangkat Lunak\TBO\praktikum-rpl-b-07\src\backend"
composer install
Copy-Item .env.example .env
```

Import database:

```powershell
mysql -u root -p < ..\database\scapes_db.sql
```

Jika path di atas tidak cocok dengan posisi shell kamu, gunakan path SQL
yang absolut menuju `src/database/scapes_db.sql`.

## Konfigurasi Environment

Isi `.env` berdasarkan `.env.example`.

Minimal konfigurasi yang dibutuhkan:

```env
APP_KEY=isi_random_yang_panjang
JWT_SECRET=isi_random_yang_panjang_dan_berbeda_dari_app_key
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

`EMAIL_VERIFICATION_URL` dan `PASSWORD_RESET_URL` sebaiknya mengarah ke
halaman frontend yang mengikuti kontrak auth. Halaman verifikasi menerima
token dari email lalu memanggil `POST /email-verifications`, sedangkan
halaman reset password menerima token lalu mengirim password baru ke
`PUT /password-resets/{token}`.

Generate secret di PowerShell:

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

Gunakan hasil yang berbeda untuk `APP_KEY` dan `JWT_SECRET`. Jangan commit
file `.env`.

## Redis

Redis wajib berjalan karena logout JWT memakai denylist Redis. Saat logout,
backend mengambil klaim `jti` token aktif dan menyimpannya ke Redis sampai
token mencapai waktu expired. Middleware akan menolak token yang `jti`-nya
sudah ada di denylist.

Jika Redis belum berjalan, endpoint login masih bisa membuat JWT, tetapi
endpoint yang memakai middleware/denylist dapat gagal saat mencoba mengakses
Redis.

## Menjalankan Server

Gunakan `router.php`, bukan `-t public`, supaya fallback route bekerja.

```powershell
php -S localhost:8000 router.php
```

Base URL lokal:

```text
http://localhost:8000
```

Cek cepat:

```powershell
curl http://localhost:8000/categories
```

## Verifikasi

```powershell
vendor\bin\phpunit
composer run stan
```

Command lain:

```powershell
composer run cs-check
composer run cs-fix
```

## Autentikasi

API memakai JWT Bearer Token dengan TTL 30 menit. Token dapat dikirim melalui:

```http
Authorization: Bearer <token>
```

Saat login berhasil, backend juga menyimpan JWT ke cookie:

```text
scapes_access_token
```

Cookie dibuat dengan `HttpOnly`, `SameSite=Lax`, dan `Secure` otomatis aktif
jika request memakai HTTPS.

Role:

| Role | Akses |
|---|---|
| Publik | `GET /wallpapers`, `GET /wallpapers/{id}`, `GET /sources`, `GET /categories`, `GET /tags` |
| contributor | Semua endpoint publik, `/me/wallpapers`, logout |
| admin | Semua endpoint publik, `/moderation/wallpapers`, logout |

## Format Response

Response sukses:

```json
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": []
}
```

Response sukses dengan pagination:

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

Response error:

```json
{
  "success": false,
  "message": "Validation failed.",
  "errors": {
    "title": ["The title field is required."]
  }
}
```

Catatan: `status_code` hanya dipakai internal oleh router dan tidak dikirim
di body response.

## Endpoint Auth

### POST `/registrations`

Mendaftarkan contributor baru. Akun dibuat dengan `is_verified = false`.
Backend membuat token di tabel `email_verifications` lalu mengirim email
verifikasi lewat SMTP.

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

### POST `/email-verifications`

Memverifikasi akun dari token.

Request:

```json
{
  "token": "token_dari_tabel_email_verifications"
}
```

Response `200`:

```json
{
  "success": true,
  "message": "Account verified successfully. You can now log in.",
  "data": null
}
```

### POST `/sessions`

Login dan membuat JWT. Akun harus sudah verified.

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

Backend juga mengirim cookie `scapes_access_token`.

### DELETE `/sessions/current`

Logout token aktif. Token akan masuk Redis denylist sampai waktu `exp`.

Header:

```http
Authorization: Bearer <token>
```

Response `200`:

```json
{
  "success": true,
  "message": "Logged out successfully.",
  "data": null
}
```

### POST `/password-resets`

Membuat token reset password jika email terdaftar dan mengirimkannya lewat
SMTP. Response sengaja selalu generik untuk mencegah user enumeration.

Request:

```json
{
  "email": "creator@example.com"
}
```

Response `200`:

```json
{
  "success": true,
  "message": "If that email is registered, a password reset link has been sent.",
  "data": null
}
```

### PUT `/password-resets/{token}`

Mengganti password memakai token reset.

Request:

```json
{
  "password": "NewSecure@5678",
  "password_confirmation": "NewSecure@5678"
}
```

Response `200`:

```json
{
  "success": true,
  "message": "Password reset successfully. You can now log in with your new password.",
  "data": null
}
```

## Endpoint Wallpaper Publik

### GET `/wallpapers`

Mengambil wallpaper `approved` yang sudah punya `published_at`.

Query:

| Parameter | Keterangan |
|---|---|
| `q` | Keyword title, description, atau tag |
| `category` | Slug kategori |
| `tag` | Slug tag, bisa multi: `tag=dark&tag=neon` |
| `target_device` | `desktop`, `mobile`, atau `tablet` |
| `page` | Default `1` |
| `per_page` | Default `20`, maksimum `100` |
| `sort_by` | `published_at` atau `title` |
| `order` | `asc` atau `desc` |

Contoh:

```http
GET /wallpapers?category=nature&tag=dark&page=1&per_page=20
```

Response `200`:

```json
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": [
    {
      "id": 42,
      "title": "Midnight Forest",
      "description": "A serene dark forest at midnight.",
      "file_path": "http://localhost:8000/wallpapers/approved/nature/file.jpg",
      "width": 3840,
      "height": 2160,
      "target_device": "desktop",
      "category": { "id": 2, "name": "Nature", "slug": "nature" },
      "tags": [{ "id": 1, "name": "dark", "slug": "dark" }],
      "contributor": { "id": 12, "email": "creator@example.com" },
      "published_at": "2026-04-20T08:00:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 1,
    "last_page": 1
  }
}
```

### GET `/wallpapers/{id}`

Mengambil detail satu wallpaper publik.

Response `200`:

```json
{
  "success": true,
  "message": "Wallpaper retrieved successfully.",
  "data": {
    "id": 42,
    "title": "Midnight Forest",
    "description": "A serene dark forest at midnight.",
    "file_path": "http://localhost:8000/wallpapers/approved/nature/file.jpg",
    "file_name": "file.jpg",
    "file_size_kb": 4096,
    "mime_type": "image/jpeg",
    "width": 3840,
    "height": 2160,
    "target_device": "desktop",
    "status": "approved",
    "category": { "id": 2, "name": "Nature", "slug": "nature" },
    "tags": [{ "id": 1, "name": "dark", "slug": "dark" }],
    "contributor": { "id": 12, "email": "creator@example.com" },
    "published_at": "2026-04-20T08:00:00Z",
    "created_at": "2026-04-18T09:00:00Z"
  }
}
```

## Endpoint Wallpaper Contributor

Semua endpoint di bagian ini hanya menerima role `contributor`. Akun admin
tidak dapat memakai endpoint `/me/wallpapers`; jika admin ingin mengunggah
wallpaper, gunakan akun contributor terpisah.

### GET `/me/wallpapers`

Mengambil semua wallpaper milik user login.

Query:

| Parameter | Keterangan |
|---|---|
| `status` | Opsional: `pending`, `approved`, `rejected` |
| `page` | Default `1` |
| `per_page` | Default `20`, maksimum `100` |

Response `200`:

```json
{
  "success": true,
  "message": "Your wallpapers retrieved successfully.",
  "data": [
    {
      "id": 55,
      "title": "Neon City Lights",
      "status": "pending",
      "target_device": "desktop",
      "category": { "id": 8, "name": "Technology", "slug": "technology" },
      "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
      "moderation": null,
      "created_at": "2026-04-19T12:00:00Z",
      "updated_at": "2026-04-19T12:00:00Z",
      "is_review_overdue": true
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 1,
    "last_page": 1
  }
}
```

`is_review_overdue` hanya muncul jika wallpaper pending lebih dari 3 hari.

### POST `/me/wallpapers`

Upload wallpaper baru untuk review admin.

Request `multipart/form-data`:

| Field | Wajib | Keterangan |
|---|---|---|
| `file` | Ya | JPG, PNG, atau WebP, maksimum 10 MB, minimum 1920x1080 |
| `title` | Ya | Maksimum 255 karakter |
| `description` | Tidak | Deskripsi wallpaper |
| `category_id` | Ya | ID dari `GET /categories` |
| `tags` | Tidak | Array ID tag, atau string JSON/CSV saat memakai form client |

`target_device` tidak dikirim oleh client. Backend mendeteksi otomatis dari
rasio `width / height`:

| Rasio | Target |
|---|---|
| `>= 1.5` | `desktop` |
| `<= 0.75` | `mobile` |
| selain itu | `tablet` |

Response `201`:

```json
{
  "success": true,
  "message": "Wallpaper submitted for review.",
  "data": {
    "id": 61,
    "title": "Neon City Lights",
    "status": "pending",
    "file_name": "1778857359_cd2cf601.jpg",
    "file_size_kb": 5120,
    "width": 3840,
    "height": 2160,
    "target_device": "desktop",
    "category": { "id": 8, "name": "Technology", "slug": "technology" },
    "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
    "created_at": "2026-04-23T10:30:00Z"
  }
}
```

File baru disimpan di:

```text
storage/wallpapers/pending/{category_slug}/
```

### PATCH `/me/wallpapers/{id}`

Update metadata wallpaper milik user login. Wallpaper yang sudah `approved`
tidak dapat diubah oleh contributor.

Request:

```json
{
  "title": "Neon City Lights Revised",
  "description": "Updated description.",
  "category_id": 3,
  "tags": [3, 10]
}
```

Response `200`:

```json
{
  "success": true,
  "message": "Wallpaper updated successfully.",
  "data": {
    "id": 61,
    "title": "Neon City Lights Revised",
    "description": "Updated description.",
    "status": "pending",
    "target_device": "desktop",
    "category": { "id": 3, "name": "Abstract", "slug": "abstract" },
    "tags": [
      { "id": 3, "name": "neon", "slug": "neon" },
      { "id": 10, "name": "futuristic", "slug": "futuristic" }
    ],
    "updated_at": "2026-04-23T11:00:00Z"
  }
}
```

### DELETE `/me/wallpapers/{id}`

Menghapus wallpaper milik user login secara permanen dari database dan
storage.

Response `200`:

```json
{
  "success": true,
  "message": "Wallpaper deleted successfully.",
  "data": null
}
```

## Endpoint Moderasi Admin

Semua endpoint di bagian ini memerlukan role `admin`.

### GET `/moderation/wallpapers`

Mengambil queue wallpaper untuk moderasi.

Query:

| Parameter | Keterangan |
|---|---|
| `status` | Default `pending`; bisa `pending`, `approved`, `rejected` |
| `contributor_id` | Opsional |
| `page` | Default `1` |
| `per_page` | Default `20`, maksimum `100` |
| `sort_by` | `created_at` atau `title` |
| `order` | `asc` atau `desc` |

Response `200`:

```json
{
  "success": true,
  "message": "Wallpapers for moderation retrieved successfully.",
  "data": [
    {
      "id": 55,
      "title": "Neon City Lights",
      "file_path": "http://localhost:8000/wallpapers/pending/technology/file.jpg",
      "width": 3840,
      "height": 2160,
      "target_device": "desktop",
      "mime_type": "image/jpeg",
      "file_size_kb": 5120,
      "status": "pending",
      "category": { "id": 8, "name": "Technology", "slug": "technology" },
      "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
      "contributor": { "id": 12, "email": "creator@example.com" },
      "moderation": null,
      "created_at": "2026-04-19T12:00:00Z",
      "updated_at": "2026-04-19T12:00:00Z",
      "published_at": null
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 1,
    "last_page": 1
  }
}
```

### PATCH `/moderation/wallpapers/{id}`

Menyetujui atau menolak wallpaper pending.

Approve:

```json
{
  "decision": "approved"
}
```

Reject:

```json
{
  "decision": "rejected",
  "reason": "Image resolution does not meet the content guideline."
}
```

Response approve `200`:

```json
{
  "success": true,
  "message": "Wallpaper approved and is now publicly visible.",
  "data": {
    "id": 55,
    "status": "approved",
    "published_at": "2026-04-23T11:30:00Z",
    "moderation": {
      "decision": "approved",
      "reason": null,
      "reviewed_at": "2026-04-23T11:30:00Z",
      "admin_id": 1
    }
  }
}
```

Response reject `200`:

```json
{
  "success": true,
  "message": "Wallpaper rejected. The contributor has been notified.",
  "data": {
    "id": 55,
    "status": "rejected",
    "moderation": {
      "decision": "rejected",
      "reason": "Image resolution does not meet the content guideline.",
      "reviewed_at": "2026-04-23T11:35:00Z",
      "admin_id": 1
    }
  }
}
```

Saat approved, file dipindahkan dari:

```text
storage/wallpapers/pending/{category_slug}/
```

ke:

```text
storage/wallpapers/approved/{category_slug}/
```

## Endpoint Metadata

### GET `/sources`

Mengambil source aktif dari tabel `api_sources`.

Response:

```json
{
  "success": true,
  "message": "Sources retrieved successfully.",
  "data": [
    {
      "id": 1,
      "name": "Scapes",
      "slug": "scapes",
      "base_url": "https://api.scapes.app/v1",
      "is_default": true
    }
  ]
}
```

### GET `/categories`

Mengambil semua kategori.

Response:

```json
{
  "success": true,
  "message": "Categories retrieved successfully.",
  "data": [
    { "id": 1, "name": "Minimalist", "slug": "minimalist" }
  ]
}
```

### GET `/tags`

Mengambil semua tag. Dapat difilter dengan `q`.

Contoh:

```http
GET /tags?q=neon
```

Response:

```json
{
  "success": true,
  "message": "Tags retrieved successfully.",
  "data": [
    { "id": 3, "name": "neon", "slug": "neon" }
  ]
}
```

## Endpoint File Wallpaper

### GET `/wallpapers/{path}`

Melayani file gambar dari storage.

Contoh:

```http
GET /wallpapers/approved/nature/file.jpg
```

Response sukses berupa binary image dengan `Content-Type` sesuai MIME file.

Catatan akses:

- File approved dapat diakses publik.
- File pending memerlukan JWT valid.
- Path traversal ditolak oleh storage layer.

## Error Umum

| Status | Kondisi |
|---|---|
| `400` | Validasi input gagal |
| `401` | Token tidak ada, invalid, expired, atau revoked |
| `403` | Role tidak punya akses |
| `404` | Resource tidak ditemukan |
| `409` | Konflik data, misalnya email sudah terdaftar |
| `410` | Token verifikasi/reset expired atau sudah digunakan |
| `422` | Data valid secara bentuk, tetapi state bisnis tidak sesuai |
| `429` | Terlalu banyak percobaan login gagal |
| `500` | Error internal server |

## Seeder Opsional

Seeder contributor lama masih tersedia untuk membantu development:

```powershell
php seed_user.php
```

Seeder admin terbaru bisa dijalankan dengan salah satu command berikut:

```powershell
php seed_admin.php
composer run seed:admin
```

Default akun admin:

```text
Email    : admin@scapes.app
Password : admin12345
Role     : admin
Verified : yes
```

Kredensial default bisa dioverride dari `.env`:

```env
ADMIN_SEED_EMAIL=admin@scapes.app
ADMIN_SEED_PASSWORD=admin12345
```

Seeder admin bersifat idempotent. Jika admin dengan email yang sama sudah
ada, seeder tidak membuat duplikat. Jika admin lama belum verified, seeder
akan menandainya verified agar bisa login ke `POST /sessions`.

## Catatan

- Email verification, password reset, dan notifikasi moderasi contributor
  dikirim melalui SMTP jika konfigurasi email di `.env` diisi dengan benar.
- JWT TTL default adalah 30 menit.
- Logout memakai Redis denylist berdasarkan klaim `jti`.
- JWT dapat dikirim via Bearer token atau cookie `scapes_access_token`.
- Upload wallpaper menentukan `target_device` otomatis dari aspek rasio.
- Upload memvalidasi MIME type, ukuran file, resolusi, kategori, dan tag.
