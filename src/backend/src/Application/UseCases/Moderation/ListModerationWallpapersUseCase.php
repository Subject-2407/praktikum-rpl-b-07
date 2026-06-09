<?php

/**
 * Use case daftar wallpaper untuk moderasi.
 *
 * Use case ini memvalidasi filter admin dan mengambil queue moderasi dari
 * repository wallpaper.
 *
 * @package Scapes\Application\UseCases\Moderation
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Moderation;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas ListModerationWallpapersUseCase - Queue moderasi admin.
 */
class ListModerationWallpapersUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor ListModerationWallpapersUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Mengambil queue moderasi.
   *
   * @param array<string, mixed> $query Query string request.
   *
   * @return array{data: array<int, array<string, mixed>>, meta: array<string, int>}
   */
  public function execute(array $query): array {
    $filters = $this->normalizeFilters($query);
    $result = $this->wallpaperRepository->listForModeration($filters);

    return [
      'data' => $result['items'],
      'meta' => [
        'current_page' => $filters['page'],
        'per_page' => $filters['per_page'],
        'total' => $result['total'],
        'last_page' => max(1, (int) ceil($result['total'] / $filters['per_page'])),
      ],
    ];
  }

  /**
   * Normalisasi filter admin.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  private function normalizeFilters(array $query): array {
    $status = (string) ($query['status'] ?? 'pending');
    $sortBy = (string) ($query['sort_by'] ?? 'created_at');
    $order = strtolower((string) ($query['order'] ?? 'asc'));
    $errors = [];

    if (!in_array($status, ['pending', 'approved', 'rejected'], true)) {
      $errors['status'][] = 'The selected status is invalid.';
    }

    if (!in_array($sortBy, ['created_at', 'title'], true)) {
      $errors['sort_by'][] = 'The selected sort_by is invalid.';
    }

    if (!in_array($order, ['asc', 'desc'], true)) {
      $errors['order'][] = 'The selected order is invalid.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    return [
      'status' => $status,
      'contributor_id' => isset($query['contributor_id'])
        ? (int) $query['contributor_id']
        : null,
      'page' => max(1, (int) ($query['page'] ?? 1)),
      'per_page' => min(100, max(1, (int) ($query['per_page'] ?? 20))),
      'sort_by' => $sortBy,
      'order' => $order,
    ];
  }
}
