<?php

/**
 * Logout User Use Case
 *
 * Menangani logout pengguna dengan mencabut session aktif.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Infrastructure\Repository\SessionRepository;

/**
 * Kelas LogoutUserUseCase - Melakukan logout pengguna.
 *
 * @class LogoutUserUseCase
 */
class LogoutUserUseCase {

  /**
   * Repository untuk akses session data.
   *
   * @var SessionRepository
   */
  private SessionRepository $sessionRepository;

  /**
   * Konstruktor LogoutUserUseCase.
   *
   * @param SessionRepository $sessionRepository Repository untuk session.
   */
  public function __construct(SessionRepository $sessionRepository) {
    $this->sessionRepository = $sessionRepository;
  }

  /**
   * Melakukan logout dengan mencabut session.
   *
   * @param string $token Token session yang akan dicabut.
   *
   * @return void
   * @throws AuthenticationException Jika session tidak ditemukan.
   */
  public function execute(string $token): void {
    // Cari session berdasarkan token
    $session = $this->sessionRepository->findByToken($token);

    if ($session === null) {
      throw new AuthenticationException('Session tidak ditemukan atau sudah expired');
    }

    // Cabut session dengan mengeset revoked_at
    $this->sessionRepository->revokeSession($token);
  }
}
