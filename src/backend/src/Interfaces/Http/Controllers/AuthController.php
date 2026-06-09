<?php

/**
 * Controller autentikasi.
 *
 * Controller ini menerjemahkan request HTTP auth menjadi input use case dan
 * membentuk response JSON sesuai kontrak API.
 *
 * @package Scapes\Interfaces\Http\Controllers
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Application\UseCases\Auth\RequestPasswordResetUseCase;
use Scapes\Application\UseCases\Auth\ResetPasswordUseCase;
use Scapes\Application\UseCases\Auth\VerifyEmailUseCase;
use Scapes\Core\Exceptions\AuthenticationException;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\ConflictException;
use Scapes\Core\Exceptions\GoneException;
use Scapes\Core\Exceptions\TooManyRequestsException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Auth\AuthMiddleware;
use Scapes\Interfaces\Http\Request;
use Scapes\Interfaces\Http\Response;

/**
 * Kelas AuthController - Endpoint registrasi, login, logout, dan reset.
 */
class AuthController {

  /**
   * Use case registrasi.
   *
   * @var RegisterContributorUseCase
   */
  private RegisterContributorUseCase $registerUseCase;

  /**
   * Use case verifikasi email.
   *
   * @var VerifyEmailUseCase
   */
  private VerifyEmailUseCase $verifyEmailUseCase;

  /**
   * Use case login.
   *
   * @var LoginUserUseCase
   */
  private LoginUserUseCase $loginUseCase;

  /**
   * Use case logout.
   *
   * @var LogoutUserUseCase
   */
  private LogoutUserUseCase $logoutUseCase;

  /**
   * Use case permintaan reset password.
   *
   * @var RequestPasswordResetUseCase
   */
  private RequestPasswordResetUseCase $requestPasswordResetUseCase;

  /**
   * Use case reset password.
   *
   * @var ResetPasswordUseCase
   */
  private ResetPasswordUseCase $resetPasswordUseCase;

  /**
   * Konstruktor AuthController.
   *
   * @param RegisterContributorUseCase $registerUseCase Use case registrasi.
   * @param VerifyEmailUseCase $verifyEmailUseCase Use case verifikasi email.
   * @param LoginUserUseCase $loginUseCase Use case login.
   * @param LogoutUserUseCase $logoutUseCase Use case logout.
   * @param RequestPasswordResetUseCase $requestPasswordResetUseCase Use case request reset.
   * @param ResetPasswordUseCase $resetPasswordUseCase Use case reset password.
   */
  public function __construct(
    RegisterContributorUseCase $registerUseCase,
    VerifyEmailUseCase $verifyEmailUseCase,
    LoginUserUseCase $loginUseCase,
    LogoutUserUseCase $logoutUseCase,
    RequestPasswordResetUseCase $requestPasswordResetUseCase,
    ResetPasswordUseCase $resetPasswordUseCase
  ) {
    $this->registerUseCase = $registerUseCase;
    $this->verifyEmailUseCase = $verifyEmailUseCase;
    $this->loginUseCase = $loginUseCase;
    $this->logoutUseCase = $logoutUseCase;
    $this->requestPasswordResetUseCase = $requestPasswordResetUseCase;
    $this->resetPasswordUseCase = $resetPasswordUseCase;
  }

  /**
   * POST /registrations.
   *
   * @param array<string, mixed> $data Body JSON.
   *
   * @return array<string, mixed>
   */
  public function register(array $data): array {
    try {
      $user = $this->registerUseCase->execute(
        (string) ($data['display_name'] ?? ''),
        (string) ($data['email'] ?? ''),
        (string) ($data['password'] ?? ''),
        (string) ($data['password_confirmation'] ?? '')
      );

      return Response::success(
        'Account created. Please check your email to verify your account.',
        [
          'id' => $user->getId(),
          'display_name' => $user->getDisplayName(),
          'email' => $user->getEmail(),
          'role' => $user->getRole(),
          'is_verified' => $user->isVerified(),
          'created_at' => gmdate(
            'Y-m-d\TH:i:s\Z',
            strtotime($user->getCreatedAt()) ?: time()
          ),
        ],
        201
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * POST /email-verifications.
   *
   * @param array<string, mixed> $data Body JSON.
   *
   * @return array<string, mixed>
   */
  public function verifyEmail(array $data): array {
    try {
      $this->verifyEmailUseCase->execute((string) ($data['token'] ?? ''));

      return Response::success(
        'Account verified successfully. You can now log in.',
        null
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * POST /sessions.
   *
   * @param array<string, mixed> $data Body JSON.
   *
   * @return array<string, mixed>
   */
  public function login(array $data): array {
    try {
      $result = $this->loginUseCase->execute(
        (string) ($data['email'] ?? ''),
        (string) ($data['password'] ?? ''),
        Request::ipAddress()
      );

      if (!is_array($result)) {
        throw new \RuntimeException('Login result tidak valid.');
      }

      $this->storeJwtCookie(
        (string) $result['token'],
        (int) $result['expires_at_unix']
      );
      unset($result['expires_at_unix']);

      return Response::success('Login successful.', $result);
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * GET /sessions/current.
   *
   * @param array<string, mixed> $authUser Payload JWT.
   *
   * @return array<string, mixed>
   */
  public function currentSession(array $authUser): array {
    $user = [
      'id' => (int) ($authUser['user_id'] ?? $authUser['sub'] ?? 0),
      'email' => (string) ($authUser['email'] ?? ''),
      'role' => (string) ($authUser['role'] ?? ''),
    ];

    if (array_key_exists('display_name', $authUser)) {
      $user = [
        'id' => $user['id'],
        'display_name' => (string) $authUser['display_name'],
        'email' => $user['email'],
        'role' => $user['role'],
      ];
    }

    return Response::success(
      'Current session retrieved successfully.',
      [
        'user' => $user,
        'expires_at' => gmdate(
          'Y-m-d\TH:i:s\Z',
          (int) ($authUser['exp'] ?? time())
        ),
      ]
    );
  }

  /**
   * DELETE /sessions/current.
   *
   * @param array<string, mixed> $authUser Payload JWT.
   *
   * @return array<string, mixed>
   */
  public function logout(array $authUser): array {
    try {
      $this->logoutUseCase->execute($authUser);
      $this->clearJwtCookie();

      return Response::success('Logged out successfully.', null);
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * POST /password-resets.
   *
   * @param array<string, mixed> $data Body JSON.
   *
   * @return array<string, mixed>
   */
  public function requestPasswordReset(array $data): array {
    try {
      $this->requestPasswordResetUseCase->execute(
        (string) ($data['email'] ?? '')
      );

      return Response::success(
        'If that email is registered, a password reset link has been sent.',
        null
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * PUT /password-resets/{token}.
   *
   * @param string $token Token reset password.
   * @param array<string, mixed> $data Body JSON.
   *
   * @return array<string, mixed>
   */
  public function resetPassword(string $token, array $data): array {
    try {
      $this->resetPasswordUseCase->execute(
        $token,
        (string) ($data['password'] ?? ''),
        (string) ($data['password_confirmation'] ?? '')
      );

      return Response::success(
        'Password reset successfully. You can now log in with your new password.',
        null
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * Simpan JWT ke cookie HttpOnly.
   *
   * @param string $token JWT.
   * @param int $expiresAt Unix timestamp expiry.
   *
   * @return void
   */
  private function storeJwtCookie(string $token, int $expiresAt): void {
    setcookie(AuthMiddleware::COOKIE_NAME, $token, [
      'expires' => $expiresAt,
      'path' => '/',
      'secure' => $this->isHttps(),
      'httponly' => true,
      'samesite' => 'Lax',
    ]);
  }

  /**
   * Hapus cookie JWT.
   *
   * @return void
   */
  private function clearJwtCookie(): void {
    setcookie(AuthMiddleware::COOKIE_NAME, '', [
      'expires' => time() - 3600,
      'path' => '/',
      'secure' => $this->isHttps(),
      'httponly' => true,
      'samesite' => 'Lax',
    ]);
  }

  /**
   * Mengecek apakah request memakai HTTPS.
   *
   * @return bool True jika HTTPS.
   */
  private function isHttps(): bool {
    return (
      ($_SERVER['HTTPS'] ?? '') === 'on'
      || ($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https'
    );
  }

  /**
   * Mengubah exception menjadi response HTTP.
   *
   * @param \Throwable $e Exception dari use case.
   *
   * @return array<string, mixed>
   */
  private function handleException(\Throwable $e): array {
    if ($e instanceof ValidationException) {
      return Response::error('Validation failed.', 400, $e->getErrors());
    }

    if ($e instanceof ConflictException) {
      return Response::error($e->getMessage(), 409);
    }

    if ($e instanceof AuthenticationException) {
      return Response::error('Unauthorized. Please log in.', 401);
    }

    if ($e instanceof AuthorizationException) {
      return Response::error($e->getMessage(), 403);
    }

    if ($e instanceof TooManyRequestsException) {
      return Response::error($e->getMessage(), 429);
    }

    if ($e instanceof GoneException) {
      return Response::error($e->getMessage(), 410);
    }

    return Response::error('Internal server error.', 500);
  }
}
