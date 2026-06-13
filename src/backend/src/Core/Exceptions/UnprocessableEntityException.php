<?php

/**
 * Exception untuk aturan bisnis yang tidak terpenuhi.
 *
 * Dipakai saat data dapat diparsing, tetapi tidak dapat diproses karena
 * state bisnis resource tidak sesuai.
 *
 * @package Scapes\Core\Exceptions
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Exceptions;

use Exception;

/**
 * Kelas UnprocessableEntityException - Exception untuk error bisnis.
 */
class UnprocessableEntityException extends Exception {
}
