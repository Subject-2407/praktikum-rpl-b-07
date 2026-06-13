<?php

/**
 * File Bootstrap Aplikasi Scapes Backend
 *
 * File ini digunakan untuk menginisialisasi seluruh konfigurasi dan environment
 * aplikasi Scapes sebelum request diproses oleh aplikasi.
 *
 * @package Scapes\Config
 * @version 1.0
 */

declare(strict_types=1);

// Tentukan base path aplikasi
if (!defined('BASE_PATH')) {
    define('BASE_PATH', dirname(__DIR__));
}

// Load environment variables dari file .env TERLEBIH DAHULU
if (file_exists(BASE_PATH . '/.env')) {
    $dotenv = Dotenv\Dotenv::createImmutable(BASE_PATH);
    $dotenv->load();
}

// Tentukan environment saat ini
if (!defined('ENVIRONMENT')) {
    define('ENVIRONMENT', $_ENV['APP_ENV'] ?? $_SERVER['APP_ENV'] ?? 'development');
}

// Tentukan debug mode aplikasi
if (!defined('APP_DEBUG')) {
    $debugValue = $_ENV['APP_DEBUG'] ?? $_SERVER['APP_DEBUG'] ?? null;
    $isDebug = $debugValue === null
      ? ENVIRONMENT !== 'production'
      : filter_var($debugValue, FILTER_VALIDATE_BOOLEAN);
    define('APP_DEBUG', $isDebug);
}

// Konfigurasi error reporting berdasarkan environment
if (!APP_DEBUG) {
    error_reporting(E_ALL);
    ini_set('display_errors', '0');
} else {
    error_reporting(E_ALL);
    ini_set('display_errors', '1');
}

// Set timezone aplikasi
date_default_timezone_set($_ENV['APP_TIMEZONE'] ?? $_SERVER['APP_TIMEZONE'] ?? 'UTC');

// Aktifkan output buffering untuk header manipulation
ob_start();
