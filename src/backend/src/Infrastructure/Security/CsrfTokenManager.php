<?php

/**
 * Template manager CSRF token.
 *
 * Kelas ini disiapkan sebagai titik awal implementasi double-submit CSRF
 * token. Tim backend dapat memperketat strategi token, misalnya dengan HMAC
 * yang diturunkan dari JWT/session id.
 *
 * @package Scapes\Infrastructure\Security
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Security;

/**
 * Kelas CsrfTokenManager - Helper generate dan validasi CSRF token.
 */
class CsrfTokenManager {

  /**
   * Nama cookie CSRF yang dapat dibaca frontend.
   */
  public const COOKIE_NAME = 'scapes_csrf_token';

  /**
   * Nama header CSRF yang dikirim frontend.
   */
  public const HEADER_NAME = 'X-CSRF-Token';

  /**
   * Generate token acak untuk cookie CSRF.
   *
   * @return string Token CSRF encoded.
   */
  public function generateToken(): string {
    return bin2hex(random_bytes(32));
  }

  /**
   * Validasi token dari cookie dan header.
   *
   * @param string|null $cookieToken Token dari cookie scapes_csrf_token.
   * @param string|null $headerToken Token dari header X-CSRF-Token.
   *
   * @return bool True jika token tersedia dan cocok.
   */
  public function isValid(?string $cookieToken, ?string $headerToken): bool {
    if (!is_string($cookieToken) || !is_string($headerToken)) {
      return false;
    }

    if ($cookieToken === '' || $headerToken === '') {
      return false;
    }

    return hash_equals($cookieToken, $headerToken);
  }

  /**
   * Template opsi cookie saat menerbitkan CSRF token.
   *
   * Cookie CSRF sengaja tidak HttpOnly agar frontend dapat membaca value-nya
   * dan mengirimkannya kembali melalui header X-CSRF-Token.
   *
   * @param int $expiresAt Unix timestamp expiry.
   * @param bool $isHttps True jika request berjalan melalui HTTPS.
   *
   * @return array<string, mixed> Opsi setcookie.
   */
  public function cookieOptions(int $expiresAt, bool $isHttps): array {
    return [
      'expires' => $expiresAt,
      'path' => '/',
      'secure' => $isHttps,
      'httponly' => false,
      'samesite' => 'Lax',
    ];
  }

  /**
   * Template opsi cookie untuk menghapus CSRF token.
   *
   * @param bool $isHttps True jika request berjalan melalui HTTPS.
   *
   * @return array<string, mixed> Opsi setcookie.
   */
  public function expiredCookieOptions(bool $isHttps): array {
    return $this->cookieOptions(time() - 3600, $isHttps);
  }
}
