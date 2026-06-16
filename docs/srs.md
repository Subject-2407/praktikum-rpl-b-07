# Software Requirements Specification (SRS)
## Scapes

**Versi:** 1.1  
**Tanggal:** 05-06-2026  
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
  - [4.1 Performance](#41-performance)
  - [4.2 Security](#42-security)
  - [4.3 Usability](#43-usability)
  - [4.4 Reliability](#44-reliability)
  - [4.5 Maintainability](#45-maintainability)
- [5. Traceability Matrix](#5-traceability-matrix)
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
- Search analytics untuk mencatat kata kunci pencarian user
- Trending categories dan search recommendations berdasarkan agregasi pencarian

**Fitur Yang TIDAK Termasuk Dalam Scope:**
- Penjadwalan publikasi wallpaper oleh Contributor
- Pembatasan jumlah upload bagi contributor oleh admin
- Fitur sosial _(like, comment, follow)_
- Model machine learning kompleks untuk personalisasi individual pada fase awal; MVP menggunakan agregasi statistik dan rule-based recommendation terlebih dahulu

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
| **Hoarding** | Kebiasaan menyimpan banyak file tanpa pengelolaan yang baik sehingga terjadi penumpukan folder |
| **Rate Limit** | Batas jumlah permintaan yang dapat dilakukan ke suatu API dalam periode waktu tertentu |
| **WCAG (Web Content Accessibility Guidelines)** | Panduan pembuatan UI/UX yang ramah untuk semua pengguna |
| **JWT (JSON Web Token)** | Token keamanan yang digunakan untuk autentikasi dan manajemen session |
| **Stress Testing** | Pengujian sistem di kondisi ekstrem untuk mengetahui batasnya |
| **Heap Dump** | Snapshot memori aplikasi untuk analisis penggunaan memori |
| **Clean Architecture** | Pendekatan desain software yang memisahkan layer agar mudah maintain |
| **CDN (Content Delivery Network)** | Jaringan server global untuk mendistribusikan konten lebih cepat ke pengguna |
| **MTTR (Mean Time to Recovery)** | Rata-rata waktu yang dibutuhkan untuk memulihkan sistem setelah gagal |
| **Certificate Pinning** | Teknik keamanan untuk memastikan aplikasi hanya menerima sertifikat tertentu |
| **bcrypt** | Algoritma hashing password yang aman dan tahan terhadap brute-force attack |
| **Search Analytics** | Proses pencatatan dan analisis kata kunci pencarian user untuk memahami tren pencarian wallpaper |
| **Trending Category** | Kategori rekomendasi yang sedang populer berdasarkan kata kunci pencarian user dalam periode tertentu, misalnya harian. Item ini bisa berasal dari kategori bawaan sistem atau kategori dinamis hasil agregasi keyword user |
| **System Category** | Kategori bawaan yang sudah tersimpan di tabel `categories`, misalnya `Minimalist`, `Nature`, atau `Abstract` |
| **User-Keyword Category** | Kategori dinamis hasil agregasi kata kunci pencarian user, misalnya `beach`, `city`, atau `forest`, yang belum tentu ada di daftar kategori bawaan |
| **Search Recommender** | Fitur yang menyarankan kata kunci atau kategori berdasarkan data pencarian historis, metadata wallpaper, dan daftar kategori bawaan sistem |
| **Tag** | Label bebas untuk mendeskripsikan wallpaper, ditulis dengan awalan `#` pada form contributor dan search user, misalnya `#colorful` |
| **Tag Proposal** | Usulan tag baru dari contributor yang belum masuk tabel `tags` sampai wallpaper terkait di-approve oleh admin |
| **Tag Suggestion** | Daftar tag resmi yang mendekati input tag contributor atau query tag user, digunakan agar user dapat memilih tag yang sudah ada sebelum membuat tag baru |
| **ML (Machine Learning)** | Pendekatan pemodelan data untuk menghasilkan rekomendasi. Pada MVP Scapes, ML belum wajib; data logging disiapkan agar sistem dapat berkembang dari rule-based ke ML saat data cukup |

---

## 2. Deskripsi Umum

### 2.1 Perspektif Produk

**Konteks Sistem:**
- Backend API: PHP
- Frontend Desktop: Kotlin Multiplatform
- Frontend Web: HTML, CSS, JavaScript
- Database: PostgreSQL/MySQL/MongoDB
- Integrasi Eksternal: Pexels, Unsplash, Pixabay

### 2.2 Fungsi Produk

| No | Fungsi | Deskripsi |
|----|--------|-----------|
| 1 | Wallpaper search & discovery | Sistem memungkinkan pengguna mencari dan menampilkan wallpaper berdasarkan kata kunci, serta mengganti sumber wallpaper dari berbagai API yang tersedia. |
| 2 | Wallpaper management & application | Sistem memungkinnkan pengguna mengunduh, menyimpan, mengorganisir, dan menerapkan wallpaper pada perangkat secara langsung. |
| 3 | System configuration & settings | Sistem menyediakan pengaturan seperti lokasi penyimpanan wallpaper dan konfigurasi API key untuk integrasi dengan layanan eksternal. |
| 4 | Content management | Contributor dapat mengunggah, mengedit, dan menghapus wallpaper. |
| 5 | Content moderation & control | Admin bertanggung jwab untuk melakukan validasi dan moderasi (approve atau reject) terhadap konten wallpaper yang diunggah oleh contributor. |
| 6 | User account & authentication management | Sistem menyediakan fitur registrasi, login, logout, dan reset password untuk pengguna terdaftar. |
| 7 | Search analytics & recommendation | Sistem mencatat pencarian user, menghitung kategori user-keyword trending harian, tetap menyertakan kategori bawaan, dan menyediakan rekomendasi kategori atau keyword untuk frontend user. |

### 2.3 Karakteristik Pengguna

| Tipe User | Tingkat Keahlian | Frekuensi | Kebutuhan |
|-----------|------------------|-----------|-----------|
| **User Umum** | Pemula-Menengah | Setiap hari | UI intuitif, mudah dipelajari. |
| **Contributor** | Menengah | Berkala | Mudah mengunggah konten, tracking status, dan manajemen konten. |
| **Admin** | Ahli | Setiap hari |Moderasi dan kontrol penuh terhadap konten. | 

**Kompatibilitas:**
- Browser: Chrome, Firefox, Safari, Edge (latest versions)
- Device: Desktop (Windows), Tablet, Mobile
- Aksesibilitas: WCAG 2.1 Level AA (jika diperlukan)

### 2.4 Batasan dan Kendala

| Kategori | Batasan |
|----------|---------|
| **Teknis** | Infrastruktur: berbasis pada cloud atau server local, Storage: kapasitas penyimpanan terbatas untuk file wallpaper yang diunggah oleh contributor, sehingga diperlukan manajemen penyimpanan file seperti limitasi ukuran file, Bandwidth: performa sistem bergantung dengan koneksi pengguna  dan API eksternal. |
| **Operasional** | Ketersediaan sistem: sistem dirancang untuk berjalan 24/7, namun dapat mengalami downtime saat maintenance atau gangguan server, Maintenance window: pemeliharaan sistem dilakukan secara berkala untuk update, backup, dan perbaikan bug. |
| **Regulasi** | Compliance: mengikuti kebijakan API pihak ketiga, Data protection: perlindungan data pengguna sesuai dengan standar keamanan dasar. |

---

## 3. Kebutuhan Fungsional (Functional Requirements)

### Ringkasan Kebutuhan Fungsional

| ID | Nama Fitur | Deskripsi | Prioritas | US Ref | Actor |
|-----|-----------|-----------|-----------|--------|-------|
| [**FR-01**](#fr-01) | Search Wallpapers by Keyword | User dapat mencari wallpaper berdasarkan kata kunci untuk menemukan tema spesifik dengan cepat | High | US-01 | User |
| [**FR-02**](#fr-02) | Set Wallpaper dengan One Click | User dapat mengatur wallpaper dengan satu klik, sistem secara otomatis mengunduh dan menerapkan wallpaper | High | US-02 | User |
| [**FR-03**](#fr-03) | Auto Save & Organize Wallpapers | Sistem otomatis menyimpan dan mengorganisir wallpaper yang diunduh ke dalam folder terstruktur | High | US-03 | User |
| [**FR-04**](#fr-04) | Customize Download Folder Location | User dapat mengubah lokasi folder download untuk menyimpan wallpaper di lokasi yang diinginkan | Medium | US-04 | User |
| [**FR-05**](#fr-05) | Switch Between Wallpaper Sources | User dapat beralih antara sumber wallpaper berbeda (Pexels, Unsplash, Pixabay, dll) | Medium | US-05 | User |
| [**FR-06**](#fr-06) | Input Personal API Keys | User dapat memasukkan API key pribadi untuk provider wallpaper guna meningkatkan rate limit | Low | US-06 | User |
| [**FR-07**](#fr-07) | Upload Wallpapers | Contributor dapat mengunggah wallpaper mereka untuk dibagikan dengan pengguna lain | High | CO-01 | Contributor |
| [**FR-08**](#fr-08) | Track Moderation Status | Contributor dapat melacak status moderasi wallpaper yang telah diunggah (Pending/Approved/Rejected) | Medium | CO-02 | Contributor |
| [**FR-09**](#fr-09) | Delete Wallpapers | Contributor dapat menghapus wallpaper yang telah diunggah | Medium | CO-03 | Contributor |
| [**FR-10**](#fr-10) | Edit Wallpaper Details | Contributor dapat mengedit detail wallpaper seperti deskripsi, kategori, tags, dan judul | Low | CO-04 | Contributor |
| [**FR-11**](#fr-11) | Schedule Wallpaper Publication | Contributor dapat menjadwalkan publikasi wallpaper pada waktu tertentu | Low | CO-05 | Contributor |
| [**FR-12**](#fr-12) | Register Account with Email | Contributor dapat mendaftar akun menggunakan email untuk mulai mengunggah wallpaper | High | CO-06 | Contributor |
| [**FR-13**](#fr-13) | Manage Session (Login/Logout) | Contributor dapat mengelola sesi dengan login dan logout dengan aman | High | CO-07 | Contributor |
| [**FR-14**](#fr-14) | Request Password Reset via Email | Contributor dapat meminta link reset password melalui email untuk pemulihan akun | High | CO-08 | Contributor |
| [**FR-15**](#fr-15) | Moderate Wallpapers | Admin dapat menyetujui atau menolak wallpaper yang telah diunggah oleh contributor | High | AD-01 | Admin |
| [**FR-16**](#fr-16) | Limit Contributor Uploads | Admin dapat membatasi jumlah upload per contributor untuk kontrol kualitas | Low | AD-02 | Admin |
| [**FR-17**](#fr-17) | Manage Admin Session via Internal Portal | Admin dapat mengelola sesi login/logout melalui portal internal dengan akses aman | High | AD-03 | Admin |
| [**FR-18**](#fr-18) | Log Search Keywords | Sistem mencatat kata kunci pencarian user dari frontend untuk bahan agregasi dan rekomendasi | High | US-08 | System |
| [**FR-19**](#fr-19) | Generate User-Keyword Trending Categories | Sistem menghitung kategori trending harian dari keyword user, termasuk keyword yang belum menjadi kategori bawaan | Medium | US-07, US-08 | System |
| [**FR-20**](#fr-20) | Provide Search Recommendations | Frontend user dapat mengambil rekomendasi keyword/kategori dari Scapes API dengan prioritas kategori bawaan saat query mendekati nama kategori | Medium | US-07 | User |

---

### Detail Kebutuhan Fungsional

#### FR-01: Search Wallpapers by Keyword <a name="fr-01"></a>
- **Pre-condition:** User telah membuka aplikasi dan berada di halaman Search/Gallery
- **Main Flow:** 
  1. User memasukkan kata kunci di kolom pencarian (mis: "Minimalist")
  2. User menekan tombol Enter atau tombol Search
  3. Sistem melakukan filter pada database wallpaper berdasarkan kata kunci
  4. Sistem menampilkan hasil wallpaper yang sesuai dengan tag atau kategori yang cocok
- **Tag Search Flow:**
  1. User mengetik query dengan awalan `#`, misalnya `#color`
  2. Frontend memeriksa source aktif
  3. Jika source aktif adalah `scapes`, frontend meminta rekomendasi/search suggestion ke Scapes API dalam mode tag
  4. Backend mencari tag yang sudah ada di tabel `tags` dengan prefix atau fuzzy match ringan, misalnya `#colorful` atau `#colorless`
  5. Jika tag ditemukan, frontend menampilkan daftar tag suggestion dan user dapat memilih tag untuk filter wallpaper
  6. Jika source aktif bukan `scapes` atau tidak ada tag yang cocok, frontend fallback ke rekomendasi normal tanpa prioritas tag internal
- **Post-condition:** Galeri wallpaper ditampilkan dengan hasil pencarian yang relevan, atau pesan "No wallpapers found" jika tidak ada hasil

#### FR-02: Set Wallpaper dengan One Click <a name="fr-02"></a>
- **Pre-condition:** User telah membuka aplikasi dan melihat galeri wallpaper; ada koneksi internet
- **Main Flow:**
  1. User memilih wallpaper dari galeri dan mengklik tombol "Set as Wallpaper" atau ikon checkmark
  2. Sistem memulai proses download file wallpaper ke lokasi penyimpanan lokal
  3. Setelah download selesai, sistem secara otomatis memanggil OS API untuk mengubah desktop wallpaper
  4. Sistem menampilkan notifikasi toast "Wallpaper updated successfully"
- **Post-condition:** Desktop wallpaper berubah ke gambar yang dipilih, notifikasi konfirmasi ditampilkan

#### FR-03: Auto Save & Organize Wallpapers <a name="fr-03"></a>
- **Pre-condition:** User telah mengunduh wallpaper melalui FR-02
- **Main Flow:**
  1. Sistem menerima hasil download wallpaper
  2. Sistem otomatis menentukan kategori wallpaper berdasarkan metadata atau tag
  3. Sistem membuat struktur folder: `/Scapes/[category]/` jika belum ada
  4. Sistem menyimpan file wallpaper ke subfolder yang sesuai dengan kategorinya
  5. Sistem mencatat informasi file di database lokal (filename, path, category, download date)
- **Post-condition:** Wallpaper tersimpan terorganisir di folder kategori yang sesuai

#### FR-04: Customize Download Folder Location <a name="fr-04"></a>
- **Pre-condition:** User berada di halaman Settings
- **Main Flow:**
  1. User mengklik tombol "Change Download Location"
  2. Sistem membuka dialog file picker untuk memilih folder
  3. User memilih lokasi folder baru (mis: OneDrive, Dropbox, atau drive lain)
  4. Sistem memvalidasi akses write ke folder yang dipilih
  5. User mengklik "Confirm" dan sistem menyimpan path baru ke konfigurasi aplikasi
  6. Sistem menampilkan konfirmasi "Download location updated"
- **Post-condition:** Lokasi download folder berubah dan disimpan, wallpaper berikutnya akan diunduh ke lokasi baru

#### FR-05: Switch Between Wallpaper Sources <a name="fr-05"></a>
- **Pre-condition:** User berada di halaman Gallery dan multiple wallpaper sources telah dikonfigurasi
- **Main Flow:**
  1. User mengklik dropdown "Source" di bagian atas galeri
  2. Sistem menampilkan daftar sumber wallpaper yang tersedia (Pexels, Unsplash, Pixabay, Scapes API)
  3. User memilih sumber yang berbeda
  4. Sistem mengakses API dari sumber yang dipilih dengan credentials yang sesuai
  5. Sistem melakukan refresh galeri dan menampilkan wallpaper dari sumber baru
- **Post-condition:** Galeri wallpaper beralih menampilkan konten dari sumber yang baru dipilih

#### FR-06: Input Personal API Keys <a name="fr-06"></a>
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

#### FR-07: Upload Wallpapers <a name="fr-07"></a>
- **Pre-condition:** Contributor telah login dan memiliki file gambar untuk diunggah
- **Main Flow:**
  1. Contributor navigasi ke halaman "Submit Wallpaper" atau "My Uploads"
  2. Contributor mengklik tombol "Select Image" dan memilih file dari komputer
  3. Sistem melakukan validasi: format file (JPG, PNG, WebP), ukuran file (max 10MB), resolusi minimal
  4. Contributor mengisi detail wallpaper: judul, deskripsi, kategori, dan tags
  5. Saat contributor mengetik tag, frontend meminta daftar tag resmi yang mendekati input agar contributor dapat memilih tag existing
  6. Untuk tag baru, contributor mengetik tag dengan awalan `#` dan memisahkan banyak tag menggunakan spasi, misalnya `#dark #city #neon`
  7. Jika tidak ada tag existing yang cocok atau contributor tetap mempertahankan inputnya, sistem memperlakukan input itu sebagai tag proposal baru
  8. Sistem memvalidasi format tag, menormalisasi menjadi lowercase/slug, dan menyimpan tag baru sebagai tag proposal, bukan langsung ke tabel `tags`
  9. Contributor mengklik "Submit for Review"
  10. Sistem mengunggah file ke server dan membuat record dengan status "Pending"
  11. Sistem menampilkan notifikasi "Wallpaper submitted for review"
- **Post-condition:** Wallpaper tersimpan di server dengan status Pending, contributor dapat melihatnya di dashboard "My Uploads"

#### FR-08: Track Moderation Status <a name="fr-08"></a>
- **Pre-condition:** Contributor telah mengunggah wallpaper dan berada di halaman "My Uploads"
- **Main Flow:**
  1. Sistem menampilkan daftar semua wallpaper yang telah diunggah oleh contributor
  2. Setiap item menampilkan status saat ini: Pending, Approved, atau Rejected
  3. Sistem menampilkan timestamp kapan wallpaper di-review
  4. Jika status Rejected, contributor dapat mengklik item untuk melihat alasan penolakan
  5. Jika wallpaper pending selama > 3 hari, sistem menampilkan badge "Review in Progress"
- **Post-condition:** Contributor dapat memantau status moderation semua wallpaper mereka

#### FR-09: Delete Wallpapers <a name="fr-09"></a>
- **Pre-condition:** Contributor berada di halaman "My Uploads" dan melihat wallpaper mereka
- **Main Flow:**
  1. Contributor mengklik tombol "Delete" pada wallpaper yang ingin dihapus
  2. Sistem menampilkan dialog konfirmasi: "Are you sure you want to delete this wallpaper?"
  3. Contributor mengklik "Yes, Delete"
  4. Sistem menghapus file dari server dan menghapus record dari database
  5. Sistem menampilkan notifikasi "Wallpaper deleted successfully"
- **Post-condition:** Wallpaper dihapus dari sistem dan tidak lagi tersedia untuk pengguna

#### FR-10: Edit Wallpaper Details <a name="fr-10"></a>
- **Pre-condition:** Contributor memiliki wallpaper yang belum di-approve dan ingin mengedit detailnya
- **Main Flow:**
  1. Contributor mengklik tombol "Edit" pada wallpaper di halaman "My Uploads"
  2. Sistem menampilkan form edit dengan field: Title, Description, Category, Tags
  3. Contributor mengubah informasi yang diperlukan
  4. Saat contributor mengetik tag, frontend menampilkan tag suggestion resmi yang mendekati input
  5. Jika contributor menambah tag baru, setiap tag harus diawali `#` dan dipisahkan spasi
  6. Contributor mengklik "Save Changes"
  7. Sistem melakukan validasi input (tidak boleh kosong, karakter spesial)
  8. Sistem menyimpan perubahan metadata dan tag proposal selama wallpaper belum `approved`
  9. Sistem menampilkan notifikasi "Wallpaper updated successfully"
- **Post-condition:** Detail wallpaper tersimpan dengan informasi terbaru

#### FR-11: Schedule Wallpaper Publication <a name="fr-11"></a>
- **Pre-condition:** Contributor telah mengunggah wallpaper dan admin telah menyetujuinya, contributor berada di halaman edit wallpaper
- **Main Flow:**
  1. Contributor mengklik tombol "Schedule Publication"
  2. Sistem menampilkan date/time picker untuk memilih waktu publikasi
  3. Contributor memilih tanggal dan waktu publikasi yang diinginkan
  4. Contributor mengklik "Schedule"
  5. Sistem menyimpan waktu publikasi di database dan membuat scheduled task
  6. Pada waktu yang ditentukan, sistem secara otomatis mengubah status wallpaper menjadi "Published"
- **Post-condition:** Wallpaper dijadwalkan untuk publikasi otomatis pada waktu yang ditentukan

#### FR-12: Register Account with Email <a name="fr-12"></a>
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

#### FR-13: Manage Session (Login/Logout) <a name="fr-13"></a>
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

#### FR-14: Request Password Reset via Email <a name="fr-14"></a>
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

#### FR-15: Moderate Wallpapers <a name="fr-15"></a>
- **Pre-condition:** Admin telah login dan berada di halaman Admin Dashboard
- **Main Flow:**
  1. Admin navigasi ke "Moderation Queue"
  2. Sistem menampilkan daftar wallpaper dengan status "Pending" untuk di-review
  3. Admin mengklik salah satu wallpaper untuk melihat preview dan detail (contributor, upload date, description)
  4. Admin melakukan inspeksi visual dan konten wallpaper
  5. Admin memilih aksi: "Approve" atau "Reject"
  6. Jika reject, admin wajib mengisi alasan penolakan
  7. Jika approve, sistem memproses tag proposal: tag yang belum ada dimasukkan ke tabel `tags`, tag yang sudah ada dipakai ulang, lalu semuanya dihubungkan ke `wallpaper_tags`
  8. Admin mengklik "Submit"
  9. Sistem menyimpan keputusan dan mengubah status wallpaper
  10. Sistem mengirim notifikasi email ke contributor tentang hasil review
- **Post-condition:** Wallpaper di-approve atau di-reject dengan alasan yang jelas, contributor diberitahu

#### FR-16: Limit Contributor Uploads <a name="fr-16"></a>
- **Pre-condition:** Admin berada di halaman "Contributor Management"
- **Main Flow:**
  1. Admin melihat daftar semua contributor dengan statistik upload mereka
  2. Admin dapat mengatur limit upload per contributor (mis: max 5 wallpaper per minggu)
  3. Jika contributor mencapai limit, sistem secara otomatis menolak upload wallpaper baru sampai periode reset
  4. Admin menerima laporan upload statistics secara berkala
- **Post-condition:** Limit upload diterapkan dan dimonitor oleh sistem

#### FR-17: Manage Admin Session via Internal Portal <a name="fr-17"></a>
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

#### FR-18: Log Search Keywords <a name="fr-18"></a>
- **Pre-condition:** User melakukan pencarian wallpaper dari frontend user dan perangkat memiliki koneksi ke Scapes API
- **Main Flow:**
  1. User memasukkan keyword pencarian dan menjalankan search
  2. Frontend mengirim event pencarian ke Scapes API secara non-blocking
  3. Backend melakukan normalisasi keyword (trim, lowercase, slug-friendly, hapus karakter berbahaya)
  4. Backend menyimpan search event dengan timestamp, source yang dipilih, dan metadata minimal
  5. Backend mengembalikan status sukses tanpa memengaruhi hasil pencarian utama
- **Post-condition:** Search event tersimpan dan dapat digunakan untuk agregasi trending

#### FR-19: Generate User-Keyword Trending Categories <a name="fr-19"></a>
- **Pre-condition:** Search logs tersedia di database dan kategori/tag wallpaper sudah terdefinisi
- **Main Flow:**
  1. Sistem menjalankan aggregation job berkala, minimal satu kali per hari
  2. Sistem mengelompokkan keyword user menjadi user-keyword category, misalnya `beach`, `city`, atau `forest`
  3. Jika keyword cocok dengan kategori bawaan, sistem tetap menyimpan referensi ke kategori bawaan tersebut
  4. Sistem menghitung skor trending berdasarkan frekuensi pencarian, recency, dan deduplication sederhana
  5. Sistem menyimpan hasil agregasi per tanggal dengan penanda sumber kategori (`user_keyword` atau `system`)
  6. Sistem menyediakan top user-keyword categories melalui endpoint publik dengan default limit 10 dan limit dapat dikustomisasi
- **Post-condition:** Daftar user-keyword trending categories harian tersedia untuk frontend user dan dapat ditampilkan di atas kategori bawaan

#### FR-20: Provide Search Recommendations <a name="fr-20"></a>
- **Pre-condition:** Trending categories atau recommended keywords tersedia dari Scapes API
- **Main Flow:**
  1. User membuka Search tab atau mulai mengetik keyword
  2. Frontend meminta rekomendasi dari Scapes API
  3. Backend membaca `source_slug` dari request rekomendasi
  4. Jika `source_slug` bukan `scapes`, backend tidak menjalankan prioritas kategori bawaan maupun tag resmi internal dan langsung memakai rekomendasi normal
  5. Jika `source_slug = scapes` dan query diawali `#`, backend mencari tag yang sudah approved/tersimpan di tabel `tags`; tag proposal pending tidak ditampilkan
  6. Jika query `#` memiliki hasil tag, backend mengembalikan tag suggestion sebagai prioritas
  7. Jika query `#` tidak memiliki hasil tag, backend fallback ke rekomendasi normal
  8. Jika `source_slug = scapes` dan query user hampir cocok dengan kategori bawaan (exact, prefix, atau fuzzy match sederhana), backend menempatkan kategori bawaan tersebut di urutan teratas
  9. Jika user-keyword trending categories tersedia, backend mengembalikan kategori keyword user setelah kategori bawaan yang match
  10. Backend tetap menyertakan kategori bawaan sebagai fallback/bagian bawah daftar ketika tidak ada match khusus dan source aktif adalah `scapes`
  11. Frontend menampilkan rekomendasi sebagai chips/category/tag list yang dapat diklik
  12. Jika API gagal, frontend menggunakan fallback local default categories
- **Post-condition:** User melihat rekomendasi pencarian yang berasal dari data aktual, bukan kategori mock statis

---

## 4. Kebutuhan Non-Fungsional (Non-Functional Requirements)

### Ringkasan NFR

| ID | Aspek | Platform | Deskripsi | Metrik Terukur | Prioritas |
|----|-------|----------|-----------|----------------|-----------|
| [**NFR-01**](#nfr-01) | Performance | Desktop App | App loading time | ≤ 5 detik | High |
| [**NFR-02**](#nfr-02) | Performance | Desktop App | RAM consumption | ≤ 500 MB (normal) | High |
| [**NFR-03**](#nfr-03) | Performance | Desktop App | Search result loading (Scapes API) | ≤ 3 detik | High |
| [**NFR-04**](#nfr-04) | Performance | Desktop App | Wallpaper download & apply | ≤ 3 detik | High |
| [**NFR-05**](#nfr-05) | Performance | Web Portal | Web page load time | ≤ 3 detik (FCP) | High |
| [**NFR-06**](#nfr-06) | Performance | Web Portal | API response time | ≤ 500ms (P95) | High |
| [**NFR-07**](#nfr-07) | Security | Desktop App | Data transmission encryption | HTTPS/TLS 1.2+ | High |
| [**NFR-08**](#nfr-08) | Security | Desktop App | API Key masking | Masked display | High |
| [**NFR-09**](#nfr-09) | Security | Web Portal | Data transmission encryption | HTTPS/TLS 1.2+ | High |
| [**NFR-10**](#nfr-10) | Security | Web Portal | Password hashing | bcrypt (cost ≥ 12) | High |
| [**NFR-11**](#nfr-11) | Security | Web Portal | Access control | RBAC | High |
| [**NFR-12**](#nfr-12) | Usability | Desktop App | Responsive window resizing | Support all resolutions | High |
| [**NFR-13**](#nfr-13) | Usability | Desktop App | UI/UX quality | Modern & elegant | High |
| [**NFR-14**](#nfr-14) | Usability | Web Portal | Responsive design | Mobile/Tablet/Desktop | High |
| [**NFR-15**](#nfr-15) | Usability | Web Portal | UI/UX quality | Modern & elegant | High |
| [**NFR-16**](#nfr-16) | Reliability | Desktop App | Uptime (Scapes API) | Max 7 hours downtime | High |
| [**NFR-17**](#nfr-17) | Reliability | Web Portal | Uptime | Max 7 hours downtime | High |
| [**NFR-18**](#nfr-18) | Reliability | System | Error handling & logging | Comprehensive logging | High |
| [**NFR-19**](#nfr-19) | Maintainability | Desktop App | Code style | Kotlin Coding Conventions | High |
| [**NFR-20**](#nfr-20) | Maintainability | Desktop App | Architecture | Clean Architecture | High |
| [**NFR-21**](#nfr-21) | Maintainability | Web Portal | Code style | Google Style Guide (JS/PHP) | High |
| [**NFR-22**](#nfr-22) | Maintainability | Web Portal | Architecture | Clean Architecture | High |
| [**NFR-23**](#nfr-23) | Privacy | System | Search analytics privacy | No direct PII in search logs | High |
| [**NFR-24**](#nfr-24) | Performance | Scapes API | Recommendation response time | <= 500ms P95 | Medium |

---

### Detail Kebutuhan Non-Fungsional

---

## 4.1 Performance

### Desktop Application (Kotlin Multiplatform)

#### NFR-01: Application Loading Time <a name="nfr-01"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Aplikasi Scapes harus selesai loading dan siap digunakan dalam waktu maksimal 5 detik dari saat launch
- **Metrik Terukur:**
  - Time to launch: ≤ 5 detik
  - Time to main window visible: ≤ 3 detik
  - Initial data load: ≤ 2 detik
- **Target Compliance:** 100% dari semua launch
- **Metode Verifikasi:**
  - Profiling tools: JProfiler, YourKit
  - Manual stopwatch testing
  - Automated launch timing
- **Prioritas:** High
- **Catatan:** Optimasi startup time melalui lazy loading dan caching

#### NFR-02: Memory Consumption <a name="nfr-02"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Aplikasi tidak boleh mengonsumsi RAM lebih dari batas wajar untuk aplikasi desktop modern
- **Metrik Terukur:**
  - Idle memory: ≤ 150 MB
  - Normal operation: ≤ 300 MB
  - Peak usage (after search): ≤ 500 MB
  - No memory leaks: monitored dengan JVM profilers
- **Target Compliance:** 100% dari runtime
- **Metode Verifikasi:**
  - Memory profilers (JProfiler, YourKit, JVM profiling tools)
  - Task Manager monitoring
  - Heap dump analysis
  - Stress testing dengan long-running sessions
- **Prioritas:** High
- **Catatan:** Regular garbage collection dan memory optimization review

#### NFR-03: Search Result Loading (Scapes API) <a name="nfr-03"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Setelah user memasukkan kata kunci pencarian dan menekan Enter, hasil pencarian harus dimuat dan ditampilkan dalam waktu maksimal 3 detik
- **Metrik Terukur:**
  - API request time: ≤ 1 detik (from server)
  - Data parsing: ≤ 500ms
  - UI rendering: ≤ 1 detik
  - Total time: ≤ 3 detik
- **Target Compliance:** 95% dari semua search queries
- **Metode Verifikasi:**
  - Network profiling (Wireshark, Fiddler)
  - Application performance monitoring
  - Load testing dengan concurrent searches
- **Prioritas:** High
- **Catatan:** Implementasi pagination dan lazy loading untuk hasil besar

#### NFR-04: Wallpaper Download & Apply <a name="nfr-04"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Setelah user mengklik wallpaper, sistem harus mendownload file dan langsung menerapkannya ke desktop dalam waktu maksimal 3 detik (untuk file ukuran standar 1920x1080)
- **Metrik Terukur:**
  - File download: ≤ 2 detik (untuk file 5-10 MB pada fast internet)
  - Desktop wallpaper apply: ≤ 500ms
  - Total process: ≤ 3 detik
- **Target Compliance:** 95% dari semua apply operations
- **Metode Verifikasi:**
  - Network bandwidth monitoring
  - System call timing
  - E2E testing dengan various file sizes
- **Prioritas:** High
- **Catatan:** Optimasi: multi-threaded download, caching downloaded files

### Web Portal (HTML/CSS/Tailwind/jQuery/PHP)

#### NFR-05: Web Page Load Time <a name="nfr-05"></a>
- **Platform:** Web Portal (Contributor/Admin)
- **Deskripsi:** Halaman web portal harus termuat dan interaktif dalam waktu maksimal 3 detik
- **Metrik Terukur:**
  - Time to First Byte (TTFB): ≤ 600ms
  - First Contentful Paint (FCP): ≤ 1.2 detik
  - Largest Contentful Paint (LCP): ≤ 2 detik
  - Cumulative Layout Shift (CLS): ≤ 0.1
- **Target Compliance:** 95% dari semua pageview
- **Metode Verifikasi:**
  - Google PageSpeed Insights
  - WebPageTest
  - Lighthouse (Chrome DevTools)
  - Synthetic monitoring
- **Prioritas:** High
- **Catatan:** Optimasi: minification, caching, CDN, image optimization

#### NFR-06: API Response Time <a name="nfr-06"></a>
- **Platform:** Web Portal Backend (PHP)
- **Deskripsi:** Semua API endpoint harus merespons dalam waktu maksimal 500ms pada kondisi normal
- **Metrik Terukur:**
  - P50 response time: ≤ 200ms
  - P95 response time: ≤ 500ms
  - P99 response time: ≤ 1000ms
  - Database query time: ≤ 100ms
- **Target Compliance:** 99% dari semua API requests
- **Metode Verifikasi:**
  - Application Performance Monitoring (APM)
  - Load testing (Apache JMeter, Gatling)
  - Real User Monitoring (RUM)
  - Log analysis
- **Prioritas:** High
- **Catatan:** Query optimization, connection pooling, caching strategy

---

## 4.2 Security

### Desktop Application (Kotlin Multiplatform)

#### NFR-07: Data Transmission Encryption (Desktop) <a name="nfr-07"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Semua komunikasi antara aplikasi desktop dan server harus dienkripsi menggunakan HTTPS/TLS
- **Metrik Terukur:**
  - Protokol: TLS 1.2 minimum (TLS 1.3 preferred)
  - Certificate validation: mandatory
  - Cipher suite: hanya strong ciphers (≥ 128-bit)
  - Pinned certificates: untuk production servers
- **Target Compliance:** 100% dari semua network requests
- **Metode Verifikasi:**
  - Network packet inspection (Wireshark)
  - SSL/TLS analyzer (TestSSLServer)
  - Code review untuk HTTP client configuration
  - Penetration testing
- **Prioritas:** High
- **Catatan:** Implementasi certificate pinning untuk critical endpoints

#### NFR-08: API Key Masking <a name="nfr-08"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** API keys yang diinput user untuk sumber wallpaper pihak ketiga (Pexels, Unsplash, Pixabay) harus disembunyikan/di-mask saat ditampilkan
- **Metrik Terukur:**
  - Display format: `***[last-4-chars]` (contoh: `***5X9K`)
  - Full key hanya visible saat input awal
  - Key tidak pernah ditampilkan di log atau debug output
  - Encrypted storage di local database
- **Target Compliance:** 100% dari semua API key displays
- **Metode Verifikasi:**
  - Code review untuk view logic
  - Manual testing pada settings screen
  - Log file inspection
  - Security scanning
- **Prioritas:** High
- **Catatan:** Gunakan encryption (AES-256) untuk penyimpanan key di disk

### Web Portal (HTML/CSS/Tailwind/jQuery/PHP)

#### NFR-09: Data Transmission Encryption (Web) <a name="nfr-09"></a>
- **Platform:** Web Portal (Contributor/Admin)
- **Deskripsi:** Semua komunikasi antara browser dan server harus dienkripsi menggunakan HTTPS/TLS
- **Metrik Terukur:**
  - Protokol: TLS 1.2 minimum (TLS 1.3 preferred)
  - HSTS header: max-age ≥ 31536000 detik (1 tahun)
  - Certificate: valid dan signed oleh trusted CA
  - Cipher suite: hanya strong ciphers
  - No HTTP fallback atau mixed content
- **Target Compliance:** 100% dari semua requests
- **Metode Verifikasi:**
  - SSL Labs (https://www.ssllabs.com/ssltest/)
  - Chrome DevTools Security tab
  - OWASP ZAP scanning
  - Penetration testing
- **Prioritas:** High
- **Catatan:** Setup automatic redirect dari HTTP ke HTTPS

#### NFR-10: Password Hashing (Web) <a name="nfr-10"></a>
- **Platform:** Web Portal (PHP Backend)
- **Deskripsi:** Semua password yang disimpan di database harus di-hash menggunakan algoritma yang aman
- **Metrik Terukur:**
  - Algoritma: bcrypt dengan cost factor ≥ 12 (atau Argon2)
  - Salt: unique per password (auto-generated oleh bcrypt)
  - Password minimum: 8 karakter
  - Password policy: kombinasi huruf, angka, simbol (recommended)
  - Plaintext password: never stored, logged, atau cached
- **Target Compliance:** 100% dari semua passwords
- **Metode Verifikasi:**
  - Code review untuk authentication logic
  - Database inspection (verify hashed passwords)
  - Security testing dengan hash analysis
- **Prioritas:** High
- **Catatan:** Implementasi password strength meter di frontend untuk UX

#### NFR-11: Access Control (Web) <a name="nfr-11"></a>
- **Platform:** Web Portal (Contributor & Admin Portal)
- **Deskripsi:** Sistem harus menerapkan kontrol akses berbasis peran (RBAC) untuk melindungi resources. Web portal Scapes dirancang khusus untuk Contributor dan Admin saja, tidak termasuk User biasa (User biasa hanya menggunakan Desktop App).
- **Metrik Terukur:**
  - Role types: Contributor, Admin (tidak ada User role di web portal)
  - Session management: secure session handling dengan timeout
  - Authentication: login required untuk protected resources
  - Authorization: role-based access checks pada setiap endpoint
  - Failed login: max 5x dalam 5 menit → lock 15 menit
  - Session timeout: 30 menit inactivity
- **Target Compliance:** 100% dari semua protected resources
- **Metode Verifikasi:**
  - Code review untuk authorization logic
  - Penetration testing (privilege escalation attempts)
  - Access control matrix verification
  - Automated security testing
- **Prioritas:** High
- **Catatan:** Mengikuti OWASP Authentication & Authorization guidelines

---

## 4.3 Usability

### Desktop Application (Kotlin Multiplatform)

#### NFR-12: Responsive Window Resizing <a name="nfr-12"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Interface aplikasi harus responsif dan dapat menyesuaikan dengan berbagai ukuran window
- **Metrik Terukur:**
  - Layout responsive: scaling dengan window size
  - Minimum window size: 800x600 pixels
  - Support semua resolusi modern: 1920x1080, 1440x900, 1280x720, dll
  - No hardcoded sizes: use flexible layouts
  - All UI elements visible: no clipping atau overflow
- **Target Compliance:** 100% dari semua window states
- **Metode Verifikasi:**
  - Manual testing dengan berbagai resolutions
  - Automated UI testing
  - Visual regression testing
- **Prioritas:** High
- **Catatan:** Gunakan layout responsif berbasis Compose Multiplatform untuk menjaga fleksibilitas tampilan

#### NFR-13: UI/UX Quality - Desktop <a name="nfr-13"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Interface harus convenient/intuitif namun tetap modern dan elegan
- **Metrik Terukur:**
  - Design consistency: uniform styling across app
  - Navigation clarity: intuitive menu structure
  - Visual hierarchy: proper use of typography, colors, spacing
  - Color palette: max 5 primary colors + neutrals
  - Typography: max 3 font families (Segoe UI, Roboto, atau sejenis)
  - Icons: consistent style (Material Design or custom)
  - Animations: smooth transitions (< 300ms)
  - Accessibility: keyboard navigation support
- **Target Compliance:** 100% of UI screens
- **Metode Verifikasi:**
  - UI/UX design review
  - User testing sessions
  - Visual consistency audit
  - Accessibility testing
- **Prioritas:** High
- **Catatan:** Ikuti Material Design atau Apple Human Interface Guidelines

### Web Portal (HTML/CSS/Tailwind/jQuery/PHP)

#### NFR-14: Responsive Web Design <a name="nfr-14"></a>
- **Platform:** Web Portal
- **Deskripsi:** Interface web harus responsif dan dapat diakses di berbagai perangkat (desktop, tablet, mobile)
- **Metrik Terukur:**
  - Desktop: 1920x1080 resolution
  - Tablet: 768x1024 (iPad), 600x800 (Android)
  - Mobile: 375x667 (iPhone), 360x640 (Android)
  - Touch targets: ≥ 44x44 pixels
  - Font size: ≥ 14px untuk body text
  - No horizontal scroll pada mobile view
  - Images: responsive dan optimized
- **Target Compliance:** 95% dari semua viewing devices
- **Metode Verifikasi:**
  - Responsive design testing tools (Responsively App, BrowserStack)
  - Google Mobile-Friendly Test
  - Chrome DevTools device emulation
  - Real device testing
- **Prioritas:** High
- **Catatan:** Gunakan Tailwind CSS untuk responsive design yang efficient

#### NFR-15: UI/UX Quality - Web <a name="nfr-15"></a>
- **Platform:** Web Portal
- **Deskripsi:** Interface web harus convenient/intuitif namun tetap modern dan elegan
- **Metrik Terukur:**
  - Design system: comprehensive component library
  - Color palette: consistent across all pages
  - Typography: readable hierarchy with proper sizes
  - Whitespace: proper spacing untuk visual clarity
  - Interactive elements: clear hover/focus states
  - Loading states: visual feedback untuk async operations
  - Error messages: clear dan helpful
  - Form UX: intuitive with validation messages
  - Navigation: clear breadcrumbs atau menu structure
  - Consistency: uniform styling dengan Tailwind CSS
- **Target Compliance:** 100% of web pages
- **Metode Verifikasi:**
  - UI/UX design review
  - User acceptance testing
  - Design consistency audit
  - Usability testing sessions
- **Prioritas:** High
- **Catatan:** Maintain Tailwind CSS design tokens untuk consistency

---

## 4.4 Reliability

### Desktop Application (Kotlin Multiplatform)

#### NFR-16: Uptime (Scapes API Source) <a name="nfr-16"></a>
- **Platform:** Desktop - Scapes API Service
- **Deskripsi:** Sumber API wallpaper utama (Scapes API) harus memiliki availability yang tinggi
- **Metrik Terukur:**
  - Target uptime: 99% per bulan
  - Max downtime: 7 jam per bulan
  - Planned maintenance: announce 24 jam sebelumnya
  - Unplanned downtime: logged dengan incident report
  - Recovery Time Objective (RTO): ≤ 30 menit
- **Target Compliance:** 99% availability
- **Metode Verifikasi:**
  - Uptime monitoring (UptimeRobot, Pingdom)
  - Health check endpoints
  - Incident tracking
  - Monthly uptime report
- **Prioritas:** High
- **Catatan:** Implementasi graceful degradation jika API down (cache, fallback sources)

### Web Portal (HTML/CSS/Tailwind/jQuery/PHP)

#### NFR-17: Uptime (Web Portal) <a name="nfr-17"></a>
- **Platform:** Web Portal (Contributor/Admin)
- **Deskripsi:** Web portal harus memiliki availability yang tinggi untuk supporting operations
- **Metrik Terukur:**
  - Target uptime: 99% per bulan
  - Max downtime: 7 jam per bulan
  - Planned maintenance: schedule off-peak hours
  - Unplanned downtime: < 30 menit MTTR
  - Recovery Point Objective (RPO): ≤ 1 jam
- **Target Compliance:** 99% availability
- **Metode Verifikasi:**
  - Server uptime monitoring
  - Health check endpoints
  - Database replication monitoring
  - Automated failover testing
- **Prioritas:** High
- **Catatan:** Setup load balancing dan auto-restart services

### System-wide

#### NFR-18: Error Handling & Logging <a name="nfr-18"></a>
- **Deskripsi:** Semua komponen sistem harus menangani error dengan baik dan mencatat aktivitas penting
- **Metrik Terukur:**
  - All exceptions: caught dan di-log dengan context (timestamp, user, action, stack trace)
  - Error messages: user-friendly, tidak expose sensitive info
  - Log levels: DEBUG, INFO, WARNING, ERROR, CRITICAL
  - Log retention: minimum 90 hari untuk audit purposes
  - Log rotation: daily dengan compression
  - Critical errors: alert dalam < 5 menit
  - Error rate threshold: > 5% akan trigger alert
- **Target Compliance:** 100% dari semua error events
- **Metode Verifikasi:**
  - Code review untuk error handling
  - Log file audits
  - Error tracking tools (Sentry, DataDog)
  - Alerting system testing
- **Prioritas:** High
- **Catatan:** Centralized logging untuk easier debugging dan monitoring

#### NFR-23: Search Analytics Privacy <a name="nfr-23"></a>
- **Deskripsi:** Search analytics harus mencatat data yang cukup untuk rekomendasi tanpa menyimpan data personal yang tidak diperlukan
- **Metrik Terukur:**
  - Search log tidak menyimpan nama user, email, raw API key, atau full local path
  - Client identifier bersifat opsional dan harus dianonimkan atau di-hash jika digunakan untuk deduplication
  - Raw search logs disimpan maksimal 90 hari; hasil agregasi harian dapat disimpan lebih lama
  - Keyword disanitasi sebelum disimpan untuk mencegah injection dan konten berbahaya
- **Target Compliance:** 100% dari search analytics events
- **Metode Verifikasi:**
  - Code review payload logging
  - Database inspection
  - Security testing untuk input keyword
- **Prioritas:** High
- **Catatan:** Fitur ini harus aman untuk user anonim karena Desktop App user tidak wajib login

#### NFR-24: Recommendation API Response Time <a name="nfr-24"></a>
- **Platform:** Scapes API
- **Deskripsi:** Endpoint rekomendasi dan trending categories harus cepat agar Search tab tetap terasa ringan
- **Metrik Terukur:**
  - P95 response time: <= 500ms untuk `GET /recommendations/search`
  - P95 response time: <= 500ms untuk `GET /categories/trending`
  - Endpoint membaca dari tabel agregasi, bukan menghitung tren dari raw logs secara real-time
  - Frontend harus bisa render fallback lokal jika API timeout
- **Target Compliance:** 95% request normal
- **Metode Verifikasi:**
  - API integration test
  - Load testing ringan
  - Monitoring response time
- **Prioritas:** Medium
- **Catatan:** Aggregation job dapat berjalan terjadwal agar endpoint publik tetap ringan

---

## 4.5 Maintainability

### Desktop Application (Kotlin Multiplatform)

#### NFR-19: Code Style - Desktop <a name="nfr-19"></a>
- **Platform:** Desktop (Kotlin Multiplatform - Kotlin)
- **Deskripsi:** Source code harus mengikuti Kotlin Coding Conventions untuk consistency dan readability
- **Metrik Terukur:**
  - Naming conventions: camelCase untuk variables, PascalCase untuk classes
  - Line length: max 120 characters
  - Indentation: 4 spaces (no tabs)
  - Documentation: JavaDoc untuk public methods dan classes
  - Code formatting: automated dengan spotless atau checkstyle
  - Comment quality: meaningful comments for complex logic
- **Target Compliance:** 100% of code
- **Metode Verifikasi:**
  - Automated style checking (Checkstyle, SpotBugs)
  - Code review process
  - Pre-commit hooks untuk style enforcement
- **Prioritas:** High
- **Catatan:** Setup IDE code formatter sesuai Kotlin Coding Conventions

#### NFR-20: Architecture - Desktop <a name="nfr-20"></a>
- **Platform:** Desktop (Kotlin Multiplatform)
- **Deskripsi:** Aplikasi harus menggunakan Clean Architecture untuk maintainability dan testability
- **Metrik Terukur:**
  - Separation of concerns: UI, Business Logic, Data layers terpisah
  - Dependency Injection: loose coupling antar components
  - Design patterns: MVC/MVVM untuk UI, Repository untuk data access
  - Testing: unit tests untuk business logic (≥ 70% coverage)
  - Module organization: logical package structure
  - No circular dependencies: verified dengan architecture tools
- **Target Compliance:** 100% of codebase
- **Metode Verifikasi:**
  - Architecture review
  - Dependency analysis tools
  - Unit test coverage reports
  - Code structure audit
- **Prioritas:** High
- **Catatan:** Gunakan pendekatan composable UI agar tampilan tetap terpisah rapi dari business logic

### Web Portal (HTML/CSS/Tailwind/jQuery/PHP)

#### NFR-21: Code Style - Web <a name="nfr-21"></a>
- **Platform:** Web Portal (Frontend: JavaScript/jQuery, Backend: PHP)
- **Deskripsi:** Source code harus mengikuti Google Style Guide (JavaScript & PHP) untuk consistency
- **Metrik Terukur:**
  - JavaScript: Google JavaScript Style Guide (camelCase, semicolons, etc)
  - PHP: PSR-12 Extended Coding Style (Google-compatible)
  - Naming conventions: meaningful variable/function names
  - Documentation: JSDoc untuk JavaScript, PHPDoc untuk PHP
  - Indentation: 2 spaces untuk JS (Tailwind convention), 4 spaces untuk PHP
  - Linting: enabled dengan ESLint (JS) dan PHP_CodeSniffer (PHP)
  - Code formatting: Prettier (JS), PHP-CS-Fixer (PHP)
- **Target Compliance:** 100% of code
- **Metode Verifikasi:**
  - Automated linting (ESLint, PHP_CodeSniffer)
  - Code review process
  - Pre-commit hooks untuk style enforcement
  - CI/CD pipeline enforcement
- **Prioritas:** High
- **Catatan:** Setup IDE dengan auto-formatting pada save

#### NFR-22: Architecture - Web <a name="nfr-22"></a>
- **Platform:** Web Portal
- **Deskripsi:** Web application harus menggunakan Clean Architecture untuk maintainability
- **Metrik Terukur:**
  - Frontend: MVC/MVVM structure dengan clear separation
  - Backend (PHP): Layered architecture (Controllers, Services, Repositories, Models)
  - Dependency Injection: minimal coupling antar modules
  - API design: RESTful principles dengan consistent response format
  - Testing: Unit tests untuk backend logic (≥ 70% coverage), integration tests untuk APIs
  - Configuration: environment-based configs (dev, staging, production)
  - Security: input validation, output encoding, CSRF protection
- **Target Compliance:** 100% of codebase
- **Metode Verifikasi:**
  - Architecture review
  - Code structure audit
  - Test coverage reports
  - API contract testing
  - Security code review
- **Prioritas:** High
- **Catatan:** Implementasi MVC pattern di backend dengan clear routing & middleware

---

## 5. Traceability Matrix

| FR | US | AC |
|----|-----|-----|
| [FR-01](#fr-01) | [US-01](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-01) | AC-01.1, AC-01.2, AC-01.3, AC-01.4, AC-01.5 |
| [FR-02](#fr-02) | [US-02](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-02) | AC-02.1, AC-02.2, AC-02.3, AC-02.4 |
| [FR-03](#fr-03) | [US-03](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-03) | AC-03.1, AC-03.2, AC-03.3 |
| [FR-04](#fr-04) | [US-04](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-04) | AC-04.1, AC-04.2, AC-04.3 |
| [FR-05](#fr-05) | [US-05](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-05) | AC-05.1, AC-05.2, AC-05.3, AC-05.4 |
| [FR-06](#fr-06) | [US-06](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-06) | AC-06.1, AC-06.2, AC-06.3, AC-06.4 |
| [FR-07](#fr-07) | [CO-01](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-01) | AC-CO01.1, AC-CO01.2, AC-CO01.3, AC-CO01.4, AC-CO01.5, AC-CO01.6, AC-CO01.7, AC-CO01.8, AC-CO01.9 |
| [FR-08](#fr-08) | [CO-02](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-02) | AC-CO02.1, AC-CO02.2, AC-CO02.3 |
| [FR-09](#fr-09) | [CO-03](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-03) | AC-CO03.1, AC-CO03.2, AC-CO03.3 |
| [FR-10](#fr-10) | [CO-04](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-04) | AC-CO04.1, AC-CO04.2, AC-CO04.3, AC-CO04.4, AC-CO04.5 |
| [FR-11](#fr-11) | [CO-05](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-05) | AC-CO05.1, AC-CO05.2, AC-CO05.3 |
| [FR-12](#fr-12) | [CO-06](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-06) | AC-CO06.1, AC-CO06.2, AC-CO06.3 |
| [FR-13](#fr-13) | [CO-07](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-07) | AC-CO07.1, AC-CO07.2, AC-CO07.3, AC-CO07.4, AC-CO07.5 |
| [FR-14](#fr-14) | [CO-08](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#co-08) | AC-CO08.1, AC-CO08.2, AC-CO08.3 |
| [FR-15](#fr-15) | [AD-01](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#ad-01) | AC-AD01.1, AC-AD01.2, AC-AD01.3, AC-AD01.4 |
| [FR-16](#fr-16) | [AD-02](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#ad-02) | AC-AD02.1, AC-AD02.2, AC-AD02.3 |
| [FR-17](#fr-17) | [AD-03](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#ad-03) | AC-AD03.1, AC-AD03.2, AC-AD03.3, AC-AD03.4, AC-AD03.5 |
| [FR-18](#fr-18) | [US-08](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-08) | AC-08.1, AC-08.2, AC-08.4 |
| [FR-19](#fr-19) | [US-07](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-07), [US-08](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-08) | AC-07.1, AC-08.3 |
| [FR-20](#fr-20) | [US-01](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-01), [US-07](https://github.com/Subject-2407/praktikum-rpl-b-07/blob/dev/docs/user-stories.md#us-07) | AC-01.4, AC-01.5, AC-07.1, AC-07.2, AC-07.3, AC-07.4, AC-07.5 |
| **TOTAL** | **20** | **79** |

---

## 6. Catatan dan Asumsi

### 6.1 Asumsi

| # | Kategori | Asumsi |
|---|----------|--------|
| 1 | Teknis | Aplikasi Desktop (Kotlin Multiplatform) berjalan di sistem operasi Windows 10 atau 11 |
| 2 | Teknis | Layanan API Scapes memiliki infrastruktur cloud dan server (web, database) yang sudah terkonfigurasi |
| 3 | Teknis | API eksternal (Unsplash, Pexels, Pixabay) tetap menyediakan layanan publik dengan limitasi stabil selama pengembangan |
| 4 | Teknis | Pengguna memiliki hak akses tulis (write access) pada folder penyimpanan wallpaper |
| 5 | Teknis | Pengguna memiliki koneksi internet stabil untuk mengunduh wallpaper; jika offline, aplikasi menampilkan cache lokal |
| 6 | Organisasi | Terdapat aturan tidak tertulis mengenai konten yang diperbolehkan (tidak mengandung SARA, kekerasan, atau pelanggaran hak cipta) |
| 7 | Organisasi | Admin dan Kontributor adalah dua entitas berbeda; Admin berwenang penuh atas moderasi konten |
| 8 | Organisasi | Admin melakukan moderasi secara berkala (setiap hari kerja) agar konten pending tidak menumpuk |
| 9 | Organisasi | Admin memberikan alasan penolakan secara objektif sebagai feedback membangun bagi Kontributor |
| 10 | Organisasi | Pengembangan menggunakan metode Agile sederhana dengan sprint 1-2 mingguan |
| 11 | Organisasi | Tim menggunakan Git (GitHub) untuk manajemen kode dan kolaborasi |
| 12 | Organisasi | Tim terdiri dari 1 frontend developer, 1 backend developer, 1 QA, dan 1 documentation & UI/UX |
| 13 | Pengguna | Pengguna memiliki literasi digital yang cukup untuk mengoperasikan aplikasi desktop dan portal web |
| 14 | Pengguna | Pengguna memiliki ruang penyimpanan lokal yang cukup untuk mengunduh dan menyimpan wallpaper |
| 15 | Pengguna | Pengguna desktop bersedia melakukan pengaturan path folder di awal untuk kustomisasi penyimpanan |
| 16 | Pengguna | Kontributor memiliki hak cipta atau izin atas gambar yang diunggah |
| 17 | Teknis | Data pencarian user cukup berulang untuk menghasilkan trending categories yang bermakna setelah beberapa hari penggunaan |
| 18 | Teknis | Rekomendasi fase awal menggunakan agregasi statistik dan rule-based mapping keyword ke kategori/tag; ML dapat ditambahkan setelah volume dan kualitas data memadai |

### 6.2 Dependensi

| # | Tipe | Dependensi | Deskripsi |
|---|------|-----------|-----------|
| 1 | External API | Pexels API | Sumber wallpaper eksternal |
| 2 | External API | Unsplash API | Sumber wallpaper eksternal |
| 3 | External Service | SMTP Email Service | Verifikasi email dan reset password contributor |
| 4 | External Library | Tailwind CSS | Styling frontend web portal |
| 5 | External Library | jQuery | DOM manipulation dan AJAX requests |
| 6 | Internal | Scapes API (PHP) | Backend untuk autentikasi, upload, dan moderasi |
| 7 | Internal | MySQL Database | Penyimpanan data user, wallpaper, dan status moderasi |
| 8 | Internal | Windows API | System call untuk mengubah desktop wallpaper |
| 9 | Development | PHPUnit | Unit testing backend |
| 10 | Development | Postman | Pengujian REST API endpoint |
| 11 | Development | Figma | Perancangan wireframe dan prototype UI/UX |
| 12 | Internal | Search Analytics Job | Proses terjadwal untuk mengagregasi raw search logs menjadi trending categories harian |
| 13 | Internal | Category/Tag Metadata | Data kategori dan tag digunakan untuk memetakan keyword pencarian ke kategori rekomendasi |

### 6.3 Batasan & Constraints

| Kategori | Batasan |
|----------|---------|
| **Database** | MySQL |
| **Backend** | PHP |
| **Frontend Desktop** | Kotlin Multiplatform |
| **Frontend Web** | HTML, CSS, JavaScript (jQuery + Tailwind) |
| **Browser Support** | Chrome 90+, Firefox 88+, Edge 90+ |
| **Platform Desktop** | Windows 10 / Windows 11 |
| **File Upload** | Maksimal 10 MB per wallpaper |
| **Format Gambar** | JPG, PNG, WebP |
| **Resolusi Minimal Upload** | 1920x1080 piksel |
| **Search Log Retention** | Raw search logs maksimal 90 hari; hasil agregasi harian dapat disimpan lebih lama |
| **Recommendation MVP** | Menggunakan agregasi statistik, deduplication sederhana, prefix/fuzzy match ringan ke kategori bawaan, dan ranking user-keyword category; belum membutuhkan training model ML |
| **Recommendation Ranking** | Saat user mengetik query yang mendekati kategori bawaan, kategori bawaan yang match tampil paling atas. Tanpa query spesifik, top user-keyword categories tampil di atas daftar kategori bawaan. Default top user-keyword category adalah 10 item dan dapat dikustomisasi melalui API |
| **Tag Lifecycle** | Tag baru dari contributor wajib diawali `#`, disimpan sebagai proposal saat wallpaper pending, baru masuk tabel `tags` setelah admin approve, dan tidak boleh membuat duplikasi slug |
| **Tag Search** | Query user yang diawali `#` harus mencari tag resmi di database lebih dulu; jika tidak ada tag match, sistem fallback ke recommender normal |
| **Internal Metadata Scope** | Prioritas kategori bawaan dan tag resmi dari database hanya berlaku ketika source aktif adalah `scapes`. Untuk source external, sistem harus fallback ke recommender normal tanpa memakai kategori/tag internal sebagai prioritas |

---

**[Kembali ke atas](#-daftar-isi-table-of-contents)**
