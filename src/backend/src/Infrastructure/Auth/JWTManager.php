<?php

/**
 * Manager untuk JWT Token
 *
 * Kelas ini menangani pembuatan dan validasi JWT token.
 * Menggunakan built-in PHP hash functions, tanpa library eksternal.
 *
 * @package Scapes\Infrastructure\Auth
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

class JWTManager {

  /**
   * Secret key untuk signing token.
   *
   * @var string
   */
  private string $secretKey;

  /**
   * Token lifetime dalam detik.
   *
   * @var int
   */
  private int $ttl;

  /**
   * Konstruktor JWTManager.
   *
   * @param string $secretKey Secret key dari .env
   * @param int $ttlMinutes Lifetime dalam menit
   */
  public function __construct(string $secretKey, int $ttlMinutes = 30) {
    $this->secretKey = $secretKey;
    $this->ttl = $ttlMinutes * 60; // Convert to seconds
  }

  /**
   * Membuat JWT token baru.
   *
   * @param array $payload Data yang akan di-encode (user_id, email, role, dll)
   * @return array ['token' => string, 'exp' => int] Token JWT dan Unix timestamp expiry-nya
   */
  public function createToken(array $payload): array {
    // Header
    $header = json_encode([
        'typ' => 'JWT',
        'alg' => 'HS256',
    ]);

    // Payload dengan issued_at dan expires_at
    $now = time();
    $payload['iat'] = $now;
    $payload['exp'] = $now + $this->ttl;
    $payloadJson = json_encode($payload);

    // Base64 encode (URL-safe)
    $headerEncoded = $this->base64UrlEncode($header);
    $payloadEncoded = $this->base64UrlEncode($payloadJson);

    // Signature
    $signatureInput = "{$headerEncoded}.{$payloadEncoded}";
    $signature = hash_hmac('sha256', $signatureInput, $this->secretKey, true);
    $signatureEncoded = $this->base64UrlEncode($signature);

    return [
        'token' => "{$headerEncoded}.{$payloadEncoded}.{$signatureEncoded}",
        'exp'   => $payload['exp'],
    ];
  }

  /**
   * Validasi dan decode JWT token.
   *
   * @param string $token JWT token
   * @return array|null Payload array jika valid, null jika invalid
   */
  public function validateAndDecode(string $token): ?array {
    try {
      $parts = explode('.', $token);

      if (count($parts) !== 3) {
        return null;
      }

      [$headerEncoded, $payloadEncoded, $signatureEncoded] = $parts;

      // Verify signature
      $signatureInput = "{$headerEncoded}.{$payloadEncoded}";
      $expectedSignature = hash_hmac('sha256', $signatureInput, $this->secretKey, true);
      $expectedSignatureEncoded = $this->base64UrlEncode($expectedSignature);

      if (!hash_equals($signatureEncoded, $expectedSignatureEncoded)) {
        return null;
      }

      // Decode payload
      $payloadJson = $this->base64UrlDecode($payloadEncoded);
      $payload = json_decode($payloadJson, true);

      if (!is_array($payload)) {
        return null;
      }

      // Check expiration
      if (isset($payload['exp']) && $payload['exp'] < time()) {
        return null; // Token sudah kadaluarsa
      }

      return $payload;
    } catch (\Exception $e) {
      return null;
    }
  }

  /**
   * Mendapatkan expiry datetime dari payload.
   *
   * @param array $payload Payload JWT
   * @return string ISO 8601 datetime
   */
  public function getExpiresAt(array $payload): string {
    $expTime = $payload['exp'] ?? time() + $this->ttl;
    return date('Y-m-d\TH:i:s\Z', $expTime);
  }

  /**
   * Base64 URL encode (untuk JWT).
   *
   * @param string $data
   * @return string
   */
  private function base64UrlEncode(string $data): string {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
  }

  /**
   * Base64 URL decode (untuk JWT).
   *
   * @param string $data
   * @return string
   */
  private function base64UrlDecode(string $data): string {
    $padding = strlen($data) % 4;
    if ($padding) {
      $data .= str_repeat('=', 4 - $padding);
    }

    return base64_decode(strtr($data, '-_', '+/'));
  }
}
