<?php

/**
 * Not Found Exception
 *
 * Exception yang dilempar ketika resource yang dicari tidak ditemukan di database.
 * Misalnya: user tidak ditemukan, wallpaper tidak ditemukan, dll.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas NotFoundException - Exception untuk resource yang tidak ditemukan.
 *
 * @class NotFoundException
 */
class NotFoundException extends Exception {

  /**
   * Konstruktor NotFoundException.
   *
   * @param string $message Pesan error.
   * @param int $code Kode error (default 0).
   */
  public function __construct(string $message = '', int $code = 0) {
    parent::__construct($message, $code);
  }
}
