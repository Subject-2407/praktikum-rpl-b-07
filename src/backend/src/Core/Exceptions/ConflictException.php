<?php

/**
 * Exception untuk konflik data.
 *
 * Dipakai saat request valid secara bentuk, tetapi bertabrakan dengan
 * state yang sudah ada, misalnya email sudah terdaftar.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas ConflictException - Exception untuk konflik resource.
 */
class ConflictException extends Exception {
}
