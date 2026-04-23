# Backend Setup Guide - Scapes

Panduan untuk setup environment backend Scapes dengan PHP dan MySQL.

## Struktur Proyek

Backend Scapes menggunakan Clean Architecture untuk memisahkan concerns dan meningkatkan maintainability.

```
src/backend/
├── config/                          # File konfigurasi aplikasi
│   └── bootstrap.php               # Bootstrap environment dan dependencies
├── src/
│   ├── Core/                       # Business logic dan domain entities
│   │   ├── Domain/                # Domain models dan entities
│   │   └── Exceptions/            # Custom exceptions
│   ├── Application/               # Use cases dan aplikasi logic
│   │   └── UseCases/             # Implementasi use cases
│   ├── Infrastructure/            # External dependencies (Database, Cache, dll)
│   │   ├── Database/             # Database connection dan queries
│   │   └── Repository/           # Data access layer
│   └── Interfaces/               # Interface adapters dan controllers
│       └── Http/
│           └── Controllers/      # HTTP controllers
├── public/                         # Entry point aplikasi
├── storage/
│   └── logs/                      # File logs aplikasi
├── composer.json                  # Dependency management
├── .env                           # Environment variables (lokal)
└── .env.example                   # Template environment variables
```

## Prerequisites

- PHP >= 8.1
- MySQL 5.7+ atau PostgreSQL 10+
- Composer (PHP package manager)

## Installation Steps

### 1. Install Dependencies

Jalankan command berikut di folder `src/backend/`:

```bash
composer install
```

Command ini akan menginstall semua dependencies yang didefinisikan di `composer.json`, termasuk library `phpdotenv`.

### 2. Setup Environment Variables

1. Buka file `.env` di folder `src/backend/`
2. Sesuaikan konfigurasi database:

```env
DB_CONNECTION=mysql           # Tipe database (mysql, pgsql, sqlite)
DB_HOST=localhost            # Host database
DB_PORT=3306                 # Port database
DB_DATABASE=scapes_db        # Nama database
DB_USERNAME=root             # Username database
DB_PASSWORD=                 # Password database
APP_ENV=development          # Environment (development, production)
APP_DEBUG=true              # Debug mode
APP_TIMEZONE=Asia/Jakarta   # Timezone
```

### 3. Create Database

Buat database MySQL baru dengan nama `scapes_db`:

```sql
CREATE DATABASE scapes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Verify Connection

Untuk memverifikasi koneksi database, jalankan script test connection:

```php
<?php
require_once __DIR__ . '/config/bootstrap.php';
require_once __DIR__ . '/vendor/autoload.php';

use Scapes\Infrastructure\Database\DatabaseConnection;

try {
    $db = DatabaseConnection::getInstance();
    echo "Koneksi database berhasil!";
    $db->close();
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}
```

## Code Style Guide

Backend Scapes mengikuti **Google PHP Style Guide** dengan beberapa penyesuaian:

### Naming Conventions

- **Classes**: PascalCase (contoh: `DatabaseConnection`)
- **Methods/Functions**: camelCase (contoh: `getPdo()`)
- **Constants**: UPPER_SNAKE_CASE (contoh: `DB_HOST`)
- **Variables**: camelCase (contoh: `$databaseHost`)

### Code Formatting

- Indentation: 2 spaces (bukan tabs)
- Line length: maksimal 80 karakter untuk komentar dan dokumentasi
- Opening braces: di baris yang sama dengan deklarasi
- Closing braces: di baris terpisah

### Documentation

Semua kelas, method, dan property publik harus memiliki dokumentasi PHPDoc:

```php
/**
 * Deskripsi singkat method.
 *
 * Deskripsi yang lebih detail tentang apa yang method lakukan,
 * parameter apa yang diterima, dan apa yang dikembalikan.
 *
 * @param string $parameter Deskripsi parameter.
 * @return string Deskripsi return value.
 * @throws ExceptionClass Deskripsi kapan exception dilempar.
 */
public function exampleMethod(string $parameter): string {
  // Implementation
}
```

## Database Connection

### Menggunakan DatabaseConnection

Database connection di-setup menggunakan pola Singleton untuk memastikan hanya ada satu koneksi aktif:

```php
use Scapes\Infrastructure\Database\DatabaseConnection;

// Dapatkan instance singleton
$db = DatabaseConnection::getInstance();

// Jalankan query dengan prepared statement
$query = "SELECT * FROM users WHERE email = ?";
$result = $db->query($query, [$email]);

// Fetch hasil
$user = $result->fetch();
```

### Transaksi Database

Untuk menjalankan multiple queries dalam satu transaksi:

```php
try {
    $db->beginTransaction();
    
    // Multiple queries...
    $db->query("INSERT INTO users ...", $params1);
    $db->query("INSERT INTO sessions ...", $params2);
    
    $db->commit();
} catch (PDOException $e) {
    $db->rollback();
    throw $e;
}
```

## Useful Commands

### Check Code Style

```bash
composer run cs-check
```

### Auto-fix Code Style

```bash
composer run cs-fix
```

### Static Analysis

```bash
composer run stan
```

## Environment Variables Reference

| Variable | Deskripsi | Contoh |
|----------|-----------|---------|
| `DB_CONNECTION` | Tipe database | `mysql` |
| `DB_HOST` | Host database | `localhost` |
| `DB_PORT` | Port database | `3306` |
| `DB_DATABASE` | Nama database | `scapes_db` |
| `DB_USERNAME` | Username database | `root` |
| `DB_PASSWORD` | Password database | `` |
| `DB_CHARSET` | Charset database | `utf8mb4` |
| `APP_ENV` | Environment aplikasi | `development` |
| `APP_DEBUG` | Debug mode | `true` |
| `APP_TIMEZONE` | Timezone aplikasi | `Asia/Jakarta` |
| `LOG_LEVEL` | Level logging | `debug` |

## Troubleshooting

### PDOException: Database tidak ditemukan

**Solusi:**
- Pastikan database sudah dibuat
- Cek nilai `DB_DATABASE` di file `.env`

### PDOException: Access denied for user

**Solusi:**
- Cek username dan password database
- Pastikan user database memiliki privileges yang cukup

### Autoloader error

**Solusi:**
- Jalankan `composer dump-autoload`
- Pastikan namespace di kode sesuai dengan struktur folder

---

Untuk pertanyaan lebih lanjut, lihat dokumentasi di folder `docs/`
