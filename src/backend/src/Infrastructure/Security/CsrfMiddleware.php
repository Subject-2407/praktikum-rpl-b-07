<?php

/**
 * Template middleware proteksi CSRF.
 *
 * Middleware ini disiapkan untuk dipasang setelah AuthMiddleware pada route
 * yang mengubah data. Implementasi awal memakai pola double-submit cookie:
 * token di cookie scapes_csrf_token harus cocok dengan header X-CSRF-Token.
 *
 * @package Scapes\Infrastructure\Security
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Security;

/**
 * Kelas CsrfMiddleware - Validasi token CSRF untuk unsafe HTTP methods.
 */
class CsrfMiddleware {

  /**
   * Method yang perlu proteksi CSRF.
   *
   * @var array<int, string>
   */
  private const UNSAFE_METHODS = ['POST', 'PUT', 'PATCH', 'DELETE'];

  /**
   * Manager CSRF token.
   *
   * @var CsrfTokenManager
   */
  private CsrfTokenManager $tokenManager;

  /**
   * Konstruktor CsrfMiddleware.
   *
   * @param CsrfTokenManager|null $tokenManager Manager CSRF token.
   */
  public function __construct(?CsrfTokenManager $tokenManager = null) {
    $this->tokenManager = $tokenManager ?? new CsrfTokenManager();
  }

  /**
   * Jalankan middleware.
   *
   * @param array<string, mixed> $params Parameter request.
   * @param callable $next Handler berikutnya.
   *
   * @return array<string, mixed> Respons route.
   */
  public function __invoke(array $params, callable $next): array {
    if (!$this->requiresCsrfCheck()) {
      return $next($params);
    }

    $cookieToken = $_COOKIE[CsrfTokenManager::COOKIE_NAME] ?? null;
    $headerToken = $this->headerToken();

    if (!$this->tokenManager->isValid($cookieToken, $headerToken)) {
      return [
        'success' => false,
        'status_code' => 403,
        'message' => 'Forbidden. Invalid CSRF token.',
        'errors' => null,
      ];
    }

    return $next($params);
  }

  /**
   * Cek apakah method request perlu validasi CSRF.
   *
   * @return bool True jika method termasuk unsafe method.
   */
  private function requiresCsrfCheck(): bool {
    $method = strtoupper((string) ($_SERVER['REQUEST_METHOD'] ?? 'GET'));

    return in_array($method, self::UNSAFE_METHODS, true);
  }

  /**
   * Ambil token dari header X-CSRF-Token.
   *
   * PHP mengubah header X-CSRF-Token menjadi HTTP_X_CSRF_TOKEN.
   *
   * @return string|null Token dari header.
   */
  private function headerToken(): ?string {
    $token = $_SERVER['HTTP_X_CSRF_TOKEN'] ?? null;

    return is_string($token) ? $token : null;
  }
}
