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
use Scapes\Application\Contracts\Auth\TokenDenylistInterface;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Core\Exceptions\AuthenticationException;

class LogoutUserUseCaseTest extends TestCase {

  private TokenDenylistInterface $tokenDenylist;
  private LogoutUserUseCase $useCase;

  protected function setUp(): void {
    $this->tokenDenylist = $this->createMock(TokenDenylistInterface::class);
    $this->useCase = new LogoutUserUseCase($this->tokenDenylist);
  }

  /**
   * Test: Logout berhasil dengan token valid
   * Arrange: Token JWT valid dengan jti dan exp
   * Act: Execute use case
   * Assert: Token berhasil di-deny ke denylist
   */
  public function test_logout_dengan_token_valid_berhasil(): void {
    // Arrange
    $payload = [
      'jti' => 'unique-token-id-123',
      'exp' => time() + 1800,
      'sub' => '1',
    ];

    $this->tokenDenylist
      ->expects($this->once())
      ->method('deny')
      ->with('unique-token-id-123', $payload['exp']);

    // Act & Assert
    $this->useCase->execute($payload);
  }

  /**
   * Test: Logout gagal karena payload tidak memiliki jti
   * Arrange: Payload tanpa jti
   * Act: Execute use case
   * Assert: AuthenticationException dilempar
   */
  public function test_logout_dengan_payload_invalid_gagal(): void {
    // Arrange
    $payload = [
      'exp' => time() + 1800,
    ];

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
