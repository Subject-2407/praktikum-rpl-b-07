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

Dokumen _Software Requirements Specification_ (SRS) ini bertujuan untuk membantu pendefinisian kebutuhan fungsional dan non-fungsional dari aplikasi **Scapes**, sebuah aplikasi dekstop untuk mencari, mengelola, dan menerapkan wallpaper dengan efisien, secara lengkap. Dokumen ini menjadi acuan utama bagi pengembang, desainer, dan stakeholder dalam memahami ruang lingkup sistem, fitur yang akan dikembangkan, dan batasan-batasan yang ada.

Selain itu, SRS ini juga berfungsi sebagai dasar dalam proses pengembangan, pengujian _(testing)_, dan evaluasi sistem, sehingga aplikasi yang dibuat dapat sesuai dengan kebutuhan dan tujuan yang ditentukan.

### 1.2 Ruang Lingkup

**Nama Produk:** Scapes

**Deskripsi Singkat:**  
Scapes merupakan aplikasi dekstop yang dirancang untuk memberi bantuan kepada pengguna dalam pencarian, pengunduhan, pengelolaan, dan penerapan wallpaper secara cepat dan efisien tanpa harus melakukan pengaturan manual melalui sistem operasi.

Pengguna utama aplikasi ini terdiri dari:
- User (pengguna umum): orang yang ingin mencari dan menerapkan wallpaper dengan mudah
- Contributor: orang yang ingin mengunggah dan membagikan wallpaper
- Admin: orang yang bertugas melakukan moderasi konten

Beberapa nilai utama yang diberikan oleh aplikasi ini adalah: menghemat waktu dalam proses pencarian dan pemasangan wallpaper, mengurangi penumpukan file _(hoarding)_ dengan sistem pengelolaan otomatis, serta memberikan pengalaman kustomisasi dekstop yang lebih praktis dan terorganisir.

**Fitur Utama yang Akan Dikembangkan:**
- Pencarian wallpaper berdasarkan kata kunci
- Penerapan wallpaper dengan satu kali tekan
- Otomatisasi penyimpanan dan pengelolaan wallpaper
- Pemilihan sumber wallpaper
- Sistem upload wallpaper oleh contributor
- Pelacakan status moderasi wallpaper
- Sistem edit dan hapus wallpaper untuk contributor
- Sistem autentikasi _(register, login, logout)_
- Sistem moderasi wallpaper oleh admin 

**Fitur Yang TIDAK Termasuk Dalam Scope:**
- Penjadwalan publikasi wallpaper oleh Contributor
- Pembatasan jumlah upload bagi contributor oleh admin
- Fitur sosial _(like, comment, follow)_

### 1.3 Definisi dan Akronim

| Istilah | Definisi |
|---------|----------|
| **User** | Pengguna akhir yang berinteraksi dengan sistem |
| **Admin** | Pengguna dengan hak akses penuh untuk manajemen sistem |
| **API** | Application Programming Interface - antarmuka untuk integrasi |
| **API key** | Kunci unik untuk mengakses layanan API dengan batas penggunaan tertentu|
| **Database** | Sistem penyimpanan data terstruktur |
| **Authentication** | Proses verifikasi identitas pengguna |
| **Authorization** | Proses penentuan akses berdasarkan peran pengguna |
| **Response Time** | Waktu yang dibutuhkan sistem merespons permintaan |
| **Uptime** | Persentase waktu sistem tersedia dan berfungsi |
| **UI/UX** | User Interface dan User Experience |
| **CRUD (Create, Read, Update, Delete)** | Operasi dasar dalam pengelolaan data |
| **Hoarding** | Kebiasaan menyimpan banyak file tanpa pengelolaan yang baik sehingga terjadi penumpukan folder |
| **Rate Limit** | Batas jumlah permintaan yang dapat dilakukan ke suatu API dalam periode waktu tertentu |
| **WCAG (Web Content Accessibility Guidelines)** | Panduan pembuatan UI/UX yang ramah untuk semua pengguna |
| **Uptime** | Persentase waktu sistem dalam kondisi aktif dan dapat digunakan |
| **JWT (JSON Web Token)** | Token keamanan yang digunakan untuk autentikasi dan manajemen session |

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

| ID | Nama Fitur | Deskripsi | Prioritas | US Ref | Actor |
|-----|-----------|-----------|-----------|--------|-------|
| FR-01 | [Nama Fitur 1] | [Deskripsi detail tentang apa yang harus dilakukan sistem] | High | US-01 | [Tipe User] |
| FR-02 | [Nama Fitur 2] | [Deskripsi detail tentang apa yang harus dilakukan sistem] | Medium | US-02 | [Tipe User] |
| FR-03 | [Nama Fitur 3] | [Deskripsi detail tentang apa yang harus dilakukan sistem] | Medium | US-03 | [Tipe User] |
| FR-04 | [Nama Fitur 4] | [Deskripsi detail tentang apa yang harus dilakukan sistem] | High | US-04 | [Tipe User] |
| FR-05 | [Nama Fitur 5] | [Deskripsi detail tentang apa yang harus dilakukan sistem] | Low | US-05 | [Tipe User] |

### Detail Kebutuhan Fungsional

#### FR-01: [Nama Fitur]
- **Pre-condition:** [Kondisi awal yang harus terpenuhi]
- **Main Flow:** 
  1. [Step 1]
  2. [Step 2]
  3. [Step 3]
- **Post-condition:** [Kondisi akhir setelah fungsi selesai]

#### FR-02: [Nama Fitur]
- **Pre-condition:** [Kondisi awal yang harus terpenuhi]
- **Main Flow:** 
  1. [Step 1]
  2. [Step 2]
  3. [Step 3]
- **Post-condition:** [Kondisi akhir setelah fungsi selesai]

#### [Tambahkan untuk FR-03, FR-04, FR-05, dst...]

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
