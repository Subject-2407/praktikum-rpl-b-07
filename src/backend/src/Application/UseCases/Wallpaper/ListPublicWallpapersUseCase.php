<?php

/**
 * Use case daftar wallpaper publik.
 *
 * Use case ini memvalidasi query filter, sorting, dan pagination sebelum
 * mengambil wallpaper approved yang sudah published.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

/**
 * Kelas ListPublicWallpapersUseCase - Daftar wallpaper publik.
 */
class ListPublicWallpapersUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Konstruktor ListPublicWallpapersUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   */
  public function __construct(WallpaperRepository $wallpaperRepository) {
    $this->wallpaperRepository = $wallpaperRepository;
  }

  /**
   * Mengambil daftar wallpaper publik.
   *
   * @param array<string, mixed> $query Query string request.
   *
   * @return array{data: array<int, array<string, mixed>>, meta: array<string, int>}
   */
  public function execute(array $query): array {
    $filters = $this->normalizeFilters($query);
    $result = $this->wallpaperRepository->listPublic($filters);

    return [
      'data' => $result['items'],
      'meta' => $this->meta(
        $filters['page'],
        $filters['per_page'],
        $result['total']
      ),
    ];
  }

  /**
   * Normalisasi filter request.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  private function normalizeFilters(array $query): array {
    $page = max(1, (int) ($query['page'] ?? 1));
    $perPage = (int) ($query['per_page'] ?? 20);
    $sortBy = (string) ($query['sort_by'] ?? 'published_at');
    $order = strtolower((string) ($query['order'] ?? 'desc'));
    $targetDevice = $query['target_device'] ?? null;

    $errors = [];
    if (!in_array($sortBy, ['published_at', 'title'], true)) {
      $errors['sort_by'][] = 'The selected sort_by is invalid.';
    }

    if (!in_array($order, ['asc', 'desc'], true)) {
      $errors['order'][] = 'The selected order is invalid.';
    }

    if ($perPage < 1 || $perPage > 100) {
      $errors['per_page'][] = 'The selected per_page is invalid.';
    }

    if (
      $targetDevice !== null
      && $targetDevice !== ''
      && !in_array($targetDevice, ['desktop', 'mobile', 'tablet'], true)
    ) {
      $errors['target_device'][] = 'The selected target_device is invalid.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    return [
      'q' => isset($query['q']) ? trim((string) $query['q']) : null,
      'category' => isset($query['category'])
        ? trim((string) $query['category'])
        : null,
      'tags' => $this->normalizeTags($query['tag'] ?? []),
      'target_device' => $targetDevice !== null && $targetDevice !== ''
        ? (string) $targetDevice
        : null,
      'page' => $page,
      'per_page' => $perPage,
      'sort_by' => $sortBy,
      'order' => $order,
    ];
  }

  /**
   * Normalisasi query tag tunggal atau multi.
   *
   * @param mixed $rawTags Input tag.
   *
   * @return array<int, string>
   */
  private function normalizeTags(mixed $rawTags): array {
    if (is_string($rawTags)) {
      return [$rawTags];
    }

    if (!is_array($rawTags)) {
      return [];
    }

    return array_values(array_map('strval', $rawTags));
  }

  /**
   * Membentuk metadata pagination.
   *
   * @param int $page Halaman saat ini.
   * @param int $perPage Jumlah item per halaman.
   * @param int $total Total data.
   *
   * @return array<string, int>
   */
  private function meta(int $page, int $perPage, int $total): array {
    return [
      'current_page' => $page,
      'per_page' => $perPage,
      'total' => $total,
      'last_page' => max(1, (int) ceil($total / $perPage)),
    ];
  }
}
