<?php

/**
 * Use case pencatatan search event.
 *
 * Use case ini memvalidasi payload search analytics sebelum disimpan sebagai
 * log privasi-aman dan bahan agregasi rekomendasi.
 *
 * @package Scapes\Application\UseCases\Search
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Search;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\SearchAnalyticsRepository;

/**
 * Kelas LogSearchEventUseCase - Mencatat event pencarian.
 */
class LogSearchEventUseCase {

  /**
   * Repository search analytics.
   *
   * @var SearchAnalyticsRepository
   */
  private SearchAnalyticsRepository $searchRepository;

  /**
   * Konstruktor LogSearchEventUseCase.
   *
   * @param SearchAnalyticsRepository $searchRepository Repository search.
   */
  public function __construct(SearchAnalyticsRepository $searchRepository) {
    $this->searchRepository = $searchRepository;
  }

  /**
   * Mencatat event pencarian.
   *
   * @param array<string, mixed> $data Payload request.
   *
   * @return array<string, mixed>
   */
  public function execute(array $data): array {
    $keyword = trim((string) ($data['keyword'] ?? ''));
    $sourceSlug = isset($data['source_slug'])
      ? trim((string) $data['source_slug'])
      : null;
    $clientHash = isset($data['client_hash'])
      ? trim((string) $data['client_hash'])
      : null;
    $locale = isset($data['locale']) ? trim((string) $data['locale']) : null;
    $resultCount = array_key_exists('result_count', $data)
      ? (int) $data['result_count']
      : null;

    $sourceSlug = $sourceSlug !== '' ? $sourceSlug : null;
    $clientHash = $clientHash !== '' ? $clientHash : null;
    $locale = $locale !== '' ? $locale : null;

    $errors = [];
    if (strlen($keyword) < 2 || strlen($keyword) > 255) {
      $errors['keyword'][] = 'The keyword field must be between 2 and 255 characters.';
    }

    if ($sourceSlug !== null && strlen($sourceSlug) > 100) {
      $errors['source_slug'][] = 'The source_slug field must not exceed 100 characters.';
    }

    if (
      $clientHash !== null
      && !preg_match('/^[a-f0-9]{64}$/i', $clientHash)
    ) {
      $errors['client_hash'][] = 'The client_hash field must be a SHA-256 hex string.';
    }

    if ($locale !== null && strlen($locale) > 20) {
      $errors['locale'][] = 'The locale field must not exceed 20 characters.';
    }

    if ($resultCount !== null && $resultCount < 0) {
      $errors['result_count'][] = 'The result_count field must be at least 0.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    return $this->searchRepository->logSearchEvent([
      'keyword' => $keyword,
      'source_slug' => $sourceSlug,
      'client_hash' => $clientHash,
      'locale' => $locale,
      'result_count' => $resultCount,
    ]);
  }
}
