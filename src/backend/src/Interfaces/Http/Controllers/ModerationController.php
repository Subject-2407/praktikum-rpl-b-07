<?php

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\AuthorizationException;

/**
 * Pengontrol untuk menangani operasi moderasi wallpaper.
 * Hanya admin yang dapat mengakses controller ini.
 */
class ModerationController
{
  private ModerateWallpaperUseCase $moderateUseCase;

  /**
   * Inisialisasi pengontrol moderasi dengan use case.
   *
   * @param ModerateWallpaperUseCase $moderateUseCase Use case moderasi
   */
  public function __construct(ModerateWallpaperUseCase $moderateUseCase)
  {
    $this->moderateUseCase = $moderateUseCase;
  }

  /**
   * Setujui atau tolak wallpaper yang pending.
   * POST /moderation/moderate
   * Body: {
   *   "admin_id": 1,
   *   "wallpaper_id": 1,
   *   "decision": "approved|rejected",
   *   "reason": "Optional reason if rejected"
   * }
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function moderate(array $data): array
  {
    try {
      // Validasi input dasar
      if (empty($data['admin_id'])) {
        return $this->errorResponse('Admin tidak terautentikasi', 401);
      }

      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('ID wallpaper harus diisi', 400);
      }

      if (empty($data['decision'])) {
        return $this->errorResponse(
          'Keputusan moderasi (approved/rejected) harus diisi',
          400
        );
      }

      $adminId = (int)$data['admin_id'];
      $wallpaperId = (int)$data['wallpaper_id'];
      $decision = trim($data['decision']);
      $reason = !empty($data['reason']) ?
        trim($data['reason']) : null;

      // Jalankan use case
      $review = $this->moderateUseCase->execute(
        $adminId,
        $wallpaperId,
        $decision,
        $reason
      );

      return $this->successResponse(
        [
          'id' => $review->getId(),
          'wallpaper_id' => $review->getWallpaperId(),
          'admin_id' => $review->getAdminId(),
          'decision' => $review->getDecision(),
          'reason' => $review->getReason(),
          'reviewed_at' => $review->getReviewedAt(),
        ],
        'Moderasi wallpaper berhasil diproses',
        200
      );
    } catch (ValidationException $e) {
      return $this->errorResponse($e->getMessage(), 400);
    } catch (NotFoundException $e) {
      return $this->errorResponse($e->getMessage(), 404);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Dapatkan daftar wallpaper yang pending moderasi.
   * GET /moderation/pending
   * Body: {"admin_id": 1, "page": 1, "limit": 20}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function getPending(array $data): array
  {
    try {
      if (empty($data['admin_id'])) {
        return $this->errorResponse('Admin tidak terautentikasi', 401);
      }

      $page = !empty($data['page']) ? (int)$data['page'] : 1;
      $limit = !empty($data['limit']) ?
        (int)$data['limit'] : 20;

      // Pastikan page dan limit valid
      $page = max(1, $page);
      $limit = min(100, max(1, $limit));

      // Jalankan use case untuk dapatkan pending wallpapers
      // Note: Sebenarnya use case ModerateWallpaperUseCase memiliki
      // getPendingWallpapers() yang bisa dipanggil
      $wallpapers = $this->moderateUseCase
        ->getPendingWallpapers($page, $limit);

      $wallpaperData = [];
      foreach ($wallpapers as $wallpaper) {
        $wallpaperData[] = [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'contributor_id' => $wallpaper->getContributorId(),
          'category_id' => $wallpaper->getCategoryId(),
          'width' => $wallpaper->getWidth(),
          'height' => $wallpaper->getHeight(),
          'size_kb' => $wallpaper->getSizeKb(),
          'uploaded_at' => $wallpaper->getUploadedAt(),
        ];
      }

      return $this->successResponse(
        [
          'wallpapers' => $wallpaperData,
          'page' => $page,
          'limit' => $limit,
          'total' => count($wallpapers),
        ],
        'Daftar wallpaper pending berhasil diambil',
        200
      );
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
