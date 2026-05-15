<?php

/**
 * Seeder untuk Membuat Akun Admin
 *
 * Script ini digunakan untuk membuat akun administrator pertama kali
 * agar bisa mengakses fitur moderasi.
 *
 * Cara menjalankan:
 * php seed_admin.php
 */

declare(strict_types=1);

require_once __DIR__ . '/vendor/autoload.php';
require_once __DIR__ . '/config/bootstrap.php';

use Scapes\Infrastructure\Database\DatabaseConnection;

$db = DatabaseConnection::getInstance();

$email = 'admin@scapes.app';
$password = 'admin12345'; // Harap ganti di production
$hash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

echo "========================================\n";
echo "  Scapes Admin Seeder\n";
echo "========================================\n\n";

try {
    // Cek apakah email sudah ada
    $check = $db->query("SELECT id FROM users WHERE email = ?", [$email]);
    if ($check->fetch()) {
        echo "❌ Error: User dengan email $email sudah terdaftar.\n";
        exit(1);
    }

    // Insert admin
    $db->query(
        "INSERT INTO users (email, password_hash, role, is_verified, created_at, updated_at) 
         VALUES (?, ?, ?, ?, NOW(), NOW())",
        [$email, $hash, 'admin', 1]
    );

    echo "✅ Admin account created successfully!\n\n";
    echo "Email    : $email\n";
    echo "Password : $password\n";
    echo "Role     : admin\n";
    echo "\nSilakan gunakan kredensial ini untuk login dan mendapatkan token admin.\n";

} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    exit(1);
}
