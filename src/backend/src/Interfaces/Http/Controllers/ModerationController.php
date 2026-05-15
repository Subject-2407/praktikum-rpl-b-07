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
   *   "wallpaper_id": 1,
   *   "decision": "approved|rejected",
   *   "reason": "Optional reason if rejected"
   * }
   *
   * @param array<string, mixed> $data Data permintaan
   * @param array<string, mixed> $authUser Data admin terautentikasi
   * @return array<string, mixed> Respons JSON
   */
  public function moderate(array $data, array $authUser): array
  {
    try {
      // Validasi input dasar
      if (empty($authUser['user_id'])) {
        return $this->errorResponse('Admin not authenticated', 401);
      }

      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('Wallpaper ID is required', 400);
      }

      if (empty($data['decision'])) {
        return $this->errorResponse(
          'Moderation decision (approved/rejected) is required',
          400
        );
      }

      $adminId = (int)$authUser['user_id'];
      $wallpaperId = (int)$data['wallpaper_id'];
      $decision = trim($data['decision']);
      $reason = !empty($data['reason']) ?
        trim($data['reason']) : null;

      // Jalankan use case
      $review = $this->moderateUseCase->execute(
        $wallpaperId,
        $adminId,
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
        'Wallpaper moderation processed successfully',
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
   * Body: {"page": 1, "limit": 20}
   *
   * @param array<string, mixed> $data Data permintaan
   * @param array<string, mixed> $authUser Data admin terautentikasi
   * @return array<string, mixed> Respons JSON
   */
  public function getPending(array $data, array $authUser): array
  {
    try {
      if (empty($authUser['user_id'])) {
        return $this->errorResponse('Admin not authenticated', 401);
      }

      $page = !empty($data['page']) ? (int)$data['page'] : 1;
      $limit = !empty($data['limit']) ?
        (int)$data['limit'] : 20;

      // Pastikan page dan limit valid
      $page = max(1, $page);
      $limit = min(100, max(1, $limit));

      // Jalankan use case untuk dapatkan pending wallpapers
      $wallpapers = $this->moderateUseCase
        ->getPendingWallpapers($limit, ($page - 1) * $limit);

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
          'size_kb' => $wallpaper->getFileSizeKb(),
          'uploaded_at' => $wallpaper->getCreatedAt(),
        ];
      }

      return $this->successResponse(
        [
          'wallpapers' => $wallpaperData,
          'page' => $page,
          'limit' => $limit,
          'total' => count($wallpapers),
        ],
        'Pending wallpapers list retrieved successfully',
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
