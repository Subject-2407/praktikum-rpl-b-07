<?php

/**
 * Use case verifikasi email.
 *
 * Use case ini memvalidasi token email_verifications dan menandai akun
 * contributor sebagai terverifikasi.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\GoneException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas VerifyEmailUseCase - Verifikasi akun via token email.
 */
class VerifyEmailUseCase {

  /**
   * Repository pengguna.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Konstruktor VerifyEmailUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   */
  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Memverifikasi akun memakai token.
   *
   * @param string $token Token verifikasi.
   *
   * @return void
   */
  public function execute(string $token): void {
    if (trim($token) === '') {
      throw new ValidationException('Validation failed.', 0, [
        'token' => ['The token field is required.'],
      ]);
    }

    $verification = $this->userRepository->findEmailVerification($token);
    if ($verification === null) {
      throw new ValidationException('Invalid verification token.', 0, [
        'token' => ['The verification token is invalid.'],
      ]);
    }

    if (!empty($verification['used_at'])) {
      throw new GoneException('Verification token has expired or already used.');
    }

    if (strtotime((string) $verification['expires_at']) < time()) {
      throw new GoneException('Verification token has expired or already used.');
    }

    $this->userRepository->transaction(function () use ($verification): void {
      $this->userRepository->markVerified((int) $verification['user_id']);
      $this->userRepository->markEmailVerificationUsed(
        (int) $verification['id']
      );
    });
  }
}
