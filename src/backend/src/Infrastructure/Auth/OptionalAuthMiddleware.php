<?php

/**
 * Middleware autentikasi opsional JWT.
 *
 * Middleware ini menambahkan `auth_user` jika token valid, tetapi tetap
 * melanjutkan request publik saat token tidak ada atau tidak valid.
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

use Scapes\Application\Contracts\Auth\TokenDenylistInterface;

/**
 * Kelas OptionalAuthMiddleware - Autentikasi tanpa memaksa login.
 */
class OptionalAuthMiddleware {

  /**
   * Manager JWT.
   *
   * @var JWTManager
   */
  private JWTManager $jwtManager;

  /**
   * Penyimpanan denylist token.
   *
   * @var TokenDenylistInterface
   */
  private TokenDenylistInterface $denylist;

  /**
   * Konstruktor OptionalAuthMiddleware.
   *
   * @param JWTManager $jwtManager Manager JWT.
   * @param TokenDenylistInterface $denylist Denylist token Redis.
   */
  public function __construct(
    JWTManager $jwtManager,
    TokenDenylistInterface $denylist
  ) {
    $this->jwtManager = $jwtManager;
    $this->denylist = $denylist;
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
    $params['auth_user'] = null;
    $params['auth_token'] = null;

    $token = $this->resolveToken();
    if ($token !== null) {
      $payload = $this->jwtManager->validateAndDecode($token);
      if (
        $payload !== null
        && !$this->denylist->isDenied((string) $payload['jti'])
      ) {
        $params['auth_user'] = $payload;
        $params['auth_token'] = $token;
      }
    }

    return $next($params);
  }

  /**
   * Ambil token dari Bearer header atau cookie.
   *
   * @return string|null Token JWT jika tersedia.
   */
  private function resolveToken(): ?string {
    $authHeader = $_SERVER['HTTP_AUTHORIZATION']
      ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION']
      ?? '';

    if (str_starts_with($authHeader, 'Bearer ')) {
      return trim(substr($authHeader, 7));
    }

    $cookieToken = $_COOKIE[AuthMiddleware::COOKIE_NAME] ?? null;
    if (is_string($cookieToken) && $cookieToken !== '') {
      return $cookieToken;
    }

    return null;
  }
}
