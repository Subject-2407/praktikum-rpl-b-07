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
   * Konstruktor LogoutUserUseCase.
   *
   * @param TokenDenylistInterface $denylist Denylist token Redis.
   */
  public function __construct(TokenDenylistInterface $denylist) {
    $this->denylist = $denylist;
  }

  /**
   * Mencabut token aktif.
   *
   * @param array<string, mixed> $payload Payload JWT.
   *
   * @return void
   */
  public function execute(array $payload): void {
    if (empty($payload['jti']) || empty($payload['exp'])) {
      throw new AuthenticationException('Invalid or expired token.');
    }

    $this->denylist->deny((string) $payload['jti'], (int) $payload['exp']);
  }
}
