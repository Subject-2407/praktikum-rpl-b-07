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

## API Endpoints

### Authentication

#### POST /auth/register
Registers a new contributor account.

**Body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

---

#### POST /auth/login
Authenticate a user and create a session.

**Body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```
**Response**: Mengembalikan token JWT yang harus digunakan untuk request berikutnya.

---

#### POST /auth/logout
Logout a user and invalidate the session.
**Protected: Requires Auth Token**

**Body (JSON):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Wallpaper

#### POST /wallpaper/upload
Upload a new wallpaper for moderation.
**Protected: Contributor Only**

**Body (FormData):**
- `title`: "Beautiful Landscape" (Text, required)
- `description`: "A scenic mountain view" (Text, optional)
- `category_id`: 1 (Text/Int, required)
- `tag_ids`: `1,2,5` (Text, optional, comma-separated atau array)
- `file`: `<binary image file>` (File, required, .jpg/.png/.webp, max 10MB)

---

#### GET /wallpaper/{id}
Retrieve wallpaper details and status.

**Body**: None.

---

#### DELETE /wallpaper/{id}
Delete a wallpaper.
**Protected: Owner or Admin Only**

**Body**: None.

---

#### GET /wallpaper/contributor/{contributor_id}
Retrieve all wallpapers uploaded by a specific contributor.

**Body**: None.

---

#### GET /wallpaper/category/{category_id}
Retrieve daftar wallpaper yang sudah disetujui (approved) berdasarkan kategori.

**Query Parameters**:
- `page`: Nomor halaman (Default: 1)
- `limit`: Jumlah item per halaman (Default: 20)

**Body**: None.

---

#### GET /wallpapers/{path}
Mengambil file fisik gambar wallpaper (JPG/PNG/WebP).

**Path Parameter**:
- `path`: Nilai dari field `image_path` yang didapat dari endpoint retrieval.

**Keamanan**:
- Jika `path` merujuk ke wallpaper **approved**, file dapat diakses secara publik.
- Jika `path` merujuk ke wallpaper **pending**, request memerlukan header `Authorization` (Hanya Admin atau Pemilik).

**Body**: None.

---

### Moderation

#### POST /moderation/moderate
Approve atau reject wallpaper.
**Protected: Admin Only**

**Body (JSON):**
```json
{
  "wallpaper_id": 42,
  "decision": "approved", 
  "reason": "Image quality is excellent"
}
```
*Note: `reason` wajib diisi jika `decision` adalah "rejected".*

---

#### GET /moderation/pending
Retrieve daftar wallpaper yang menunggu moderasi.
**Protected: Admin Only**

**Body (JSON/Query):**
```json
{
  "page": 1,
  "limit": 20
}
```

## Project Structure

```
src/backend/
├── config/          - Konfigurasi database & bootstrap
├── public/          - Entry point (index.php) & .htaccess
├── storage/         - Penyimpanan fisik wallpaper & logs
├── src/
│   ├── Core/        - Domain entities (User, Wallpaper, Tag, dll)
│   ├── Application/ - Use cases (Business Logic)
│   ├── Infrastructure/ - Repositories, Storage, Auth, Security
│   └── Interfaces/  - HTTP controllers
└── tests/           - Unit tests
```

## Catatan

- Gunakan Postman atau tool serupa untuk testing.
- Pastikan folder `storage/` memiliki permission write (755 atau 777).
- Semua test menggunakan mocking dan pattern AAA (Arrange, Act, Assert).
