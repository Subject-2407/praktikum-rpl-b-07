<?php

/**
 * Moderate Wallpaper Use Case
 *
 * Menangani keputusan moderasi wallpaper oleh admin.
 * Admin dapat approve atau reject dengan alasan jika perlu.
 *
 * @package Scapes\Application\UseCases\Moderation
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Moderation;

use Scapes\Core\Domain\ModerationReview;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\ModerationReviewRepository;

/**
 * Kelas ModerateWallpaperUseCase - Melakukan moderasi wallpaper.
 *
 * @class ModerateWallpaperUseCase
 */
class ModerateWallpaperUseCase {

  /**
   * Repository untuk akses wallpaper data.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository untuk akses review moderasi.
   *
   * @var ModerationReviewRepository
   */
  private ModerationReviewRepository $moderationRepository;

  /**
   * Konstanta untuk keputusan approve.
   *
   * @var string
   */
  private const DECISION_APPROVED = 'approved';

  /**
   * Konstanta untuk keputusan reject.
   *
   * @var string
   */
  private const DECISION_REJECTED = 'rejected';

  /**
   * Konstruktor ModerateWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository untuk wallpaper.
   * @param ModerationReviewRepository $moderationRepository Repository untuk review.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    ModerationReviewRepository $moderationRepository
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->moderationRepository = $moderationRepository;
  }

  /**
   * Melakukan moderasi wallpaper.
   *
   * @param int $wallpaperId ID wallpaper yang dimoderasi.
   * @param int $adminId ID admin yang membuat keputusan.
   * @param string $decision Keputusan (approved atau rejected).
   * @param string|null $reason Alasan penolakan (wajib jika rejected).
   *
   * @return ModerationReview Review moderasi yang dibuat.
   * @throws NotFoundException Jika wallpaper tidak ditemukan.
   * @throws AuthorizationException Jika user bukan admin.
   * @throws ValidationException Jika data tidak valid.
   */
  public function execute(
    int $wallpaperId,
    int $adminId,
    string $decision,
    ?string $reason = null
  ): ModerationReview {
    // Validasi decision
    if (!in_array($decision, [self::DECISION_APPROVED, self::DECISION_REJECTED], true)) {
      throw new ValidationException('Keputusan harus approved atau rejected');
    }

    // Validasi reason wajib jika reject
    if ($decision === self::DECISION_REJECTED && empty(trim((string) $reason))) {
      throw new ValidationException('Alasan penolakan wajib diisi');
    }

    // Cari wallpaper
    $wallpaper = $this->wallpaperRepository->findByIdEntity($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Wallpaper tidak ditemukan');
    }

    // Cek wallpaper belum dimoderasi
    if (!$wallpaper->isPending()) {
      throw new ValidationException('Wallpaper sudah dimoderasi sebelumnya');
    }

    // Buat review moderasi
    $review = new ModerationReview(
      0,  // ID akan di-assign oleh database
      $wallpaperId,
      $adminId,
      $decision,
      $reason,
      date('Y-m-d H:i:s')
    );

    // Simpan review
    $review = $this->moderationRepository->save($review);

    // Update status wallpaper
    $wallpaper->setStatus($decision);
    $this->wallpaperRepository->save($wallpaper);

    return $review;
  }

  /**
   * Mendapatkan queue wallpaper yang pending moderasi.
   *
   * @param int $limit Jumlah item per page.
   * @param int $offset Offset untuk pagination.
   *
   * @return array Array dari Wallpaper dengan status pending.
   */
  public function getPendingWallpapers(int $limit = 10, int $offset = 0): array {
    return $this->wallpaperRepository->findByStatus('pending', $limit, $offset);
  }
}
