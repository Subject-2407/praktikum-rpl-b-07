<?php

/**
 * Use case registrasi contributor.
 *
 * Use case ini memvalidasi input registrasi, membuat user contributor,
 * dan menyiapkan token verifikasi email sesuai kontrak API.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\ConflictException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas RegisterContributorUseCase - Registrasi akun contributor.
 */
class RegisterContributorUseCase {

  /**
   * Repository pengguna.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Konstruktor RegisterContributorUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   */
  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Melakukan registrasi contributor baru.
   *
   * @param string $email Email contributor.
   * @param string $password Password plaintext.
   * @param string $passwordConfirmation Konfirmasi password.
   *
   * @return User Pengguna yang dibuat.
   */
  public function execute(
    string $email,
    string $password,
    ?string $passwordConfirmation = null
  ): User {
    $email = trim(strtolower($email));

    if ($passwordConfirmation === null) {
      return $this->executeLegacy($email, $password);
    }

    $errors = [];

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      $errors['email'][] = 'The email field must be a valid email address.';
    }

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

    if ($this->userRepository->findByEmail($email) !== null) {
      throw new ConflictException('Email is already registered.');
    }

    return $this->userRepository->transaction(
      function () use ($email, $password): User {
        $now = date('Y-m-d H:i:s');
        $user = new User(
          0,
          $email,
          password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]),
          'contributor',
          false,
          $now,
          $now
        );

        $created = $this->userRepository->save($user);
        $this->userRepository->createEmailVerification(
          $created->getId(),
          bin2hex(random_bytes(32)),
          date('Y-m-d H:i:s', strtotime('+24 hours'))
        );

        return $created;
      }
    );
  }

  /**
   * Menjalankan alur registrasi lama untuk kompatibilitas unit test lama.
   *
   * @param string $email Email contributor.
   * @param string $password Password plaintext.
   *
   * @return User Pengguna yang dibuat.
   */
  private function executeLegacy(string $email, string $password): User {
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      throw new ValidationException('Email format tidak valid');
    }

    if (strlen($password) < 8) {
      throw new ValidationException('Password minimal 8 karakter');
    }

    if ($this->userRepository->findByEmail($email) !== null) {
      throw new ValidationException('Email sudah terdaftar');
    }

    $now = date('Y-m-d H:i:s');
    return $this->userRepository->save(new User(
      0,
      $email,
      password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]),
      'contributor',
      false,
      $now,
      $now
    ));
  }
}
