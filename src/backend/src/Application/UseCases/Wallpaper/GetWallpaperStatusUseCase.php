<?php

/**
 * Get Wallpaper Status Use Case
 *
 * Mendapatkan status moderasi wallpaper untuk contributor.
 * Menampilkan informasi lengkap wallpaper beserta riwayat review.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas GetWallpaperStatusUseCase - Mendapatkan status wallpaper.
 *
 * @class GetWallpaperStatusUseCase
 */
class GetWallpaperStatusUseCase {

  /**
   * Repository untuk akses wallpaper data.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor GetWallpaperStatusUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository untuk wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Mendapatkan status wallpaper.
   *
   * @param int $wallpaperId ID wallpaper.
   *
   * @return Wallpaper Wallpaper dengan informasi status.
   * @throws NotFoundException Jika wallpaper tidak ditemukan.
   */
  public function execute(int $wallpaperId): Wallpaper {
    $wallpaper = $this->wallpaperRepository->findByIdEntity($wallpaperId);

    if ($wallpaper === null) {
      throw new NotFoundException('Wallpaper tidak ditemukan');
    }

    return $wallpaper;
  }

  /**
   * Mendapatkan semua wallpaper dari contributor.
   *
   * @param int $contributorId ID contributor.
   *
   * @return array Array dari Wallpaper.
   */
  public function getAllByContributor(int $contributorId): array {
    return $this->wallpaperRepository->findByContributorId($contributorId);
  }
}
