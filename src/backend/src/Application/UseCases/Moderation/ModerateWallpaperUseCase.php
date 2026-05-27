<?php

/**
 * Use case keputusan moderasi wallpaper.
 *
 * Admin dapat menyetujui atau menolak wallpaper pending. Approval akan
 * memindahkan file ke folder approved dan mengisi published_at.
 *
 * @package Scapes\Application\UseCases\Moderation
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Moderation;

use Scapes\Core\Domain\ModerationReview;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\UnprocessableEntityException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\ModerationReviewRepository;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Storage\FileStorage;

/**
 * Kelas ModerateWallpaperUseCase - Menyimpan keputusan moderasi.
 */
class ModerateWallpaperUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository review moderasi.
   *
   * @var ModerationReviewRepository
   */
  private ModerationReviewRepository $moderationRepository;

  /**
   * Storage file.
   *
   * @var FileStorage
   */
  private ?FileStorage $storage;

  /**
   * Konstruktor ModerateWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   * @param ModerationReviewRepository $moderationRepository Repository review.
   * @param FileStorage $storage Storage file.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    ModerationReviewRepository $moderationRepository,
    ?FileStorage $storage = null
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->moderationRepository = $moderationRepository;
    $this->storage = $storage;
  }

  /**
   * Menyimpan keputusan moderasi.
   *
   * @param int $wallpaperId ID wallpaper.
   * @param int $adminId ID admin.
   * @param array<string, mixed> $data Data request.
   *
   * @return array<string, mixed> Detail wallpaper hasil moderasi.
   */
  public function execute(
    int $wallpaperId,
    int $adminId,
    array|string $data,
    ?string $legacyReason = null
  ): array|ModerationReview {
    if (is_string($data)) {
      return $this->executeLegacy(
        $wallpaperId,
        $adminId,
        $data,
        $legacyReason
      );
    }

    $storage = $this->storage;
    if ($storage === null) {
      throw new \LogicException('Dependency storage moderasi belum lengkap.');
    }

    $decision = (string) ($data['decision'] ?? '');
    $reason = isset($data['reason']) ? trim((string) $data['reason']) : null;

    $this->validateDecision($decision, $reason);

    $wallpaper = $this->wallpaperRepository->findDetailedById($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Resource not found.');
    }

    if ((string) $wallpaper['status'] !== 'pending') {
      throw new UnprocessableEntityException(
        "Wallpaper is not in 'pending' status."
      );
    }

    $publishedAt = null;
    $newPath = null;
    $oldPath = (string) $wallpaper['file_path'];

    if ($decision === 'approved') {
      $newPath = $storage->move(
        $oldPath,
        'approved' . DIRECTORY_SEPARATOR . $wallpaper['category']['slug']
      );
      $publishedAt = date('Y-m-d H:i:s');
    }

    try {
      $this->wallpaperRepository->transaction(
        function () use (
          $wallpaperId,
          $adminId,
          $decision,
          $reason,
          $newPath,
          $publishedAt
        ): void {
          $review = new ModerationReview(
            0,
            $wallpaperId,
            $adminId,
            $decision,
            $reason,
            date('Y-m-d H:i:s')
          );

          $this->moderationRepository->save($review);
          $this->wallpaperRepository->updateModerationState(
            $wallpaperId,
            $decision,
            $newPath,
            $publishedAt
          );
        }
      );
    } catch (\Throwable $e) {
      if ($newPath !== null) {
        $storage->move(
          $newPath,
          'pending' . DIRECTORY_SEPARATOR . $wallpaper['category']['slug']
        );
      }
      throw $e;
    }

    return $this->wallpaperRepository->findDetailedById($wallpaperId) ?? [];
  }

  /**
   * Mendapatkan queue wallpaper pending untuk kompatibilitas lama.
   *
   * @param int $limit Jumlah item.
   * @param int $offset Offset item.
   *
   * @return array<int, \Scapes\Core\Domain\Wallpaper>
   */
  public function getPendingWallpapers(int $limit = 10, int $offset = 0): array {
    return $this->wallpaperRepository->findByStatus('pending', $limit, $offset);
  }

  /**
   * Validasi keputusan moderasi.
   *
   * @param string $decision Keputusan admin.
   * @param string|null $reason Alasan penolakan.
   *
   * @return void
   */
  private function validateDecision(string $decision, ?string $reason): void {
    $errors = [];

    if (!in_array($decision, ['approved', 'rejected'], true)) {
      $errors['decision'][] = "The decision field must be 'approved' or 'rejected'.";
    }

    if ($decision === 'rejected' && ($reason === null || $reason === '')) {
      $errors['reason'][] =
        "Rejection reason is required when decision is 'rejected'.";
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }
  }

  /**
   * Menjalankan alur moderasi lama untuk kompatibilitas unit test MVP.
   *
   * @param int $wallpaperId ID wallpaper.
   * @param int $adminId ID admin.
   * @param string $decision Keputusan.
   * @param string|null $reason Alasan penolakan.
   *
   * @return ModerationReview Review tersimpan.
   */
  private function executeLegacy(
    int $wallpaperId,
    int $adminId,
    string $decision,
    ?string $reason
  ): ModerationReview {
    if (!in_array($decision, ['approved', 'rejected'], true)) {
      throw new ValidationException('Keputusan harus approved atau rejected');
    }

    if ($decision === 'rejected' && empty(trim((string) $reason))) {
      throw new ValidationException('Alasan penolakan wajib diisi');
    }

    $wallpaper = $this->wallpaperRepository->findByIdEntity($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Wallpaper tidak ditemukan');
    }

    if (!$wallpaper->isPending()) {
      throw new ValidationException('Wallpaper sudah dimoderasi sebelumnya');
    }

    $review = new ModerationReview(
      0,
      $wallpaperId,
      $adminId,
      $decision,
      $reason,
      date('Y-m-d H:i:s')
    );
    $savedReview = $this->moderationRepository->save($review);

    $wallpaper->setStatus($decision);
    $this->wallpaperRepository->save($wallpaper);

    return $savedReview;
  }
}
