<?php

/**
 * Use Case untuk Login
 *
 * Menghandle logika bisnis login: validasi email/password, buat token, simpan session.
 *
 * @package Scapes\Application\UseCases\Auth
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\DatabaseException;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Infrastructure\Repository\SessionRepository;

class LoginUseCase {

  private UserRepository $userRepository;
  private SessionRepository $sessionRepository;
  private JWTManager $jwtManager;

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
   * Melakukan login dengan email dan password.
   *
   * @param string $email Email user
   * @param string $password Password plaintext
   * @param string|null $ipAddress IP address user (untuk audit)
   *
   * @return array Array dengan token, expires_at, dan user data
   *
   * @throws \RuntimeException Jika email/password salah
   * @throws \RuntimeException Jika akun belum terverifikasi
   * @throws DatabaseException Jika terjadi error database
   */
  public function execute(string $email, string $password, ?string $ipAddress = null): array {
    // Cari user berdasarkan email
    $user = $this->userRepository->findByEmail($email);

    if (!$user) {
      throw new \RuntimeException('Email or password incorrect.');
    }

    // Verifikasi password
    if (!password_verify($password, $user['password_hash'])) {
      throw new \RuntimeException('Email or password incorrect.');
    }

    // Cek apakah akun sudah terverifikasi
    if (!$user['is_verified']) {
      throw new \RuntimeException('Account not verified. Please check your email.');
    }

    // Buat JWT token
    $tokenPayload = [
        'user_id' => (int) $user['id'],
        'email' => $user['email'],
        'role' => $user['role'],
    ];

    $tokenResult = $this->jwtManager->createToken($tokenPayload);
    $token      = $tokenResult['token'];
    $expiresAt  = $this->jwtManager->getExpiresAt(['exp' => $tokenResult['exp']]);

    // Simpan session ke database
    try {
      $this->sessionRepository->create(
        (int) $user['id'],
        $token,
        $ipAddress,
        date('Y-m-d H:i:s', strtotime($expiresAt))
      );
    } catch (DatabaseException $e) {
      throw new \RuntimeException('Failed to create session. Please try again.');
    }

    // Return response
    return [
        'token' => $token,
        'expires_at' => $expiresAt,
        'user' => [
            'id' => (int) $user['id'],
            'email' => $user['email'],
            'role' => $user['role'],
        ],
    ];
  }
}
