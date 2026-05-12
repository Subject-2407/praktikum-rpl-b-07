<?php

/**
 * Bootstrap file untuk PHPUnit tests
 *
 * Memuat autoloader dan mengatur environment untuk testing.
 */

declare(strict_types=1);

// Load composer autoloader
require_once __DIR__ . '/../vendor/autoload.php';

// Load environment variables jika ada file .env
if (file_exists(__DIR__ . '/../.env')) {
  $dotenv = Dotenv\Dotenv::createImmutable(__DIR__ . '/..');
  $dotenv->load();
}
