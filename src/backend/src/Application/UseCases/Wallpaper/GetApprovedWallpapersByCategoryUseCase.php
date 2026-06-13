<?php

/**
 * Get Approved Wallpapers By Category Use Case
 *
 * Mendapatkan daftar wallpaper yang sudah disetujui berdasarkan kategori
 * dengan dukungan pagination.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\CategoryRepository;

/**
 * Kelas GetApprovedWallpapersByCategoryUseCase - Mendapatkan wallpaper approved.
 *
 * @class GetApprovedWallpapersByCategoryUseCase
 */
class GetApprovedWallpapersByCategoryUseCase {

  /**
   * Repository untuk akses wallpaper data.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository untuk akses kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Konstruktor GetApprovedWallpapersByCategoryUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository untuk wallpaper.
   * @param CategoryRepository $categoryRepository Repository untuk kategori.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    CategoryRepository $categoryRepository
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->categoryRepository = $categoryRepository;
  }

  /**
   * Mendapatkan wallpaper approved berdasarkan kategori.
   *
   * @param int $categoryId ID kategori.
   * @param int $limit Jumlah item per halaman.
   * @param int $page Nomor halaman (mulai dari 1).
   *
   * @return array<int, Wallpaper> Daftar wallpaper approved.
   * @throws ValidationException Jika kategori tidak ditemukan.
   */
  public function execute(int $categoryId, int $limit = 10, int $page = 1): array {
    // Validasi kategori
    $category = $this->categoryRepository->findByIdEntity($categoryId);
    if ($category === null) {
      throw new ValidationException('Kategori tidak ditemukan');
    }

    $offset = ($page - 1) * $limit;

    return $this->wallpaperRepository->findByCategoryAndStatus(
      $categoryId,
      'approved',
      $limit,
      $offset
    );
  }
}
