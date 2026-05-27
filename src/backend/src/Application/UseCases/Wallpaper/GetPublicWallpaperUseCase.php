<?php

/**
 * Use case detail wallpaper publik.
 *
 * Mengambil satu wallpaper yang sudah approved dan published.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas GetPublicWallpaperUseCase - Detail wallpaper publik.
 */
class GetPublicWallpaperUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor GetPublicWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Mengambil detail wallpaper publik.
   *
   * @param int $id ID wallpaper.
   *
   * @return array<string, mixed> Detail wallpaper.
   */
  public function execute(int $id): array {
    $wallpaper = $this->wallpaperRepository->findPublicById($id);
    if ($wallpaper === null) {
      throw new NotFoundException('Resource not found.');
    }

    return $wallpaper;
  }
}
