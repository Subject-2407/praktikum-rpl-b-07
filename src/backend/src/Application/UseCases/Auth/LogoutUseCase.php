<?php

/**
 * Use Case untuk Logout
 *
 * Menghandle logika bisnis logout: validasi token dan revoke session.
 *
 * @package Scapes\Application\UseCases\Auth
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Core\Exceptions\DatabaseException;
use Scapes\Infrastructure\Auth\JWTManager;
use Scapes\Infrastructure\Repository\SessionRepository;

class LogoutUseCase {

  private SessionRepository $sessionRepository;
  private JWTManager $jwtManager;

  public function __construct(
    SessionRepository $sessionRepository,
    JWTManager $jwtManager
  ) {
    $this->sessionRepository = $sessionRepository;
    $this->jwtManager = $jwtManager;
  }

  /**
   * Melakukan logout dengan mencabut token.
   *
   * @param string $token JWT token yang ingin dicabut
   *
   * @throws \RuntimeException Jika token tidak valid
   * @throws DatabaseException Jika terjadi error database
   */
  public function execute(string $token): void {
    // Validasi token terlebih dahulu
    $payload = $this->jwtManager->validateAndDecode($token);

    if (!$payload) {
      throw new \RuntimeException('Invalid or expired token.');
    }

    // Revoke session di database
    try {
      $this->sessionRepository->revokeByToken($token);
    } catch (DatabaseException $e) {
      throw new \RuntimeException('Failed to logout. Please try again.');
    }
  }
}
