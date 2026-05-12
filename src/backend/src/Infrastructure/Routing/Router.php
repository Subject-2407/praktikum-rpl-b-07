<?php

declare(strict_types=1);

namespace Scapes\Infrastructure\Routing;

/**
 * Router sederhana untuk menangani request HTTP dan routing ke controller.
 * Mendukung REST endpoints dengan parameter dinamis.
 */
class Router
{
  /** @var array<string, array<string, callable>> */
  private array $routes = [];

  /**
   * Daftarkan route GET.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @return self
   */
  public function get(string $path, callable $handler): self
  {
    return $this->register('GET', $path, $handler);
  }

  /**
   * Daftarkan route POST.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @return self
   */
  public function post(string $path, callable $handler): self
  {
    return $this->register('POST', $path, $handler);
  }

  /**
   * Daftarkan route DELETE.
   *
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @return self
   */
  public function delete(string $path, callable $handler): self
  {
    return $this->register('DELETE', $path, $handler);
  }

  /**
   * Daftarkan route untuk method HTTP tertentu.
   *
   * @param string $method HTTP method (GET, POST, DELETE, etc.)
   * @param string $path Path route dengan parameter {param}
   * @param callable $handler Handler callback
   * @return self
   */
  private function register(
    string $method,
    string $path,
    callable $handler
  ): self {
    $method = strtoupper($method);
    if (!isset($this->routes[$method])) {
      $this->routes[$method] = [];
    }

    $this->routes[$method][$path] = $handler;
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

    [$handler, $params] = $match;

    // Panggil handler dengan parameter
    $response = call_user_func($handler, $params);

    // Kirim response
    $this->sendResponse($response);
  }

  /**
   * Cari route yang sesuai dengan method dan path.
   *
   * @param string $method HTTP method
   * @param string $path Request path
   * @return array{callable, array<string, string>}|null Route match atau null
   */
  private function match(string $method, string $path): ?array
  {
    if (!isset($this->routes[$method])) {
      return null;
    }

    foreach ($this->routes[$method] as $routePath => $handler) {
      $params = $this->matchPath($routePath, $path);
      if ($params !== null) {
        return [$handler, $params];
      }
    }

    return null;
  }

  /**
   * Cocokkan path pattern dengan request path.
   * Contoh: /wallpaper/{id} akan cocok dengan /wallpaper/5
   *
   * @param string $pattern Pattern route dengan {param}
   * @param string $path Request path
   * @return array<string, string>|null Parameter yang diambil atau null
   */
  private function matchPath(string $pattern, string $path): ?array
  {
    // Escape special regex characters kecuali {param}
    $regex = preg_quote($pattern, '#');

    // Ganti {param} dengan capture group
    $regex = preg_replace(
      '#\\\{([a-zA-Z_][a-zA-Z0-9_]*)\\\}#',
      '([a-zA-Z0-9_\-]+)',
      $regex
    );

    $regex = "#^{$regex}$#";

    if (!preg_match($regex, $path, $matches)) {
      return null;
    }

    // Extract parameter names dari pattern
    preg_match_all(
      '#\{([a-zA-Z_][a-zA-Z0-9_]*)\}#',
      $pattern,
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
   *
   * @return string Request path tanpa query string
   */
  private function getPath(): string
  {
    $uri = $_SERVER['REQUEST_URI'] ?? '/';

    // Hapus query string
    if (($pos = strpos($uri, '?')) !== false) {
      $uri = substr($uri, 0, $pos);
    }

    // Hapus base path jika ada (untuk deployment di subdirectory)
    $basePath = dirname($_SERVER['SCRIPT_NAME'] ?? '');
    if ($basePath !== '/' && strpos($uri, $basePath) === 0) {
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
