<?php

/**
 * Use case penghapusan wallpaper.
 *
 * Use case ini memastikan wallpaper ada, peminta berhak menghapus, lalu
 * menghapus file storage dan record database secara permanen.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Storage\FileStorage;

/**
 * Kelas DeleteWallpaperUseCase - Menghapus wallpaper.
 */
class DeleteWallpaperUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Storage file.
   *
   * @var FileStorage
   */
  private ?FileStorage $storage;

  /**
   * Konstruktor DeleteWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   * @param FileStorage $storage Storage file.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    ?FileStorage $storage = null
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->storage = $storage;
  }

  /**
   * Menghapus wallpaper.
   *
   * @param int|string $wallpaperId ID wallpaper.
   * @param int $requesterId ID pengguna peminta.
   * @param string $requesterRole Role pengguna peminta.
   *
   * @return void
   */
  public function execute(
    int|string $wallpaperId,
    int $requesterId,
    string $requesterRole
  ): void {
    if ($this->storage === null) {
      $this->executeLegacy($wallpaperId, $requesterId, $requesterRole);
      return;
    }

    $wallpaper = $this->wallpaperRepository->findDetailedById($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Resource not found.');
    }

    $isOwner = (int) $wallpaper['contributor_id'] === $requesterId;
    $isAdmin = $requesterRole === 'admin';

    if (!$isOwner && !$isAdmin) {
      throw new AuthorizationException(
        'Forbidden. You do not have access to this resource.'
      );
    }

    $this->wallpaperRepository->transaction(function () use ($wallpaperId): void {
      $this->wallpaperRepository->delete($wallpaperId);
    });

    $this->storage->delete((string) $wallpaper['file_path']);
    $this->storage->delete((string) $wallpaper['thumbnail_path']);
  }

  /**
   * Menjalankan alur delete lama untuk kompatibilitas unit test MVP.
   *
   * @param int $wallpaperId ID wallpaper.
   * @param int $requesterId ID peminta.
   * @param string $requesterRole Role peminta.
   *
   * @return void
   */
  private function executeLegacy(
    int $wallpaperId,
    int $requesterId,
    string $requesterRole
  ): void {
    $wallpaper = $this->wallpaperRepository->findByIdEntity($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Wallpaper tidak ditemukan');
    }

    $isOwner = $wallpaper->getContributorId() === $requesterId
      && $requesterRole === 'contributor';
    $isAdmin = $requesterRole === 'admin';

    if (!$isOwner && !$isAdmin) {
      throw new AuthorizationException(
        'Anda tidak memiliki permission untuk menghapus wallpaper ini'
      );
    }

    $this->wallpaperRepository->delete($wallpaperId);
  }
}
