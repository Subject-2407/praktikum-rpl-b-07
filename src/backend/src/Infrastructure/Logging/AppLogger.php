<?php

/**
 * Logger aplikasi sederhana untuk error tracking backend.
 *
 * @package Scapes\Infrastructure\Logging
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Logging;

/**
 * Kelas AppLogger - Menulis log JSON ke storage/logs/app.log.
 */
class AppLogger {

  /**
   * Daftar field sensitif yang harus disamarkan.
   *
   * @var array<int, string>
   */
  private const SENSITIVE_KEYS = [
    'password',
    'password_confirmation',
    'token',
    'authorization',
    'cookie',
    'set-cookie',
    'smtp_password',
    'mail_password',
  ];

  /**
   * Menulis detail throwable beserta context request ke app.log.
   *
   * @param string $event Nama event log.
   * @param \Throwable $e Exception yang terjadi.
   * @param array<string, mixed> $context Context tambahan.
   *
   * @return void
   */
  public static function logThrowable(
    string $event,
    \Throwable $e,
    array $context = []
  ): void {
    $payload = [
      'time' => gmdate('Y-m-d\TH:i:s\Z'),
      'event' => $event,
      'request' => self::buildRequestContext(),
      'context' => self::sanitize($context),
      'exception' => [
        'class' => get_class($e),
        'message' => $e->getMessage(),
        'code' => $e->getCode(),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString(),
      ],
    ];

    self::write($payload);
  }

  /**
   * Menulis data arbitrary ke log aplikasi.
   *
   * @param string $event Nama event log.
   * @param array<string, mixed> $context Context log.
   *
   * @return void
   */
  public static function log(string $event, array $context = []): void {
    self::write([
      'time' => gmdate('Y-m-d\TH:i:s\Z'),
      'event' => $event,
      'request' => self::buildRequestContext(),
      'context' => self::sanitize($context),
    ]);
  }

  /**
   * Menulis payload JSON ke file log.
   *
   * @param array<string, mixed> $payload Payload log.
   *
   * @return void
   */
  private static function write(array $payload): void {
    if (!defined('BASE_PATH')) {
      return;
    }

    $logDir = BASE_PATH . DIRECTORY_SEPARATOR . 'storage'
      . DIRECTORY_SEPARATOR . 'logs';
    if (!is_dir($logDir)) {
      @mkdir($logDir, 0755, true);
    }

    @file_put_contents(
      $logDir . DIRECTORY_SEPARATOR . 'app.log',
      json_encode($payload, JSON_UNESCAPED_SLASHES) . PHP_EOL,
      FILE_APPEND
    );
  }

  /**
   * Membentuk context request dari superglobal.
   *
   * @return array<string, mixed>
   */
  private static function buildRequestContext(): array {
    $headers = [];

    foreach ($_SERVER as $key => $value) {
      if (!str_starts_with($key, 'HTTP_')) {
        continue;
      }

      $headerName = strtolower(str_replace('_', '-', substr($key, 5)));
      $headers[$headerName] = $value;
    }

    return [
      'method' => (string) ($_SERVER['REQUEST_METHOD'] ?? ''),
      'uri' => (string) ($_SERVER['REQUEST_URI'] ?? ''),
      'host' => (string) ($_SERVER['HTTP_HOST'] ?? ''),
      'ip_address' => (string) ($_SERVER['REMOTE_ADDR'] ?? ''),
      'user_agent' => (string) ($_SERVER['HTTP_USER_AGENT'] ?? ''),
      'headers' => self::sanitize($headers),
    ];
  }

  /**
   * Menyamarkan field sensitif secara rekursif.
   *
   * @param mixed $value Nilai yang akan disanitasi.
   * @param string|null $key Nama key parent.
   *
   * @return mixed
   */
  private static function sanitize(mixed $value, ?string $key = null): mixed {
    if ($key !== null && self::isSensitiveKey($key)) {
      return '[REDACTED]';
    }

    if (is_array($value)) {
      $sanitized = [];
      foreach ($value as $childKey => $childValue) {
        $childKeyString = is_string($childKey) ? $childKey : (string) $childKey;
        $sanitized[$childKey] = self::sanitize($childValue, $childKeyString);
      }

      return $sanitized;
    }

    if (is_object($value)) {
      return self::sanitize((array) $value, $key);
    }

    return $value;
  }

  /**
   * Mengecek apakah key termasuk data sensitif.
   *
   * @param string $key Nama key.
   *
   * @return bool
   */
  private static function isSensitiveKey(string $key): bool {
    $normalized = strtolower(trim($key));
    return in_array($normalized, self::SENSITIVE_KEYS, true);
  }
}
