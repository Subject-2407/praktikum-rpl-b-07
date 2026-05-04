# ERD Data Dictionary - Scapes

**Database:** MySQL  
**Versi:** 1.0  
**Tanggal:** 2026-04-21  

---

## Daftar Tabel

1. [users](#1-users)
2. [email_verifications](#2-email_verifications)
3. [password_resets](#3-password_resets)
4. [sessions](#4-sessions)
5. [login_attempts](#5-login_attempts)
6. [categories](#6-categories)
7. [tags](#7-tags)
8. [wallpapers](#8-wallpapers)
9. [wallpaper_tags](#9-wallpaper_tags)
10. [moderation_reviews](#10-moderation_reviews)
11. [upload_limits](#11-upload_limits)
12. [api_sources](#12-api_sources)
13. [user_api_keys](#13-user_api_keys)
14. [audit_logs](#14-audit_logs)

---

## 1. `users`

Menyimpan akun terdaftar, mencakup **Contributor** dan **Admin** yang dibedakan melalui kolom `role`. Tabel ini menjadi pusat referensi FK untuk hampir seluruh tabel lainnya.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik pengguna |
| `email` | `VARCHAR(255)` | UNIQUE, NOT NULL | Alamat email; digunakan sebagai identifier login |
| `password_hash` | `VARCHAR(255)` | NOT NULL | Password yang di-hash menggunakan bcrypt (cost ≥ 12) |
| `role` | `ENUM('contributor','admin')` | NOT NULL | Peran pengguna; menentukan hak akses (RBAC) |
| `is_verified` | `TINYINT(1)` | NOT NULL, DEFAULT 0 | Status verifikasi email; `0` = belum, `1` = sudah |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu akun dibuat |
| `updated_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Waktu terakhir data diperbarui |

---

## 2. `email_verifications`

Menyimpan token sementara untuk proses verifikasi email saat registrasi contributor baru. Token memiliki masa berlaku dan hanya bisa digunakan sekali.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record verifikasi |
| `user_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke pengguna yang melakukan registrasi |
| `token` | `VARCHAR(255)` | UNIQUE, NOT NULL | Token acak yang dikirim ke email pengguna |
| `expires_at` | `DATETIME` | NOT NULL | Batas waktu kedaluwarsa token |
| `used_at` | `DATETIME` | NULL | Waktu token digunakan; `NULL` berarti belum digunakan |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu token dibuat |

---

## 3. `password_resets`

Menyimpan token reset password yang dikirim ke email contributor. Token berlaku selama 24 jam dan hanya bisa dipakai satu kali.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record reset |
| `user_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke pengguna yang meminta reset |
| `token` | `VARCHAR(255)` | UNIQUE, NOT NULL | Token unik yang disematkan di URL reset password |
| `expires_at` | `DATETIME` | NOT NULL | Batas waktu token; berlaku 24 jam sejak dibuat |
| `used_at` | `DATETIME` | NULL | Waktu token diklaim; `NULL` berarti belum digunakan |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu token dibuat |

---

## 4. `sessions`

Melacak sesi aktif pengguna (JWT atau server-side token). Digunakan untuk invalidasi token saat logout, mencegah akses kembali via tombol Back browser, dan mencatat IP untuk keamanan sesi admin.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik sesi |
| `user_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke pengguna pemilik sesi |
| `token` | `VARCHAR(512)` | UNIQUE, NOT NULL | Nilai token sesi / JWT |
| `ip_address` | `VARCHAR(45)` | NULL | Alamat IP saat sesi dibuat; mendukung IPv6 (45 karakter) |
| `expires_at` | `DATETIME` | NOT NULL | Waktu kedaluwarsa sesi; default 30 menit |
| `revoked_at` | `DATETIME` | NULL | Waktu sesi dicabut (logout); `NULL` berarti sesi masih aktif |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu sesi dibuat |

---

## 5. `login_attempts`

Mencatat setiap percobaan login (berhasil maupun gagal) untuk keperluan deteksi brute-force. Sistem akan memblokir akun atau IP setelah 5 kali gagal berturut-turut.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record percobaan login |
| `identifier` | `VARCHAR(255)` | NOT NULL | Email atau identifier akun yang dicoba |
| `ip_address` | `VARCHAR(45)` | NOT NULL | IP address asal percobaan login |
| `is_success` | `TINYINT(1)` | NOT NULL, DEFAULT 0 | Status percobaan; `1` = berhasil, `0` = gagal |
| `attempted_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu percobaan dilakukan |

---

## 6. `categories`

Menyimpan daftar kategori wallpaper (misal: Minimalist, Nature, Abstract). Kategori digunakan sebagai dasar penamaan sub-folder penyimpanan lokal (`/Scapes/[slug]/`).

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik kategori |
| `name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Nama kategori yang ditampilkan di UI |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | Versi URL-friendly dari nama; digunakan sebagai nama sub-folder |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu kategori dibuat |

---

## 7. `tags`

Menyimpan daftar tag bebas yang dapat dilampirkan ke wallpaper. Tag digunakan sebagai basis pencarian keyword oleh pengguna.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik tag |
| `name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Nama tag (misal: dark, neon, pastel) |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | Versi URL-friendly dari nama tag |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu tag dibuat |

---

## 8. `wallpapers`

Entitas utama konten aplikasi. Menyimpan metadata wallpaper yang diunggah oleh contributor, termasuk status moderasi dan jadwal publikasi.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik wallpaper |
| `contributor_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke contributor pemilik wallpaper |
| `category_id` | `INT` | NOT NULL, FK → `categories.id` | Referensi ke kategori utama wallpaper |
| `title` | `VARCHAR(255)` | NOT NULL | Judul wallpaper; wajib diisi saat upload |
| `description` | `TEXT` | NULL | Deskripsi opsional dari wallpaper |
| `file_path` | `VARCHAR(500)` | NOT NULL | Path lengkap file di server (termasuk sub-folder) |
| `file_name` | `VARCHAR(255)` | NOT NULL | Nama file asli yang tersimpan di server |
| `file_size_kb` | `INT` | NOT NULL | Ukuran file dalam kilobyte; maksimal 10.240 KB (10 MB) |
| `mime_type` | `VARCHAR(50)` | NOT NULL | Tipe MIME file; hanya `image/jpeg`, `image/png`, `image/webp` |
| `width` | `INT` | NOT NULL | Lebar gambar dalam piksel; minimal 1920 px |
| `height` | `INT` | NOT NULL | Tinggi gambar dalam piksel; minimal 1080 px |
| `status` | `ENUM('pending','approved','rejected','scheduled')` | NOT NULL, DEFAULT 'pending' | Status moderasi wallpaper |
| `scheduled_at` | `DATETIME` | NULL | Waktu publikasi terjadwal; diisi jika `status = 'scheduled'` |
| `published_at` | `DATETIME` | NULL | Waktu wallpaper benar-benar dipublikasikan |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu wallpaper pertama kali diunggah |
| `updated_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Waktu terakhir data wallpaper diperbarui |

---

## 9. `wallpaper_tags`

Tabel jembatan (*junction table*) untuk menangani relasi **many-to-many** antara `wallpapers` dan `tags`. Satu wallpaper dapat memiliki banyak tag, dan satu tag dapat melekat pada banyak wallpaper.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `wallpaper_id` | `INT` | PK (composite), NOT NULL, FK → `wallpapers.id` | Referensi ke wallpaper |
| `tag_id` | `INT` | PK (composite), NOT NULL, FK → `tags.id` | Referensi ke tag |

> **Catatan:** Primary key bersifat komposit `(wallpaper_id, tag_id)` untuk mencegah duplikasi pasangan yang sama.

---

## 10. `moderation_reviews`

Menyimpan riwayat keputusan moderasi oleh admin terhadap setiap wallpaper. Dipisahkan dari tabel `wallpapers` agar histori review tetap tersimpan meskipun status wallpaper berubah.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record review |
| `wallpaper_id` | `INT` | NOT NULL, FK → `wallpapers.id` | Referensi ke wallpaper yang dimoderasi |
| `admin_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke admin yang membuat keputusan |
| `decision` | `ENUM('approved','rejected')` | NOT NULL | Keputusan moderasi |
| `reason` | `TEXT` | NULL | Alasan penolakan; **wajib diisi** jika `decision = 'rejected'` (enforced di application layer) |
| `reviewed_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu keputusan dibuat |

---

## 11. `upload_limits`

Menyimpan konfigurasi dan counter upload harian per contributor. Admin dapat mengubah `max_uploads_per_day`; sistem mereset `upload_count_today` secara otomatis berdasarkan `last_reset_at`.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record limit |
| `contributor_id` | `INT` | UNIQUE, NOT NULL, FK → `users.id` | Referensi ke contributor; satu baris per contributor |
| `max_uploads_per_day` | `INT` | NOT NULL, DEFAULT 5 | Batas maksimum upload per hari yang ditetapkan admin |
| `upload_count_today` | `INT` | NOT NULL, DEFAULT 0 | Counter upload hari ini; direset ke `0` setiap pergantian hari |
| `last_reset_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu terakhir counter direset; digunakan untuk logika reset harian |

---

## 12. `api_sources`

Menyimpan daftar sumber wallpaper eksternal yang tersedia (Scapes, Pexels, Unsplash, Pixabay). Kolom `is_default` menandai sumber fallback jika API key pengguna tidak valid.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik sumber API |
| `name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Nama sumber yang ditampilkan di UI (misal: "Unsplash") |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | Identifier internal sumber (misal: `unsplash`) |
| `base_url` | `VARCHAR(500)` | NOT NULL | URL dasar endpoint API sumber |
| `is_default` | `TINYINT(1)` | NOT NULL, DEFAULT 0 | `1` = sumber default (Scapes API); hanya boleh ada satu |
| `is_active` | `TINYINT(1)` | NOT NULL, DEFAULT 1 | `1` = sumber aktif dan bisa dipilih pengguna |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu sumber ditambahkan |

---

## 13. `user_api_keys`

Menyimpan API key pribadi yang dimasukkan pengguna untuk meningkatkan rate limit provider eksternal. Key disimpan dalam bentuk terenkripsi dan ditampilkan dalam bentuk *masked* di UI.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record API key |
| `user_id` | `INT` | NOT NULL, FK → `users.id` | Referensi ke pengguna pemilik key |
| `source_id` | `INT` | NOT NULL, FK → `api_sources.id` | Referensi ke sumber API terkait |
| `api_key_cipher` | `VARCHAR(512)` | NOT NULL | Nilai API key dalam bentuk terenkripsi (bukan plaintext) |
| `is_valid` | `TINYINT(1)` | NOT NULL, DEFAULT 1 | Status validitas key; `0` jika validasi ke provider gagal |
| `last_validated` | `DATETIME` | NULL | Waktu terakhir key divalidasi ke provider |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu key pertama kali disimpan |
| `updated_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Waktu terakhir key diperbarui |

> **Catatan:** Terdapat constraint `UNIQUE(user_id, source_id)`, satu pengguna hanya dapat menyimpan satu API key per sumber.

---

## 14. `audit_logs`

Mencatat jejak aktivitas penting di sistem untuk keperluan keamanan, debugging, dan forensik. Kolom `entity_type` dan `entity_id` memungkinkan log dikaitkan ke entitas manapun secara generik.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik log |
| `user_id` | `INT` | NULL, FK → `users.id` | Referensi ke pengguna yang melakukan aksi; `NULL` untuk aksi sistem |
| `action` | `VARCHAR(100)` | NOT NULL | Deskripsi aksi singkat (misal: `wallpaper.approved`, `user.login`) |
| `entity_type` | `VARCHAR(100)` | NULL | Nama tabel/entitas yang terdampak (misal: `wallpapers`, `users`) |
| `entity_id` | `INT` | NULL | ID record entitas yang terdampak |
| `meta` | `JSON` | NULL | Data tambahan kontekstual (misal: alasan reject, IP address, perubahan nilai) |
| `ip_address` | `VARCHAR(45)` | NULL | IP address pelaku aksi; mendukung IPv6 |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu aksi tercatat |

---

## Ringkasan Relasi Antar Tabel

| Tabel Asal | Kolom FK | Tabel Tujuan | Tipe Relasi | Keterangan |
|---|---|---|---|---|
| `email_verifications` | `user_id` | `users` | Many-to-One | Satu user bisa punya beberapa token verifikasi |
| `password_resets` | `user_id` | `users` | Many-to-One | Satu user bisa punya beberapa token reset |
| `sessions` | `user_id` | `users` | Many-to-One | Satu user bisa punya beberapa sesi aktif |
| `login_attempts` | *(tidak ada FK langsung)* | *(tidak ada)* | *(tidak ada)* | Mencatat via `identifier` (email), bukan FK |
| `wallpapers` | `contributor_id` | `users` | Many-to-One | Banyak wallpaper dimiliki satu contributor |
| `wallpapers` | `category_id` | `categories` | Many-to-One | Banyak wallpaper dalam satu kategori |
| `wallpaper_tags` | `wallpaper_id` | `wallpapers` | Many-to-Many | Resolusi relasi m:n wallpaper ↔ tag |
| `wallpaper_tags` | `tag_id` | `tags` | Many-to-Many | Resolusi relasi m:n wallpaper ↔ tag |
| `moderation_reviews` | `wallpaper_id` | `wallpapers` | Many-to-One | Satu wallpaper bisa direview lebih dari sekali |
| `moderation_reviews` | `admin_id` | `users` | Many-to-One | Satu admin bisa mereview banyak wallpaper |
| `upload_limits` | `contributor_id` | `users` | One-to-One | Satu baris limit per contributor |
| `user_api_keys` | `user_id` | `users` | Many-to-One | Satu user bisa simpan key untuk beberapa sumber |
| `user_api_keys` | `source_id` | `api_sources` | Many-to-One | Satu sumber bisa dipakai banyak user |
| `audit_logs` | `user_id` | `users` | Many-to-One | Banyak log bisa dimiliki satu user |