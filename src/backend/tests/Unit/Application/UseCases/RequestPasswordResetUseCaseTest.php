<?php

/**
 * Unit Tests untuk RequestPasswordResetUseCase.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Application\UseCases\Auth\RequestPasswordResetUseCase;
use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\NotificationException;
use Scapes\Infrastructure\Repository\UserRepository;

class RequestPasswordResetUseCaseTest extends TestCase {

  private UserRepository $userRepository;
  private EmailNotificationInterface $emailNotification;
  private RequestPasswordResetUseCase $useCase;

  protected function setUp(): void {
    $this->userRepository = $this->createMock(UserRepository::class);
    $this->emailNotification = $this->createMock(EmailNotificationInterface::class);
    $this->useCase = new RequestPasswordResetUseCase(
      $this->userRepository,
      $this->emailNotification
    );
  }

  public function test_request_password_reset_mengirim_email_jika_user_ditemukan(): void {
    $user = new User(
      7,
      'Creator Two',
      'creator2@example.com',
      'hash',
      'contributor',
      true
    );

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with('creator2@example.com')
      ->willReturn($user);

    $this->userRepository
      ->expects($this->once())
      ->method('transaction')
      ->willReturnCallback(fn (callable $callback): mixed => $callback());

    $this->userRepository
      ->expects($this->once())
      ->method('createPasswordReset')
      ->with(
        7,
        $this->callback(
          fn (string $token): bool => ctype_xdigit($token) && strlen($token) === 64
        ),
        $this->isType('string')
      );

    $this->emailNotification
      ->expects($this->once())
      ->method('sendPasswordReset')
      ->with(
        $user,
        $this->callback(
          fn (string $token): bool => ctype_xdigit($token) && strlen($token) === 64
        )
      );

    $this->useCase->execute('creator2@example.com');
  }

  public function test_request_password_reset_tetap_generik_saat_email_gagal(): void {
    $user = new User(
      8,
      'Creator Three',
      'creator3@example.com',
      'hash',
      'contributor',
      true
    );

    $this->userRepository
      ->expects($this->once())
      ->method('findByEmail')
      ->with('creator3@example.com')
      ->willReturn($user);

    $this->userRepository
      ->expects($this->once())
      ->method('transaction')
      ->willReturnCallback(fn (callable $callback): mixed => $callback());

    $this->userRepository
      ->expects($this->once())
      ->method('createPasswordReset');

    $this->emailNotification
      ->expects($this->once())
      ->method('sendPasswordReset')
      ->willThrowException(new NotificationException('SMTP unavailable'));

    $this->useCase->execute('creator3@example.com');

    $this->assertTrue(true);
  }
}
