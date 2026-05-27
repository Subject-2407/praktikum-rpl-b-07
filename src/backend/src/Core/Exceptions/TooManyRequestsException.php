<?php

/**
 * Exception untuk pembatasan percobaan request.
 *
 * Dipakai saat sistem memblokir percobaan login sementara karena terlalu
 * banyak kegagalan autentikasi.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas TooManyRequestsException - Exception untuk rate limit.
 */
class TooManyRequestsException extends Exception {
}
