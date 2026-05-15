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
use Scapes\Infrastructure\Routing\Router;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Infrastructure\Repository\SessionRepository;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\TagRepository;
use Scapes\Infrastructure\Repository\ModerationReviewRepository;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Security\EncryptionManager;
use Scapes\Infrastructure\Storage\FileStorage;

// Set response header
header('Content-Type: application/json');

try {
  // Inisialisasi database connection
  $db = DatabaseConnection::getInstance();

  // Inisialisasi Storage Manager
  $storage = new FileStorage(BASE_PATH . DIRECTORY_SEPARATOR . 'storage');

  // Inisialisasi Security Manager (App key dari .env)
  $appKey = $_ENV['APP_KEY'] ?? 'your_32_chars_secret_key_here_!!!';
  $encryption = new EncryptionManager($appKey);

  // Inisialisasi JWT Manager (Secret key dari .env)
  $jwtSecret = $_ENV['JWT_SECRET'] ?? 'default_secret_key_change_me';
  $jwtManager = new JWTManager($jwtSecret);

  // Setup service container dengan semua repositories dan managers
  $services = [
    'db' => $db,
    'storage' => $storage,
    'encryption' => $encryption,
    'jwtManager' => $jwtManager,
    'userRepository' => new UserRepository($db),
    'sessionRepository' => new SessionRepository($db, $encryption),
    'wallpaperRepository' => new WallpaperRepository($db),
    'categoryRepository' => new CategoryRepository($db),
    'tagRepository' => new TagRepository($db),
    'moderationReviewRepository' => new ModerationReviewRepository($db),
  ];

  // Inisialisasi router
  $router = new Router();

  // Daftarkan welcome endpoint
  $router->get('/', function (array $params) {
    return [
      'success' => true,
      'status_code' => 200,
      'message' => 'Scapes Backend API sedang berjalan',
      'data' => [
        'version' => '1.0',
        'timestamp' => date('Y-m-d H:i:s'),
      ],
    ];
  });

  // Daftarkan semua MVP routes
  require_once __DIR__ . '/../src/Infrastructure/Routing/routes.php';
  $router = \Scapes\Infrastructure\Routing\registerMVPRoutes($router, $services);

  // Jalankan router
  $router->dispatch();
} catch (\Exception $e) {
  http_response_code(500);
  echo json_encode([
    'success' => false,
    'status_code' => 500,
    'message' => 'Internal server error',
    'data' => [],
  ]);
}
