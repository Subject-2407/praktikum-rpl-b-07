<?php

/**
 * Alias use case login lama.
 *
 * Kelas ini dipertahankan agar kode lama yang mengimpor LoginUseCase tetap
 * dapat memakai implementasi login terbaru.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas LoginUseCase - Alias dari LoginUserUseCase.
 */
class LoginUseCase extends LoginUserUseCase {

  /**
   * Konstruktor LoginUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   * @param JWTManager $jwtManager Manager JWT.
   */
  public function __construct(
    UserRepository $userRepository,
    JWTManager $jwtManager
  ) {
    parent::__construct($userRepository, $jwtManager);
  }
}
