<?php

/**
 * Unit Tests untuk LoginUserUseCase
 *
 * Menguji fungsi login dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Infrastructure\Repository\UserRepository;
use Scapes\Infrastructure\Repository\SessionRepository;

class LoginUserUseCaseTest extends TestCase {

  private UserRepository $userRepository;
  private SessionRepository $sessionRepository;
  private LoginUserUseCase $useCase;

  protected function setUp(): void {
    $this->userRepository = $this->createMock(UserRepository::class);
    $this->sessionRepository = $this->createMock(SessionRepository::class);
    $this->useCase = new LoginUserUseCase($this->userRepository, $this->sessionRepository);
  }

  /**
   * Test: Login berhasil dengan email dan password valid
   * Arrange: User ada dan password cocok
   * Act: Execute use case
   * Assert: Token session dikembalikan
   */
  public function test_login_dengan_kredensial_valid_berhasil(): void {
    // Arrange
    $email = 'user@example.com';
    $password = 'password123';
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $user = new User(1, $email, $passwordHash, 'contributor');

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn($user);

    $this->sessionRepository
      ->expects($this->once())
      ->method('createSession')
      ->with(1, $this->isType('string'), '192.168.1.1');

    // Act
    $token = $this->useCase->execute($email, $password, '192.168.1.1');

    // Assert
    $this->assertIsString($token);
    $this->assertNotEmpty($token);
  }

  /**
   * Test: Login gagal karena email tidak ditemukan
   * Arrange: Email tidak ada di database
   * Act: Execute use case
   * Assert: AuthenticationException dilempar
   */
  public function test_login_dengan_email_tidak_ada_gagal(): void {
    // Arrange
    $email = 'nonexistent@example.com';
    $password = 'password123';

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn(null);

    // Assert
    $this->expectException(AuthenticationException::class);
    $this->expectExceptionMessage('Email atau password salah');

    // Act
    $this->useCase->execute($email, $password);
  }

  /**
   * Test: Login gagal karena password salah
   * Arrange: User ada tapi password tidak cocok
   * Act: Execute use case
   * Assert: AuthenticationException dilempar
   */
  public function test_login_dengan_password_salah_gagal(): void {
    // Arrange
    $email = 'user@example.com';
    $password = 'password123';
    $wrongPassword = 'wrongpassword';
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $user = new User(1, $email, $passwordHash, 'contributor');

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn($user);

    // Assert
    $this->expectException(AuthenticationException::class);
    $this->expectExceptionMessage('Email atau password salah');

    // Act
    $this->useCase->execute($email, $wrongPassword);
  }
}
