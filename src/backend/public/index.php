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
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\TagRepository;
use Scapes\Infrastructure\Repository\ModerationReviewRepository;
use Scapes\Infrastructure\Repository\ApiSourceRepository;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Auth\RedisTokenDenylist;
use Scapes\Infrastructure\Storage\FileStorage;

// Set response header
header('Content-Type: application/json');

try {
  // Inisialisasi database connection
  $db = DatabaseConnection::getInstance();

  // Inisialisasi Storage Manager
  $storage = new FileStorage(BASE_PATH . DIRECTORY_SEPARATOR . 'storage');

  // Inisialisasi JWT Manager (Secret key dari .env)
  $jwtSecret = $_ENV['JWT_SECRET'] ?? '';
  if ($jwtSecret === '') {
    throw new \RuntimeException('JWT_SECRET belum dikonfigurasi.');
  }
  $jwtManager = new JWTManager($jwtSecret);

  // Inisialisasi denylist JWT berbasis Redis (Predis)
  $redisConfig = [
    'scheme' => $_ENV['REDIS_SCHEME'] ?? 'tcp',
    'host' => $_ENV['REDIS_HOST'] ?? '127.0.0.1',
    'port' => (int) ($_ENV['REDIS_PORT'] ?? 6379),
    'database' => (int) ($_ENV['REDIS_DATABASE'] ?? 0),
    'prefix' => $_ENV['REDIS_PREFIX'] ?? 'scapes:',
  ];
  if (($_ENV['REDIS_PASSWORD'] ?? '') !== '') {
    $redisConfig['password'] = $_ENV['REDIS_PASSWORD'];
  }

  $tokenDenylist = new RedisTokenDenylist($redisConfig);

  // Setup service container dengan semua repositories dan managers
  $services = [
    'db' => $db,
    'storage' => $storage,
    'jwtManager' => $jwtManager,
    'tokenDenylist' => $tokenDenylist,
    'userRepository' => new UserRepository($db),
    'wallpaperRepository' => new WallpaperRepository($db),
    'categoryRepository' => new CategoryRepository($db),
    'tagRepository' => new TagRepository($db),
    'moderationReviewRepository' => new ModerationReviewRepository($db),
    'apiSourceRepository' => new ApiSourceRepository($db),
  ];

  // Inisialisasi router
  $router = new Router();

  // Daftarkan welcome endpoint
  $router->get('/', function (array $params) {
    return [
      'success' => true,
      'status_code' => 200,
      'message' => 'Scapes Backend API is running.',
      'data' => [
        'version' => '1.1.0',
        'timestamp' => gmdate('Y-m-d\TH:i:s\Z'),
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
    'message' => 'Internal server error.',
    'errors' => null,
  ], JSON_UNESCAPED_SLASHES);
}
