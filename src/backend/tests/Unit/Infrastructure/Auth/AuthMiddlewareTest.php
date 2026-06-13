<?php

/**
 * Unit Tests untuk AuthMiddleware.
 *
 * Menguji autentikasi JWT dan pembatasan role.
 *
 * @package Scapes\Tests\Unit\Infrastructure\Auth
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Infrastructure\Auth;

use PHPUnit\Framework\TestCase;
use Scapes\Application\Contracts\Auth\TokenDenylistInterface;
use Scapes\Infrastructure\Auth\AuthMiddleware;
use Scapes\Infrastructure\Auth\JWTManager;

class AuthMiddlewareTest extends TestCase {

  protected function tearDown(): void {
    unset($_SERVER['HTTP_AUTHORIZATION']);
    unset($_SERVER['REDIRECT_HTTP_AUTHORIZATION']);
    unset($_COOKIE[AuthMiddleware::COOKIE_NAME]);
  }

  public function test_contributor_route_menolak_role_admin(): void {
    // Arrange
    $jwtManager = new JWTManager('test-secret');
    $token = $jwtManager->createToken([
      'sub' => 1,
      'email' => 'admin@scapes.app',
      'role' => 'admin',
    ])['token'];

    $_SERVER['HTTP_AUTHORIZATION'] = 'Bearer ' . $token;

    $denylist = $this->createMock(TokenDenylistInterface::class);
    $denylist
      ->expects($this->once())
      ->method('isDenied')
      ->willReturn(false);

    $middleware = new AuthMiddleware($jwtManager, $denylist, 'contributor');

    // Act
    $response = $middleware([], function (array $params): array {
      return ['success' => true];
    });

    // Assert
    $this->assertFalse($response['success']);
    $this->assertSame(403, $response['status_code']);
  }

  public function test_contributor_route_menerima_role_contributor(): void {
    // Arrange
    $jwtManager = new JWTManager('test-secret');
    $token = $jwtManager->createToken([
      'sub' => 2,
      'email' => 'creator@example.com',
      'role' => 'contributor',
    ])['token'];

    $_SERVER['HTTP_AUTHORIZATION'] = 'Bearer ' . $token;

    $denylist = $this->createMock(TokenDenylistInterface::class);
    $denylist
      ->expects($this->once())
      ->method('isDenied')
      ->willReturn(false);

    $middleware = new AuthMiddleware($jwtManager, $denylist, 'contributor');

    // Act
    $response = $middleware([], function (array $params): array {
      return [
        'success' => true,
        'role' => $params['auth_user']['role'],
      ];
    });

    // Assert
    $this->assertTrue($response['success']);
    $this->assertSame('contributor', $response['role']);
  }
}
