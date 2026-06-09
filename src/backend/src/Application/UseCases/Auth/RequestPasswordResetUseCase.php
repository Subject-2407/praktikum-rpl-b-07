<?php

/**
 * Use case permintaan reset password.
 *
 * Respons endpoint tetap generik untuk mencegah user enumeration, tetapi
 * token reset dibuat jika email memang terdaftar.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas RequestPasswordResetUseCase - Membuat token reset password.
 */
class RequestPasswordResetUseCase {

  /**
   * Repository pengguna.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Konstruktor RequestPasswordResetUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   */
  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Meminta reset password.
   *
   * @param string $email Email pengguna.
   *
   * @return void
   */
  public function execute(string $email): void {
    $email = trim(strtolower($email));

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      throw new ValidationException('Validation failed.', 0, [
        'email' => ['The email field must be a valid email address.'],
      ]);
    }

    $user = $this->userRepository->findByEmail($email);
    if ($user === null) {
      return;
    }

    $this->userRepository->createPasswordReset(
      $user->getId(),
      bin2hex(random_bytes(32)),
      date('Y-m-d H:i:s', strtotime('+24 hours'))
    );
  }
}
