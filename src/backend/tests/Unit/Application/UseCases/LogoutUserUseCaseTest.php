<?php

/**
 * Unit Tests untuk LogoutUserUseCase
 *
 * Menguji fungsi logout dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Infrastructure\Repository\SessionRepository;

class LogoutUserUseCaseTest extends TestCase {

  private SessionRepository $sessionRepository;
  private LogoutUserUseCase $useCase;

  protected function setUp(): void {
    $this->sessionRepository = $this->createMock(SessionRepository::class);
    $this->useCase = new LogoutUserUseCase($this->sessionRepository);
  }

  /**
   * Test: Logout berhasil dengan token valid
   * Arrange: Session dengan token ada
   * Act: Execute use case
   * Assert: Session berhasil di-revoke
   */
  public function test_logout_dengan_token_valid_berhasil(): void {
    // Arrange
    $token = 'valid-session-token';
    $sessionData = [
      'id' => 1,
      'user_id' => 1,
      'token' => $token,
      'revoked_at' => null,
    ];

    $this->sessionRepository
      ->expects($this->once())
      ->method('findByToken')
      ->with($token)
      ->willReturn($sessionData);

    $this->sessionRepository
      ->expects($this->once())
      ->method('revokeSession')
      ->with($token);

    // Act & Assert
    $this->useCase->execute($token);
  }

  /**
   * Test: Logout gagal karena token tidak valid
   * Arrange: Token tidak ditemukan atau expired
   * Act: Execute use case
   * Assert: AuthenticationException dilempar
   */
  public function test_logout_dengan_token_invalid_gagal(): void {
    // Arrange
    $token = 'invalid-token';

    $this->sessionRepository
      ->expects($this->once())
      ->method('findByToken')
      ->with($token)
      ->willReturn(null);

    // Assert
    $this->expectException(AuthenticationException::class);
    $this->expectExceptionMessage('Session tidak ditemukan atau sudah expired');

    // Act
    $this->useCase->execute($token);
  }
}
