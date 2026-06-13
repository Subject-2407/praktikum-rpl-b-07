<?php

/**
 * Unit Tests untuk RegisterContributorUseCase
 *
 * Menguji fungsi registrasi contributor dengan pattern AAA:
 * - Arrange: Setup data dan mock
 * - Act: Execute use case
 * - Assert: Verifikasi hasil
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\UserRepository;

class RegisterContributorUseCaseTest extends TestCase {

  private UserRepository $userRepository;
  private EmailNotificationInterface $emailNotification;
  private RegisterContributorUseCase $useCase;

  protected function setUp(): void {
    $this->userRepository = $this->createMock(UserRepository::class);
    $this->emailNotification = $this->createMock(EmailNotificationInterface::class);
    $this->useCase = new RegisterContributorUseCase(
      $this->userRepository,
      $this->emailNotification
    );
  }

  /**
   * Test: Registrasi berhasil dengan data valid
   * Arrange: Siapkan email dan password valid
   * Act: Execute use case
   * Assert: User berhasil dibuat dengan ID
   */
  public function test_register_dengan_data_valid_berhasil(): void {
    // Arrange
    $email = 'contributor@example.com';
    $password = 'password123';
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $expectedUser = new User(1, $email, $passwordHash, 'contributor');

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn(null);

    $this->userRepository
      ->expects($this->once())
      ->method('save')
      ->willReturn($expectedUser);

    // Act
    $result = $this->useCase->execute($email, $password);

    // Assert
    $this->assertInstanceOf(User::class, $result);
    $this->assertEquals($email, $result->getEmail());
    $this->assertEquals('contributor', $result->getRole());
    $this->assertTrue($result->verifyPassword($password));
  }

  /**
   * Test: Email format tidak valid gagal
   * Arrange: Email dengan format invalid
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_register_dengan_email_invalid_gagal(): void {
    // Arrange
    $email = 'invalid-email';
    $password = 'password123';

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Email format tidak valid');

    // Act
    $this->useCase->execute($email, $password);
  }

  /**
   * Test: Password terlalu pendek gagal
   * Arrange: Password kurang dari 8 karakter
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_register_dengan_password_terlalu_pendek_gagal(): void {
    // Arrange
    $email = 'contributor@example.com';
    $password = 'pass123';  // 7 karakter

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Password minimal 8 karakter');

    // Act
    $this->useCase->execute($email, $password);
  }

  /**
   * Test: Email sudah terdaftar gagal
   * Arrange: Email yang sudah ada di database
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_register_dengan_email_sudah_terdaftar_gagal(): void {
    // Arrange
    $email = 'existing@example.com';
    $password = 'password123';
    $existingUser = new User(1, $email, 'hash', 'contributor');

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn($existingUser);

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Email sudah terdaftar');

    // Act
    $this->useCase->execute($email, $password);
  }

  /**
   * Test: Alur registrasi API membuat token verifikasi dan mengirim email.
   *
   * @return void
   */
  public function test_register_api_flow_membuat_token_dan_mengirim_email(): void {
    $displayName = 'Creator One';
    $email = 'creator@example.com';
    $password = 'SecurePass123';
    $createdUser = new User(
      10,
      $displayName,
      $email,
      password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]),
      'contributor',
      false,
      '2026-06-10 12:00:00',
      '2026-06-10 12:00:00'
    );

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with($email)
      ->willReturn(null);

    $this->userRepository
      ->expects($this->once())
      ->method('transaction')
      ->willReturnCallback(fn (callable $callback): mixed => $callback());

    $this->userRepository
      ->expects($this->once())
      ->method('save')
      ->willReturn($createdUser);

    $this->userRepository
      ->expects($this->once())
      ->method('createEmailVerification')
      ->with(
        10,
        $this->callback(
          fn (string $token): bool => ctype_xdigit($token) && strlen($token) === 64
        ),
        $this->isType('string')
      );

    $this->emailNotification
      ->expects($this->once())
      ->method('sendEmailVerification')
      ->with(
        $createdUser,
        $this->callback(
          fn (string $token): bool => ctype_xdigit($token) && strlen($token) === 64
        )
      );

    $result = $this->useCase->execute(
      $displayName,
      $email,
      $password,
      $password
    );

    $this->assertSame($createdUser, $result);
  }
}
