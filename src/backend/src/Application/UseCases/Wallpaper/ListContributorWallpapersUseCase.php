<?php

/**
 * Use case daftar wallpaper milik contributor.
 *
 * Use case ini menampilkan wallpaper milik user login, termasuk status
 * pending, approved, dan rejected.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas ListContributorWallpapersUseCase - Daftar wallpaper pribadi.
 */
class ListContributorWallpapersUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor ListContributorWallpapersUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Mengambil wallpaper milik contributor.
   *
   * @param int $contributorId ID contributor.
   * @param array<string, mixed> $query Query string.
   *
   * @return array{data: array<int, array<string, mixed>>, meta: array<string, int>}
   */
  public function execute(int $contributorId, array $query): array {
    $status = isset($query['status']) && $query['status'] !== ''
      ? (string) $query['status']
      : null;
    if ($status !== null && !in_array($status, ['pending', 'approved', 'rejected'], true)) {
      throw new ValidationException('Validation failed.', 0, [
        'status' => ['The selected status is invalid.'],
      ]);
    }

    $page = max(1, (int) ($query['page'] ?? 1));
    $perPage = min(100, max(1, (int) ($query['per_page'] ?? 20)));
    $result = $this->wallpaperRepository->listByContributor(
      $contributorId,
      $status,
      $page,
      $perPage
    );

    return [
      'data' => $result['items'],
      'meta' => [
        'current_page' => $page,
        'per_page' => $perPage,
        'total' => $result['total'],
        'last_page' => max(1, (int) ceil($result['total'] / $perPage)),
      ],
    ];
  }
}
