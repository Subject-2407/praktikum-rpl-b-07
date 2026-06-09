<?php

/**
 * Exception untuk resource sementara yang sudah tidak berlaku.
 *
 * Dipakai untuk token verifikasi atau reset password yang kedaluwarsa
 * atau sudah pernah digunakan.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas GoneException - Exception untuk resource yang sudah tidak berlaku.
 */
class GoneException extends Exception {
}
