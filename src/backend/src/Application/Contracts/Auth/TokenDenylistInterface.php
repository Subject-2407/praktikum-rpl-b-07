<?php

/**
 * Kontrak denylist token JWT.
 *
 * Interface ini menjaga application layer bebas dari detail Redis, tetapi
 * tetap dapat meminta token aktif dicabut sampai waktu kedaluwarsanya.
 *
 * @package Scapes\Application\Contracts\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\Contracts\Auth;

/**
 * Kontrak penyimpanan denylist JWT.
 */
interface TokenDenylistInterface {

  /**
   * Memasukkan JTI token ke denylist sampai token kedaluwarsa.
   *
   * @param string $jti Identitas unik token JWT.
   * @param int $expiresAt Unix timestamp kedaluwarsa token.
   *
   * @return void
   */
  public function deny(string $jti, int $expiresAt): void;

  /**
   * Mengecek apakah JTI token sudah dicabut.
   *
   * @param string $jti Identitas unik token JWT.
   *
   * @return bool True jika token sudah masuk denylist.
   */
  public function isDenied(string $jti): bool;
}
