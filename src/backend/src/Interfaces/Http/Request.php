<?php

/**
 * Helper untuk membaca input HTTP.
 *
 * Kelas ini mengisolasi akses superglobal dari controller dan use case agar
 * application layer tetap bebas dari detail HTTP.
 *
 * @package Scapes\Interfaces\Http
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http;

/**
 * Kelas Request - Adapter superglobal HTTP.
 */
class Request {

  /**
   * Membaca body JSON request.
   *
   * @return array<string, mixed>
   */
  public static function json(): array {
    $input = file_get_contents('php://input');
    if ($input === false || trim($input) === '') {
      return [];
    }

    $decoded = json_decode($input, true);
    return is_array($decoded) ? $decoded : [];
  }

  /**
   * Membaca data form request.
   *
   * @return array<string, mixed>
   */
  public static function form(): array {
    return $_POST;
  }

  /**
   * Membaca query string request.
   *
   * @return array<string, mixed>
   */
  public static function query(): array {
    return $_GET;
  }

  /**
   * Membaca file upload dari field tertentu.
   *
   * @param string $field Nama field file.
   *
   * @return array<string, mixed>|null Data file upload.
   */
  public static function file(string $field): ?array {
    $file = $_FILES[$field] ?? null;
    return is_array($file) ? $file : null;
  }

  /**
   * Mendapatkan IP address client.
   *
   * @return string IP address.
   */
  public static function ipAddress(): string {
    $forwardedFor = $_SERVER['HTTP_X_FORWARDED_FOR'] ?? '';
    if (is_string($forwardedFor) && $forwardedFor !== '') {
      $parts = explode(',', $forwardedFor);
      return trim($parts[0]);
    }

    return (string) ($_SERVER['REMOTE_ADDR'] ?? '0.0.0.0');
  }

  /**
   * Membaca base URL request saat ini.
   *
   * @return string Base URL tanpa trailing slash.
   */
  public static function baseUrl(): string {
    $isHttps = (
      ($_SERVER['HTTPS'] ?? '') === 'on'
      || ($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https'
    );
    $scheme = $isHttps ? 'https' : 'http';
    $host = (string) ($_SERVER['HTTP_HOST'] ?? 'localhost:8000');

    return $scheme . '://' . $host;
  }
}
