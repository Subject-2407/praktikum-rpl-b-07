<?php

/**
 * Seeder akun admin Scapes.
 *
 * Jalankan dari folder src/backend:
 * php seed_admin.php
 *
 * Opsional override via .env:
 * ADMIN_SEED_EMAIL=admin@scapes.app
 * ADMIN_SEED_PASSWORD=admin12345
 */

declare(strict_types=1);

require_once __DIR__ . '\..\vendor\autoload.php';
require_once __DIR__ . '\..\config\bootstrap.php';

use Scapes\Infrastructure\Database\DatabaseConnection;

$email = $_ENV['ADMIN_SEED_EMAIL'] ?? $_SERVER['ADMIN_SEED_EMAIL'] ?? 'admin@scapes.app';
$password = $_ENV['ADMIN_SEED_PASSWORD'] ?? $_SERVER['ADMIN_SEED_PASSWORD'] ?? 'admin12345';

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
  fwrite(STDERR, "ADMIN_SEED_EMAIL tidak valid.\n");
  exit(1);
}

if (strlen($password) < 8) {
  fwrite(STDERR, "ADMIN_SEED_PASSWORD minimal 8 karakter.\n");
  exit(1);
}

echo "========================================\n";
echo "  Scapes Admin Seeder\n";
echo "========================================\n\n";

try {
  $db = DatabaseConnection::getInstance();
  $existingUser = $db
    ->query('SELECT id, email, role, is_verified FROM users WHERE email = ?', [$email])
    ->fetch();

  if ($existingUser !== false) {
    if ($existingUser['role'] !== 'admin') {
      fwrite(
        STDERR,
        "Email {$email} sudah terdaftar sebagai {$existingUser['role']}. Seeder tidak mengubah role user yang ada.\n"
      );
      exit(1);
    }

    if ((int) $existingUser['is_verified'] !== 1) {
      $db->query('UPDATE users SET is_verified = 1, updated_at = NOW() WHERE id = ?', [$existingUser['id']]);
      echo "Admin sudah ada dan sekarang sudah diverifikasi.\n\n";
    } else {
      echo "Admin sudah ada. Tidak ada data baru yang dibuat.\n\n";
    }

    echo "Email : {$email}\n";
    echo "Role  : admin\n";
    exit(0);
  }

  $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

  $db->query(
    'INSERT INTO users (email, password_hash, role, is_verified, created_at, updated_at)
     VALUES (?, ?, ?, ?, NOW(), NOW())',
    [$email, $passwordHash, 'admin', 1]
  );

  echo "Admin berhasil dibuat.\n\n";
  echo "Email    : {$email}\n";
  echo "Password : {$password}\n";
  echo "Role     : admin\n";
  echo "Verified : yes\n\n";
  echo "Gunakan kredensial ini untuk login ke POST /sessions.\n";
} catch (Throwable $e) {
  fwrite(STDERR, 'Seeder admin gagal: ' . $e->getMessage() . "\n");
  exit(1);
}
