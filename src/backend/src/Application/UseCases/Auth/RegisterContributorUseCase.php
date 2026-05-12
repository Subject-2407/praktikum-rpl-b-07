<?php

/**
 * Register Contributor Use Case
 *
 * Menangani registrasi akun contributor baru.
 * Memvalidasi email dan password, kemudian menyimpan ke database.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas RegisterContributorUseCase - Melakukan registrasi contributor.
 *
 * @class RegisterContributorUseCase
 */
class RegisterContributorUseCase {

  /**
   * Repository untuk akses user data.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Konstruktor RegisterContributorUseCase.
   *
   * @param UserRepository $userRepository Repository untuk user.
   */
  public function __construct(UserRepository $userRepository) {
    $this->userRepository = $userRepository;
  }

  /**
   * Melakukan registrasi contributor baru.
   *
   * @param string $email Email contributor.
   * @param string $password Password plaintext (akan di-hash).
   *
   * @return User User yang baru terdaftar.
   * @throws ValidationException Jika validasi gagal.
   */
  public function execute(string $email, string $password): User {
    // Validasi format email
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
      throw new ValidationException('Email format tidak valid');
    }

    // Validasi password minimal 8 karakter
    if (strlen($password) < 8) {
      throw new ValidationException('Password minimal 8 karakter');
    }

    // Cek apakah email sudah terdaftar
    if ($this->userRepository->findByEmail($email) !== null) {
      throw new ValidationException('Email sudah terdaftar');
    }

    // Hash password menggunakan bcrypt dengan cost 12
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

    // Buat user baru dengan role contributor
    $user = new User(
      0,  // ID akan di-assign oleh database
      $email,
      $passwordHash,
      'contributor',
      false,  // is_verified = false untuk MVP (tanpa email verification)
      date('Y-m-d H:i:s'),
      date('Y-m-d H:i:s')
    );

    // Simpan ke repository
    return $this->userRepository->save($user);
  }
}
