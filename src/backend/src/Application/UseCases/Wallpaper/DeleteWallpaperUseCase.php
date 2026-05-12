<?php

/**
 * Delete Wallpaper Use Case
 *
 * Menangani penghapusan wallpaper oleh contributor.
 * Hanya contributor yang upload atau admin yang bisa menghapus.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas DeleteWallpaperUseCase - Menghapus wallpaper.
 *
 * @class DeleteWallpaperUseCase
 */
class DeleteWallpaperUseCase {

  /**
   * Repository untuk akses wallpaper data.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor DeleteWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository untuk wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Menghapus wallpaper.
   *
   * @param int $wallpaperId ID wallpaper yang akan dihapus.
   * @param int $requesterId ID user yang request penghapusan.
   * @param string $requesterRole Role user yang request.
   *
   * @return void
   * @throws NotFoundException Jika wallpaper tidak ditemukan.
   * @throws AuthorizationException Jika user tidak punya permission.
   */
  public function execute(int $wallpaperId, int $requesterId, string $requesterRole): void {
    // Cari wallpaper
    $wallpaper = $this->wallpaperRepository->findByIdEntity($wallpaperId);

    if ($wallpaper === null) {
      throw new NotFoundException('Wallpaper tidak ditemukan');
    }

    // Cek authorization: hanya contributor pemilik atau admin yang bisa hapus
    $isOwner = $wallpaper->getContributorId() === $requesterId && $requesterRole === 'contributor';
    $isAdmin = $requesterRole === 'admin';

    if (!$isOwner && !$isAdmin) {
      throw new AuthorizationException('Anda tidak memiliki permission untuk menghapus wallpaper ini');
    }

    // Hapus file dari filesystem (akan diimplementasikan di Infrastructure)
    // TODO: Delete file logic

    // Hapus dari database
    $this->wallpaperRepository->delete($wallpaperId);
  }
}
