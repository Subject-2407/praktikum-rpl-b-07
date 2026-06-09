<?php

/**
 * Alias use case logout lama.
 *
 * Kelas ini dipertahankan agar kode lama yang mengimpor LogoutUseCase tetap
 * menggunakan denylist JWT Redis yang baru.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Application\Contracts\Auth\TokenDenylistInterface;

/**
 * Kelas LogoutUseCase - Alias dari LogoutUserUseCase.
 */
class LogoutUseCase extends LogoutUserUseCase {

  /**
   * Konstruktor LogoutUseCase.
   *
   * @param TokenDenylistInterface $denylist Denylist token Redis.
   */
  public function __construct(TokenDenylistInterface $denylist) {
    parent::__construct($denylist);
  }
}
