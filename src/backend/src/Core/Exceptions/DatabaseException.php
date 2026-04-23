<?php

/**
 * Exception untuk Database Error
 *
 * Kelas ini digunakan untuk menangani error yang berkaitan dengan database.
 * Merupakan custom exception yang mewarisi dari Exception.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas DatabaseException - Custom exception untuk database error.
 *
 * @class DatabaseException
 */
class DatabaseException extends Exception {

  /**
   * Konstruktor DatabaseException.
   *
   * @param string $message Pesan error yang akan ditampilkan.
   * @param int $code Kode error (default: 0).
   * @param Exception|null $previous Exception sebelumnya untuk error chaining.
   */
  public function __construct(
    string $message = '',
    int $code = 0,
    ?Exception $previous = null
  ) {
    parent::__construct($message, $code, $previous);
  }
}
