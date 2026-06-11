<?php

/**
 * Use case login pengguna.
 *
 * Use case ini memvalidasi kredensial, mengecek verifikasi email,
 * mencatat percobaan login, dan membuat JWT 30 menit.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\TooManyRequestsException;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Repository\UserRepository;

/**
 * Kelas LoginUserUseCase - Autentikasi pengguna.
 */
class LoginUserUseCase {

  /**
   * Batas percobaan gagal.
   *
   * @var int
   */
  private const MAX_FAILED_ATTEMPTS = 5;

  /**
   * Jendela pengecekan percobaan gagal dalam menit.
   *
   * @var int
   */
  private const FAILED_ATTEMPT_WINDOW_MINUTES = 15;

  /**
   * Repository pengguna.
   *
   * @var UserRepository
   */
  private UserRepository $userRepository;

  /**
   * Manager JWT.
   *
   * @var JWTManager
   */
  private JWTManager $jwtManager;

  /**
   * Konstruktor LoginUserUseCase.
   *
   * @param UserRepository $userRepository Repository pengguna.
   * @param JWTManager $jwtManager Manager JWT.
   */
  public function __construct(
    UserRepository $userRepository,
    JWTManager $jwtManager
  ) {
    $this->userRepository = $userRepository;
    $this->jwtManager = $jwtManager;
  }

  /**
   * Melakukan login pengguna.
   *
   * @param string $email Email pengguna.
   * @param string $password Password plaintext.
   * @param string $ipAddress IP address client.
   *
   * @return array<string, mixed>|string Data token dan user, atau token lama.
   */
  public function execute(
    string $email,
    string $password,
    string $ipAddress = ''
  ): array|string {
    $email = trim(strtolower($email));

    $failedAttempts = $this->userRepository->countRecentFailedLoginAttempts(
      $email,
      $ipAddress,
      self::FAILED_ATTEMPT_WINDOW_MINUTES
    );

    if ($failedAttempts >= self::MAX_FAILED_ATTEMPTS) {
      throw new TooManyRequestsException(
        'Too many failed login attempts. Please try again later.'
      );
    }

    $user = $this->userRepository->findByEmail($email);
    if ($user === null || !$user->verifyPassword($password)) {
      $this->userRepository->recordLoginAttempt($email, $ipAddress, false);
      throw new AuthenticationException('Email or password is incorrect.');
    }

    if (!$user->isVerified()) {
      $this->userRepository->recordLoginAttempt($email, $ipAddress, false);
      throw new AuthorizationException('Account is not verified.');
    }

    $token = $this->jwtManager->createToken([
      'sub' => (string) $user->getId(),
      'user_id' => $user->getId(),
      'display_name' => $user->getDisplayName(),
      'email' => $user->getEmail(),
      'role' => $user->getRole(),
    ]);

    $this->userRepository->recordLoginAttempt($email, $ipAddress, true);

    return [
      'token' => $token['token'],
      'expires_at' => $this->jwtManager->formatExpiresAt($token['exp']),
      'expires_at_unix' => $token['exp'],
      'user' => [
        'id' => $user->getId(),
        'display_name' => $user->getDisplayName(),
        'email' => $user->getEmail(),
        'role' => $user->getRole(),
      ],
    ];
}
}