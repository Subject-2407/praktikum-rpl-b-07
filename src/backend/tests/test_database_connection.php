<?php

/**
 * Test Koneksi Database
 *
 * Script untuk memverifikasi bahwa koneksi database sudah berhasil
 * dikonfigurasi dan dapat terhubung dengan baik.
 *
 * Cara menjalankan:
 * php test_database_connection.php
 */

declare(strict_types=1);

require_once __DIR__ . '/vendor/autoload.php';
require_once __DIR__ . '/config/bootstrap.php';

use Scapes\Infrastructure\Database\DatabaseConnection;

echo "========================================\n";
echo "  Test Koneksi Database Scapes\n";
echo "========================================\n\n";

try {
    // Coba mendapatkan instance DatabaseConnection
    echo "[1] Inisialisasi DatabaseConnection... ";
    $db = DatabaseConnection::getInstance();
    echo "✓ Berhasil\n";

    // Coba menjalankan simple query
    echo "[2] Menjalankan query test... ";
    $pdo = $db->getPdo();
    $result = $pdo->query('SELECT 1 as test');
    echo "✓ Berhasil\n";

    // Ambil hasil
    echo "[3] Mengambil hasil query... ";
    $data = $result->fetch();
    echo "✓ Berhasil\n";

    // Tampilkan hasil
    echo "\nHasil Query:\n";
    echo json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    echo "\n\n";

    // Info koneksi
    echo "========================================\n";
    echo "  Informasi Koneksi\n";
    echo "========================================\n";
    echo "Status        : ✓ Terhubung\n";
    echo "Database Type : " . (getenv('DB_CONNECTION') ?: 'mysql') . "\n";
    echo "Host          : " . (getenv('DB_HOST') ?: 'localhost') . "\n";
    echo "Port          : " . (getenv('DB_PORT') ?: '3306') . "\n";
    echo "Database      : " . (getenv('DB_DATABASE') ?: 'scapes_db') . "\n";
    echo "Charset       : " . (getenv('DB_CHARSET') ?: 'utf8mb4') . "\n";
    echo "Environment   : " . (getenv('APP_ENV') ?: 'development') . "\n";
    echo "Debug Mode    : " . (getenv('APP_DEBUG') ? 'true' : 'false') . "\n";
    echo "\n";

    // Tutup koneksi
    $db->close();
    echo "Koneksi ditutup.\n";

} catch (Exception $e) {
    echo "\n";
    echo "❌ ERROR: " . $e->getMessage() . "\n";
    echo "\nFile: " . $e->getFile() . "\n";
    echo "Line: " . $e->getLine() . "\n";
    echo "\nStacktrace:\n";
    echo $e->getTraceAsString() . "\n";

    exit(1);
}

exit(0);
