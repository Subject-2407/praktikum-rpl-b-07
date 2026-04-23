# PHP Code Style Guide untuk Scapes

Panduan ini mendokumentasikan standar code style yang harus diikuti di project Scapes Backend.
Berdasarkan **Google PHP Style Guide** dengan penyesuaian untuk project kami.

## Table of Contents

1. [File Structure](#file-structure)
2. [Naming Conventions](#naming-conventions)
3. [Formatting](#formatting)
4. [Documentation](#documentation)
5. [Best Practices](#best-practices)

---

## File Structure

### File Headers

Setiap file PHP harus dimulai dengan blok dokumentasi:

```php
<?php

/**
 * Deskripsi singkat tentang file ini
 *
 * Deskripsi yang lebih lengkap tentang tujuan file, class, atau function
 * yang ada di dalamnya.
 *
 * @package Scapes\NamaPackage
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\NamaPackage;
```

### Declare Statement

Selalu gunakan `declare(strict_types=1);` untuk type safety:

```php
<?php

declare(strict_types=1);

namespace Scapes\SomePackage;
```

---

## Naming Conventions

### Classes

- Gunakan **PascalCase**
- Nama harus noun dan deskriptif

```php
// ✓ Good
class DatabaseConnection {
}

class UserRepository {
}

// ✗ Bad
class databaseConnection {
}

class get_user_data {
}
```

### Methods dan Functions

- Gunakan **camelCase**
- Dimulai dengan verb jika action-based

```php
// ✓ Good
public function getPdo(): PDO {
}

private function createConnection(): PDO {
}

public function beginTransaction(): bool {
}

// ✗ Bad
public function get_pdo() {
}

private function pdoCreate() {
}
```

### Properties

- Gunakan **camelCase**
- Gunakan akses modifier (private/protected/public)

```php
// ✓ Good
private DatabaseConnection $db;
private string $tableName = 'users';
private ?array $data = null;

// ✗ Bad
private $databaseConnection;
var $table_name;
public $data;
```

### Constants

- Gunakan **UPPER_SNAKE_CASE**
- Ditempatkan di bagian atas class

```php
// ✓ Good
private const DEFAULT_CHARSET = 'utf8mb4';
public const BATCH_SIZE = 100;

// ✗ Bad
private const default_charset = 'utf8mb4';
public $BATCH_SIZE = 100;
```

### Variables

- Gunakan **camelCase**
- Hindari single letter variable kecuali untuk loop index

```php
// ✓ Good
$userId = 123;
$userEmail = 'user@example.com';
for ($i = 0; $i < 10; $i++) {
}

// ✗ Bad
$user_id = 123;
$u = 'user@example.com';
for ($index = 0; $index < 10; $index++) {
}
```

---

## Formatting

### Indentation

- Gunakan **2 spaces** untuk indentation (bukan tabs)

```php
// ✓ Good
if ($condition) {
  echo 'Hello';
}

// ✗ Bad
if ($condition) {
    echo 'Hello';  // 4 spaces
}
```

### Line Length

- Maksimal **80 karakter** untuk komentar dan dokumentasi
- Boleh lebih panjang untuk code jika diperlukan, tapi usahakan tetap readable

```php
// ✓ Good - komentar tetap di bawah 80 karakter
// Mengecek apakah user memiliki permission untuk
// menghapus wallpaper yang sudah diunggah

// ✗ Bad
// Mengecek apakah user memiliki permission untuk menghapus wallpaper yang sudah diunggah oleh contributor lain
```

### Braces

**Opening braces** pada baris yang sama, **closing braces** di baris terpisah:

```php
// ✓ Good
if ($condition) {
  // code
}

// ✗ Bad
if ($condition)
{
  // code
}
```

### Type Hints

Selalu gunakan type hints untuk parameters dan return types:

```php
// ✓ Good
public function getUserById(int $id): ?User {
  return $this->repository->find($id);
}

private function validateEmail(string $email): bool {
  return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

// ✗ Bad
public function getUserById($id) {
  return $this->repository->find($id);
}
```

### Nullable Types

Gunakan nullable type hints dengan `?` untuk value yang bisa null:

```php
// ✓ Good
public function findById(int $id): ?User {
}

private ?string $status = null;

// ✗ Bad
public function findById(int $id): User {
  // Tapi bisa return null
}
```

---

## Documentation

### PHPDoc Comments

Semua public classes, methods, dan properties harus memiliki PHPDoc:

```php
/**
 * Deskripsi singkat (satu baris).
 *
 * Deskripsi yang lebih detail jika diperlukan. Menjelaskan
 * apa yang class/method lakukan, parameter, return value,
 * dan exception yang mungkin dilempar.
 *
 * @param string $parameter Deskripsi parameter.
 * @param int $count Jumlah item.
 *
 * @return string Deskripsi return value.
 * @throws DatabaseException Jika database error terjadi.
 */
public function exampleMethod(string $parameter, int $count): string {
}
```

### Inline Comments

Gunakan `//` untuk inline comments yang menjelaskan logika:

```php
// Validasi bahwa user memiliki permission
if (!$this->hasPermission($user, 'delete_wallpaper')) {
  throw new UnauthorizedException('Permission denied');
}

// Loop melalui hasil query dan filter
foreach ($results as $item) {
  if ($item['status'] === 'active') {
    $filtered[] = $item;
  }
}
```

### Dokumentasi dalam Bahasa Indonesia

Semua dokumentasi dan comments **harus dalam bahasa Indonesia**:

```php
<?php

/**
 * Kelas untuk mengelola koneksi database Scapes.
 *
 * Kelas ini bertanggung jawab membuat dan mengelola koneksi
 * ke database menggunakan pola Singleton.
 *
 * @package Scapes\Infrastructure\Database
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Database;

class DatabaseConnection {
  /**
   * Instance singleton dari koneksi database.
   *
   * @var self|null
   */
  private static ?self $instance = null;

  /**
   * Mendapatkan instance singleton DatabaseConnection.
   *
   * @return self Instance singleton.
   */
  public static function getInstance(): self {
    // Implementation
  }
}
```

---

## Best Practices

### 1. Use Type Checking

Selalu gunakan strict type checking:

```php
// ✓ Good
declare(strict_types=1);

public function setCount(int $count): void {
  $this->count = $count;  // Akan error jika pass string
}

// ✗ Bad
public function setCount($count) {
  $this->count = (int) $count;  // Terlalu permissive
}
```

### 2. Error Handling

Selalu handle exceptions dengan proper:

```php
// ✓ Good
try {
  $result = $this->db->query($sql, $params);
  return $result->fetch();
} catch (PDOException $e) {
  throw new DatabaseException('Query gagal: ' . $e->getMessage());
}

// ✗ Bad
$result = $this->db->query($sql, $params);
return $result->fetch();  // Bisa crash jika error
```

### 3. Use Constants

Gunakan constants untuk magic values:

```php
// ✓ Good
private const MAX_UPLOAD_SIZE = 10485760;  // 10 MB
private const DEFAULT_TIMEOUT = 30;

if ($fileSize > self::MAX_UPLOAD_SIZE) {
  throw new ValidationException('File terlalu besar');
}

// ✗ Bad
if ($fileSize > 10485760) {  // Magic number!
  throw new ValidationException('File terlalu besar');
}
```

### 4. DRY Principle

Jangan ulangi code, extract ke method:

```php
// ✓ Good
private function validateUser(User $user): bool {
  return !empty($user->id) && !empty($user->email);
}

public function updateUser(User $user): void {
  if (!$this->validateUser($user)) {
    throw new ValidationException('User data invalid');
  }
  // Update logic
}

public function createUser(User $user): void {
  if (!$this->validateUser($user)) {
    throw new ValidationException('User data invalid');
  }
  // Create logic
}

// ✗ Bad
public function updateUser(User $user): void {
  if (empty($user->id) || empty($user->email)) {
    throw new ValidationException('User data invalid');
  }
  // Update logic
}

public function createUser(User $user): void {
  if (empty($user->id) || empty($user->email)) {
    throw new ValidationException('User data invalid');
  }
  // Create logic
}
```

### 5. Use Access Modifiers

Selalu tentukan akses modifier (public/protected/private):

```php
// ✓ Good
public function getUser(): ?User {
}

private function validateInput(array $input): void {
}

protected function executeQuery(string $sql): array {
}

// ✗ Bad
function getUser() {  // Implicitly public, unclear
}
```

---

## Summary

| Aspek | Gaya |
|-------|------|
| Classes | PascalCase |
| Methods/Functions | camelCase |
| Properties | camelCase |
| Constants | UPPER_SNAKE_CASE |
| Variables | camelCase |
| Indentation | 2 spaces |
| Type Hints | Always |
| Documentation | Indonesian |
| Access Modifiers | Always |
| Braces | Same line (opening), new line (closing) |

---

Untuk pertanyaan lebih lanjut, konsultasikan dengan tech lead project.
