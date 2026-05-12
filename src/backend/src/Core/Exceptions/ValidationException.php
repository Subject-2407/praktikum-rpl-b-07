<?php

/**
 * Validation Exception
 *
 * Exception yang dilempar ketika validasi data gagal.
 * Biasanya digunakan di Application layer saat validasi input.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas ValidationException - Exception untuk error validasi.
 *
 * @class ValidationException
 * @extends Exception
 */
class ValidationException extends Exception {

  /**
   * Konstruktor ValidationException.
   *
   * @param string $message Pesan error.
   * @param int $code Kode error (default 0).
   */
  public function __construct(string $message = '', int $code = 0) {
    parent::__construct($message, $code);
  }
}
