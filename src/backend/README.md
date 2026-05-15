# Scapes Backend

REST API untuk aplikasi Scapes.

## Prasyarat

- PHP 8.1+
- MySQL 5.7+
- Composer (untuk manajemen dependency PHP)

## Setup Awal

1. Install dependencies:
```bash
composer install
```

2. Buat database MySQL dan import schema:
```bash
mysql -u root -p < ../sql/scapes_db.sql
```

3. Konfigurasi database dan keamanan di file `.env` (copy dari `.env.example`).
   - **PENTING**: Isi `APP_KEY` dengan 32 karakter unik untuk enkripsi session.
   - **PENTING**: Isi `JWT_SECRET` untuk keamanan token.

4. (Opsional) Buat akun testing:
```bash
# Buat akun contributor (test@example.com / password123)
php seed_user.php

# Buat akun admin (admin@scapes.app / admin12345)
php seed_admin.php
```

## Menjalankan Server

**PENTING**: Gunakan router.php agar routing bekerja dengan baik di PHP Built-in Server:

```bash
php -S localhost:8000 router.php
```

Kemudian server akan berjalan di `http://localhost:8000`

> **Catatan**: File router.php diperlukan karena PHP Built-in Server tidak mendukung .htaccess (Apache RewriteRules). Router ini mengarahkan semua request ke index.php untuk diproses oleh aplikasi.

## Manajemen File (Storage)

Aplikasi ini mengelola file wallpaper secara fisik di folder `storage/wallpapers/`. File dipisahkan berdasarkan status moderasinya:

- **Pending**: `storage/wallpapers/pending/{category_slug}/` (Saat baru diupload)
- **Approved**: `storage/wallpapers/approved/{category_slug}/` (Setelah disetujui admin)

Sistem akan otomatis memindahkan file secara fisik ketika status moderasi berubah.

**Catatan URL/Path**:
- Semua endpoint retrieval (wallpaper details, list wallpapers) menyertakan dua field:
  - `image_path`: Path relatif ke file (misal: `minimalist/file.jpg`)
  - `image_url`: Full URL ke file dengan base URL (misal: `http://localhost:8000/wallpapers/minimalist/file.jpg`)
- Untuk wallpaper yang sudah **approved**, bagian `approved/` akan dihapus otomatis dari path.
- Wallpaper dengan status **pending** hanya dapat dilihat oleh **Admin** atau **Pemilik** (contributor yang mengupload). Jika diakses secara publik, endpoint akan mengembalikan error 403 atau menyembunyikannya dari daftar.

## Keamanan Data

- **Encrypted Sessions**: Token JWT disimpan di database dalam bentuk terenkripsi menggunakan AES-256-CBC untuk mencegah kebocoran data jika database terekspos.
- **Protected Endpoints**: Endpoint sensitif dilindungi oleh `AuthMiddleware` dan memerlukan header `Authorization: Bearer <token>`.

## Dokumentasi API

### Autentikasi

#### POST /auth/register
Mendaftarkan akun kontributor baru.

**Request Body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Contoh Response (201 Created):**
```json
{
  "success": true,
  "status_code": 201,
  "message": "Akun berhasil dibuat. Silakan login dengan email dan password Anda.",
  "data": {
    "user_id": 1,
    "email": "user@example.com",
    "role": "contributor",
    "created_at": "2026-05-16 10:30:45"
  }
}
```

---

#### POST /auth/login
Masuk dan mendapatkan token JWT untuk mengakses endpoint yang dilindungi.

**Request Body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Login berhasil",
  "data": {
    "user_id": 1,
    "email": "user@example.com",
    "role": "contributor",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjoiY29udHJpYnV0b3IiLCJpYXQiOjE2MzA3MDMyNDUsImV4cCI6MTYzMDc4OTY0NX0.abcdefg123456",
    "expires_in": 86400
  }
}
```

> **Catatan**: Gunakan token di atas dalam header `Authorization: Bearer {token}` untuk request ke endpoint yang dilindungi.

---

#### POST /auth/logout
Logout dan membatalkan sesi token JWT.

**Request Header:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Logout berhasil",
  "data": {}
}
```

---

### Wallpaper

#### POST /wallpaper/upload
Upload wallpaper baru untuk menunggu moderasi admin.
**Dilindungi**: Hanya kontributor yang terdaftar

**Request Body (FormData):**
- `title`: Judul wallpaper (Text, wajib)
- `description`: Deskripsi wallpaper (Text, opsional)
- `category_id`: ID kategori (Integer, wajib)
- `tag_ids`: ID tag, dipisahkan koma (Text, opsional. Contoh: `1,2,5`)
- `file`: File gambar dalam format .jpg, .png, atau .webp, max 10MB (File, wajib)

**Request Header:**
```
Authorization: Bearer {token}
```

**Contoh Response (201 Created):**
```json
{
  "success": true,
  "status_code": 201,
  "message": "Wallpaper berhasil diunggah. Menunggu moderasi admin.",
  "data": {
    "id": 42,
    "title": "Beautiful Minimalist",
    "status": "pending",
    "category_id": 1,
    "contributor_id": 5,
    "published_at": "2026-05-16 10:30:45"
  }
}
```

---

#### GET /wallpaper/{id}
Mengambil detail dan status wallpaper tertentu.

**Path Parameter:**
- `id`: ID wallpaper

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Detail wallpaper berhasil diambil",
  "data": {
    "id": 42,
    "title": "Beautiful Minimalist",
    "status": "approved",
    "width": 1920,
    "height": 1080,
    "size_kb": 245,
    "category_id": 1,
    "contributor_id": 5,
    "description": "A beautiful minimalist design wallpaper",
    "image_path": "minimalist/1778857359_cd2cf601.jpg",
    "image_url": "http://localhost:8000/wallpapers/minimalist/1778857359_cd2cf601.jpg",
    "uploaded_at": "2026-05-15 14:20:30",
    "updated_at": "2026-05-16 09:15:00"
  }
}
```

---

#### DELETE /wallpaper/{id}
Menghapus wallpaper.
**Dilindungi**: Hanya pemilik (kontributor) atau admin yang bisa menghapus

**Path Parameter:**
- `id`: ID wallpaper

**Request Header:**
```
Authorization: Bearer {token}
```

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper berhasil dihapus",
  "data": {}
}
```

**Contoh Response Error (403 Forbidden):**
```json
{
  "success": false,
  "status_code": 403,
  "message": "Anda tidak memiliki izin untuk menghapus wallpaper ini",
  "data": {}
}
```

---

#### GET /wallpaper/contributor/{contributor_id}
Mengambil semua wallpaper yang diunggah oleh kontributor tertentu.

**Path Parameter:**
- `contributor_id`: ID kontributor

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper kontributor berhasil diambil",
  "data": {
    "wallpapers": [
      {
        "id": 42,
        "title": "Beautiful Minimalist",
        "status": "approved",
        "category_id": 1,
        "image_path": "minimalist/1778857359_cd2cf601.jpg",
        "image_url": "http://localhost:8000/wallpapers/minimalist/1778857359_cd2cf601.jpg",
        "uploaded_at": "2026-05-15 14:20:30"
      },
      {
        "id": 43,
        "title": "Nature Landscape",
        "status": "pending",
        "category_id": 2,
        "image_path": "pending/nature/1778857400_abc123def.jpg",
        "image_url": "http://localhost:8000/wallpapers/pending/nature/1778857400_abc123def.jpg",
        "uploaded_at": "2026-05-16 10:15:00"
      }
    ]
  }
}
```

---

#### GET /wallpaper/category/{category_id}
Mengambil daftar wallpaper yang sudah disetujui berdasarkan kategori.

**Path Parameter:**
- `category_id`: ID kategori

**Query Parameter:**
- `page`: Nomor halaman (Default: 1)
- `limit`: Jumlah item per halaman (Default: 20)

**Contoh Request:**
```
GET /wallpaper/category/1?page=1&limit=10
```

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper disetujui berhasil diambil",
  "data": {
    "wallpapers": [
      {
        "id": 42,
        "title": "Beautiful Minimalist",
        "status": "approved",
        "category_id": 1,
        "contributor_id": 5,
        "width": 1920,
        "height": 1080,
        "size_kb": 245,
        "image_path": "minimalist/1778857359_cd2cf601.jpg",
        "image_url": "http://localhost:8000/wallpapers/minimalist/1778857359_cd2cf601.jpg",
        "uploaded_at": "2026-05-15 14:20:30"
      }
    ],
    "page": 1,
    "limit": 10,
    "total": 1
  }
}
```

---

#### GET /wallpapers/{path}
Mengunduh file gambar wallpaper secara langsung.

**Path Parameter:**
- `path`: Nilai dari field `image_path` yang didapat dari endpoint pengambilan wallpaper
  (Contoh: `minimalist/1778857359_cd2cf601.jpg`)

**Keamanan:**
- Jika `path` merujuk ke wallpaper **disetujui** (approved), file dapat diakses secara publik.
- Jika `path` merujuk ke wallpaper **tertunda** (pending), request memerlukan header `Authorization` dengan role **Admin** atau pemilik wallpaper.

**Contoh Request untuk pending:**
```
GET /wallpapers/pending/minimalist/1778857359_cd2cf601.jpg
Authorization: Bearer {token}
```

**Contoh Response (200 OK):**
- Content-Type: `image/jpeg` (atau sesuai tipe file)
- Body: Binary file dari gambar

---

### Moderasi

#### POST /moderation/moderate
Menyetujui atau menolak wallpaper untuk dipublikasikan.
**Dilindungi**: Hanya admin

**Request Body (JSON):**
```json
{
  "wallpaper_id": 42,
  "decision": "approved",
  "reason": "Kualitas gambar sangat baik"
}
```

> **Catatan**: Field `reason` wajib diisi jika `decision` adalah `"rejected"`.

**Request Header:**
```
Authorization: Bearer {admin_token}
```

**Contoh Response (200 OK - Disetujui):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper disetujui oleh admin",
  "data": {
    "wallpaper_id": 42,
    "decision": "approved",
    "reviewed_by": 1,
    "reviewed_at": "2026-05-16 11:00:00"
  }
}
```

**Contoh Response (200 OK - Ditolak):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Wallpaper ditolak oleh admin",
  "data": {
    "wallpaper_id": 42,
    "decision": "rejected",
    "reason": "Gambar mengandung konten yang tidak sesuai",
    "reviewed_by": 1,
    "reviewed_at": "2026-05-16 11:00:00"
  }
}
```

---

#### GET /moderation/pending
Mengambil daftar wallpaper yang menunggu moderasi.
**Dilindungi**: Hanya admin

**Query Parameter:**
- `page`: Nomor halaman (Default: 1)
- `limit`: Jumlah item per halaman (Default: 20)

**Request Header:**
```
Authorization: Bearer {admin_token}
```

**Contoh Request:**
```
GET /moderation/pending?page=1&limit=20
```

**Contoh Response (200 OK):**
```json
{
  "success": true,
  "status_code": 200,
  "message": "Daftar wallpaper tertunda berhasil diambil",
  "data": {
    "wallpapers": [
      {
        "id": 43,
        "title": "Nature Landscape",
        "status": "pending",
        "category_id": 2,
        "contributor_id": 5,
        "width": 2560,
        "height": 1440,
        "size_kb": 512,
        "image_path": "pending/nature/1778857400_abc123def.jpg",
        "image_url": "http://localhost:8000/wallpapers/pending/nature/1778857400_abc123def.jpg",
        "uploaded_at": "2026-05-16 10:15:00"
      }
    ],
    "page": 1,
    "limit": 20,
    "total": 1
  }
}
```

---

## Struktur Proyek

```
src/backend/
├── config/              - Konfigurasi database & bootstrap
├── public/              - Entry point (index.php) & .htaccess
├── storage/             - Penyimpanan fisik wallpaper & logs
├── src/
│   ├── Core/            - Domain entities (User, Wallpaper, Tag, dll)
│   ├── Application/     - Use cases (Business Logic)
│   ├── Infrastructure/  - Repositories, Storage, Auth, Security
│   └── Interfaces/      - HTTP controllers
├── tests/               - Unit tests
├── vendor/              - PHP dependencies (Composer)
├── router.php           - Router untuk PHP Built-in Server
├── composer.json        - Konfigurasi Composer
└── phpunit.xml          - Konfigurasi PHPUnit
```

## Catatan Penting

- Gunakan Postman atau tool API testing lainnya untuk testing endpoint.
- Pastikan folder `storage/` memiliki permission write (755 atau 777).
- Semua test menggunakan mocking dan pattern AAA (Arrange, Act, Assert).
- Untuk development, selalu gunakan `php -S localhost:8000 router.php` bukan `-t public/`.
- Token JWT memiliki masa berlaku 24 jam, setelah itu perlu login ulang.
