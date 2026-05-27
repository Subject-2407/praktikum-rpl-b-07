<?php

/**
 * Use case logout pengguna.
 *
 * Logout dilakukan dengan memasukkan JTI token aktif ke denylist Redis
 * sampai token tersebut mencapai waktu kedaluwarsanya.
 *
 * @package Scapes\Application\UseCases\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Auth;

use Scapes\Application\Contracts\Auth\TokenDenylistInterface;
use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Infrastructure\Repository\SessionRepository;

/**
 * Kelas LogoutUserUseCase - Revoke token JWT aktif.
 */
class LogoutUserUseCase {

  /**
   * Denylist token.
   *
   * @var TokenDenylistInterface
   */
  private TokenDenylistInterface $denylist;

  /**
   * Repository session lama untuk kompatibilitas test MVP.
   *
   * @var SessionRepository|null
   */
  private ?SessionRepository $legacySessionRepository;

  /**
   * Konstruktor LogoutUserUseCase.
   *
   * @param TokenDenylistInterface|SessionRepository $denylist Denylist token Redis.
   */
  public function __construct(TokenDenylistInterface|SessionRepository $denylist) {
    $this->legacySessionRepository = null;

    if ($denylist instanceof SessionRepository) {
      $this->legacySessionRepository = $denylist;
      $this->denylist = new class implements TokenDenylistInterface {
        public function deny(string $jti, int $expiresAt): void {
        }

        public function isDenied(string $jti): bool {
          return false;
        }
      };
      return;
    }

    $this->denylist = $denylist;
  }

  /**
   * Mencabut token aktif.
   *
   * @param array<string, mixed>|string $payload Payload JWT atau token lama.
   *
   * @return void
   */
  public function execute(array|string $payload): void {
    if (is_string($payload)) {
      $this->executeLegacy($payload);
      return;
    }

    if (empty($payload['jti']) || empty($payload['exp'])) {
      throw new AuthenticationException('Invalid or expired token.');
    }

    $this->denylist->deny((string) $payload['jti'], (int) $payload['exp']);
  }

  /**
   * Menjalankan logout lama berbasis tabel sessions.
   *
   * @param string $token Token session lama.
   *
   * @return void
   */
  private function executeLegacy(string $token): void {
    if ($this->legacySessionRepository === null) {
      throw new AuthenticationException('Invalid or expired token.');
    }

    $session = $this->legacySessionRepository->findByToken($token);
    if ($session === null) {
      throw new AuthenticationException(
        'Session tidak ditemukan atau sudah expired'
      );
    }

    $this->legacySessionRepository->revokeSession($token);
  }
}
