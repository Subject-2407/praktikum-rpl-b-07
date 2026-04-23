<?php

/**
 * Controller untuk Auth Endpoints
 *
 * Menangani HTTP requests untuk login dan logout.
 *
 * @package Scapes\Interfaces\Http\Controllers
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Auth\LoginUseCase;
use Scapes\Application\UseCases\Auth\LogoutUseCase;

class AuthController {

  private LoginUseCase $loginUseCase;
  private LogoutUseCase $logoutUseCase;

  public function __construct(LoginUseCase $loginUseCase, LogoutUseCase $logoutUseCase) {
    $this->loginUseCase = $loginUseCase;
    $this->logoutUseCase = $logoutUseCase;
  }

  /**
   * Handle POST /auth/login
   *
   * Request body:
   * {
   *   "email": "user@example.com",
   *   "password": "password123"
   * }
   *
   * @return void Response JSON dikirim langsung ke output
   */
  public function login(): void {
    try {
      // Validasi method
      if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        $this->sendError('Method not allowed.', 405);
        return;
      }

      // Baca request body
      $input = $this->getJsonInput();

      // Validasi input
      $email = $input['email'] ?? '';
      $password = $input['password'] ?? '';

      if (empty($email) || empty($password)) {
        $this->sendValidationError([
            'email' => !empty($email) ? [] : ['The email field is required.'],
            'password' => !empty($password) ? [] : ['The password field is required.'],
        ]);
        return;
      }

      // Validasi format email
      if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $this->sendValidationError(['email' => ['The email must be a valid email address.']]);
        return;
      }

      // Eksekusi use case
      $ipAddress = $_SERVER['REMOTE_ADDR'] ?? null;
      $result = $this->loginUseCase->execute($email, $password, $ipAddress);

      // Success response
      http_response_code(200);
      echo json_encode([
          'success' => true,
          'message' => 'Login successful.',
          'data' => $result,
      ]);
    } catch (\RuntimeException $e) {
      // Handle business logic errors
      if (strpos($e->getMessage(), 'Email or password') !== false) {
        $this->sendError($e->getMessage(), 401);
      } elseif (strpos($e->getMessage(), 'Account not verified') !== false) {
        $this->sendError($e->getMessage(), 403);
      } else {
        $this->sendError($e->getMessage(), 400);
      }
    } catch (\Exception $e) {
      $this->sendError('Internal server error.', 500);
    }
  }

  /**
   * Handle POST /auth/logout
   *
   * Memerlukan Authorization header dengan Bearer token.
   *
   * @return void Response JSON dikirim langsung ke output
   */
  public function logout(): void {
    try {
      // Validasi method
      if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        $this->sendError('Method not allowed.', 405);
        return;
      }

      // Ambil token dari header
      $token = $this->getBearerToken();

      if (empty($token)) {
        $this->sendError('Unauthorized. Token not provided.', 401);
        return;
      }

      // Eksekusi use case
      $this->logoutUseCase->execute($token);

      // Success response
      http_response_code(200);
      echo json_encode([
          'success' => true,
          'message' => 'Logged out successfully.',
          'data' => null,
      ]);
    } catch (\RuntimeException $e) {
      $this->sendError($e->getMessage(), 401);
    } catch (\Exception $e) {
      $this->sendError('Internal server error.', 500);
    }
  }

  /**
   * Mengambil JSON input dari request body.
   *
   * @return array
   */
  private function getJsonInput(): array {
    $input = file_get_contents('php://input');

    if (empty($input)) {
      return [];
    }

    $decoded = json_decode($input, true);
    return is_array($decoded) ? $decoded : [];
  }

  /**
   * Mengambil Bearer token dari Authorization header.
   *
   * @return string|null
   */
  private function getBearerToken(): ?string {
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';

    if (empty($authHeader)) {
      return null;
    }

    if (!preg_match('/^Bearer\s+(.+)$/i', $authHeader, $matches)) {
      return null;
    }

    return trim($matches[1]);
  }

  /**
   * Kirim error response.
   *
   * @param string $message
   * @param int $statusCode
   */
  private function sendError(string $message, int $statusCode): void {
    http_response_code($statusCode);
    echo json_encode([
        'success' => false,
        'message' => $message,
        'errors' => null,
    ]);
  }

  /**
   * Kirim validation error response.
   *
   * @param array $errors Array dengan field => [messages]
   */
  private function sendValidationError(array $errors): void {
    // Filter hanya field yang ada error
    $filteredErrors = array_filter($errors, function ($msgs) {
      return is_array($msgs) && count($msgs) > 0;
    });

    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Validation failed.',
        'errors' => $filteredErrors,
    ]);
  }
}
