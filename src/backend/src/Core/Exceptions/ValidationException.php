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
 */
class ValidationException extends Exception {

  /**
   * Daftar error validasi per field.
   *
   * @var array<string, array<int, string>>
   */
  private array $errors;

  /**
   * Konstruktor ValidationException.
   *
   * @param string $message Pesan error.
   * @param int $code Kode error (default 0).
   * @param array<string, array<int, string>> $errors Detail error per field.
   */
  public function __construct(
    string $message = '',
    int $code = 0,
    array $errors = []
  ) {
    parent::__construct($message, $code);
    $this->errors = $errors;
  }

  /**
   * Mendapatkan detail error validasi.
   *
   * @return array<string, array<int, string>>
   */
  public function getErrors(): array {
    return $this->errors;
  }
}
