<?php

/**
 * Middleware untuk Autentikasi JWT
 *
 * Middleware ini bertanggung jawab untuk memvalidasi token JWT yang dikirimkan
 * melalui header Authorization. Jika valid, data user akan ditambahkan ke params.
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

use Scapes\Infrastructure\Repository\SessionRepository;

/**
 * Kelas AuthMiddleware - Menangani proteksi route dengan JWT.
 *
 * @class AuthMiddleware
 */
class AuthMiddleware {

  /**
   * Manager JWT untuk validasi token.
   *
   * @var JWTManager
   */
  private JWTManager $jwtManager;

  /**
   * Repository session untuk mengecek status pencabutan.
   *
   * @var SessionRepository
   */
  private SessionRepository $sessionRepository;

  /**
   * Role yang dibutuhkan untuk mengakses route (opsional).
   *
   * @var string|null
   */
  private ?string $requiredRole;

  /**
   * Konstruktor AuthMiddleware.
   *
   * @param JWTManager $jwtManager Manager JWT.
   * @param SessionRepository $sessionRepository Repository session.
   * @param string|null $requiredRole Role yang dibutuhkan.
   */
  public function __construct(
    JWTManager $jwtManager,
    SessionRepository $sessionRepository,
    ?string $requiredRole = null
  ) {
    $this->jwtManager = $jwtManager;
    $this->sessionRepository = $sessionRepository;
    $this->requiredRole = $requiredRole;
  }

  /**
   * Jalankan middleware.
   *
   * @param array $params Parameter request.
   * @param callable $next Handler berikutnya dalam chain.
   *
   * @return array Response array.
   */
  public function __invoke(array $params, callable $next): array {
    // Ambil token dari header Authorization
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    
    if (empty($authHeader) || strpos($authHeader, 'Bearer ') !== 0) {
      return $this->unauthorizedResponse('Token tidak ditemukan atau format salah');
    }

    $token = substr($authHeader, 7);

    // Validasi dan decode token
    $payload = $this->jwtManager->validateAndDecode($token);
    if (!$payload) {
      return $this->unauthorizedResponse('Token tidak valid atau sudah expired');
    }

    // Cek apakah session sudah dicabut di database
    if ($this->sessionRepository->isRevoked($token)) {
      return $this->unauthorizedResponse('Sesi sudah berakhir, silakan login kembali');
    }

    // Cek role jika dibutuhkan
    if ($this->requiredRole !== null && $payload['role'] !== $this->requiredRole) {
      return [
        'success' => false,
        'status_code' => 403,
        'message' => 'Anda tidak memiliki izin untuk mengakses resource ini',
        'data' => [],
      ];
    }

    // Tambahkan data auth ke params agar bisa diakses di controller
    $params['auth_user'] = $payload;

    return $next($params);
  }

  /**
   * Format respons unauthorized.
   *
   * @param string $message Pesan kesalahan.
   * @return array
   */
  private function unauthorizedResponse(string $message): array {
    return [
      'success' => false,
      'status_code' => 401,
      'message' => $message,
      'data' => [],
    ];
  }
}
