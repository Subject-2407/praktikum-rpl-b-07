<?php

/**
 * Middleware autentikasi JWT.
 *
 * Middleware ini membaca token dari header Authorization Bearer atau cookie
 * HttpOnly, memvalidasi JWT, lalu menolak token yang JTI-nya ada di Redis.
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

use Scapes\Application\Contracts\Auth\TokenDenylistInterface;

/**
 * Kelas AuthMiddleware - Proteksi route dengan JWT dan role.
 */
class AuthMiddleware {

  /**
   * Nama cookie penyimpan JWT.
   *
   * @var string
   */
  public const COOKIE_NAME = 'scapes_access_token';

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
   * Role persis yang dibutuhkan route.
   *
   * @var string|null
   */
  private ?string $requiredRole;

  /**
   * Konstruktor AuthMiddleware.
   *
   * @param JWTManager $jwtManager Manager JWT.
   * @param TokenDenylistInterface $denylist Denylist token Redis.
   * @param string|null $requiredRole Role yang dibutuhkan.
   */
  public function __construct(
    JWTManager $jwtManager,
    TokenDenylistInterface $denylist,
    ?string $requiredRole = null
  ) {
    $this->jwtManager = $jwtManager;
    $this->denylist = $denylist;
    $this->requiredRole = $requiredRole;
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
    $token = $this->resolveToken();
    if ($token === null) {
      return $this->unauthorizedResponse();
    }

    $payload = $this->jwtManager->validateAndDecode($token);
    if ($payload === null || $this->denylist->isDenied((string) $payload['jti'])) {
      return $this->unauthorizedResponse();
    }

    if (!$this->hasRequiredRole((string) ($payload['role'] ?? ''))) {
      return [
        'success' => false,
        'status_code' => 403,
        'message' => 'Forbidden. You do not have access to this resource.',
        'errors' => null,
      ];
    }

    $params['auth_user'] = $payload;
    $params['auth_token'] = $token;

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

    $cookieToken = $_COOKIE[self::COOKIE_NAME] ?? null;
    if (is_string($cookieToken) && $cookieToken !== '') {
      return $cookieToken;
    }

    return null;
  }

  /**
   * Mengecek apakah role pengguna boleh mengakses route.
   *
   * @param string $role Role dari payload JWT.
   *
   * @return bool True jika role boleh.
   */
  private function hasRequiredRole(string $role): bool {
    if ($this->requiredRole === null) {
      return true;
    }

    return $role === $this->requiredRole;
  }

  /**
   * Membuat respons unauthorized standar.
   *
   * @return array<string, mixed> Respons JSON.
   */
  private function unauthorizedResponse(): array {
    return [
      'success' => false,
      'status_code' => 401,
      'message' => 'Unauthorized. Please log in.',
      'errors' => null,
    ];
  }
}
