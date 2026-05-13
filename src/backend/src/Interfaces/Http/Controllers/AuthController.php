<?php

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Core\Exceptions\AuthenticationException;

/**
 * Pengontrol untuk menangani operasi autentikasi pengguna.
 * Mengelola registrasi akun baru, login, dan logout.
 */
class AuthController
{
  private RegisterContributorUseCase $registerUseCase;
  private LoginUserUseCase $loginUseCase;
  private LogoutUserUseCase $logoutUseCase;

  /**
   * Inisialisasi pengontrol autentikasi dengan use case.
   *
   * @param RegisterContributorUseCase $registerUseCase Use case registrasi
   * @param LoginUserUseCase $loginUseCase Use case login
   * @param LogoutUserUseCase $logoutUseCase Use case logout
   */
  public function __construct(
    RegisterContributorUseCase $registerUseCase,
    LoginUserUseCase $loginUseCase,
    LogoutUserUseCase $logoutUseCase
  ) {
    $this->registerUseCase = $registerUseCase;
    $this->loginUseCase = $loginUseCase;
    $this->logoutUseCase = $logoutUseCase;
  }

  /**
   * Daftarkan akun kontributor baru.
   * POST /auth/register
   * Body: {"email": "user@example.com", "password": "securepassword"}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function register(array $data): array
  {
    try {
      // Validasi input dasar
      if (empty($data['email']) || empty($data['password'])) {
        return $this->errorResponse(
          'Email dan password harus diisi',
          400
        );
      }

      $email = trim($data['email']);
      $password = trim($data['password']);

      // Jalankan use case
      $user = $this->registerUseCase->execute($email, $password);

      return $this->successResponse(
        [
          'id' => $user->getId(),
          'email' => $user->getEmail(),
          'role' => $user->getRole(),
          'is_verified' => $user->isVerified(),
        ],
        'Account registered successfully. Please login.',
        201
      );
    } catch (ValidationException $e) {
      return $this->errorResponse($e->getMessage(), 400);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Login pengguna dan buat sesi.
   * POST /auth/login
   * Body: {"email": "user@example.com", "password": "securepassword"}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function login(array $data): array
  {
    try {
      // Validasi input dasar
      if (empty($data['email']) || empty($data['password'])) {
        return $this->errorResponse(
          'Email and password are required',
          400
        );
      }

      $email = trim($data['email']);
      $password = trim($data['password']);

      // Jalankan use case
      $result = $this->loginUseCase->execute($email, $password);

      return $this->successResponse(
        [ 'token' => $result ],
        'Login successful',
        200
      );
    } catch (AuthenticationException $e) {
      return $this->errorResponse($e->getMessage(), 401);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Logout pengguna dan batalkan sesi.
   * POST /auth/logout
   * Body: {"token": "session_token"}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function logout(array $data): array
  {
    try {
      // Validasi token
      if (empty($data['token'])) {
        return $this->errorResponse('Token not found', 401);
      }

      $token = trim($data['token']);

      // Jalankan use case
      $this->logoutUseCase->execute($token);

      return $this->successResponse(
        [],
        'Logout successful',
        200
      );
    } catch (AuthenticationException $e) {
      return $this->errorResponse($e->getMessage(), 401);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Format respons sukses.
   *
   * @param array<string, mixed> $data Data respons
   * @param string $message Pesan sukses
   * @param int $statusCode Kode HTTP
   * @return array<string, mixed> Respons JSON
   */
  private function successResponse(
    array $data,
    string $message,
    int $statusCode
  ): array {
    return [
      'success' => true,
      'status_code' => $statusCode,
      'message' => $message,
      'data' => $data,
    ];
  }

  /**
   * Format respons kesalahan.
   *
   * @param string $message Pesan kesalahan
   * @param int $statusCode Kode HTTP
   * @return array<string, mixed> Respons JSON
   */
  private function errorResponse(string $message, int $statusCode): array
  {
    return [
      'success' => false,
      'status_code' => $statusCode,
      'message' => $message,
      'data' => [],
    ];
  }
}


