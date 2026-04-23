# Installation Guide - Scapes Backend

Panduan lengkap untuk install dan setup backend Scapes.

## Prerequisites

Pastikan sistem Anda memiliki:

- **PHP 8.1+** (dengan extensions: PDO, PDO_MySQL, JSON, Curl)
- **MySQL 5.7+** atau **PostgreSQL 10+**
- **Composer 2.x**
- **Git** (opsional)

### Verify PHP Version

```bash
php --version
```

### Verify Composer Installation

```bash
composer --version
```

## Installation Steps

### 1. Navigate ke Folder Backend

```bash
cd src/backend
```

### 2. Install Dependencies

Install semua dependencies yang didefinisikan di `composer.json`:

```bash
composer install
```

Output yang diharapkan:

```
Loading composer repositories with package information
Updating dependencies
- Installing vlucas/phpdotenv (v5.6.x)
...
Writing lock file
Generating autoload files
```

### 3. Create Database

Buka MySQL client atau tool seperti phpMyAdmin, kemudian jalankan SQL berikut:

```sql
-- Create database
CREATE DATABASE scapes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify (optional)
SHOW DATABASES;
```

#### Alternate untuk PostgreSQL

```sql
CREATE DATABASE scapes_db;
\c scapes_db
```

### 4. Configure Environment Variables

**Langkah 1**: Buka file `.env` di folder `src/backend/`

**Langkah 2**: Sesuaikan konfigurasi sesuai setup lokal Anda

Untuk MySQL:

```env
# Database Configuration
DB_CONNECTION=mysql
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=scapes_db
DB_USERNAME=root
DB_PASSWORD=         # Kosongkan jika tidak ada password

# Application Configuration
APP_ENV=development
APP_DEBUG=true
APP_TIMEZONE=Asia/Jakarta

# Logging
LOG_LEVEL=debug
```

Untuk PostgreSQL:

```env
# Database Configuration
DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=scapes_db
DB_USERNAME=postgres
DB_PASSWORD=         # Sesuaikan password Anda

# Application Configuration
APP_ENV=development
APP_DEBUG=true
APP_TIMEZONE=Asia/Jakarta

# Logging
LOG_LEVEL=debug
```

### 5. Test Database Connection

Jalankan script test untuk memverifikasi koneksi:

```bash
php test_database_connection.php
```

Output yang diharapkan:

```
========================================
  Test Koneksi Database Scapes
========================================

[1] Inisialisasi DatabaseConnection... ✓ Berhasil
[2] Menjalankan query test... ✓ Berhasil
[3] Mengambil hasil query... ✓ Berhasil

Hasil Query:
{
  "test": 1
}

========================================
  Informasi Koneksi
========================================
Status        : ✓ Terhubung
Database Type : mysql
Host          : localhost
Port          : 3306
Database      : scapes_db
Charset       : utf8mb4
Environment   : development
Debug Mode    : true

Koneksi ditutup.
```

Jika ada error, baca bagian [Troubleshooting](#troubleshooting).

### 6. (Optional) Setup Web Server

#### Using PHP Built-in Server

```bash
php -S localhost:8000 -t public
```

Aplikasi akan dapat diakses di: `http://localhost:8000`

#### Using Apache

1. Ubah document root Apache ke folder `public/`
2. Pastikan `mod_rewrite` enabled
3. Pastikan file `.htaccess` di folder `public/` sudah ada

#### Using Nginx

Buat virtual host configuration:

```nginx
server {
    listen 80;
    server_name scapes.local;

    root /path/to/src/backend/public;
    index index.php;

    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }

    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php-fpm.sock;
        fastcgi_index index.php;
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
    }
}
```

---

## Verification

### 1. Check PHP Version

```bash
php --version
```

Pastikan versi minimal PHP 8.1.

### 2. Check Extensions

```bash
php -m | grep -E "PDO|json|curl"
```

Output harus menampilkan: `PDO`, `pdo_mysql` atau `pdo_pgsql`, `json`, `curl`.

### 3. Check Composer

```bash
composer --version
composer dump-autoload -o
```

### 4. Check Database Connection

```bash
php test_database_connection.php
```

### 5. Test API Endpoint

Buka browser dan akses:

```
http://localhost:8000
```

Atau gunakan curl:

```bash
curl http://localhost:8000
```

Expected response:

```json
{
  "success": true,
  "message": "Scapes Backend API is running",
  "version": "1.0",
  "timestamp": "2026-04-23 10:30:45"
}
```

---

## Common Issues

### "Composer command not found"

**Solusi**: Install Composer atau tambahkan ke PATH

```bash
# Install Composer (Linux/Mac)
curl -sS https://getcomposer.org/installer | php

# Move ke global location
sudo mv composer.phar /usr/local/bin/composer
```

### "PHP version does not match"

**Solusi**: Update PHP ke versi 8.1+

```bash
php --version  # Check current version
```

### "Database does not exist"

**Solusi**: Buat database terlebih dahulu

```sql
CREATE DATABASE scapes_db;
```

### "PDOException: SQLSTATE[HY000] [2002] Connection refused"

**Solusi**: 

1. Pastikan MySQL/PostgreSQL sudah running
2. Cek konfigurasi di file `.env`
3. Pastikan host dan port sudah benar

```bash
# Test MySQL connection
mysql -h localhost -u root -p -e "SELECT 1"

# Test PostgreSQL connection
psql -h localhost -U postgres -c "SELECT 1"
```

### "Access denied for user 'root'@'localhost'"

**Solusi**: Cek password di file `.env`

```env
DB_USERNAME=root
DB_PASSWORD=your_actual_password  # Update ini
```

### "Autoloader not found"

**Solusi**: Regenerate autoloader

```bash
composer dump-autoload -o
composer install
```

---

## Project Structure Overview

```
src/backend/
├── config/                # Konfigurasi aplikasi
│   └── bootstrap.php     # Initialize aplikasi
├── src/                  # Source code (Clean Architecture)
│   ├── Core/            # Domain & business logic
│   ├── Application/     # Use cases
│   ├── Infrastructure/  # Database, external services
│   └── Interfaces/      # HTTP controllers
├── public/              # Web root
│   └── index.php       # Entry point
├── storage/            # Non-code files (logs, cache)
├── vendor/             # Composer dependencies
├── .env               # Environment variables (local)
├── .env.example       # Environment template
├── composer.json      # Dependencies definition
├── README.md          # Project overview
├── ARCHITECTURE.md    # Clean Architecture docs
└── PHP_STYLE_GUIDE.md # Code style guidelines
```

---

## Next Steps

Setelah setup berhasil, Anda dapat:

1. **Read Documentation**
   - [README.md](./README.md) - Overview proyek
   - [ARCHITECTURE.md](./ARCHITECTURE.md) - Clean Architecture details
   - [PHP_STYLE_GUIDE.md](./PHP_STYLE_GUIDE.md) - Code style guidelines

2. **Create First Entity**
   - Lihat folder `src/Core/Domain/` untuk contoh

3. **Create First Repository**
   - Lihat folder `src/Infrastructure/Repository/` untuk contoh

4. **Create First Use Case**
   - Lihat folder `src/Application/UseCases/` untuk contoh

5. **Create First Controller**
   - Lihat folder `src/Interfaces/Http/Controllers/` untuk contoh

---

## Useful Commands

```bash
# Install dependencies
composer install

# Update dependencies
composer update

# Check code style
composer run cs-check

# Fix code style automatically
composer run cs-fix

# Run static analysis
composer run stan

# Test database connection
php test_database_connection.php

# Start development server
php -S localhost:8000 -t public

# Dump autoloader
composer dump-autoload -o
```

---

## Support

Jika mengalami masalah:

1. Baca bagian Troubleshooting di atas
2. Cek dokumentasi di folder `docs/`
3. Konsultasikan dengan tech lead
4. Buat issue di repository

---

Happy coding! 🚀
