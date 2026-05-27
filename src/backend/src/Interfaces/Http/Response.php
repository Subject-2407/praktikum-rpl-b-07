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
   * @param array<string, array<int, string>>|null $errors Detail error.
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
}
