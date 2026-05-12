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

3. Konfigurasi database di `config/bootstrap.php` (sesuaikan HOST, USER, PASSWORD jika diperlukan)

## Menjalankan Server

Jalankan development server dengan:
```bash
php -S localhost:8000 -t public/
```

Server akan berjalan di `http://localhost:8000`

**Test endpoint welcome:**
```bash
curl http://localhost:8000/
```

## Menjalankan Tests

Jalankan semua unit tests:
```bash
vendor/bin/phpunit
```

**Options:**
```bash
# Jalankan test file spesifik
vendor/bin/phpunit tests/Unit/Application/UseCases/RegisterContributorUseCaseTest.php

# Jalankan dengan verbose output
vendor/bin/phpunit --verbose

# Jalankan dengan code coverage
vendor/bin/phpunit --coverage-html coverage/
```

## API Endpoints

### Authentication
- `POST /auth/register` - Daftar contributor baru
- `POST /auth/login` - Login
- `POST /auth/logout` - Logout

### Wallpaper
- `POST /wallpaper/upload` - Upload wallpaper baru
- `GET /wallpaper/{id}` - Ambil detail wallpaper
- `DELETE /wallpaper/{id}` - Hapus wallpaper
- `GET /wallpaper/contributor/{contributor_id}` - Ambil wallpaper by contributor

### Moderation
- `POST /moderation/moderate` - Moderasi wallpaper (approve/reject)
- `GET /moderation/pending` - Ambil wallpaper pending moderasi

## Project Structure

```
src/backend/
├── config/          - Konfigurasi database
├── public/          - Entry point (index.php)
├── src/
│   ├── Core/        - Domain entities & exceptions
│   ├── Application/ - Use cases
│   ├── Infrastructure/ - Repositories, routing, database
│   └── Interfaces/  - HTTP controllers
├── tests/           - Unit tests
└── vendor/          - Dependencies
```

## Catatan

- Semua endpoint mengembalikan JSON responses
- Error responses menggunakan HTTP status codes yang sesuai
- Semua tests menggunakan mocking dan pattern AAA (Arrange, Act, Assert)
