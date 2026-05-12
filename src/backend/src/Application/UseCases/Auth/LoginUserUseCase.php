<?php

/**
 * Login User Use Case
 *
 * Menangani autentikasi pengguna (Contributor atau Admin).
 * Memvalidasi email dan password, mencatat login attempt, dan membuat session.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Infrastructure\Repository\SessionRepository;

/**
 * Kelas LoginUserUseCase - Melakukan autentikasi pengguna.
 *
 * @class LoginUserUseCase
 */
class LoginUserUseCase {

  /**
   * Repository untuk akses user data.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Repository untuk akses session data.
   *
   * @var SessionRepository
   */
  private SessionRepository $sessionRepository;

  /**
   * Konstruktor LoginUserUseCase.
   *
   * @param UserRepository $userRepository Repository untuk user.
   * @param SessionRepository $sessionRepository Repository untuk session.
   */
  public function __construct(
    UserRepository $userRepository,
    SessionRepository $sessionRepository
  ) {
    $this->userRepository = $userRepository;
    $this->sessionRepository = $sessionRepository;
  }

  /**
   * Melakukan login pengguna.
   *
   * @param string $email Email pengguna.
   * @param string $password Password plaintext.
   * @param string $ipAddress IP address pengguna untuk keamanan.
   *
   * @return string Token session JWT.
   * @throws AuthenticationException Jika autentikasi gagal.
   */
  public function execute(string $email, string $password, string $ipAddress = ''): string {
    // Cari user berdasarkan email
    $user = $this->userRepository->findByEmail($email);

    if ($user === null) {
      throw new AuthenticationException('Email atau password salah');
    }

    // Verifikasi password
    if (!$user->verifyPassword($password)) {
      throw new AuthenticationException('Email atau password salah');
    }

    // TODO: Generate JWT token (akan diimplementasikan di Infrastructure)
    // Untuk MVP ini, kita akan menggunakan token sederhana
    $token = bin2hex(random_bytes(32));

    // Simpan session ke database
    $this->sessionRepository->createSession($user->getId(), $token, $ipAddress);

    return $token;
  }
}
