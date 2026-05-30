<?php

/**
 * Unit Tests untuk AuthController.
 *
 * Menguji response endpoint auth yang tidak memerlukan wiring router penuh.
 *
 * @package Scapes\Tests\Unit\Interfaces\Http\Controllers
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Interfaces\Http\Controllers;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Application\UseCases\Auth\RequestPasswordResetUseCase;
use Scapes\Application\UseCases\Auth\ResetPasswordUseCase;
use Scapes\Application\UseCases\Auth\VerifyEmailUseCase;
use Scapes\Interfaces\Http\Controllers\AuthController;

class AuthControllerTest extends TestCase {

  private AuthController $controller;

  protected function setUp(): void {
    $this->controller = new AuthController(
      $this->createMock(RegisterContributorUseCase::class),
      $this->createMock(VerifyEmailUseCase::class),
      $this->createMock(LoginUserUseCase::class),
      $this->createMock(LogoutUserUseCase::class),
      $this->createMock(RequestPasswordResetUseCase::class),
      $this->createMock(ResetPasswordUseCase::class)
    );
  }

  public function test_current_session_mengembalikan_data_user_dan_expiry(): void {
    // Arrange
    $payload = [
      'sub' => '12',
      'user_id' => 12,
      'email' => 'creator@example.com',
      'role' => 'contributor',
      'exp' => 1777546800,
      'jti' => 'session-jti',
    ];

    // Act
    $response = $this->controller->currentSession($payload);

    // Assert
    $this->assertTrue($response['success']);
    $this->assertSame(200, $response['status_code']);
    $this->assertSame(
      'Current session retrieved successfully.',
      $response['message']
    );
    $this->assertSame([
      'user' => [
        'id' => 12,
        'email' => 'creator@example.com',
        'role' => 'contributor',
      ],
      'expires_at' => '2026-04-30T11:00:00Z',
    ], $response['data']);
  }

  public function test_current_session_memakai_sub_saat_user_id_tidak_tersedia(): void {
    // Arrange
    $payload = [
      'sub' => '7',
      'email' => 'admin@example.com',
      'role' => 'admin',
      'exp' => 1777548600,
      'jti' => 'admin-session-jti',
    ];

    // Act
    $response = $this->controller->currentSession($payload);

    // Assert
    $this->assertSame(7, $response['data']['user']['id']);
    $this->assertSame('2026-04-30T11:30:00Z', $response['data']['expires_at']);
  }
}
