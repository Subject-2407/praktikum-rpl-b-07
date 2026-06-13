<?php

/**
 * Helper untuk membentuk response HTTP JSON.
 *
 * Kelas ini menjaga controller tetap ringkas dan memastikan semua response
 * memakai envelope yang sama dengan kontrak API.
 *
 * @package Scapes\Interfaces\Http
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http;

/**
 * Kelas Response - Factory response JSON.
 */
class Response {

  /**
   * Membuat response sukses.
   *
   * @param string $message Pesan sukses.
   * @param mixed $data Data response.
   * @param int $statusCode Kode HTTP.
   * @param array<string, int>|null $meta Metadata pagination.
   *
   * @return array<string, mixed>
   */
  public static function success(
    string $message,
    mixed $data = null,
    int $statusCode = 200,
    ?array $meta = null
  ): array {
    $response = [
      'success' => true,
      'status_code' => $statusCode,
      'message' => $message,
      'data' => $data,
    ];

    if ($meta !== null) {
      $response['meta'] = $meta;
    }

    return $response;
  }

  /**
   * Membuat response error.
   *
   * @param string $message Pesan error.
   * @param int $statusCode Kode HTTP.
   * @param array<string, mixed>|null $errors Detail error.
   *
   * @return array<string, mixed>
   */
  public static function error(
    string $message,
    int $statusCode,
    ?array $errors = null
  ): array {
    return [
      'success' => false,
      'status_code' => $statusCode,
      'message' => $message,
      'errors' => $errors,
    ];
  }

  /**
   * Membuat response error internal dengan detail exception saat debug aktif.
   *
   * @param \Throwable $e Exception yang terjadi.
   * @param string $message Pesan umum response.
   *
   * @return array<string, mixed>
   */
  public static function internalErrorFromThrowable(
    \Throwable $e,
    string $message = 'Internal server error.'
  ): array {
    return self::error(
      $message,
      500,
      self::isDebugEnabled()
        ? [
          'exception' => get_class($e),
          'debug_message' => $e->getMessage(),
          'file' => $e->getFile(),
          'line' => $e->getLine(),
        ]
        : null
    );
  }

  /**
   * Mengecek apakah response boleh menampilkan detail debug.
   *
   * @return bool
   */
  private static function isDebugEnabled(): bool {
    if (defined('APP_DEBUG')) {
      return APP_DEBUG === true;
    }

    $debugValue = $_ENV['APP_DEBUG'] ?? $_SERVER['APP_DEBUG'] ?? null;
    if ($debugValue === null) {
      return (defined('ENVIRONMENT') && ENVIRONMENT !== 'production');
    }

    return filter_var($debugValue, FILTER_VALIDATE_BOOLEAN);
  }
}
