<?php

/**
 * Use case reset password.
 *
 * Use case ini memvalidasi token reset, mengganti password hash, dan
 * menandai token reset sudah digunakan.
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
 * Kelas ResetPasswordUseCase - Reset password memakai token.
 */
class ResetPasswordUseCase {

  /**
   * Repository pengguna.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Konstruktor ResetPasswordUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   */
  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Mengganti password pengguna.
   *
   * @param string $token Token reset password.
   * @param string $password Password baru.
   * @param string $passwordConfirmation Konfirmasi password baru.
   *
   * @return void
   */
  public function execute(
    string $token,
    string $password,
    string $passwordConfirmation
  ): void {
    $errors = [];

    if (strlen($password) < 8) {
      $errors['password'][] = 'The password field must be at least 8 characters.';
    }

    if ($password !== $passwordConfirmation) {
      $errors['password_confirmation'][] =
        'The password confirmation does not match.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    $reset = $this->userRepository->findPasswordReset($token);
    if ($reset === null || !empty($reset['used_at'])) {
      throw new GoneException('Password reset token has expired or already used.');
    }

    if (strtotime((string) $reset['expires_at']) < time()) {
      throw new GoneException('Password reset token has expired or already used.');
    }

    $this->userRepository->transaction(function () use ($reset, $password): void {
      $this->userRepository->updatePasswordHash(
        (int) $reset['user_id'],
        password_hash($password, PASSWORD_BCRYPT, ['cost' => 12])
      );
      $this->userRepository->markPasswordResetUsed((int) $reset['id']);
    });
  }
}
