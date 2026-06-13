<?php
/**
 * Router Script untuk PHP Built-in Server
 * 
 * File ini menangani routing untuk PHP development server.
 * Jalankan dengan: php -S localhost:8000 router.php
 */

$requested = parse_url($_SERVER["REQUEST_URI"], PHP_URL_PATH);

// Jika request adalah ke file/folder yang actually ada di public folder, serve itu
$public_file = __DIR__ . '/public' . $requested;

if ($requested !== "/" && file_exists($public_file) && is_file($public_file)) {
  return false;  // Biarkan built-in server serve file tersebut
}

// Untuk semua request lainnya (routes, dynamic paths), route ke index.php
$_SERVER['REQUEST_URI'] = $requested;
$_SERVER['SCRIPT_NAME'] = '/index.php';
$_SERVER['SCRIPT_FILENAME'] = __DIR__ . '/public/index.php';

require __DIR__ . '/public/index.php';

