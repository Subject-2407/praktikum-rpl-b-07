<?php

/**
 * Entry Point Aplikasi Scapes Backend
 *
 * File ini adalah entry point utama untuk semua request yang masuk ke
 * backend Scapes. Semua routing dan pemrosesan request dimulai dari sini.
 *
 * @package Scapes
 * @version 1.0
 */

declare(strict_types=1);

// Load dependencies dan bootstrap
require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../config/bootstrap.php';

// Import
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Infrastructure\Repository\SessionRepository;
use Scapes\Application\UseCases\Auth\LoginUseCase;
use Scapes\Application\UseCases\Auth\LogoutUseCase;
use Scapes\Interfaces\Http\Controllers\AuthController;

/**
 * Router sederhana untuk menangani request
 */
$method = $_SERVER['REQUEST_METHOD'];
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$basePath = '/src/backend/public';

// Remove base path dari request URI
if (strpos($path, $basePath) === 0) {
  $path = substr($path, strlen($basePath));
}

// Normalisasi path
$path = rtrim($path, '/') ?: '/';

// Set response header
header('Content-Type: application/json');

/**
 * Route handler
 */
if ($path === '/' && $method === 'GET') {
  // Welcome endpoint
  echo json_encode([
      'success' => true,
      'message' => 'Scapes Backend API is running',
      'version' => '1.0',
      'timestamp' => date('Y-m-d H:i:s'),
  ]);
} elseif ($path === '/auth/login' && $method === 'POST') {
  // Login endpoint
  try {
    $db = DatabaseConnection::getInstance();
    $jwtManager = new JWTManager(
      $_ENV['JWT_SECRET'] ?? $_SERVER['JWT_SECRET'] ?? 'please_change_this_secret',
      (int) ($_ENV['JWT_TTL'] ?? $_SERVER['JWT_TTL'] ?? 30)
    );

    $userRepository = new UserRepository($db);
    $sessionRepository = new SessionRepository($db);

    $loginUseCase = new LoginUseCase($userRepository, $sessionRepository, $jwtManager);
    $authController = new AuthController($loginUseCase, new LogoutUseCase($sessionRepository, $jwtManager));

    $authController->login();
  } catch (\Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Internal server error.',
        'errors' => null,
    ]);
  }
} elseif ($path === '/auth/logout' && $method === 'POST') {
  // Logout endpoint
  try {
    $db = DatabaseConnection::getInstance();
    $jwtManager = new JWTManager(
      $_ENV['JWT_SECRET'] ?? $_SERVER['JWT_SECRET'] ?? 'please_change_this_secret',
      (int) ($_ENV['JWT_TTL'] ?? $_SERVER['JWT_TTL'] ?? 30)
    );

    $sessionRepository = new SessionRepository($db);
    $logoutUseCase = new LogoutUseCase($sessionRepository, $jwtManager);
    $userRepository = new UserRepository($db);

    $authController = new AuthController(new LoginUseCase($userRepository, $sessionRepository, $jwtManager), $logoutUseCase);

    $authController->logout();
  } catch (\Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Internal server error.',
        'errors' => null,
    ]);
  }
} else {
  // 404 Not Found
  http_response_code(404);
  echo json_encode([
      'success' => false,
      'message' => 'Endpoint not found.',
      'errors' => null,
  ]);
}
