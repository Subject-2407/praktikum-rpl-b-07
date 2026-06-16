# ERD Data Dictionary - Scapes

**Database:** MySQL  
**Versi:** 1.4  
**Tanggal:** 2026-06-09  

> **Changelog v1.2:**
> - menambahkan `display_name` untuk `users`
> - `wallpapers.id` diubah dari `INT AUTO_INCREMENT` menjadi `CHAR(36)` UUID v4 (generate di application layer)
> - `wallpapers.file_path` dan `wallpapers.file_name` dihapus
> - `wallpaper_tags.wallpaper_id` diubah menjadi `CHAR(36)` (cascade dari FK)
> - `moderation_reviews.wallpaper_id` diubah menjadi `CHAR(36)` (cascade dari FK)
> - `audit_logs.entity_id` diubah dari `INT` menjadi `VARCHAR(36)` (agar kompatibel dengan UUID maupun int ID)
>
> **Changelog v1.3:**
> - menambahkan `search_logs` untuk mencatat keyword pencarian user secara privacy-safe
> - menambahkan `daily_trending_categories` untuk menyimpan hasil agregasi kategori trending harian dari keyword user maupun kategori bawaan
>
> **Changelog v1.4:**
> - menambahkan `wallpaper_tag_proposals` untuk menyimpan usulan tag baru dari contributor sampai wallpaper di-approve admin

---

## Daftar Tabel

1. [users](#1-users)
2. [email_verifications](#2-email_verifications)
3. [password_resets](#3-password_resets)
4. [login_attempts](#4-login_attempts)
5. [categories](#5-categories)
6. [tags](#6-tags)
7. [wallpapers](#7-wallpapers)
8. [wallpaper_tags](#8-wallpaper_tags)
9. [moderation_reviews](#9-moderation_reviews)
10. [api_sources](#10-api_sources)
11. [audit_logs](#11-audit_logs)
12. [search_logs](#12-search_logs)
13. [daily_trending_categories](#13-daily_trending_categories)
14. [wallpaper_tag_proposals](#14-wallpaper_tag_proposals)

---

## 1. `users`

Menyimpan akun terdaftar, mencakup **Contributor** dan **Admin** yang dibedakan melalui kolom `role`. Tabel ini menjadi pusat referensi FK untuk hampir seluruh tabel lainnya.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik pengguna |
| `email` | `VARCHAR(255)` | UNIQUE, NOT NULL | Alamat email; digunakan sebagai identifier login |
| `display_name` | `VARCHAR(100)` | NOT NULL | Nama tampilan pengguna yang terlihat di aplikasi |
| `password_hash` | `VARCHAR(255)` | NOT NULL | Password yang di-hash menggunakan bcrypt (cost >= 12) |
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
| `user_id` | `INT` | NOT NULL, FK -> `users.id` | Referensi ke pengguna yang melakukan registrasi |
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
| `user_id` | `INT` | NOT NULL, FK -> `users.id` | Referensi ke pengguna yang meminta reset |
| `token` | `VARCHAR(255)` | UNIQUE, NOT NULL | Token unik yang disematkan di URL reset password |
| `expires_at` | `DATETIME` | NOT NULL | Batas waktu token; berlaku 24 jam sejak dibuat |
| `used_at` | `DATETIME` | NULL | Waktu token diklaim; `NULL` berarti belum digunakan |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu token dibuat |

---

## 4. `login_attempts`

Mencatat setiap percobaan login (berhasil maupun gagal) untuk keperluan deteksi brute-force. Sistem akan memblokir akun atau IP setelah 5 kali gagal berturut-turut.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record percobaan login |
| `identifier` | `VARCHAR(255)` | NOT NULL | Email atau identifier akun yang dicoba |
| `ip_address` | `VARCHAR(45)` | NOT NULL | IP address asal percobaan login |
| `is_success` | `TINYINT(1)` | NOT NULL, DEFAULT 0 | Status percobaan; `1` = berhasil, `0` = gagal |
| `attempted_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu percobaan dilakukan |

---

## 5. `categories`

Menyimpan daftar kategori wallpaper (misal: Minimalist, Nature, Abstract). Kategori digunakan sebagai dasar penamaan sub-folder penyimpanan lokal (`/Scapes/[slug]/`).

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik kategori |
| `name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Nama kategori yang ditampilkan di UI |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | Versi URL-friendly dari nama; digunakan sebagai nama sub-folder |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu kategori dibuat |

---

## 6. `tags`

Menyimpan daftar tag bebas yang dapat dilampirkan ke wallpaper. Tag digunakan sebagai basis pencarian keyword oleh pengguna. Tabel ini hanya berisi tag yang sudah resmi/approved. Tag baru dari contributor tidak langsung masuk tabel ini sampai wallpaper terkait di-approve admin.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik tag |
| `name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Nama tag (misal: dark, neon, pastel) |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | Versi URL-friendly dari nama tag |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu tag dibuat |

> **Aturan tag:** Pada form contributor dan search user, tag ditulis dengan awalan `#` (misal `#colorful`). Di database, `name` dan `slug` disimpan tanpa karakter `#`.

---

## 7. `wallpapers`

Entitas utama konten aplikasi. Menyimpan metadata wallpaper yang diunggah oleh contributor, termasuk status moderasi dan waktu publikasi.

> **v1.1:** `id` menggunakan UUID v4 (`CHAR(36)`) yang di-generate di application layer.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `CHAR(36)` | PK, NOT NULL | UUID v4 wallpaper; di-generate di application layer (bukan AUTO_INCREMENT) |
| `contributor_id` | `INT` | NOT NULL, FK -> `users.id` | Referensi ke contributor pemilik wallpaper |
| `category_id` | `INT` | NOT NULL, FK -> `categories.id` | Referensi ke kategori utama wallpaper |
| `title` | `VARCHAR(255)` | NOT NULL | Judul wallpaper; wajib diisi saat upload |
| `description` | `TEXT` | NULL | Deskripsi opsional dari wallpaper |
| `file_size_kb` | `INT` | NOT NULL | Ukuran file dalam kilobyte; maksimal 10.240 KB (10 MB) |
| `mime_type` | `VARCHAR(50)` | NOT NULL | Tipe MIME file; hanya `image/jpeg`, `image/png`, `image/webp` |
| `width` | `INT` | NOT NULL | Lebar gambar dalam piksel; minimal 1920 px |
| `height` | `INT` | NOT NULL | Tinggi gambar dalam piksel; minimal 1080 px |
| `target_device` | `ENUM('desktop','mobile','tablet')` | NOT NULL | Target perangkat wallpaper; dipakai untuk klasifikasi layar |
| `status` | `ENUM('pending','approved','rejected')` | NOT NULL, DEFAULT 'pending' | Status moderasi wallpaper |
| `published_at` | `DATETIME` | NULL | Waktu wallpaper benar-benar dipublikasikan |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu wallpaper pertama kali diunggah |
| `updated_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Waktu terakhir data wallpaper diperbarui |

---

## 8. `wallpaper_tags`

Tabel jembatan (*junction table*) untuk menangani relasi **many-to-many** antara `wallpapers` dan `tags`. Satu wallpaper dapat memiliki banyak tag, dan satu tag dapat melekat pada banyak wallpaper.

> **v1.1:** `wallpaper_id` diubah menjadi `CHAR(36)` mengikuti perubahan tipe PK pada tabel `wallpapers`.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `wallpaper_id` | `CHAR(36)` | PK (composite), NOT NULL, FK -> `wallpapers.id` | Referensi ke wallpaper; tipe mengikuti UUID PK `wallpapers` |
| `tag_id` | `INT` | PK (composite), NOT NULL, FK -> `tags.id` | Referensi ke tag |

> **Catatan:** Primary key bersifat komposit `(wallpaper_id, tag_id)` untuk mencegah duplikasi pasangan yang sama.

---

## 9. `moderation_reviews`

Menyimpan riwayat keputusan moderasi oleh admin terhadap setiap wallpaper. Dipisahkan dari tabel `wallpapers` agar histori review tetap tersimpan meskipun status wallpaper berubah.

> **v1.1:** `wallpaper_id` diubah menjadi `CHAR(36)` mengikuti perubahan tipe PK pada tabel `wallpapers`.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record review |
| `wallpaper_id` | `CHAR(36)` | NOT NULL, FK -> `wallpapers.id` | Referensi ke wallpaper yang dimoderasi; tipe mengikuti UUID PK `wallpapers` |
| `admin_id` | `INT` | NOT NULL, FK -> `users.id` | Referensi ke admin yang membuat keputusan |
| `decision` | `ENUM('approved','rejected')` | NOT NULL | Keputusan moderasi |
| `reason` | `TEXT` | NULL | Alasan penolakan; **wajib diisi** jika `decision = 'rejected'` (enforced di application layer) |
| `reviewed_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu keputusan dibuat |

---

## 10. `api_sources`

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

## 11. `audit_logs`

Mencatat jejak aktivitas penting di sistem untuk keperluan keamanan, debugging, dan forensik. Kolom `entity_type` dan `entity_id` memungkinkan log dikaitkan ke entitas manapun secara generik.

> **v1.1:** `entity_id` diubah dari `INT` menjadi `VARCHAR(36)` agar dapat menampung UUID (`CHAR(36)`) dari tabel `wallpapers` maupun `INT` dari tabel lain yang masih menggunakan AUTO_INCREMENT.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | ID unik log |
| `user_id` | `INT` | NULL, FK -> `users.id` | Referensi ke pengguna yang melakukan aksi; `NULL` untuk aksi sistem |
| `action` | `VARCHAR(100)` | NOT NULL | Deskripsi aksi singkat (misal: `wallpaper.approved`, `user.login`) |
| `entity_type` | `VARCHAR(100)` | NULL | Nama tabel/entitas yang terdampak (misal: `wallpapers`, `users`) |
| `entity_id` | `VARCHAR(36)` | NULL | ID record entitas yang terdampak; `VARCHAR(36)` agar kompatibel dengan UUID maupun INT ID |
| `meta` | `JSON` | NULL | Data tambahan kontekstual (misal: alasan reject, IP address, perubahan nilai) |
| `ip_address` | `VARCHAR(45)` | NULL | IP address pelaku aksi; mendukung IPv6 |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu aksi tercatat |

---

## 12. `search_logs`

Menyimpan event pencarian wallpaper dari frontend user. Tabel ini menjadi bahan utama untuk menghitung rekomendasi dan kategori trending. Data yang disimpan harus minimal dan privacy-safe karena user desktop tidak wajib login.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT, NOT NULL | ID unik search event |
| `keyword_raw` | `VARCHAR(255)` | NOT NULL | Keyword setelah trim dasar; digunakan untuk audit kualitas data, bukan untuk ditampilkan langsung |
| `keyword_normalized` | `VARCHAR(255)` | NOT NULL, INDEX | Keyword hasil normalisasi lowercase, whitespace normalized, dan karakter berbahaya dihapus |
| `keyword_slug` | `VARCHAR(255)` | NOT NULL, INDEX | Bentuk slug dari keyword untuk grouping cepat |
| `source_slug` | `VARCHAR(100)` | NULL, INDEX | Source yang dipilih user saat search, misal `scapes`, `unsplash`, `pexels`, `pixabay` |
| `matched_category_id` | `INT` | NULL, FK -> `categories.id` | Kategori hasil mapping keyword; `NULL` jika belum bisa dipetakan |
| `matched_tag_id` | `INT` | NULL, FK -> `tags.id` | Tag hasil mapping keyword; `NULL` jika belum bisa dipetakan |
| `client_hash` | `CHAR(64)` | NULL, INDEX | Hash opsional untuk deduplication anonim; tidak boleh berupa email, device name, atau identifier mentah |
| `locale` | `VARCHAR(20)` | NULL | Locale kasar dari client jika tersedia, misal `id-ID` atau `en-US` |
| `result_count` | `INT` | NULL | Jumlah hasil yang dikembalikan untuk keyword tersebut jika tersedia |
| `searched_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP, INDEX | Waktu pencarian dilakukan |

> **Catatan privasi:** Jangan menyimpan API key, full local path, email, nama user, atau nilai device identifier mentah. Raw search logs maksimal disimpan 90 hari sebelum dihapus atau hanya dipertahankan dalam bentuk agregasi.

---

## 13. `daily_trending_categories`

Menyimpan hasil agregasi harian dari `search_logs`. Endpoint rekomendasi dan trending harus membaca dari tabel ini agar tidak menghitung raw logs secara real-time. Kategori trending tidak selalu harus sudah ada di tabel `categories`; keyword populer seperti `beach`, `city`, atau `forest` dapat menjadi kategori dinamis selama belum dipromosikan menjadi kategori bawaan.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT, NOT NULL | ID unik record agregasi |
| `trend_date` | `DATE` | NOT NULL, INDEX | Tanggal agregasi berdasarkan timezone sistem/server |
| `category_id` | `INT` | NULL, FK -> `categories.id` | Referensi kategori bawaan jika keyword cocok dengan `categories`; `NULL` untuk user-keyword category dinamis |
| `label` | `VARCHAR(100)` | NOT NULL | Nama kategori yang ditampilkan di UI, misal `Beach` atau `Minimalist` |
| `slug` | `VARCHAR(100)` | NOT NULL, INDEX | Slug kategori untuk search/filter, misal `beach` atau `minimalist` |
| `origin` | `ENUM('user_keyword','system')` | NOT NULL, DEFAULT 'user_keyword' | Sumber kategori; `user_keyword` untuk hasil agregasi keyword user, `system` untuk kategori bawaan |
| `search_count` | `INT` | NOT NULL, DEFAULT 0 | Total event pencarian yang dipetakan ke kategori tersebut |
| `unique_client_count` | `INT` | NOT NULL, DEFAULT 0 | Jumlah client anonim unik jika `client_hash` tersedia |
| `score` | `DECIMAL(10,4)` | NOT NULL, DEFAULT 0 | Skor ranking hasil kombinasi frekuensi, recency, dan deduplication |
| `top_keywords` | `JSON` | NULL | Keyword populer dalam kategori tersebut, misal `["beach", "ocean", "coast"]` |
| `computed_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu agregasi dihitung |

> **Constraint yang disarankan:** unique key `(trend_date, origin, slug)` agar satu kategori dinamis maupun bawaan hanya memiliki satu agregasi per hari.

---

## 14. `wallpaper_tag_proposals`

Menyimpan usulan tag baru atau tag hasil input contributor untuk wallpaper yang masih menunggu moderasi. Saat admin approve wallpaper, sistem melakukan upsert ke tabel `tags`: tag yang belum ada dibuat, tag yang sudah ada dipakai ulang, lalu relasi final dibuat di `wallpaper_tags`.

| Kolom | Tipe Data | Constraint | Keterangan |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT, NOT NULL | ID unik proposal tag |
| `wallpaper_id` | `CHAR(36)` | NOT NULL, FK -> `wallpapers.id` | Wallpaper pending yang membawa tag proposal |
| `tag_text` | `VARCHAR(100)` | NOT NULL | Tag asli setelah trim dan tanpa `#`, misal `colorful` |
| `tag_slug` | `VARCHAR(100)` | NOT NULL, INDEX | Slug hasil normalisasi untuk deduplication, misal `colorful` |
| `existing_tag_id` | `INT` | NULL, FK -> `tags.id` | Diisi jika tag sudah ada saat proposal dibuat; `NULL` untuk tag baru |
| `status` | `ENUM('pending','approved','discarded')` | NOT NULL, DEFAULT 'pending' | Status proposal tag mengikuti hasil moderasi wallpaper |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Waktu proposal dibuat |
| `resolved_at` | `DATETIME` | NULL | Waktu proposal diproses saat approve/reject |

> **Constraint yang disarankan:** unique key `(wallpaper_id, tag_slug)` agar satu wallpaper tidak punya proposal tag duplikat.

---

## Ringkasan Relasi Antar Tabel

| Tabel Asal | Kolom FK | Tabel Tujuan | Tipe Relasi | Keterangan |
|---|---|---|---|---|
| `email_verifications` | `user_id` | `users` | Many-to-One | Satu user bisa punya beberapa token verifikasi |
| `password_resets` | `user_id` | `users` | Many-to-One | Satu user bisa punya beberapa token reset |
| `login_attempts` | *(tidak ada FK langsung)* | *(tidak ada)* | *(tidak ada)* | Mencatat via `identifier` (email), bukan FK |
| `wallpapers` | `contributor_id` | `users` | Many-to-One | Banyak wallpaper dimiliki satu contributor |
| `wallpapers` | `category_id` | `categories` | Many-to-One | Banyak wallpaper dalam satu kategori |
| `wallpaper_tags` | `wallpaper_id` | `wallpapers` | Many-to-Many | Resolusi relasi m:n wallpaper <-> tag; FK bertipe `CHAR(36)` |
| `wallpaper_tags` | `tag_id` | `tags` | Many-to-Many | Resolusi relasi m:n wallpaper <-> tag |
| `moderation_reviews` | `wallpaper_id` | `wallpapers` | Many-to-One | Satu wallpaper bisa direview lebih dari sekali; FK bertipe `CHAR(36)` |
| `moderation_reviews` | `admin_id` | `users` | Many-to-One | Satu admin bisa mereview banyak wallpaper |
| `audit_logs` | `user_id` | `users` | Many-to-One | Banyak log bisa dimiliki satu user |
| `search_logs` | `matched_category_id` | `categories` | Many-to-One | Banyak search event dapat dipetakan ke satu kategori |
| `search_logs` | `matched_tag_id` | `tags` | Many-to-One | Banyak search event dapat dipetakan ke satu tag |
| `daily_trending_categories` | `category_id` | `categories` | Many-to-One | Banyak record agregasi harian dapat mengacu ke satu kategori bawaan; nullable untuk kategori dinamis dari keyword user |
| `wallpaper_tag_proposals` | `wallpaper_id` | `wallpapers` | Many-to-One | Satu wallpaper pending dapat memiliki banyak tag proposal |
| `wallpaper_tag_proposals` | `existing_tag_id` | `tags` | Many-to-One | Proposal dapat mengacu ke tag existing jika sudah tersedia di database |
