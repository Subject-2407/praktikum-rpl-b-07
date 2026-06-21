<?php

/**
 * Use case daftar kategori trending.
 *
 * Use case ini membaca agregasi kategori harian dari search analytics dan
 * dapat menyertakan kategori bawaan sebagai fallback.
 *
 * @package Scapes\Application\UseCases\Search
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Search;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\SearchAnalyticsRepository;

/**
 * Kelas ListTrendingCategoriesUseCase - Daftar kategori trending.
 */
class ListTrendingCategoriesUseCase {

  /**
   * Repository search analytics.
   *
   * @var SearchAnalyticsRepository
   */
  private SearchAnalyticsRepository $searchRepository;

  /**
   * Konstruktor ListTrendingCategoriesUseCase.
   *
   * @param SearchAnalyticsRepository $searchRepository Repository search.
   */
  public function __construct(SearchAnalyticsRepository $searchRepository) {
    $this->searchRepository = $searchRepository;
  }

  /**
   * Mengambil kategori trending.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<int, array<string, mixed>>
   */
  public function execute(array $query): array {
    $date = isset($query['date']) ? trim((string) $query['date']) : null;
    $limit = isset($query['limit']) ? (int) $query['limit'] : 10;
    $includeSystem = $this->boolValue($query['include_system'] ?? true);

    $errors = [];
    if ($date !== null && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
      $errors['date'][] = 'The date field must use YYYY-MM-DD format.';
    }

    if ($limit < 1 || $limit > 50) {
      $errors['limit'][] = 'The limit field must be between 1 and 50.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    return $this->searchRepository->listTrendingCategories(
      $date,
      $limit,
      $includeSystem
    );
  }

  /**
   * Mengubah nilai query menjadi boolean.
   *
   * @param mixed $value Nilai query.
   *
   * @return bool Nilai boolean.
   */
  private function boolValue(mixed $value): bool {
    if (is_bool($value)) {
      return $value;
    }

    return filter_var($value, FILTER_VALIDATE_BOOLEAN);
  }
}
