<?php

declare(strict_types=1);

namespace Scapes\Infrastructure\Routing;

/**
 * Router sederhana untuk menangani request HTTP dan routing ke controller.
 * Mendukung REST endpoints dengan parameter dinamis.
 */
class Router
{
  /** @var array<string, array<string, array{handler: callable, middlewares: array}>> */
  private array $routes = [];

  /** @var array<callable> */
  private array $globalMiddlewares = [];

  /**
   * Daftarkan route GET.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @param array $middlewares Middleware khusus untuk route ini
   * @return self
   */
  public function get(string $path, callable $handler, array $middlewares = []): self
  {
    return $this->register('GET', $path, $handler, $middlewares);
  }

  /**
   * Daftarkan route POST.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @param array $middlewares Middleware khusus untuk route ini
   * @return self
   */
  public function post(string $path, callable $handler, array $middlewares = []): self
  {
    return $this->register('POST', $path, $handler, $middlewares);
  }

  /**
   * Daftarkan route DELETE.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @param array $middlewares Middleware khusus untuk route ini
   * @return self
   */
  public function delete(string $path, callable $handler, array $middlewares = []): self
  {
    return $this->register('DELETE', $path, $handler, $middlewares);
  }

  /**
   * Tambahkan middleware global.
   *
   * @param callable $middleware
   * @return self
   */
  public function use(callable $middleware): self
  {
    $this->globalMiddlewares[] = $middleware;
    return $this;
  }

  /**
   * Daftarkan route untuk method HTTP tertentu.
   *
   * @param string $method HTTP method (GET, POST, DELETE, etc.)
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @param array $middlewares Middleware untuk route ini
   * @return self
   */
  private function register(
    string $method,
    string $path,
    callable $handler,
    array $middlewares = []
  ): self {
    $method = strtoupper($method);
    if (!isset($this->routes[$method])) {
      $this->routes[$method] = [];
    }

    $this->routes[$method][$path] = [
      'handler' => $handler,
      'middlewares' => $middlewares
    ];
    return $this;
  }

  /**
   * Jalankan router dan matching terhadap request.
   * Mengirim response dan exit.
   *
   * @return void
   */
  public function dispatch(): void
  {
    $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
    $path = $this->getPath();

    // Cari route yang cocok
    $match = $this->match($method, $path);

    if ($match === null) {
      $this->sendNotFound();
      return;
    }

    [$routeData, $params] = $match;
    $handler = $routeData['handler'];
    $middlewares = array_merge($this->globalMiddlewares, $routeData['middlewares']);

    // Jalankan middleware chain
    $response = $this->runMiddlewareChain($middlewares, $handler, $params);

    // Kirim response
    $this->sendResponse($response);
  }

  /**
   * Menjalankan chain middleware dan handler terakhir.
   *
   * @param array $middlewares
   * @param callable $handler
   * @param array $params
   * @return array
   */
  private function runMiddlewareChain(array $middlewares, callable $handler, array $params): array
  {
    $next = function (array $currentParams) use (&$middlewares, $handler, &$next) {
      if (empty($middlewares)) {
        return call_user_func($handler, $currentParams);
      }

      $middleware = array_shift($middlewares);
      return call_user_func($middleware, $currentParams, $next);
    };

    return $next($params);
  }

  /**
   * Cari route yang sesuai dengan method dan path.
   *
   * @param string $method HTTP method
   * @param string $path Request path
   * @return array{array, array<string, string>}|null Route data dan params atau null
   */
  private function match(string $method, string $path): ?array
  {
    if (!isset($this->routes[$method])) {
      return null;
    }

    // Normalisasi path: trim slashes
    $normalizedPath = trim($path, '/');

    foreach ($this->routes[$method] as $routePath => $routeData) {
      $params = $this->matchPath($routePath, $normalizedPath);
      if ($params !== null) {
        return [$routeData, $params];
      }
    }

    return null;
  }

  /**
   * Cocokkan path pattern dengan request path.
   * Contoh: /wallpaper/{id} akan cocok dengan /wallpaper/5
   *
   * @param string $pattern Pattern route dengan {param}
   * @param string $path Request path (normalized, no leading slash)
   * @return array<string, string>|null Parameter yang diambil atau null
   */
  private function matchPath(string $pattern, string $path): ?array
  {
    // Normalisasi pattern: trim slashes
    $normalizedPattern = trim($pattern, '/');

    // Escape special regex characters kecuali {param}
    $regex = preg_quote($normalizedPattern, '#');

    // Ganti {param} dengan capture group
    // Jika nama param adalah 'path', izinkan karakter slash (wildcard)
    $regex = preg_replace(
      '#\\\{path\\\}#',
      '(.+)',
      $regex
    );

    // Ganti parameter lainnya dengan standard capture group (tanpa slash)
    $regex = preg_replace(
      '#\\\{([a-zA-Z_][a-zA-Z0-9_]*)\\\}#',
      '([^/]+)',
      $regex
    );

    $regex = "#^{$regex}$#";

    if (!preg_match($regex, $path, $matches)) {
      return null;
    }

    // Extract parameter names dari pattern
    preg_match_all(
      '#\{([a-zA-Z_][a-zA-Z0-9_]*)\}#',
      $normalizedPattern,
      $paramNames
    );

    $params = [];
    for ($i = 0; $i < count($paramNames[1]); $i++) {
      $params[$paramNames[1][$i]] = $matches[$i + 1];
    }

    return $params;
  }

  /**
   * Ambil request path dari URL.
   * Mencoba multiple methods untuk mendapatkan path yang benar,
   * khususnya untuk mendukung Apache rewrite.
   *
   * @return string Request path tanpa query string
   */
  private function getPath(): string
  {
    // Method 1: Coba gunakan REQUEST_URI (paling reliable)
    $uri = $_SERVER['REQUEST_URI'] ?? null;

    // Method 2: Jika REQUEST_URI tidak ada, rekonstruksi dari SCRIPT_NAME + PATH_INFO
    if (!$uri && isset($_SERVER['SCRIPT_NAME'])) {
      $scriptName = $_SERVER['SCRIPT_NAME'];
      // Dapatkan hanya directory dari script name
      $scriptDir = dirname($scriptName);
      if ($scriptDir === '\\') {
        $scriptDir = '/';
      }
      
      // Gabungkan dengan PATH_INFO jika ada
      $pathInfo = $_SERVER['PATH_INFO'] ?? '';
      $uri = $pathInfo ?: $scriptDir;
    }

    // Default ke '/' jika tidak bisa mendapatkan path
    if (!$uri) {
      $uri = '/';
    }

    // Hapus query string
    if (($pos = strpos($uri, '?')) !== false) {
      $uri = substr($uri, 0, $pos);
    }

    // Hapus base path jika ada (untuk deployment di subdirectory)
    $basePath = dirname($_SERVER['SCRIPT_NAME'] ?? '');
    if ($basePath !== '/' && $basePath !== '\\' && strpos($uri, $basePath) === 0) {
      $uri = substr($uri, strlen($basePath));
    }

    // Pastikan path dimulai dengan /
    if (empty($uri)) {
      $uri = '/';
    }

    return $uri;
  }

  /**
   * Kirim response 404 Not Found.
   *
   * @return void
   */
  private function sendNotFound(): void
  {
    http_response_code(404);
    echo json_encode([
      'success' => false,
      'status_code' => 404,
      'message' => 'Endpoint tidak ditemukan',
      'data' => [],
    ]);
  }

  /**
   * Kirim response dengan format JSON.
   *
   * @param array<string, mixed> $response Data response
   * @return void
   */
  private function sendResponse(array $response): void
  {
    $statusCode = $response['status_code'] ?? 200;
    http_response_code($statusCode);

    header('Content-Type: application/json');
    echo json_encode($response);
  }
}
