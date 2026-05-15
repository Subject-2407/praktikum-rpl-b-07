<?php

/**
 * Middleware untuk Autentikasi Opsional
 *
 * Middleware ini mencoba memvalidasi token JWT jika ada. Jika valid, data user
 * ditambahkan ke params. Jika tidak ada atau tidak valid, request tetap dilanjutkan
 * tanpa memberhentikan proses (tanpa 401).
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

use Scapes\Infrastructure\Repository\SessionRepository;

/**
 * Kelas OptionalAuthMiddleware - Mencoba autentikasi tanpa memaksa.
 *
 * @class OptionalAuthMiddleware
 */
class OptionalAuthMiddleware {

  /**
   * Manager JWT.
   *
   * @var JWTManager
   */
  private JWTManager $jwtManager;

  /**
   * Repository session.
   *
   * @var SessionRepository
   */
  private SessionRepository $sessionRepository;

  /**
   * Konstruktor OptionalAuthMiddleware.
   *
   * @param JWTManager $jwtManager Manager JWT.
   * @param SessionRepository $sessionRepository Repository session.
   */
  public function __construct(
    JWTManager $jwtManager,
    SessionRepository $sessionRepository
  ) {
    $this->jwtManager = $jwtManager;
    $this->sessionRepository = $sessionRepository;
  }

  /**
   * Jalankan middleware.
   *
   * @param array $params Parameter request.
   * @param callable $next Handler berikutnya.
   *
   * @return array Response array.
   */
  public function __invoke(array $params, callable $next): array {
    // Ambil token dari header Authorization
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    
    // Inisialisasi auth_user sebagai null
    $params['auth_user'] = null;

    if (!empty($authHeader) && strpos($authHeader, 'Bearer ') === 0) {
      $token = substr($authHeader, 7);

      // Validasi dan decode token
      $payload = $this->jwtManager->validateAndDecode($token);
      
      // Jika valid dan session tidak dicabut, set auth_user
      if ($payload && !$this->sessionRepository->isRevoked($token)) {
        $params['auth_user'] = $payload;
      }
    }

    return $next($params);
  }
}
