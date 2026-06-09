<?php

/**
 * Manager untuk JSON Web Token.
 *
 * Kelas ini membuat dan memvalidasi JWT HS256 tanpa menyimpan state di
 * database. Pencabutan token dilakukan melalui denylist Redis berdasarkan
 * klaim `jti`.
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

/**
 * Kelas JWTManager - Membuat dan memvalidasi token JWT.
 */
class JWTManager {

  /**
   * Algoritma signature JWT.
   *
   * @var string
   */
  private const ALGORITHM = 'HS256';

  /**
   * Secret key untuk signature token.
   *
   * @var string
   */
  private string $secretKey;

  /**
   * Masa berlaku token dalam detik.
   *
   * @var int
   */
  private int $ttl;

  /**
   * Konstruktor JWTManager.
   *
   * @param string $secretKey Secret key dari environment.
   * @param int $ttlMinutes Masa berlaku token dalam menit.
   */
  public function __construct(string $secretKey, int $ttlMinutes = 30) {
    $this->secretKey = $secretKey;
    $this->ttl = $ttlMinutes * 60;
  }

  /**
   * Membuat JWT baru untuk pengguna.
   *
   * @param array<string, mixed> $payload Klaim tambahan token.
   *
   * @return array{token: string, exp: int, jti: string}
   */
  public function createToken(array $payload): array {
    $now = time();
    $jti = bin2hex(random_bytes(16));

    $claims = array_merge($payload, [
      'iat' => $now,
      'exp' => $now + $this->ttl,
      'jti' => $jti,
    ]);

    $header = [
      'typ' => 'JWT',
      'alg' => self::ALGORITHM,
    ];

    $headerEncoded = $this->base64UrlEncode(
      (string) json_encode($header, JSON_THROW_ON_ERROR)
    );
    $payloadEncoded = $this->base64UrlEncode(
      (string) json_encode($claims, JSON_THROW_ON_ERROR)
    );

    $signature = hash_hmac(
      'sha256',
      "{$headerEncoded}.{$payloadEncoded}",
      $this->secretKey,
      true
    );

    return [
      'token' => "{$headerEncoded}.{$payloadEncoded}."
        . $this->base64UrlEncode($signature),
      'exp' => (int) $claims['exp'],
      'jti' => $jti,
    ];
  }

  /**
   * Validasi signature, algoritma, dan expiry token.
   *
   * @param string $token Token JWT dari client.
   *
   * @return array<string, mixed>|null Payload jika valid.
   */
  public function validateAndDecode(string $token): ?array {
    try {
      $parts = explode('.', $token);
      if (count($parts) !== 3) {
        return null;
      }

      [$headerEncoded, $payloadEncoded, $signatureEncoded] = $parts;
      $headerJson = $this->base64UrlDecode($headerEncoded);
      $header = json_decode($headerJson, true, 512, JSON_THROW_ON_ERROR);

      if (($header['alg'] ?? '') !== self::ALGORITHM) {
        return null;
      }

      $expectedSignature = hash_hmac(
        'sha256',
        "{$headerEncoded}.{$payloadEncoded}",
        $this->secretKey,
        true
      );

      if (!hash_equals(
        $this->base64UrlEncode($expectedSignature),
        $signatureEncoded
      )) {
        return null;
      }

      $payloadJson = $this->base64UrlDecode($payloadEncoded);
      $payload = json_decode($payloadJson, true, 512, JSON_THROW_ON_ERROR);

      if (!is_array($payload)) {
        return null;
      }

      if (!isset($payload['exp']) || (int) $payload['exp'] < time()) {
        return null;
      }

      if (empty($payload['jti'])) {
        return null;
      }

      return $payload;
    } catch (\Throwable $e) {
      return null;
    }
  }

  /**
   * Mendapatkan expiry datetime ISO 8601 dari timestamp.
   *
   * @param int $expiresAt Unix timestamp expiry.
   *
   * @return string Datetime ISO 8601 UTC.
   */
  public function formatExpiresAt(int $expiresAt): string {
    return gmdate('Y-m-d\TH:i:s\Z', $expiresAt);
  }

  /**
   * Base64 URL encode untuk JWT.
   *
   * @param string $data Data mentah.
   *
   * @return string Data encoded URL-safe.
   */
  private function base64UrlEncode(string $data): string {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
  }

  /**
   * Base64 URL decode untuk JWT.
   *
   * @param string $data Data encoded URL-safe.
   *
   * @return string Data mentah.
   */
  private function base64UrlDecode(string $data): string {
    $padding = strlen($data) % 4;
    if ($padding > 0) {
      $data .= str_repeat('=', 4 - $padding);
    }

    return (string) base64_decode(strtr($data, '-_', '+/'), true);
  }
}
