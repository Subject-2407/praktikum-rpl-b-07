<?php

/**
 * Authorization Exception
 *
 * Exception yang dilempar ketika user tidak memiliki permission untuk aksi tertentu.
 * Misalnya: user bukan admin saat mengakses fitur admin.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas AuthorizationException - Exception untuk error otorisasi.
 *
 * @class AuthorizationException
 */
class AuthorizationException extends Exception {

  /**
   * Konstruktor AuthorizationException.
   *
   * @param string $message Pesan error.
   * @param int $code Kode error (default 0).
   */
  public function __construct(string $message = '', int $code = 0) {
    parent::__construct($message, $code);
  }
}
