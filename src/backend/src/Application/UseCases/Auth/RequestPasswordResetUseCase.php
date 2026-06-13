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

use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Core\Exceptions\NotificationException;
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
   * Notifikasi email aplikasi.
   *
   * @var EmailNotificationInterface|null
   */
  private ?EmailNotificationInterface $emailNotification;

  /**
   * Konstruktor RequestPasswordResetUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   * @param EmailNotificationInterface|null $emailNotification Notifikasi email.
   */
  public function __construct(
    UserRepository $userRepository,
    ?EmailNotificationInterface $emailNotification = null
  ) {
    $this->userRepository = $userRepository;
    $this->emailNotification = $emailNotification;
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

    try {
      $this->userRepository->transaction(function () use ($user): void {
        $resetToken = bin2hex(random_bytes(32));
        $this->userRepository->createPasswordReset(
          $user->getId(),
          $resetToken,
          date('Y-m-d H:i:s', strtotime('+24 hours'))
        );
        $this->emailNotification?->sendPasswordReset($user, $resetToken);
      });
    } catch (NotificationException $e) {
      error_log(
        '[Scapes][PasswordResetEmail] '
        . $user->getEmail()
        . ' - '
        . $e->getMessage()
      );
    }
  }
}
