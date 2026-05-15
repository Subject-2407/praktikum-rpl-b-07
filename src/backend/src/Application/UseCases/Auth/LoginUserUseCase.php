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
use Scapes\Infrastructure\Auth\JWTManager;

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
   * Manager JWT.
   *
   * @var JWTManager
   */
  private JWTManager $jwtManager;

  /**
   * Konstruktor LoginUserUseCase.
   *
   * @param UserRepository $userRepository Repository untuk user.
   * @param SessionRepository $sessionRepository Repository untuk session.
   * @param JWTManager $jwtManager Manager JWT.
   */
  public function __construct(
    UserRepository $userRepository,
    SessionRepository $sessionRepository,
    JWTManager $jwtManager
  ) {
    $this->userRepository = $userRepository;
    $this->sessionRepository = $sessionRepository;
    $this->jwtManager = $jwtManager;
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

    // Cek apakah akun sudah terverifikasi (opsional, tergantung kebijakan project)
    // if (!$user->isVerified()) {
    //   throw new AuthenticationException('Akun Anda belum terverifikasi');
    // }

    // Buat JWT token
    $tokenPayload = [
      'user_id' => $user->getId(),
      'email' => $user->getEmail(),
      'role' => $user->getRole(),
    ];

    $tokenResult = $this->jwtManager->createToken($tokenPayload);
    $token = $tokenResult['token'];

    // Simpan session ke database (akan dienkripsi oleh repository)
    $this->sessionRepository->createSession($user->getId(), $token, $ipAddress);

    return $token;
  }
}
