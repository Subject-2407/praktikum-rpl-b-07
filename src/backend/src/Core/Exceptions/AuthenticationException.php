<?php

/**
 * Authentication Exception
 *
 * Exception yang dilempar ketika authentication gagal.
 * Misalnya: email/password tidak cocok, token invalid, dll.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas AuthenticationException - Exception untuk error autentikasi.
 *
 * @class AuthenticationException
 * @extends Exception
 */
class AuthenticationException extends Exception {

  /**
   * Konstruktor AuthenticationException.
   *
   * @param string $message Pesan error.
   * @param int $code Kode error (default 0).
   */
  public function __construct(string $message = '', int $code = 0) {
    parent::__construct($message, $code);
  }
}
