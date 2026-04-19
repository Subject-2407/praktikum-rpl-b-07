# Software Requirements Specification (SRS)
## Scapes

**Versi:** 1.0  
**Tanggal:** 19-04-2026  
**Penulis:** Scapes  
**Organisasi:** Program Studi Informatika, Universitas Sebelas Maret  
**Periode:** 2025/2026

---

## Daftar Isi

- [1. Pendahuluan](#1-pendahuluan)
  - [1.1 Tujuan Dokumen](#11-tujuan-dokumen)
  - [1.2 Ruang Lingkup](#12-ruang-lingkup)
  - [1.3 Definisi dan Akronim](#13-definisi-dan-akronim)
- [2. Deskripsi Umum](#2-deskripsi-umum)
  - [2.1 Perspektif Produk](#21-perspektif-produk)
  - [2.2 Fungsi Produk](#22-fungsi-produk)
  - [2.3 Karakteristik Pengguna](#23-karakteristik-pengguna)
  - [2.4 Batasan dan Kendala](#24-batasan-dan-kendala)
- [3. Kebutuhan Fungsional (Functional Requirements)](#3-kebutuhan-fungsional-functional-requirements)
- [4. Kebutuhan Non-Fungsional (Non-Functional Requirements)](#4-kebutuhan-non-fungsional-non-functional-requirements)
  - [4.2 Keamanan (Security)](#42-keamanan-security)
  - [4.3 Keandalan (Reliability)](#43-keandalan-reliability)
  - [4.4 Usability (Kegunaan/Kemudahan Penggunaan)](#44-usability-kegunaankemudahan-penggunaan)
- [5. Traceability Matrix](#5-traceability-matrix)
  - [5.1 FR ke User Story ke Acceptance Criteria](#51-fr-ke-user-story-ke-acceptance-criteria)
  - [5.2 Ringkasan Traceability](#52-ringkasan-traceability)
- [6. Catatan dan Asumsi](#6-catatan-dan-asumsi)
  - [6.1 Asumsi](#61-asumsi)
  - [6.2 Dependensi](#62-dependensi)
  - [6.3 Batasan & Constraints](#63-batasan--constraints)

---

## 1. Pendahuluan

### 1.1 Tujuan Dokumen

Dokumen Software Requirements Specification (SRS) ini bertujuan untuk:
- Tujuan 1
- Tujuan 2

### 1.2 Ruang Lingkup

**Nama Produk:** [Isikan nama produk/aplikasi]

**Deskripsi Singkat:**  
[Jelaskan secara singkat apa yang akan dibangun, siapa pengguna utamanya, dan nilai yang diberikan]

**Fitur Utama yang Akan Dikembangkan:**
- [Fitur 1]
- [Fitur 2]
- [Fitur 3]
- [dst...]

**Fitur Yang TIDAK Termasuk Dalam Scope:**
- [Fitur yang dikecualikan]
- [dst...]

### 1.3 Definisi dan Akronim

| Istilah | Definisi |
|---------|----------|
| **User** | Pengguna akhir yang berinteraksi dengan sistem |
| **Admin** | Pengguna dengan hak akses penuh untuk manajemen sistem |
| **API** | Application Programming Interface - antarmuka untuk integrasi |
| **Database** | Sistem penyimpanan data terstruktur |
| **Authentication** | Proses verifikasi identitas pengguna |
| **Authorization** | Proses penentuan akses berdasarkan peran pengguna |
| **Response Time** | Waktu yang dibutuhkan sistem merespons permintaan |
| **Uptime** | Persentase waktu sistem tersedia dan berfungsi |
| **UI/UX** | User Interface dan User Experience |
| **[Istilah Lainnya]** | [Definisi] |

---

## 2. Deskripsi Umum

### 2.1 Perspektif Produk

**Konteks Sistem:**
- Backend API: [Technology Stack]
- Frontend Desktop: [Technology Stack]
- Frontend Web: [Technology Stack]
- Database: [Technology Stack]
- Integrasi Eksternal: [Sistem/API yang diintegrasikan]

### 2.2 Fungsi Produk

| No | Fungsi | Deskripsi |
|----|--------|-----------|
| 1 | [Nama Fungsi 1] | [Penjelasan singkat] |
| 2 | [Nama Fungsi 2] | [Penjelasan singkat] |
| 3 | [Nama Fungsi 3] | [Penjelasan singkat] |
| 4 | [Nama Fungsi 4] | [Penjelasan singkat] |

### 2.3 Karakteristik Pengguna

| Tipe User | Tingkat Keahlian | Frekuensi | Kebutuhan |
|-----------|------------------|-----------|-----------|
| **User Umum** | Pemula-Menengah | Setiap hari | UI intuitif, mudah dipelajari |
| **[Tipe Lain]** | [Level] | [Frekuensi] | [Kebutuhan] |

**Kompatibilitas:**
- Browser: Chrome, Firefox, Safari, Edge (latest versions)
- Device: Desktop (Windows), Tablet, Mobile
- Aksesibilitas: WCAG 2.1 Level AA (jika diperlukan)

### 2.4 Batasan dan Kendala

| Kategori | Batasan |
|----------|---------|
| **Teknis** | Infrastruktur: [jelaskan], Storage: [jelaskan], Bandwidth: [jelaskan] |
| **Operasional** | Jam kerja: [jelaskan], Maintenance window: [jelaskan] |
| **Regulasi** | Compliance: [sebutkan jika ada], Data protection: [sebutkan jika ada] |

---

## 3. Kebutuhan Fungsional (Functional Requirements)

### Ringkasan Kebutuhan Fungsional

| ID | Nama Fitur | Deskripsi | Prioritas | US Ref | Actor |
|-----|-----------|-----------|-----------|--------|-------|
| **FR-01** | Search Wallpapers by Keyword | User dapat mencari wallpaper berdasarkan kata kunci untuk menemukan tema spesifik dengan cepat | High | US-01 | User |
| **FR-02** | Set Wallpaper dengan One Click | User dapat mengatur wallpaper dengan satu klik, sistem secara otomatis mengunduh dan menerapkan wallpaper | High | US-02 | User |
| **FR-03** | Auto Save & Organize Wallpapers | Sistem otomatis menyimpan dan mengorganisir wallpaper yang diunduh ke dalam folder terstruktur | High | US-03 | User |
| **FR-04** | Customize Download Folder Location | User dapat mengubah lokasi folder download untuk menyimpan wallpaper di lokasi yang diinginkan | Medium | US-04 | User |
| **FR-05** | Switch Between Wallpaper Sources | User dapat beralih antara sumber wallpaper berbeda (Pexels, Unsplash, Pixabay, dll) | Medium | US-05 | User |
| **FR-06** | Input Personal API Keys | User dapat memasukkan API key pribadi untuk provider wallpaper guna meningkatkan rate limit | Low | US-06 | User |
| **FR-07** | Upload Wallpapers | Contributor dapat mengunggah wallpaper mereka untuk dibagikan dengan pengguna lain | High | CO-01 | Contributor |
| **FR-08** | Track Moderation Status | Contributor dapat melacak status moderasi wallpaper yang telah diunggah (Pending/Approved/Rejected) | Medium | CO-02 | Contributor |
| **FR-09** | Delete Wallpapers | Contributor dapat menghapus wallpaper yang telah diunggah | Medium | CO-03 | Contributor |
| **FR-10** | Edit Wallpaper Details | Contributor dapat mengedit detail wallpaper seperti deskripsi, kategori, tags, dan judul | Low | CO-04 | Contributor |
| **FR-11** | Schedule Wallpaper Publication | Contributor dapat menjadwalkan publikasi wallpaper pada waktu tertentu | Low | CO-05 | Contributor |
| **FR-12** | Register Account with Email | Contributor dapat mendaftar akun menggunakan email untuk mulai mengunggah wallpaper | High | CO-06 | Contributor |
| **FR-13** | Manage Session (Login/Logout) | Contributor dapat mengelola sesi dengan login dan logout dengan aman | High | CO-07 | Contributor |
| **FR-14** | Request Password Reset via Email | Contributor dapat meminta link reset password melalui email untuk pemulihan akun | High | CO-08 | Contributor |
| **FR-15** | Moderate Wallpapers | Admin dapat menyetujui atau menolak wallpaper yang telah diunggah oleh contributor | High | AD-01 | Admin |
| **FR-16** | Limit Contributor Uploads | Admin dapat membatasi jumlah upload per contributor untuk kontrol kualitas | Low | AD-02 | Admin |
| **FR-17** | Manage Admin Session via Internal Portal | Admin dapat mengelola sesi login/logout melalui portal internal dengan akses aman | High | AD-03 | Admin |

---

### Detail Kebutuhan Fungsional

#### FR-01: Search Wallpapers by Keyword
- **Pre-condition:** User telah membuka aplikasi dan berada di halaman Search/Gallery
- **Main Flow:** 
  1. User memasukkan kata kunci di kolom pencarian (mis: "Minimalist")
  2. User menekan tombol Enter atau tombol Search
  3. Sistem melakukan filter pada database wallpaper berdasarkan kata kunci
  4. Sistem menampilkan hasil wallpaper yang sesuai dengan tag atau kategori yang cocok
- **Post-condition:** Galeri wallpaper ditampilkan dengan hasil pencarian yang relevan, atau pesan "No wallpapers found" jika tidak ada hasil

#### FR-02: Set Wallpaper dengan One Click
- **Pre-condition:** User telah membuka aplikasi dan melihat galeri wallpaper; ada koneksi internet
- **Main Flow:**
  1. User memilih wallpaper dari galeri dan mengklik tombol "Set as Wallpaper" atau ikon checkmark
  2. Sistem memulai proses download file wallpaper ke lokasi penyimpanan lokal
  3. Setelah download selesai, sistem secara otomatis memanggil OS API untuk mengubah desktop wallpaper
  4. Sistem menampilkan notifikasi toast "Wallpaper updated successfully"
- **Post-condition:** Desktop wallpaper berubah ke gambar yang dipilih, notifikasi konfirmasi ditampilkan

#### FR-03: Auto Save & Organize Wallpapers
- **Pre-condition:** User telah mengunduh wallpaper melalui FR-02
- **Main Flow:**
  1. Sistem menerima hasil download wallpaper
  2. Sistem otomatis menentukan kategori wallpaper berdasarkan metadata atau tag
  3. Sistem membuat struktur folder: `/Scapes/[category]/` jika belum ada
  4. Sistem menyimpan file wallpaper ke subfolder yang sesuai dengan kategorinya
  5. Sistem mencatat informasi file di database lokal (filename, path, category, download date)
- **Post-condition:** Wallpaper tersimpan terorganisir di folder kategori yang sesuai

#### FR-04: Customize Download Folder Location
- **Pre-condition:** User berada di halaman Settings
- **Main Flow:**
  1. User mengklik tombol "Change Download Location"
  2. Sistem membuka dialog file picker untuk memilih folder
  3. User memilih lokasi folder baru (mis: OneDrive, Dropbox, atau drive lain)
  4. Sistem memvalidasi akses write ke folder yang dipilih
  5. User mengklik "Confirm" dan sistem menyimpan path baru ke konfigurasi aplikasi
  6. Sistem menampilkan konfirmasi "Download location updated"
- **Post-condition:** Lokasi download folder berubah dan disimpan, wallpaper berikutnya akan diunduh ke lokasi baru

#### FR-05: Switch Between Wallpaper Sources
- **Pre-condition:** User berada di halaman Gallery dan multiple wallpaper sources telah dikonfigurasi
- **Main Flow:**
  1. User mengklik dropdown "Source" di bagian atas galeri
  2. Sistem menampilkan daftar sumber wallpaper yang tersedia (Pexels, Unsplash, Pixabay, Scapes API)
  3. User memilih sumber yang berbeda
  4. Sistem mengakses API dari sumber yang dipilih dengan credentials yang sesuai
  5. Sistem melakukan refresh galeri dan menampilkan wallpaper dari sumber baru
- **Post-condition:** Galeri wallpaper beralih menampilkan konten dari sumber yang baru dipilih

#### FR-06: Input Personal API Keys
- **Pre-condition:** User berada di halaman Settings dan sudah mengetahui API key dari provider
- **Main Flow:**
  1. User navigasi ke Settings > API Keys Configuration
  2. User mengklik tombol "Add API Key"
  3. User memilih provider (Unsplash, Pexels, Pixabay)
  4. User memasukkan API key ke dalam field yang disediakan
  5. Sistem melakukan validasi API key dengan mengirim test request ke provider
  6. Jika valid, sistem menyimpan key (encrypted) ke database lokal
  7. Sistem menampilkan notifikasi "API Key saved successfully"
- **Post-condition:** API key tersimpan dan sistem akan menggunakan key pribadi untuk meningkatkan rate limit provider

#### FR-07: Upload Wallpapers
- **Pre-condition:** Contributor telah login dan memiliki file gambar untuk diunggah
- **Main Flow:**
  1. Contributor navigasi ke halaman "Submit Wallpaper" atau "My Uploads"
  2. Contributor mengklik tombol "Select Image" dan memilih file dari komputer
  3. Sistem melakukan validasi: format file (JPG, PNG, WebP), ukuran file (max 10MB), resolusi minimal
  4. Contributor mengisi detail wallpaper: judul, deskripsi, kategori, tags
  5. Contributor mengklik "Submit for Review"
  6. Sistem mengunggah file ke server dan membuat record dengan status "Pending"
  7. Sistem menampilkan notifikasi "Wallpaper submitted for review"
- **Post-condition:** Wallpaper tersimpan di server dengan status Pending, contributor dapat melihatnya di dashboard "My Uploads"

#### FR-08: Track Moderation Status
- **Pre-condition:** Contributor telah mengunggah wallpaper dan berada di halaman "My Uploads"
- **Main Flow:**
  1. Sistem menampilkan daftar semua wallpaper yang telah diunggah oleh contributor
  2. Setiap item menampilkan status saat ini: Pending, Approved, atau Rejected
  3. Sistem menampilkan timestamp kapan wallpaper di-review
  4. Jika status Rejected, contributor dapat mengklik item untuk melihat alasan penolakan
  5. Jika wallpaper pending selama > 3 hari, sistem menampilkan badge "Review in Progress"
- **Post-condition:** Contributor dapat memantau status moderation semua wallpaper mereka

#### FR-09: Delete Wallpapers
- **Pre-condition:** Contributor berada di halaman "My Uploads" dan melihat wallpaper mereka
- **Main Flow:**
  1. Contributor mengklik tombol "Delete" pada wallpaper yang ingin dihapus
  2. Sistem menampilkan dialog konfirmasi: "Are you sure you want to delete this wallpaper?"
  3. Contributor mengklik "Yes, Delete"
  4. Sistem menghapus file dari server dan menghapus record dari database
  5. Sistem menampilkan notifikasi "Wallpaper deleted successfully"
- **Post-condition:** Wallpaper dihapus dari sistem dan tidak lagi tersedia untuk pengguna

#### FR-10: Edit Wallpaper Details
- **Pre-condition:** Contributor memiliki wallpaper yang belum di-approve dan ingin mengedit detailnya
- **Main Flow:**
  1. Contributor mengklik tombol "Edit" pada wallpaper di halaman "My Uploads"
  2. Sistem menampilkan form edit dengan field: Title, Description, Category, Tags
  3. Contributor mengubah informasi yang diperlukan
  4. Contributor mengklik "Save Changes"
  5. Sistem melakukan validasi input (tidak boleh kosong, karakter spesial)
  6. Sistem menyimpan perubahan ke database
  7. Sistem menampilkan notifikasi "Wallpaper updated successfully"
- **Post-condition:** Detail wallpaper tersimpan dengan informasi terbaru

#### FR-11: Schedule Wallpaper Publication
- **Pre-condition:** Contributor telah mengunggah wallpaper dan admin telah menyetujuinya, contributor berada di halaman edit wallpaper
- **Main Flow:**
  1. Contributor mengklik tombol "Schedule Publication"
  2. Sistem menampilkan date/time picker untuk memilih waktu publikasi
  3. Contributor memilih tanggal dan waktu publikasi yang diinginkan
  4. Contributor mengklik "Schedule"
  5. Sistem menyimpan waktu publikasi di database dan membuat scheduled task
  6. Pada waktu yang ditentukan, sistem secara otomatis mengubah status wallpaper menjadi "Published"
- **Post-condition:** Wallpaper dijadwalkan untuk publikasi otomatis pada waktu yang ditentukan

#### FR-12: Register Account with Email
- **Pre-condition:** User yang ingin menjadi contributor berada di halaman pendaftaran
- **Main Flow:**
  1. Calon contributor mengklik tombol "Sign Up"
  2. Sistem menampilkan form registrasi dengan field: Email, Password, Confirm Password
  3. Contributor memasukkan email dan password (minimal 8 karakter dengan kombinasi huruf, angka, simbol)
  4. Sistem melakukan validasi: email belum terdaftar, password memenuhi kriteria
  5. Contributor mengklik "Create Account"
  6. Sistem membuat akun baru dan mengirim email verifikasi
  7. Contributor mengklik link di email untuk memverifikasi akun
  8. Sistem menampilkan pesan "Account verified! Redirecting to login..."
- **Post-condition:** Akun contributor berhasil dibuat dan terverifikasi, ready untuk login

#### FR-13: Manage Session (Login/Logout)
- **Pre-condition:** Contributor memiliki akun terdaftar dan terverifikasi
- **Main Flow (Login):**
  1. Contributor navigasi ke halaman Login
  2. Contributor memasukkan email dan password
  3. Sistem melakukan validasi credentials terhadap database
  4. Jika valid, sistem membuat session/JWT token dengan timeout 30 menit
  5. Contributor di-redirect ke Contributor Dashboard
- **Main Flow (Logout):**
  1. Contributor mengklik tombol "Logout" di dashboard
  2. Sistem menghapus session/token di server
  3. Contributor di-redirect ke halaman Login
- **Post-condition:** Contributor berhasil login/logout, session dikelola dengan aman

#### FR-14: Request Password Reset via Email
- **Pre-condition:** Contributor lupa password dan berada di halaman "Forgot Password"
- **Main Flow:**
  1. Contributor memasukkan email mereka di form
  2. Sistem memvalidasi apakah email terdaftar di database
  3. Jika terdaftar, sistem membuat token reset yang unik dengan masa berlaku 24 jam
  4. Sistem mengirim email dengan link reset: `https://app.com/reset-password?token=...`
  5. Contributor menerima email dan mengklik link
  6. Sistem memvalidasi token dan menampilkan form untuk memasukkan password baru
  7. Contributor memasukkan password baru dan mengklik "Reset"
  8. Sistem memvalidasi password baru dan menyimpannya (hashed)
  9. Sistem menampilkan pesan "Password reset successful, redirecting to login..."
- **Post-condition:** Password contributor berhasil direset, dapat login dengan password baru

#### FR-15: Moderate Wallpapers
- **Pre-condition:** Admin telah login dan berada di halaman Admin Dashboard
- **Main Flow:**
  1. Admin navigasi ke "Moderation Queue"
  2. Sistem menampilkan daftar wallpaper dengan status "Pending" untuk di-review
  3. Admin mengklik salah satu wallpaper untuk melihat preview dan detail (contributor, upload date, description)
  4. Admin melakukan inspeksi visual dan konten wallpaper
  5. Admin memilih aksi: "Approve" atau "Reject"
  6. Jika reject, admin wajib mengisi alasan penolakan
  7. Admin mengklik "Submit"
  8. Sistem menyimpan keputusan dan mengubah status wallpaper
  9. Sistem mengirim notifikasi email ke contributor tentang hasil review
- **Post-condition:** Wallpaper di-approve atau di-reject dengan alasan yang jelas, contributor diberitahu

#### FR-16: Limit Contributor Uploads
- **Pre-condition:** Admin berada di halaman "Contributor Management"
- **Main Flow:**
  1. Admin melihat daftar semua contributor dengan statistik upload mereka
  2. Admin dapat mengatur limit upload per contributor (mis: max 5 wallpaper per minggu)
  3. Jika contributor mencapai limit, sistem secara otomatis menolak upload wallpaper baru sampai periode reset
  4. Admin menerima laporan upload statistics secara berkala
- **Post-condition:** Limit upload diterapkan dan dimonitor oleh sistem

#### FR-17: Manage Admin Session via Internal Portal
- **Pre-condition:** Admin user memiliki kredensial akses ke internal portal
- **Main Flow:**
  1. Admin navigasi ke halaman internal portal login
  2. Admin memasukkan username dan password khusus admin
  3. Sistem melakukan validasi credentials dengan role "Admin"
  4. Sistem membuat secure session dengan encryption dan IP logging
  5. Admin di-redirect ke Admin Dashboard
  6. Ketika selesai, admin mengklik "Logout"
  7. Sistem menghapus session dan mencatat waktu logout
- **Post-condition:** Admin session dibuat dan dikelola dengan keamanan tinggi di internal portal

---

## 4. Kebutuhan Non-Fungsional (Non-Functional Requirements)

### Ringkasan NFR

| ID | Kategori | Deskripsi | Metrik Terukur | Prioritas |
|----|----------|-----------|----------------|-----------|
| NFR-01 | Performance | Page load time | ≤ 3 detik (FCP) | High |
| NFR-02 | Performance | API response time | ≤ 500ms (P95) | High |
| NFR-03 | Performance | Database query time | ≤ 100ms | High |
| NFR-04 | Security | Password encryption | bcrypt (cost ≥ 10) | High |
| NFR-05 | Security | HTTPS/TLS | TLS 1.2+ | High |
| NFR-06 | Security | Access control | RBAC + 2FA | High |
| NFR-07 | Reliability | System uptime | 99.5% per bulan | High |
| NFR-08 | Reliability | Error handling | Log + Alert < 5 min | High |
| NFR-09 | Usability | Responsive design | Mobile/Tablet/Desktop | High |
| NFR-10 | Usability | UI consistency | Design system 100% | Medium |
| NFR-11 | Usability | Accessibility | WCAG 2.1 Level AA | Medium |
| NFR-12 | Maintainability | Code quality | SonarQube Grade A | Medium |

---

### Detail Kebutuhan Non-Fungsional

#### NFR-01 (Performance - Page Load Time)
- **Kategori:** Performance
- **Deskripsi:** Halaman web utama harus termuat dan interaktif dalam waktu maksimal 3 detik pada koneksi 4G standard
- **Metrik Terukur:**
  - Time to First Byte (TTFB): ≤ 600ms
  - First Contentful Paint (FCP): ≤ 1.5 detik
  - Largest Contentful Paint (LCP): ≤ 2.5 detik
  - Cumulative Layout Shift (CLS): ≤ 0.1
- **Target Compliance:** 95% dari semua pageview
- **Metode Verifikasi:**
  - Browser DevTools Performance
  - Google PageSpeed Insights
  - WebPageTest
  - Synthetic monitoring tools
- **Prioritas:** High
- **Catatan:** Mengikuti Core Web Vitals dari Google

#### NFR-02 (Performance - Response Time API)
- **Kategori:** Performance
- **Deskripsi:** API endpoint harus merespons dalam waktu maksimal 500ms untuk request normal
- **Metrik Terukur:**
  - Rata-rata response time: ≤ 200ms
  - P95 response time: ≤ 500ms
  - P99 response time: ≤ 1000ms
- **Target Compliance:** 99% dari semua API request
- **Metode Verifikasi:**
  - Load testing dengan Apache JMeter / Gatling
  - APM tools (New Relic, DataDog, Grafana)
  - Real User Monitoring (RUM)
- **Prioritas:** High
- **Catatan:** Diukur dari server response, tidak termasuk network latency

#### NFR-03 (Performance - Database Query)
- **Kategori:** Performance
- **Deskripsi:** Query database untuk transaksi normal harus selesai dalam 100ms
- **Metrik Terukur:**
  - Query execution time: ≤ 100ms
  - Database connection pool: min 10, max 50 connections
  - Slow query threshold: > 1000ms akan di-log
- **Target Compliance:** 99% dari semua queries
- **Metode Verifikasi:**
  - Database slow query logs
  - Query profiling tools
  - Performance schema (MySQL) atau pg_stat_statements (PostgreSQL)
- **Prioritas:** High
- **Catatan:** Termasuk index optimization review secara berkala

---

### 4.2 Keamanan (Security)

#### NFR-04 (Security - Password Encryption)
- **Kategori:** Security
- **Deskripsi:** Semua password pengguna harus dienkripsi menggunakan algoritma hashing yang aman
- **Metrik Terukur:**
  - Algoritma: bcrypt dengan cost factor ≥ 10 (atau PBKDF2, Argon2)
  - Salt harus unik untuk setiap password
  - Password minimum 8 karakter dengan kombinasi huruf, angka, dan simbol
  - Password tidak boleh disimpan dalam plaintext di log atau cache
- **Target Compliance:** 100% dari semua password yang disimpan
- **Metode Verifikasi:**
  - Code review oleh security team
  - SAST (Static Application Security Testing) tools
  - Penetration testing
  - Password audit tools
- **Prioritas:** High
- **Catatan:** Mengikuti OWASP Password Storage Cheat Sheet

#### NFR-05 (Security - Data Encryption in Transit)
- **Kategori:** Security
- **Deskripsi:** Semua data yang ditransmisikan harus dienkripsi menggunakan HTTPS/TLS
- **Metrik Terukur:**
  - Protokol: TLS 1.2 minimum (TLS 1.3 lebih baik)
  - Certificate: valid, signed oleh trusted CA
  - Cipher suite: hanya strong ciphers (minimal 128-bit encryption)
  - HSTS header: max-age ≥ 31536000 detik (1 tahun)
  - Certificate pinning: untuk critical endpoints (opsional)
- **Target Compliance:** 100% dari semua request/response
- **Metode Verifikasi:**
  - SSL/TLS analyzer (SSL Labs, Nessus)
  - Browser developer tools
  - Network packet inspection (Wireshark)
  - Automated security scanning
- **Prioritas:** High
- **Catatan:** Tidak ada downgrade atau fallback ke HTTP

#### NFR-06 (Security - Access Control)
- **Kategori:** Security
- **Deskripsi:** Sistem harus menerapkan kontrol akses berbasis peran (Role-Based Access Control)
- **Metrik Terukur:**
  - Setiap pengguna harus memiliki role yang jelas (user, admin, moderator, dll)
  - Setiap fungsi harus dilindungi dengan authorization check
  - Session timeout: ≤ 30 menit inactivity
  - Password change enforcement: setiap 90 hari untuk admin
  - Failed login attempt: max 5x dalam 5 menit (lock account 15 menit)
  - 2FA (Two-Factor Authentication): wajib untuk admin, opsional untuk user
- **Target Compliance:** 100% dari semua protected resources
- **Metode Verifikasi:**
  - Code review untuk authorization logic
  - Penetration testing dengan privilege escalation attempts
  - Access control matrix verification
  - Automated security testing
- **Prioritas:** High
- **Catatan:** Mengikuti OWASP Authentication Cheat Sheet

---

### 4.3 Keandalan (Reliability)

#### NFR-07 (Reliability - System Uptime)
- **Kategori:** Reliability
- **Deskripsi:** Sistem harus memiliki ketersediaan (uptime) yang tinggi
- **Metrik Terukur:**
  - Target uptime: 99.5% dalam periode 1 bulan
  - Planned downtime: maksimal 4 jam per bulan untuk maintenance
  - Unplanned downtime: maksimal 72 jam per tahun
  - Recovery Time Objective (RTO): ≤ 15 menit
  - Recovery Point Objective (RPO): ≤ 1 jam
- **Target Compliance:** 99.5% availability di production
- **Metode Verifikasi:**
  - Uptime monitoring tools (Prometheus, Grafana, Datadog)
  - Health check endpoints
  - Synthetic monitoring
  - Incident log review
- **Prioritas:** High
- **Catatan:** 99.5% uptime = max 21.6 jam downtime per tahun

#### NFR-08 (Reliability - Error Handling & Logging)
- **Kategori:** Reliability
- **Deskripsi:** Sistem harus menangani error dengan baik dan mencatat semua aktivitas penting
- **Metrik Terukur:**
  - Semua error harus di-catch dan di-log dengan detail (timestamp, user, action, error message)
  - Error response harus user-friendly tanpa expose sensitive information
  - Log retention: minimal 90 hari untuk audit
  - Log rotation: daily dengan compression
  - Error rate threshold: > 5% akan trigger alert
  - Critical errors harus di-alert dalam 5 menit
- **Target Compliance:** 100% dari semua error events
- **Metode Verifikasi:**
  - Log review dan analysis
  - Automated alerting testing
  - Error tracking tools (Sentry, Rollbar, DataDog)
  - Log aggregation verification (ELK Stack, Splunk)
- **Prioritas:** High
- **Catatan:** Termasuk database errors, API errors, dan application errors

---

### 4.4 Usability (Kegunaan/Kemudahan Penggunaan)

#### NFR-09 (Usability - Responsive Design)
- **Deskripsi:** Responsif di Desktop (1920x1080), Tablet (768x1024), Mobile (375x667)
- **Metrik:** Touch targets ≥ 44x44px, Font ≥ 14px body, Contrast 4.5:1, Load ≤ 5s pada mobile 4G
- **Metode Verifikasi:** BrowserStack, Axe DevTools, Manual testing

#### NFR-10 (Usability - UI Consistency)
- **Deskripsi:** Design system coverage 100% untuk common components
- **Metrik:** Max 5 primary colors, Max 3 font families, 8px grid system, Design doc up-to-date
- **Metode Verifikasi:** Design review, Visual regression testing, Component library audit

#### NFR-11 (Usability - Accessibility)
- **Deskripsi:** WCAG 2.1 Level AA compliance
- **Metrik:** Semantic HTML, ARIA labels, Keyboard navigation, Screen reader compatible
- **Metode Verifikasi:** Axe, Lighthouse, Screen reader testing, Keyboard navigation test

#### NFR-12 (Maintainability - Code Quality)
- **Deskripsi:** Source code berkualitas tinggi dengan standar industri
- **Metrik:** Cyclomatic complexity ≤ 10, Code duplication < 5%, Test coverage ≥ 80% backend/70% frontend, SonarQube Grade A
- **Metode Verifikasi:** SonarQube, Code coverage tools, Linting tools, Code review (2 approvals)

---

## 5. Traceability Matrix

### 5.1 FR ke User Story ke Acceptance Criteria

#### FR-01 ↔ US-01
**Functional Requirement:** FR-01 - [Nama Fitur]

**User Story:**
```
US-01: Sebagai [tipe user], saya ingin [action] 
       sehingga [benefit/value]

Acceptance Criteria:
✓ AC-01.1: [Kondisi spesifik yang harus terpenuhi]
✓ AC-01.2: [Kondisi spesifik yang harus terpenuhi]
✓ AC-01.3: [Kondisi spesifik yang harus terpenuhi]
```

**Mapping:**
| Element | Referensi | Deskripsi |
|---------|-----------|-----------|
| FR | FR-01 | [Deskripsi FR] |
| US | US-01 | [Deskripsi US] |
| AC | AC-01.1, AC-01.2, AC-01.3 | [Daftar AC] |

---

#### FR-02 ↔ US-02
**Functional Requirement:** FR-02 - [Nama Fitur]

**User Story:**
```
US-02: Sebagai [tipe user], saya ingin [action] 
       sehingga [benefit/value]

Acceptance Criteria:
✓ AC-02.1: [Kondisi spesifik yang harus terpenuhi]
✓ AC-02.2: [Kondisi spesifik yang harus terpenuhi]
✓ AC-02.3: [Kondisi spesifik yang harus terpenuhi]
```

**Mapping:**
| Element | Referensi | Deskripsi |
|---------|-----------|-----------|
| FR | FR-02 | [Deskripsi FR] |
| US | US-02 | [Deskripsi US] |
| AC | AC-02.1, AC-02.2, AC-02.3 | [Daftar AC] |

---

#### [Tambahkan untuk FR-03, FR-04, FR-05, dst...]

---

### 5.2 Ringkasan Traceability

| FR | US | AC | Status |
|----|----|----|--------|
| FR-01 | US-01 | AC-01.1, AC-01.2, AC-01.3 | ✓ Lengkap |
| FR-02 | US-02 | AC-02.1, AC-02.2, AC-02.3 | ✓ Lengkap |
| FR-03 | US-03 | AC-03.1, AC-03.2, AC-03.3 | ✓ Lengkap |
| FR-04 | US-04 | AC-04.1, AC-04.2, AC-04.3 | ✓ Lengkap |
| FR-05 | US-05 | AC-05.1, AC-05.2, AC-05.3 | ✓ Lengkap |
| **TOTAL** | **5** | **15** | **✓ 100%** |

---

## 6. Catatan dan Asumsi

### 6.1 Asumsi

| # | Kategori | Asumsi |
|---|----------|--------|
| 1 | Teknis | Infrastruktur cloud sudah tersedia (AWS/GCP/Azure) |
| 2 | Teknis | Database server dengan proper backup sudah dikonfigurasi |
| 3 | Teknis | Network connectivity selalu tersedia di production |
| 4 | Teknis | Server dapat handle [X] request/detik peak traffic |
| 5 | Organisasi | Tim: [X] developer dengan skill level [minimum/medium/expert] |
| 6 | Organisasi | Timeline: [X] minggu development + [X] minggu testing |
| 8 | User/Business | Data volume: [X GB] dalam 1 tahun |

### 6.2 Dependensi

| # | Tipe | Dependensi | Deskripsi |
|---|------|-----------|-----------|
| 1 | External | [Nama Library] v[version] | [Purpose] |
| 2 | External | [Nama Payment Gateway] | Integrasi pembayaran |
| 3 | External | [Nama Email Service] | Notifikasi email |
| 4 | Internal | [Legacy System] | Data migration |
| 5 | Internal | [Team/Resource] | [Approval/Resource needed] |

### 6.3 Batasan & Constraints

| Kategori | Batasan |
|----------|---------|
| **Database** | [Technology: PostgreSQL/MySQL/MongoDB] |
| **Backend** | PHP |
| **Frontend** | Desktop: Java; Web: HTML, CSS, JavaScript |
| **Browser Support** | Chrome 90+, Firefox 88+, Safari 14+, Edge 90+ |
| **File Upload** | Max [100 MB] |
| **[Kategori Lain]** | [Batasan lain] |

---

**[Kembali ke atas](#-daftar-isi-table-of-contents)**