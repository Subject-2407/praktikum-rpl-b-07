# Scapes API Contract

> **Versi Dokumen:** 1.2.0  
> **Spesifikasi:** OpenAPI 3.1.0  
> **Base URL:** `https://scapes.my.id`  
> **Tanggal:** 2026-06-16  
> **Organisasi:** Program Studi Informatika, Universitas Sebelas Maret

---

## Daftar Isi

1. [Pendahuluan](#1-pendahuluan)
   - 1.1 [Deskripsi API](#11-deskripsi-api)
   - 1.2 [Server & Environment](#12-server--environment)
   - 1.3 [Autentikasi](#13-autentikasi)
   - 1.4 [Konvensi Umum](#14-konvensi-umum)
   - 1.5 [Format Respons Standar](#15-format-respons-standar)
   - 1.6 [Kode Error Global](#16-kode-error-global)
2. [Auth](#2-auth)
   - 2.1 [POST /registrations](#21-post-registrations)
   - 2.2 [POST /email-verifications](#22-post-email-verifications)
   - 2.3 [POST /sessions](#23-post-sessions)
   - 2.4 [GET /sessions/current](#24-get-sessionscurrent)
   - 2.5 [DELETE /sessions/current](#25-delete-sessionscurrent)
   - 2.6 [POST /password-resets](#26-post-password-resets)
   - 2.7 [PUT /password-resets/{token}](#27-put-password-resetstoken)
3. [Wallpapers — Publik](#3-wallpapers--publik)
   - 3.1 [GET /wallpapers](#31-get-wallpapers)
   - 3.2 [GET /wallpapers/{id}](#32-get-wallpapersid)
   - 3.3 [POST /search-logs](#33-post-search-logs)
4. [Wallpapers — Contributor](#4-wallpapers--contributor)
   - 4.1 [GET /me/wallpapers](#41-get-mewallpapers)
   - 4.2 [POST /me/wallpapers](#42-post-mewallpapers)
   - 4.3 [PATCH /me/wallpapers/{id}](#43-patch-mewallpapersid)
   - 4.4 [DELETE /me/wallpapers/{id}](#44-delete-mewallpapersid)
5. [Moderation — Admin](#5-moderation--admin)
   - 5.1 [GET /moderation/wallpapers](#51-get-moderationwallpapers)
   - 5.2 [PATCH /moderation/wallpapers/{id}](#52-patch-moderationwallpapersid)
6. [API Sources](#6-api-sources)
   - 6.1 [GET /sources](#61-get-sources)
7. [Categories & Tags](#7-categories--tags)
   - 7.1 [GET /categories](#71-get-categories)
   - 7.2 [GET /tags](#72-get-tags)
   - 7.3 [GET /categories/trending](#73-get-categoriestrending)
   - 7.4 [GET /recommendations/search](#74-get-recommendationssearch)
8. [Schema Komponen](#8-schema-komponen)
9. [OpenAPI YAML](#9-openapi-yaml-lengkap)

---

## 1. Pendahuluan

### 1.1 Deskripsi API

**Scapes API** adalah RESTful backend berbasis PHP yang melayani dua klien utama:

- **Desktop App (JavaFX)** — untuk pencarian, pengunduhan, dan penerapan wallpaper
- **Web Portal (HTML/JS)** — untuk manajemen konten contributor dan moderasi admin

Semua respons menggunakan format **JSON** (`application/json`). Upload file menggunakan `multipart/form-data`.

> **Catatan Scope v1:** Dokumen ini memfokuskan kontrak backend internal Scapes. Fitur backlog berprioritas rendah seperti **schedule publication** dan **limit contributor uploads** sengaja belum dimasukkan ke endpoint v1. Integrasi pencarian ke provider eksternal (Unsplash, Pexels, Pixabay) dan pengelolaan personal API key tetap dilakukan oleh desktop client; API ini hanya menyediakan metadata source melalui `GET /sources`.

---

### 1.2 Server & Environment

| Environment | Base URL |
|---|---|
| Production | `https://scapes.my.id` |
| Staging | `https://staging-api.scapes.app/v1` |
| Development | `http://localhost:8000/v1` |

Pada implementasi API, field `file_path` dan `thumbnail_path` dapat dibentuk dari base URL API. Contoh URL final:

- Wallpaper asli: `{{base_url}}/{file_path}`
- Thumbnail: `{{base_url}}/{thumbnail_path}`

---

### 1.3 Autentikasi

API menggunakan **JWT Bearer Token** yang diperoleh dari endpoint `POST /sessions`.

```
Authorization: Bearer <token>
```

Token berlaku selama **30 menit**. Setelah kedaluwarsa, klien harus login ulang. Setiap token memiliki identitas unik (`jti`). Endpoint `GET /sessions/current` dapat dipakai klien untuk memverifikasi token aktif dan mengambil identitas user saat ini. Saat `DELETE /sessions/current` dipanggil, `jti` token aktif dimasukkan ke denylist server-side sampai token tersebut mencapai waktu kedaluwarsanya. Middleware autentikasi wajib menolak token yang sudah direvoke dengan respons `401 Unauthorized`.

| Role | Akses |
|---|---|
| `contributor` | Endpoint `/registrations`, `/email-verifications`, `/sessions`, `/sessions/current`, `/password-resets`, `/wallpapers` (read), `/me/*`, `/sources`, `/categories`, `/tags` |
| `admin` | Semua endpoint contributor + `/moderation/*` |
| *Publik (tanpa token)* | `GET /wallpapers`, `GET /wallpapers/{id}`, `POST /search-logs`, `GET /sources`, `GET /categories`, `GET /tags`, `GET /categories/trending`, `GET /recommendations/search` |

---

### 1.4 Konvensi Umum

| Aspek | Konvensi |
|---|---|
| Format tanggal | ISO 8601 — `2026-04-23T10:30:00Z` |
| Pagination | Query param `page` (default: 1) & `per_page` (default: 20, maks: 100) |
| Sorting | Query param `sort_by` & `order` (`asc` / `desc`) |
| Soft delete | Tidak digunakan; delete bersifat permanen |
| Bahasa error | Bahasa Inggris (konsisten untuk klien multi-platform) |
| HTTP method | `PATCH` untuk update parsial, `PUT` untuk replace penuh |
| Penamaan endpoint | Utamakan noun plural berbasis resource; action diwakili HTTP method, bukan verb di path |

---

### 1.5 Format Respons Standar

Semua respons membungkus data dalam envelope berikut:

**✅ Sukses**
```json
{
  "success": true,
  "message": "Wallpaper retrieved successfully.",
  "data": { ... }
}
```

**✅ Sukses dengan Pagination**
```json
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": [ ... ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 150,
    "last_page": 8
  }
}
```

**❌ Error**
```json
{
  "success": false,
  "message": "Validation failed.",
  "errors": {
    "title": ["The title field is required."],
    "file": ["File size must not exceed 10 MB."]
  }
}
```

---

### 1.6 Kode Error Global

| HTTP Status | Kode | Deskripsi |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Input tidak valid / field wajib kosong |
| `401` | `UNAUTHORIZED` | Token tidak ada, kedaluwarsa, atau dicabut |
| `403` | `FORBIDDEN` | Token valid tetapi role tidak memiliki akses |
| `404` | `NOT_FOUND` | Resource tidak ditemukan |
| `409` | `CONFLICT` | Duplikasi data (misal: email sudah terdaftar) |
| `429` | `TOO_MANY_REQUESTS` | Terlalu banyak percobaan login gagal; akun atau IP diblokir sementara |
| `422` | `UNPROCESSABLE` | Data dapat diparsing tapi secara bisnis tidak valid |

| `500` | `SERVER_ERROR` | Kesalahan internal server |

---

## 2. Auth

### 2.1 `POST /registrations`

Mendaftarkan contributor baru dengan email dan password. Sistem akan mengirim email verifikasi setelah registrasi berhasil.

**🔓 Publik — tidak memerlukan token**

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `display_name` | `string` | ✅ | Nama tampilan pengguna; maks 100 karakter |
| `email` | `string` | ✅ | Format email valid; harus belum terdaftar |
| `password` | `string` | ✅ | Minimal 8 karakter |
| `password_confirmation` | `string` | ✅ | Harus sama dengan `password` |

```json
{
  "display_name": "Creator One",
  "email": "creator@example.com",
  "password": "Secure@1234",
  "password_confirmation": "Secure@1234"
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `201 Created` | Akun berhasil dibuat; email verifikasi dikirim |
| `400` | Validasi gagal (password tidak cocok, format email salah) |
| `409` | Email sudah terdaftar |

```json
// 201 Created
{
  "success": true,
  "message": "Account created. Please check your email to verify your account.",
  "data": {
    "id": 12,
    "display_name": "Creator One",
    "email": "creator@example.com",
    "role": "contributor",
    "is_verified": false,
    "created_at": "2026-04-23T10:00:00Z"
  }
}
```

---

### 2.2 `POST /email-verifications`

Memverifikasi token yang dikirim ke email pengguna saat registrasi.

**🔓 Publik — tidak memerlukan token**

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `token` | `string` | ✅ | Token dari link email verifikasi |

```json
{
  "token": "eyJ0eXAiOiJhbHQ..."
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Akun berhasil diverifikasi |
| `400` | Token tidak valid atau sudah digunakan |
| `410 Gone` | Token sudah kedaluwarsa |

```json
// 200 OK
{
  "success": true,
  "message": "Account verified successfully. You can now log in.",
  "data": null
}
```

---

### 2.3 `POST /sessions`

Login dan mendapatkan JWT Bearer Token. Sistem juga mencatat percobaan login untuk brute-force protection dan dapat memblokir sementara akun atau IP setelah 5 kali gagal berturut-turut.

**🔓 Publik — tidak memerlukan token**

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `email` | `string` | ✅ | Email terdaftar |
| `password` | `string` | ✅ | Password akun |

```json
{
  "email": "creator@example.com",
  "password": "Secure@1234"
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Login berhasil; token dikembalikan |
| `401` | Email atau password salah |
| `403` | Akun belum diverifikasi |
| `429` | Akun atau IP diblokir sementara karena terlalu banyak percobaan login gagal |

```json
// 200 OK
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_at": "2026-04-23T11:00:00Z",
    "user": {
      "id": 12,
      "display_name": "Creator One",
      "email": "creator@example.com",
      "role": "contributor"
    }
  }
}
```

---

### 2.4 `GET /sessions/current`

Memvalidasi token JWT aktif dan mengembalikan data user pada sesi saat ini. Endpoint ini berguna untuk auth guard frontend, session restore saat refresh halaman, dan pengecekan apakah token sudah kedaluwarsa atau direvoke.

**ðŸ”’ Memerlukan token (Contributor / Admin)**

**Request Body** â€” kosong

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Token valid; data sesi saat ini dikembalikan |
| `401` | Token tidak valid, sudah direvoke, atau sudah kedaluwarsa |

```json
// 200 OK
{
  "success": true,
  "message": "Current session retrieved successfully.",
  "data": {
    "user": {
      "id": 12,
      "display_name": "Creator One",
      "email": "creator@example.com",
      "role": "contributor"
    },
    "expires_at": "2026-04-23T11:00:00Z"
  }
}
```

---

### 2.5 `DELETE /sessions/current`

Mencabut (revoke) token JWT aktif di server. Implementasi yang direkomendasikan adalah menyimpan `jti` token aktif ke denylist sampai `exp` token tercapai. Setelah ini, token tidak bisa digunakan lagi meskipun belum kedaluwarsa.

**🔒 Memerlukan token (Contributor / Admin)**

**Request Body** — kosong

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Logout berhasil; sesi dicabut |
| `401` | Token tidak valid atau sudah kedaluwarsa |

```json
// 200 OK
{
  "success": true,
  "message": "Logged out successfully.",
  "data": null
}
```

---

### 2.6 `POST /password-resets`

Mengirim link reset password ke email yang terdaftar. Respons selalu sukses (200) terlepas dari apakah email ada di database, untuk mencegah user enumeration.

**🔓 Publik — tidak memerlukan token**

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `email` | `string` | ✅ | Email yang ingin direset passwordnya |

```json
{
  "email": "creator@example.com"
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Selalu sukses (generic message untuk keamanan) |

```json
// 200 OK
{
  "success": true,
  "message": "If that email is registered, a password reset link has been sent.",
  "data": null
}
```

---

### 2.7 `PUT /password-resets/{token}`

Mengatur password baru menggunakan token dari email reset. Token berlaku 24 jam dan hanya bisa dipakai sekali.

**🔓 Publik — tidak memerlukan token**

**Path Parameters**

| Parameter | Tipe | Keterangan |
|---|---|---|
| `token` | `string` | Token dari link email reset password |

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `password` | `string` | ✅ | Password baru; minimal 8 karakter |
| `password_confirmation` | `string` | ✅ | Konfirmasi password baru |

```json
{
  "password": "NewSecure@5678",
  "password_confirmation": "NewSecure@5678"
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Password berhasil direset |
| `400` | Validasi gagal |
| `410 Gone` | Token kedaluwarsa atau sudah digunakan |

```json
// 200 OK
{
  "success": true,
  "message": "Password reset successfully. You can now log in with your new password.",
  "data": null
}
```

---

## 3. Wallpapers — Publik

### 3.1 `GET /wallpapers`

Mengembalikan daftar wallpaper internal Scapes yang sudah `approved` dan sudah dipublikasikan (`published_at` tidak `null`). Mendukung pencarian keyword, filter kategori/tag/perangkat, dan pagination.

**🔓 Publik — tidak memerlukan token**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `q` | `string` | — | Keyword pencarian; mencocokkan `title`, `description`, dan `tags` |
| `category` | `string` | — | Slug kategori (misal: `minimalist`) |
| `tag` | `string` | — | Slug tag (misal: `dark`); bisa multi: `tag=dark&tag=neon` |
| `target_device` | `string` | â€” | Filter target perangkat: `desktop`, `mobile`, `tablet` |
| `page` | `integer` | `1` | Nomor halaman |
| `per_page` | `integer` | `20` | Jumlah item per halaman (maks: 100) |
| `sort_by` | `string` | `published_at` | Field pengurutan: `published_at`, `title` |
| `order` | `string` | `desc` | Arah urutan: `asc` / `desc` |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar wallpaper berhasil dikembalikan |
| `400` | Query parameter tidak valid |

```json
// 200 OK
{
  "success": true,
  "message": "Wallpapers retrieved successfully.",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Midnight Forest",
      "description": "A serene dark forest at midnight.",
      "file_path": "https://scapes.my.id/wallpapers/2/550e8400-e29b-41d4-a716-446655440000.jpeg",
      "thumbnail_path": "https://scapes.my.id/wallpapers/2/thumbnails/550e8400-e29b-41d4-a716-446655440000.webp",
      "width": 3840,
      "height": 2160,
      "target_device": "desktop",
      "category": {
        "id": 2,
        "name": "Nature",
        "slug": "nature"
      },
      "tags": [
        { "id": 1, "name": "dark", "slug": "dark" },
        { "id": 9, "name": "retro", "slug": "retro" }
      ],
      "contributor": {
        "id": 12,
        "display_name": "Creator One",
        "email": "creator@example.com"
      },
      "published_at": "2026-04-20T08:00:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 87,
    "last_page": 5
  }
}
```

---

### 3.2 `GET /wallpapers/{id}`

Mengembalikan detail lengkap satu wallpaper internal Scapes yang sudah `approved` dan sudah dipublikasikan.

**🔓 Publik — tidak memerlukan token**

**Path Parameters**

| Parameter | Tipe | Keterangan |
|---|---|---|
| `id` | `string (uuid)` | UUID v4 wallpaper |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Detail wallpaper berhasil dikembalikan |
| `404` | Wallpaper tidak ditemukan, belum approved, atau belum dipublikasikan |

```json
// 200 OK
{
  "success": true,
  "message": "Wallpaper retrieved successfully.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Midnight Forest",
    "description": "A serene dark forest at midnight.",
    "file_path": "https://scapes.my.id/wallpapers/2/550e8400-e29b-41d4-a716-446655440000.jpeg",
    "thumbnail_path": "https://scapes.my.id/wallpapers/2/thumbnails/550e8400-e29b-41d4-a716-446655440000.webp",
    "file_size_kb": 4096,
    "mime_type": "image/jpeg",
    "width": 3840,
    "height": 2160,
    "target_device": "desktop",
    "status": "approved",
    "category": {
      "id": 2,
      "name": "Nature",
      "slug": "nature"
    },
    "tags": [
      { "id": 1, "name": "dark", "slug": "dark" }
    ],
    "contributor": {
      "id": 12,
      "display_name": "Creator One",
      "email": "creator@example.com"
    },
    "published_at": "2026-04-20T08:00:00Z",
    "created_at": "2026-04-18T09:00:00Z"
  }
}
```

---

### 3.3 `POST /search-logs`

Mencatat event pencarian keyword dari frontend user untuk kebutuhan search analytics, trending categories, dan dataset awal recommender. Endpoint ini bersifat best-effort: kegagalan logging tidak boleh memblokir proses pencarian wallpaper utama di frontend.

**Publik - tidak memerlukan token**

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `keyword` | `string` | ya | Keyword pencarian user; 2-255 karakter setelah trim |
| `source_slug` | `string` | tidak | Source aktif saat search, misal `scapes`, `unsplash`, `pexels`, `pixabay` |
| `client_hash` | `string` | tidak | Hash anonim untuk deduplication; tidak boleh berisi identifier mentah |
| `locale` | `string` | tidak | Locale kasar client, misal `id-ID` |
| `result_count` | `integer` | tidak | Jumlah hasil pencarian jika tersedia |

```json
{
  "keyword": "beach sunset",
  "source_slug": "scapes",
  "client_hash": "0b4f2f8d0f2d4a1c9d5b9c8a0f6e2d3c7b8a9f0e1d2c3b4a5968776655443322",
  "locale": "id-ID",
  "result_count": 24
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `202 Accepted` | Search log diterima untuk diproses |
| `400` | Payload tidak valid |
| `429` | Terlalu banyak event dari client yang sama |

```json
// 202 Accepted
{
  "success": true,
  "message": "Search event accepted.",
  "data": {
    "accepted": true
  }
}
```

**Catatan Implementasi**

- Backend harus menormalisasi keyword sebelum menyimpan ke `search_logs`.
- Endpoint tidak boleh menyimpan raw API key, email, full local path, atau direct personal identifier.
- Backend boleh melakukan deduplication untuk keyword dan `client_hash` yang sama dalam window pendek, misalnya 5 menit.

---

## 4. Wallpapers — Contributor

### 4.1 `GET /me/wallpapers`

Mengembalikan semua wallpaper milik contributor yang sedang login, termasuk semua status yang tersedia (`pending`, `approved`, `rejected`).

**🔒 Memerlukan token — Role: `contributor`**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `status` | `string` | — | Filter status: `pending`, `approved`, `rejected` |
| `page` | `integer` | `1` | Nomor halaman |
| `per_page` | `integer` | `20` | Jumlah item per halaman |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar wallpaper milik contributor |
| `401` | Token tidak valid |

```json
// 200 OK
{
  "success": true,
  "message": "Your wallpapers retrieved successfully.",
  "data": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "title": "Neon City Lights",
      "description": "Cyberpunk skyline with glowing neon reflections.",
      "file_path": "https://scapes.my.id/wallpapers/pending/8/650e8400-e29b-41d4-a716-446655440001.jpeg",
      "thumbnail_path": "https://scapes.my.id/wallpapers/pending/8/thumbnails/650e8400-e29b-41d4-a716-446655440001.webp",
      "status": "pending",
      "target_device": "desktop",
      "category": { "id": 8, "name": "Technology", "slug": "technology" },
      "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
      "is_review_overdue": true,
      "moderation": null,
      "created_at": "2026-04-19T12:00:00Z",
      "updated_at": "2026-04-19T12:00:00Z"
    },
    {
      "id": "750e8400-e29b-41d4-a716-446655440002",
      "title": "Pastel Dreams",
      "description": "Soft pastel gradient landscape for calm tablet setups.",
      "file_path": "https://scapes.my.id/wallpapers/1/750e8400-e29b-41d4-a716-446655440002.jpeg",
      "thumbnail_path": "https://scapes.my.id/wallpapers/1/thumbnails/750e8400-e29b-41d4-a716-446655440002.webp",
      "status": "rejected",
      "target_device": "tablet",
      "category": { "id": 1, "name": "Minimalist", "slug": "minimalist" },
      "tags": [{ "id": 4, "name": "pastel", "slug": "pastel" }],
      "moderation": {
        "decision": "rejected",
        "reason": "Image resolution does not meet the minimum requirement of 1920x1080.",
        "reviewed_at": "2026-04-21T14:00:00Z"
      },
      "created_at": "2026-04-17T10:00:00Z",
      "updated_at": "2026-04-21T14:00:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 2,
    "last_page": 1
  }
}
```

> **Catatan:** Field `is_review_overdue` bersifat kondisional dan hanya dikembalikan ketika wallpaper berstatus `pending` lebih dari 3 hari sejak `created_at` (CO-02 AC-3). Jika kondisi itu tidak terpenuhi, field dapat dihilangkan dari respons.

---

### 4.2 `POST /me/wallpapers`

Mengunggah wallpaper baru untuk direview oleh admin. Wallpaper langsung masuk status `pending`. Nilai `target_device` ditentukan otomatis oleh server berdasarkan business logic saat file diproses.

**🔒 Memerlukan token — Role: `contributor`**

**Request Body** `multipart/form-data`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `file` | `file` | ✅ | File gambar; format JPG/PNG/WebP; maks 10 MB; min resolusi 1920×1080 |
| `title` | `string` | ✅ | Judul wallpaper; maks 255 karakter |
| `description` | `string` | — | Deskripsi opsional |
| `category_id` | `integer` | ✅ | ID kategori dari `GET /categories` |
| `tags` | `array[integer]` | — | Array ID tag existing dari `GET /tags` |
| `tag_text` | `string` | — | Tag input contributor dengan awalan `#`, dipisahkan spasi, misal `#dark #city #neon`. Tag baru disimpan sebagai proposal sampai wallpaper di-approve |

**Aturan Tag Contributor**

- Setiap tag baru harus diawali `#`.
- Banyak tag dapat dipisahkan dengan spasi, misalnya `#dark #city #neon`.
- Saat contributor mengetik tag, frontend sebaiknya memanggil `GET /tags` untuk menampilkan suggestion tag resmi yang mendekati input.
- Backend menyimpan tag baru ke `wallpaper_tag_proposals`, bukan langsung ke tabel `tags`.
- Saat admin approve wallpaper, backend melakukan upsert ke `tags`; jika slug tag sudah ada, pakai tag existing dan jangan membuat duplikasi.
- Jika wallpaper di-reject, proposal tag tidak dimasukkan ke tabel `tags`.

**Responses**

| Status | Keterangan |
|---|---|
| `201 Created` | Wallpaper berhasil diunggah dan masuk antrian review |
| `400` | Validasi gagal (format/ukuran/resolusi/field wajib) |
| `401` | Token tidak valid |

```json
// 201 Created
{
  "success": true,
  "message": "Wallpaper submitted for review.",
  "data": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "title": "Neon City Lights",
    "description": "Soft pastel gradient landscape for calm tablet setups.",
    "file_path": "https://scapes.my.id/wallpapers/pending/8/650e8400-e29b-41d4-a716-446655440001.jpeg",
    "thumbnail_path": "https://scapes.my.id/wallpapers/pending/8/thumbnails/650e8400-e29b-41d4-a716-446655440001.webp",
    "status": "pending",
    "file_size_kb": 5120,
    "width": 3840,
    "height": 2160,
    "target_device": "desktop",
    "category": { "id": 8, "name": "Technology", "slug": "technology" },
    "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
    "created_at": "2026-04-23T10:30:00Z"
  }
}

// 400 Validation Error
{
  "success": false,
  "message": "Validation failed.",
  "errors": {
    "file": ["Invalid format or file too large. Allowed: JPG, PNG, WebP. Max: 10 MB."],
    "title": ["The title field is required."]
  }
}

```

---

### 4.3 `PATCH /me/wallpapers/{id}`

Memperbarui metadata wallpaper milik contributor (title, description, category, tags). Hanya bisa dilakukan selama wallpaper belum `approved`.

**🔒 Memerlukan token — Role: `contributor`**

**Path Parameters**

| Parameter | Tipe | Keterangan |
|---|---|---|
| `id` | `string (uuid)` | UUID v4 wallpaper |

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `title` | `string` | — | Judul baru; maks 255 karakter |
| `description` | `string` | — | Deskripsi baru |
| `category_id` | `integer` | — | ID kategori baru |
| `tags` | `array[integer]` | — | Array ID tag existing baru (menggantikan semua tag existing lama) |
| `tag_text` | `string` | — | Tag input contributor dengan awalan `#`, dipisahkan spasi. Hanya dapat diubah selama wallpaper belum `approved` |

```json
{
  "title": "Neon City Lights — Revised",
  "description": "Updated description with more detail.",
  "category_id": 3,
  "tags": [3, 10],
  "tag_text": "#cyberpunk #nightcity"
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Data wallpaper berhasil diperbarui |
| `400` | Validasi gagal |
| `401` | Token tidak valid |
| `403` | Wallpaper bukan milik contributor ini, atau sudah `approved` |
| `404` | Wallpaper tidak ditemukan |

```json
// 200 OK
{
  "success": true,
  "message": "Wallpaper updated successfully.",
  "data": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "title": "Neon City Lights — Revised",
    "description": "Updated description with more detail.",
    "file_path": "https://scapes.my.id/wallpapers/pending/3/850e8400-e29b-41d4-a716-446655440003.jpeg",
    "thumbnail_path": "https://scapes.my.id/wallpapers/pending/3/thumbnails/850e8400-e29b-41d4-a716-446655440003.webp",
    "status": "pending",
    "target_device": "mobile",
    "category": { "id": 3, "name": "Abstract", "slug": "abstract" },
    "tags": [
      { "id": 3, "name": "neon", "slug": "neon" },
      { "id": 10, "name": "futuristic", "slug": "futuristic" }
    ],
    "updated_at": "2026-04-23T11:00:00Z"
  }
}
```

---

### 4.4 `DELETE /me/wallpapers/{id}`

Menghapus wallpaper milik contributor secara permanen dari server dan database.

**🔒 Memerlukan token — Role: `contributor`**

**Path Parameters**

| Parameter | Tipe | Keterangan |
|---|---|---|
| `id` | `string (uuid)` | UUID v4 wallpaper |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Wallpaper berhasil dihapus |
| `401` | Token tidak valid |
| `403` | Wallpaper bukan milik contributor ini |
| `404` | Wallpaper tidak ditemukan atau sudah dihapus sebelumnya |

```json
// 200 OK
{
  "success": true,
  "message": "Wallpaper deleted successfully.",
  "data": null
}
```

---

## 5. Moderation — Admin

### 5.1 `GET /moderation/wallpapers`

Mengembalikan daftar wallpaper untuk keperluan moderasi, dengan filter status dan informasi contributor.

**🔒 Memerlukan token — Role: `admin`**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `status` | `string` | `pending` | Filter status: `pending`, `approved`, `rejected` |
| `contributor_id` | `integer` | — | Filter berdasarkan contributor tertentu |
| `page` | `integer` | `1` | Nomor halaman |
| `per_page` | `integer` | `20` | Jumlah item per halaman |
| `sort_by` | `string` | `created_at` | Field pengurutan: `created_at`, `title` |
| `order` | `string` | `asc` | Arah urutan: `asc` / `desc` |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar wallpaper untuk moderasi |
| `401` | Token tidak valid |
| `403` | Bukan admin |

```json
// 200 OK
{
  "success": true,
  "message": "Wallpapers for moderation retrieved successfully.",
  "data": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "title": "Neon City Lights",
      "description": "Cyberpunk skyline with glowing neon reflections.",
      "file_path": "https://scapes.my.id/wallpapers/pending/8/650e8400-e29b-41d4-a716-446655440001.jpeg",
      "thumbnail_path": "https://scapes.my.id/wallpapers/pending/8/thumbnails/650e8400-e29b-41d4-a716-446655440001.webp",
      "width": 3840,
      "height": 2160,
      "target_device": "desktop",
      "mime_type": "image/jpeg",
      "file_size_kb": 5120,
      "status": "pending",
      "category": { "id": 8, "name": "Technology", "slug": "technology" },
      "tags": [{ "id": 3, "name": "neon", "slug": "neon" }],
      "contributor": {
        "id": 12,
        "display_name": "Creator One",
        "email": "creator@example.com"
      },
      "is_review_overdue": true,
      "created_at": "2026-04-19T12:00:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 20,
    "total": 14,
    "last_page": 1
  }
}
```

---

### 5.2 `PATCH /moderation/wallpapers/{id}`

Menyetujui atau menolak wallpaper. Jika `decision` adalah `rejected`, field `reason` wajib diisi. Jika `decision` adalah `approved`, backend wajib memproses `wallpaper_tag_proposals`: tag baru di-upsert ke tabel `tags`, tag existing dipakai ulang berdasarkan slug, dan relasi final dibuat di `wallpaper_tags`. Jika wallpaper ditolak, tag proposal tidak boleh masuk ke tabel `tags`.

**🔒 Memerlukan token — Role: `admin`**

**Path Parameters**

| Parameter | Tipe | Keterangan |
|---|---|---|
| `id` | `string (uuid)` | UUID v4 wallpaper |

**Request Body** `application/json`

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `decision` | `string` | ✅ | Nilai: `approved` atau `rejected` |
| `reason` | `string` | ✅ jika rejected | Alasan penolakan; wajib jika `decision = rejected` |

```json
// Approve
{
  "decision": "approved"
}

// Reject
{
  "decision": "rejected",
  "reason": "Image contains inappropriate content and does not meet community guidelines."
}
```

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Keputusan moderasi berhasil disimpan |
| `400` | `decision` tidak valid; atau `reason` kosong saat rejected |
| `401` | Token tidak valid |
| `403` | Bukan admin |
| `404` | Wallpaper tidak ditemukan |
| `422` | Wallpaper tidak dalam status `pending` |

```json
// 200 OK — Approved
{
  "success": true,
  "message": "Wallpaper approved and is now publicly visible.",
  "data": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "file_path": "https://scapes.my.id/wallpapers/8/650e8400-e29b-41d4-a716-446655440001.jpeg",
    "thumbnail_path": "https://scapes.my.id/wallpapers/8/thumbnails/650e8400-e29b-41d4-a716-446655440001.webp",
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

// 200 OK — Rejected
{
  "success": true,
  "message": "Wallpaper rejected. The contributor has been notified.",
  "data": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "file_path": "https://scapes.my.id/wallpapers/8/650e8400-e29b-41d4-a716-446655440001.jpeg",
    "thumbnail_path": "https://scapes.my.id/wallpapers/8/thumbnails/650e8400-e29b-41d4-a716-446655440001.webp",
    "status": "rejected",
    "moderation": {
      "decision": "rejected",
      "reason": "Image contains inappropriate content and does not meet community guidelines.",
      "reviewed_at": "2026-04-23T11:35:00Z",
      "admin_id": 1
    }
  }
}

// 400 — Reason Missing
{
  "success": false,
  "message": "Validation failed.",
  "errors": {
    "reason": ["Rejection reason is required when decision is 'rejected'."]
  }
}
```

---

## 6. API Sources

### 6.1 `GET /sources`

Mengembalikan daftar sumber wallpaper aktif dari tabel `api_sources`. Endpoint ini hanya menyediakan **metadata source** seperti `name`, `slug`, dan `base_url` agar desktop client dapat menentukan provider yang dipakai. Request pencarian ke provider eksternal serta penyimpanan API key pribadi tetap dikelola langsung oleh desktop client (misalnya via Java Preferences API), sehingga API key tidak dikirim atau divalidasi oleh Scapes API.

**🔓 Publik — tidak memerlukan token**

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar sumber wallpaper aktif |

```json
// 200 OK
{
  "success": true,
  "message": "Sources retrieved successfully.",
  "data": [
    { "id": 1, "name": "Scapes",   "slug": "scapes",   "base_url": "https://scapes.my.id",         "is_default": true  },
    { "id": 2, "name": "Unsplash", "slug": "unsplash", "base_url": "https://api.unsplash.com",    "is_default": false },
    { "id": 3, "name": "Pexels",   "slug": "pexels",   "base_url": "https://api.pexels.com/v1",   "is_default": false },
    { "id": 4, "name": "Pixabay",  "slug": "pixabay",  "base_url": "https://pixabay.com/api",     "is_default": false }
  ]
}
```

---

## 7. Categories & Tags

### 7.1 `GET /categories`

Mengembalikan semua kategori wallpaper yang tersedia.

**🔓 Publik — tidak memerlukan token**

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar kategori |

```json
// 200 OK
{
  "success": true,
  "message": "Categories retrieved successfully.",
  "data": [
    { "id": 1, "name": "Minimalist",   "slug": "minimalist"   },
    { "id": 2, "name": "Nature",       "slug": "nature"       },
    { "id": 3, "name": "Abstract",     "slug": "abstract"     },
    { "id": 4, "name": "Architecture", "slug": "architecture" },
    { "id": 5, "name": "Dark",         "slug": "dark"         },
    { "id": 6, "name": "Space",        "slug": "space"        },
    { "id": 7, "name": "Anime",        "slug": "anime"        },
    { "id": 8, "name": "Technology",   "slug": "technology"   }
  ]
}
```

---

### 7.2 `GET /tags`

Mengembalikan semua tag resmi yang tersedia di database, opsional difilter dengan keyword. Endpoint ini tidak mengembalikan tag proposal yang masih pending moderation. Endpoint ini dipakai baik untuk autocomplete search user maupun suggestion tag pada form contributor.

**🔓 Publik — tidak memerlukan token**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `q` | `string` | — | Keyword pencarian tag; boleh dikirim dengan atau tanpa awalan `#`, misalnya `#color` atau `color` |
| `match` | `string` | `prefix` | Mode pencocokan: `prefix` atau `contains` |
| `limit` | `integer` | `10` | Jumlah tag maksimal, rentang 1-50 |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar tag |

```json
// 200 OK
{
  "success": true,
  "message": "Tags retrieved successfully.",
  "data": [
    { "id": 1,  "name": "dark",       "slug": "dark"       },
    { "id": 2,  "name": "light",      "slug": "light"      },
    { "id": 3,  "name": "neon",       "slug": "neon"       },
    { "id": 4,  "name": "pastel",     "slug": "pastel"     },
    { "id": 5,  "name": "4k",         "slug": "4k"         },
    { "id": 6,  "name": "monochrome", "slug": "monochrome" },
    { "id": 7,  "name": "colorful",   "slug": "colorful"   },
    { "id": 11, "name": "colorless",  "slug": "colorless"  },
    { "id": 8,  "name": "gradient",   "slug": "gradient"   },
    { "id": 9,  "name": "retro",      "slug": "retro"      },
    { "id": 10, "name": "futuristic", "slug": "futuristic" }
  ]
}
```

---

### 7.3 `GET /categories/trending`

Mengembalikan kategori trending berdasarkan agregasi search logs harian. Kategori trending utama adalah kategori dinamis dari keyword user, misalnya `beach`, `city`, atau `forest`, dan tidak harus sudah ada di daftar kategori bawaan. Endpoint ini juga dapat menyertakan kategori bawaan sebagai fallback agar frontend tidak bergantung pada kategori mock/static seperti "Quiet Forest" atau "Amber Evening".

**Publik - tidak memerlukan token**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `date` | `string (date)` | hari ini | Tanggal agregasi format `YYYY-MM-DD` |
| `limit` | `integer` | `10` | Jumlah user-keyword category maksimal, rentang 1-50 |
| `include_system` | `boolean` | `true` | Jika `true`, response juga menyertakan kategori bawaan sistem sebagai fallback setelah user-keyword categories |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar kategori trending |
| `400` | Query parameter tidak valid |

```json
// 200 OK
{
  "success": true,
  "message": "Trending categories retrieved successfully.",
  "data": [
    {
      "origin": "user_keyword",
      "category": null,
      "label": "Beach",
      "slug": "beach",
      "trend_date": "2026-06-16",
      "search_count": 128,
      "score": 92.5,
      "top_keywords": ["beach", "sunset beach", "ocean"]
    },
    {
      "origin": "user_keyword",
      "category": null,
      "label": "City",
      "slug": "city",
      "trend_date": "2026-06-16",
      "search_count": 96,
      "score": 81.2,
      "top_keywords": ["city", "night city", "urban"]
    },
    {
      "origin": "system",
      "category": { "id": 1, "name": "Minimalist", "slug": "minimalist" },
      "label": "Minimalist",
      "slug": "minimalist",
      "trend_date": "2026-06-16",
      "search_count": 0,
      "score": 0,
      "top_keywords": []
    }
  ]
}
```

---

### 7.4 `GET /recommendations/search`

Mengembalikan rekomendasi keyword, kategori, atau tag untuk Search tab. Prioritas kategori bawaan dan tag resmi dari database hanya berlaku ketika `source_slug = scapes`. Jika source aktif adalah external (`unsplash`, `pexels`, `pixabay`, dll), endpoint harus fallback ke rekomendasi normal tanpa memakai kategori/tag internal sebagai prioritas. Jika `source_slug = scapes` dan `q` diawali `#`, endpoint masuk tag suggestion mode dan mencari tag resmi dari tabel `tags`. Jika tidak ada tag yang cocok, endpoint fallback ke rekomendasi normal. Pada MVP, rekomendasi normal dihasilkan dari agregasi statistik, rule-based ranking user-keyword categories, dan match ringan ke kategori bawaan. ML dapat ditambahkan kemudian jika volume data sudah cukup.

**Publik - tidak memerlukan token**

**Query Parameters**

| Parameter | Tipe | Default | Keterangan |
|---|---|---|---|
| `q` | `string` | kosong | Prefix atau keyword yang sedang diketik user |
| `source_slug` | `string` | `scapes` | Source aktif di frontend. Kategori/tag internal hanya diprioritaskan jika nilainya `scapes` |
| `limit` | `integer` | `10` | Jumlah user-keyword recommendation maksimal, rentang 1-50 |
| `include_system` | `boolean` | `true` | Jika `true`, kategori bawaan sistem ikut dikembalikan |
| `system_match_first` | `boolean` | `true` | Jika `true`, kategori bawaan yang exact/prefix/fuzzy match dengan `q` ditempatkan di atas rekomendasi lain |
| `tag_match_first` | `boolean` | `true` | Jika `true` dan `q` diawali `#`, tag resmi yang match ditempatkan di atas rekomendasi lain |

**Responses**

| Status | Keterangan |
|---|---|
| `200 OK` | Daftar rekomendasi search |
| `400` | Query parameter tidak valid |

```json
// 200 OK
{
  "success": true,
  "message": "Search recommendations retrieved successfully.",
  "data": [
    {
      "type": "tag",
      "label": "#colorful",
      "value": "colorful",
      "score": 100,
      "match_reason": "prefix_match"
    },
    {
      "type": "system_category",
      "label": "Minimalist",
      "value": "minimalist",
      "score": 100,
      "match_reason": "prefix_match"
    },
    {
      "type": "user_keyword_category",
      "label": "Beach",
      "value": "beach",
      "score": 92.5,
      "match_reason": "trending"
    }
  ],
  "meta": {
    "strategy": "system_match_then_daily_trending",
    "generated_from": "2026-06-16",
    "source_slug": "scapes",
    "internal_metadata_enabled": true,
    "system_match_first": true,
    "tag_match_first": true,
    "user_keyword_limit": 10
  }
}
```

---

## 8. Schema Komponen

Komponen schema yang dapat digunakan ulang di seluruh endpoint.

### `UserPublic`
```yaml
type: object
properties:
  id:    { type: integer, example: 12 }
  display_name: { type: string, maxLength: 100, example: "Creator One" }
  email: { type: string, format: email, example: "creator@example.com" }
```

### `Category`
```yaml
type: object
properties:
  id:   { type: integer, example: 2 }
  name: { type: string, example: "Nature" }
  slug: { type: string, example: "nature" }
```

### `Tag`
```yaml
type: object
properties:
  id:   { type: integer, example: 1 }
  name: { type: string, example: "dark" }
  slug: { type: string, example: "dark" }
```

### `TagProposal`
```yaml
type: object
properties:
  tag_text: { type: string, example: "cyberpunk" }
  tag_slug: { type: string, example: "cyberpunk" }
  existing_tag_id: { type: integer, nullable: true, example: null }
  status:
    type: string
    enum: [pending, approved, discarded]
    example: "pending"
```

### `TrendingCategory`
```yaml
type: object
properties:
  category:
    $ref: "#/components/schemas/Category"
    nullable: true
  origin:
    type: string
    enum: [user_keyword, system]
    example: "user_keyword"
  label: { type: string, example: "Beach" }
  slug: { type: string, example: "beach" }
  trend_date: { type: string, format: date, example: "2026-06-16" }
  search_count: { type: integer, example: 128 }
  score: { type: number, format: float, example: 92.5 }
  top_keywords:
    type: array
    items: { type: string }
    example: ["beach", "sunset beach", "ocean"]
```

### `SearchRecommendation`
```yaml
type: object
properties:
  type:
    type: string
    enum: [system_category, user_keyword_category, keyword, tag]
    example: "system_category"
  label: { type: string, example: "Beach" }
  value: { type: string, example: "beach" }
  score: { type: number, format: float, example: 92.5 }
  match_reason:
    type: string
    enum: [exact_match, prefix_match, fuzzy_match, trending, fallback]
    example: "prefix_match"
```

### `ApiSource`
```yaml
type: object
properties:
  id:         { type: integer }
  name:       { type: string }
  slug:       { type: string }
  base_url:   { type: string, format: uri }
  is_default: { type: boolean }  
```

### `TargetDevice`
```yaml
type: string
enum: [desktop, mobile, tablet]
```

### `WallpaperPublic`
```yaml
type: object
properties:
  id:          { type: string, format: uuid }
  title:       { type: string }
  description: { type: string, nullable: true }
  file_path:   { type: string, format: uri }
  thumbnail_path: { type: string, format: uri }
  width:       { type: integer, minimum: 1920 }
  height:      { type: integer, minimum: 1080 }
  target_device: { $ref: '#/components/schemas/TargetDevice' }
  category:    { $ref: '#/components/schemas/Category' }
  tags:        { type: array, items: { $ref: '#/components/schemas/Tag' } }
  proposed_tags:
    type: array
    items: { $ref: '#/components/schemas/TagProposal' }
  contributor: { $ref: '#/components/schemas/UserPublic' }
  published_at: { type: string, format: date-time }
```

### `WallpaperContributorItem`
```yaml
type: object
properties:
  id:          { type: string, format: uuid }
  title:       { type: string }
  description: { type: string, nullable: true }
  thumbnail_path: { type: string, format: uri }
  status:      { type: string, enum: [pending, approved, rejected] }
  target_device: { $ref: '#/components/schemas/TargetDevice' }
  category:    { $ref: '#/components/schemas/Category' }
  tags:        { type: array, items: { $ref: '#/components/schemas/Tag' } }
  is_review_overdue:
    type: boolean
    description: Returned only when the wallpaper has been pending for more than 3 days.
  moderation:
    allOf:
      - $ref: '#/components/schemas/ModerationResult'
    nullable: true
  created_at:  { type: string, format: date-time }
  updated_at:  { type: string, format: date-time }
```

### `WallpaperAdminQueueItem`
```yaml
type: object
properties:
  id:           { type: string, format: uuid }
  title:        { type: string }
  description:  { type: string, nullable: true }
  file_path:    { type: string, format: uri }
  thumbnail_path: { type: string, format: uri }
  width:        { type: integer, minimum: 1920 }
  height:       { type: integer, minimum: 1080 }
  target_device: { $ref: '#/components/schemas/TargetDevice' }
  mime_type:    { type: string, enum: [image/jpeg, image/png, image/webp] }
  file_size_kb: { type: integer }
  status:       { type: string, enum: [pending, approved, rejected] }
  category:     { $ref: '#/components/schemas/Category' }
  tags:         { type: array, items: { $ref: '#/components/schemas/Tag' } }
  proposed_tags:
    type: array
    items: { $ref: '#/components/schemas/TagProposal' }
  contributor:  { $ref: '#/components/schemas/UserPublic' }
  is_review_overdue:
    type: boolean
    description: Returned only when the wallpaper has been pending for more than 3 days.
  moderation:
    allOf:
      - $ref: '#/components/schemas/ModerationResult'
    nullable: true
  created_at:   { type: string, format: date-time }
  updated_at:   { type: string, format: date-time, nullable: true }
  published_at: { type: string, format: date-time, nullable: true }
```

### `ModerationResult`
```yaml
type: object
properties:
  decision:    { type: string, enum: [approved, rejected] }
  reason:      { type: string, nullable: true }
  reviewed_at: { type: string, format: date-time }
  admin_id:    { type: integer }
```

### `PaginationMeta`
```yaml
type: object
properties:
  current_page: { type: integer }
  per_page:     { type: integer }
  total:        { type: integer }
  last_page:    { type: integer }
```

### `ErrorResponse`
```yaml
type: object
properties:
  success: { type: boolean, example: false }
  message: { type: string }
  errors:  { type: object, nullable: true, additionalProperties: { type: array, items: { type: string } } }
```

---

## 9. OpenAPI YAML Lengkap

```yaml
openapi: "3.1.0"

info:
  title: Scapes API
  version: "1.0.3"
  description: |
    RESTful API for the Scapes wallpaper desktop application.
    Serves both the JavaFX desktop client and the web portal.
    This v1 contract covers Scapes internal backend capabilities only.
    Low-priority features such as scheduled publication and contributor upload limits are intentionally deferred.
    External provider requests and personal API key handling remain client-side; /sources exposes metadata only.
  contact:
    name: Scapes Team
    email: dev@scapes.app
  license:
    name: MIT

servers:
  - url: https://scapes.my.id
    description: Production
  - url: https://staging-api.scapes.app/v1
    description: Staging
  - url: http://localhost:8000/v1
    description: Development

security:
  - BearerAuth: []

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    UserPublic:
      type: object
      properties:
        id:    { type: integer, example: 12 }
        display_name: { type: string, maxLength: 100, example: "Creator One" }
        email: { type: string, format: email }

    Category:
      type: object
      properties:
        id:   { type: integer }
        name: { type: string }
        slug: { type: string }

    Tag:
      type: object
      properties:
        id:   { type: integer }
        name: { type: string }
        slug: { type: string }

    TagProposal:
      type: object
      properties:
        tag_text: { type: string, example: "cyberpunk" }
        tag_slug: { type: string, example: "cyberpunk" }
        existing_tag_id: { type: integer, nullable: true }
        status: { type: string, enum: [pending, approved, discarded] }

    TargetDevice:
      type: string
      enum: [desktop, mobile, tablet]

    WallpaperPublic:
      type: object
      properties:
        id:           { type: string, format: uuid }
        title:        { type: string }
        description:  { type: string, nullable: true }
        file_path:    { type: string, format: uri }
        thumbnail_path: { type: string, format: uri }
        file_size_kb: { type: integer }
        mime_type:    { type: string, enum: [image/jpeg, image/png, image/webp] }
        width:        { type: integer, minimum: 1920 }
        height:       { type: integer, minimum: 1080 }
        target_device: { $ref: '#/components/schemas/TargetDevice' }
        status:       { type: string, enum: [pending, approved, rejected] }
        category:     { $ref: '#/components/schemas/Category' }
        tags:
          type: array
          items: { $ref: '#/components/schemas/Tag' }
        contributor:  { $ref: '#/components/schemas/UserPublic' }
        published_at: { type: string, format: date-time, nullable: true }
        created_at:   { type: string, format: date-time }
        updated_at:   { type: string, format: date-time }

    WallpaperContributorItem:
      type: object
      properties:
        id:           { type: string, format: uuid }
        title:        { type: string }
        description:  { type: string, nullable: true }
        thumbnail_path: { type: string, format: uri }
        status:       { type: string, enum: [pending, approved, rejected] }
        target_device: { $ref: '#/components/schemas/TargetDevice' }
        category:     { $ref: '#/components/schemas/Category' }
        tags:
          type: array
          items: { $ref: '#/components/schemas/Tag' }
        proposed_tags:
          type: array
          items: { $ref: '#/components/schemas/TagProposal' }
        is_review_overdue:
          type: boolean
          description: Returned only when the wallpaper has been pending for more than 3 days.
        moderation:
          allOf:
            - { $ref: '#/components/schemas/ModerationResult' }
          nullable: true
        created_at:   { type: string, format: date-time }
        updated_at:   { type: string, format: date-time }

    WallpaperAdminQueueItem:
      type: object
      properties:
        id:           { type: string, format: uuid }
        title:        { type: string }
        description:  { type: string, nullable: true }
        file_path:    { type: string, format: uri }
        thumbnail_path: { type: string, format: uri }
        width:        { type: integer, minimum: 1920 }
        height:       { type: integer, minimum: 1080 }
        target_device: { $ref: '#/components/schemas/TargetDevice' }
        mime_type:    { type: string, enum: [image/jpeg, image/png, image/webp] }
        file_size_kb: { type: integer }
        status:       { type: string, enum: [pending, approved, rejected] }
        category:     { $ref: '#/components/schemas/Category' }
        tags:
          type: array
          items: { $ref: '#/components/schemas/Tag' }
        proposed_tags:
          type: array
          items: { $ref: '#/components/schemas/TagProposal' }
        contributor:  { $ref: '#/components/schemas/UserPublic' }
        is_review_overdue:
          type: boolean
          description: Returned only when the wallpaper has been pending for more than 3 days.
        moderation:
          allOf:
            - { $ref: '#/components/schemas/ModerationResult' }
          nullable: true
        created_at:   { type: string, format: date-time }
        updated_at:   { type: string, format: date-time, nullable: true }
        published_at: { type: string, format: date-time, nullable: true }

    ModerationResult:
      type: object
      properties:
        decision:    { type: string, enum: [approved, rejected] }
        reason:      { type: string, nullable: true }
        reviewed_at: { type: string, format: date-time }
        admin_id:    { type: integer }

    PaginationMeta:
      type: object
      properties:
        current_page: { type: integer }
        per_page:     { type: integer }
        total:        { type: integer }
        last_page:    { type: integer }

    SuccessResponse:
      type: object
      properties:
        success: { type: boolean, example: true }
        message: { type: string }
        data:    { nullable: true }

    ErrorResponse:
      type: object
      properties:
        success: { type: boolean, example: false }
        message: { type: string }
        errors:
          type: object
          nullable: true
          additionalProperties:
            type: array
            items: { type: string }

  responses:
    Unauthorized:
      description: Token not provided, expired, or revoked.
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
          example:
            success: false
            message: "Unauthorized. Please log in."
            errors: null

    Forbidden:
      description: Token valid but insufficient role permissions.
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
          example:
            success: false
            message: "Forbidden. You do not have access to this resource."
            errors: null

    NotFound:
      description: Resource not found.
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
          example:
            success: false
            message: "Resource not found."
            errors: null

    ValidationError:
      description: Request validation failed.
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }

    TooManyRequests:
      description: Too many failed login attempts; account or IP temporarily blocked.
      content:
        application/json:
          schema: { $ref: '#/components/schemas/ErrorResponse' }
          example:
            success: false
            message: "Too many failed login attempts. Please try again later."
            errors: null

tags:
  - name: Auth
    description: Registrasi, login, logout, verifikasi email, dan reset password.
  - name: Wallpapers
    description: Pencarian dan pengambilan wallpaper publik.
  - name: Contributor
    description: Manajemen wallpaper oleh contributor (upload, edit, delete).
  - name: Admin
    description: Moderasi wallpaper oleh admin.
  - name: Metadata
    description: Data referensi: sumber wallpaper, kategori, dan tag.

paths:

  # ── AUTH ──────────────────────────────────────────────────

  /registrations:
    post:
      tags: [Auth]
      summary: Register contributor account
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [display_name, email, password, password_confirmation]
              properties:
                display_name:          { type: string, maxLength: 100 }
                email:                 { type: string, format: email }
                password:              { type: string, minLength: 8 }
                password_confirmation: { type: string, minLength: 8 }
      responses:
        "201":
          description: Account created; verification email sent.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "409":
          description: Email already registered.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ErrorResponse' }

  /email-verifications:
    post:
      tags: [Auth]
      summary: Verify email with token
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [token]
              properties:
                token: { type: string }
      responses:
        "200":
          description: Email verified successfully.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "410":
          description: Token expired or already used.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ErrorResponse' }

  /sessions:
    post:
      tags: [Auth]
      summary: Login and obtain JWT
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email, password]
              properties:
                email:    { type: string, format: email }
                password: { type: string }
      responses:
        "200":
          description: Login successful; token returned.
          content:
            application/json:
              schema:
                allOf:
                  - { $ref: '#/components/schemas/SuccessResponse' }
                  - type: object
                    properties:
                      data:
                        type: object
                        properties:
                          token:      { type: string }
                          expires_at: { type: string, format: date-time }
                          user:       { $ref: '#/components/schemas/UserPublic' }
        "401": { $ref: '#/components/responses/Unauthorized' }
        "403":
          description: Account exists but email has not been verified.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ErrorResponse' }
        "429": { $ref: '#/components/responses/TooManyRequests' }

  /sessions/current:
    get:
      tags: [Auth]
      summary: Validate current JWT and return the active session user
      responses:
        "200":
          description: Current session retrieved successfully.
          content:
            application/json:
              schema:
                allOf:
                  - { $ref: '#/components/schemas/SuccessResponse' }
                  - type: object
                    properties:
                      data:
                        type: object
                        properties:
                          user:       { $ref: '#/components/schemas/UserPublic' }
                          expires_at: { type: string, format: date-time }
        "401": { $ref: '#/components/responses/Unauthorized' }
    delete:
      tags: [Auth]
      summary: Logout and revoke current JWT by denylisting its jti until expiration
      responses:
        "200":
          description: Logged out successfully.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "401": { $ref: '#/components/responses/Unauthorized' }

  /password-resets:
    post:
      tags: [Auth]
      summary: Request password reset email
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email]
              properties:
                email: { type: string, format: email }
      responses:
        "200":
          description: Generic success response (regardless of email existence).
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }

  /password-resets/{token}:
    put:
      tags: [Auth]
      summary: Reset password using token from email
      security: []
      parameters:
        - { name: token, in: path, required: true, schema: { type: string } }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [password, password_confirmation]
              properties:
                password:              { type: string, minLength: 8 }
                password_confirmation: { type: string, minLength: 8 }
      responses:
        "200":
          description: Password reset successfully.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "410":
          description: Token expired or already used.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ErrorResponse' }

  # ── WALLPAPERS PUBLIC ────────────────────────────────────

  /wallpapers:
    get:
      tags: [Wallpapers]
      summary: List approved Scapes wallpapers with search and filter
      security: []
      parameters:
        - { name: q,        in: query, schema: { type: string } }
        - { name: category, in: query, schema: { type: string } }
        - { name: tag,      in: query, schema: { type: array, items: { type: string } }, style: form, explode: true }
        - { name: target_device, in: query, schema: { $ref: '#/components/schemas/TargetDevice' } }
        - { name: page,     in: query, schema: { type: integer, default: 1 } }
        - { name: per_page, in: query, schema: { type: integer, default: 20, maximum: 100 } }
        - { name: sort_by,  in: query, schema: { type: string, enum: [published_at, title], default: published_at } }
        - { name: order,    in: query, schema: { type: string, enum: [asc, desc], default: desc } }
      responses:
        "200":
          description: List of approved Scapes wallpapers.
          content:
            application/json:
              schema:
                type: object
                properties:
                  success: { type: boolean }
                  message: { type: string }
                  data:
                    type: array
                    items: { $ref: '#/components/schemas/WallpaperPublic' }
                  meta: { $ref: '#/components/schemas/PaginationMeta' }
        "400": { $ref: '#/components/responses/ValidationError' }

  /wallpapers/{id}:
    get:
      tags: [Wallpapers]
      summary: Get single approved and published Scapes wallpaper detail
      security: []
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        "200":
          description: Approved and published Scapes wallpaper detail.
          content:
            application/json:
              schema:
                type: object
                properties:
                  success: { type: boolean }
                  message: { type: string }
                  data:    { $ref: '#/components/schemas/WallpaperPublic' }
        "404": { $ref: '#/components/responses/NotFound' }

  # ── CONTRIBUTOR ──────────────────────────────────────────

  /me/wallpapers:
    get:
      tags: [Contributor]
      summary: List all wallpapers owned by the logged-in contributor
      parameters:
        - { name: status,   in: query, schema: { type: string, enum: [pending, approved, rejected] } }
        - { name: page,     in: query, schema: { type: integer, default: 1 } }
        - { name: per_page, in: query, schema: { type: integer, default: 20 } }
      responses:
        "200":
          description: Contributor's wallpapers.
          content:
            application/json:
              schema:
                type: object
                properties:
                  success: { type: boolean }
                  data:
                    type: array
                    items: { $ref: '#/components/schemas/WallpaperContributorItem' }
                  meta: { $ref: '#/components/schemas/PaginationMeta' }
        "401": { $ref: '#/components/responses/Unauthorized' }

    post:
      tags: [Contributor]
      summary: Upload a new wallpaper for review
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required: [file, title, category_id]
              properties:
                file:        { type: string, format: binary }
                title:       { type: string, maxLength: 255 }
                description: { type: string }
                category_id: { type: integer }
                tags:
                  type: array
                  items: { type: integer }
                tag_text:
                  type: string
                  example: "#dark #city #neon"
      responses:
        "201":
          description: Wallpaper submitted for review.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "401": { $ref: '#/components/responses/Unauthorized' }

  /me/wallpapers/{id}:
    patch:
      tags: [Contributor]
      summary: Update wallpaper metadata (title, description, category, tags)
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                title:       { type: string, maxLength: 255 }
                description: { type: string }
                category_id: { type: integer }
                tags:        { type: array, items: { type: integer } }
                tag_text:    { type: string, example: "#cyberpunk #nightcity" }
      responses:
        "200":
          description: Wallpaper updated.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "401": { $ref: '#/components/responses/Unauthorized' }
        "403": { $ref: '#/components/responses/Forbidden' }
        "404": { $ref: '#/components/responses/NotFound' }

    delete:
      tags: [Contributor]
      summary: Permanently delete a wallpaper
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      responses:
        "200":
          description: Wallpaper deleted.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "401": { $ref: '#/components/responses/Unauthorized' }
        "403": { $ref: '#/components/responses/Forbidden' }
        "404": { $ref: '#/components/responses/NotFound' }

  # ── ADMIN ────────────────────────────────────────────────

  /moderation/wallpapers:
    get:
      tags: [Admin]
      summary: List wallpapers for moderation queue
      parameters:
        - { name: status,         in: query, schema: { type: string, enum: [pending, approved, rejected], default: pending } }
        - { name: contributor_id, in: query, schema: { type: integer } }
        - { name: page,           in: query, schema: { type: integer, default: 1 } }
        - { name: per_page,       in: query, schema: { type: integer, default: 20 } }
        - { name: sort_by,        in: query, schema: { type: string, enum: [created_at, title], default: created_at } }
        - { name: order,          in: query, schema: { type: string, enum: [asc, desc], default: asc } }
      responses:
        "200":
          description: Moderation queue.
          content:
            application/json:
              schema:
                type: object
                properties:
                  success: { type: boolean }
                  data:
                    type: array
                    items: { $ref: '#/components/schemas/WallpaperAdminQueueItem' }
                  meta: { $ref: '#/components/schemas/PaginationMeta' }
        "401": { $ref: '#/components/responses/Unauthorized' }
        "403": { $ref: '#/components/responses/Forbidden' }

  /moderation/wallpapers/{id}:
    patch:
      tags: [Admin]
      summary: Approve or reject a pending wallpaper
      parameters:
        - { name: id, in: path, required: true, schema: { type: string, format: uuid } }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [decision]
              properties:
                decision: { type: string, enum: [approved, rejected] }
                reason:   { type: string, description: "Required when decision is 'rejected'." }
      responses:
        "200":
          description: Moderation decision saved.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400": { $ref: '#/components/responses/ValidationError' }
        "401": { $ref: '#/components/responses/Unauthorized' }
        "403": { $ref: '#/components/responses/Forbidden' }
        "404": { $ref: '#/components/responses/NotFound' }
        "422":
          description: Wallpaper is not in 'pending' status.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ErrorResponse' }

  # ── METADATA ─────────────────────────────────────────────

  /sources:
    get:
      tags: [Metadata]
      summary: List all active wallpaper sources
      description: Returns source metadata only. Desktop clients are responsible for calling external provider APIs and managing personal API keys locally.
      security: []
      responses:
        "200":
          description: Active wallpaper sources.
          content:
            application/json:
              schema:
                allOf:
                  - { $ref: '#/components/schemas/SuccessResponse' }
                  - type: object
                    properties:
                      data:
                        type: array
                        items: { $ref: '#/components/schemas/ApiSource' }

  /categories:
    get:
      tags: [Metadata]
      summary: List all wallpaper categories
      security: []
      responses:
        "200":
          description: All categories.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }

  /tags:
    get:
      tags: [Metadata]
      summary: List all official tags, optionally filtered by keyword
      security: []
      parameters:
        - { name: q, in: query, schema: { type: string } }
        - { name: match, in: query, schema: { type: string, enum: [prefix, contains], default: prefix } }
        - { name: limit, in: query, schema: { type: integer, minimum: 1, maximum: 50, default: 10 } }
      responses:
        "200":
          description: All tags.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }

  /search-logs:
    post:
      tags: [Search Analytics]
      summary: Log a user search keyword event
      description: Best-effort endpoint for search analytics. Frontend search must continue even if this request fails.
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [keyword]
              properties:
                keyword: { type: string, minLength: 2, maxLength: 255 }
                source_slug: { type: string, example: scapes }
                client_hash: { type: string, maxLength: 64 }
                locale: { type: string, example: id-ID }
                result_count: { type: integer, minimum: 0 }
      responses:
        "202":
          description: Search event accepted.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
        "400":
          description: Invalid payload.
        "429":
          description: Too many search events.

  /categories/trending:
    get:
      tags: [Search Analytics]
      summary: List daily trending categories
      security: []
      parameters:
        - { name: date, in: query, schema: { type: string, format: date } }
        - { name: limit, in: query, schema: { type: integer, minimum: 1, maximum: 50, default: 10 } }
        - { name: include_system, in: query, schema: { type: boolean, default: true } }
      responses:
        "200":
          description: Trending categories.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }

  /recommendations/search:
    get:
      tags: [Search Analytics]
      summary: List search keyword/category recommendations
      description: MVP uses daily aggregation and rule-based scoring. ML can be introduced after enough clean logs are collected.
      security: []
      parameters:
        - { name: q, in: query, schema: { type: string } }
        - { name: source_slug, in: query, schema: { type: string, default: scapes } }
        - { name: limit, in: query, schema: { type: integer, minimum: 1, maximum: 50, default: 10 } }
        - { name: include_system, in: query, schema: { type: boolean, default: true } }
        - { name: system_match_first, in: query, schema: { type: boolean, default: true } }
        - { name: tag_match_first, in: query, schema: { type: boolean, default: true } }
      responses:
        "200":
          description: Search recommendations.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SuccessResponse' }
```

---

*— End of API Contract —*
