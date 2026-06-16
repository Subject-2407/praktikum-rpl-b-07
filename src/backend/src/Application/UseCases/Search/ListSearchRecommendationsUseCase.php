<?php

/**
 * Use case rekomendasi search.
 *
 * Use case ini menggabungkan prioritas kategori/tag internal Scapes dengan
 * rekomendasi user-keyword dari agregasi search logs.
 *
 * @package Scapes\Application\UseCases\Search
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Search;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\SearchAnalyticsRepository;
use Scapes\Infrastructure\Repository\TagRepository;

/**
 * Kelas ListSearchRecommendationsUseCase - Rekomendasi search.
 */
class ListSearchRecommendationsUseCase {

  /**
   * Source internal Scapes.
   *
   * @var string
   */
  private const INTERNAL_SOURCE = 'scapes';

  /**
   * Repository search analytics.
   *
   * @var SearchAnalyticsRepository
   */
  private SearchAnalyticsRepository $searchRepository;

  /**
   * Repository kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Repository tag.
   *
   * @var TagRepository
   */
  private TagRepository $tagRepository;

  /**
   * Konstruktor ListSearchRecommendationsUseCase.
   *
   * @param SearchAnalyticsRepository $searchRepository Repository search.
   * @param CategoryRepository $categoryRepository Repository kategori.
   * @param TagRepository $tagRepository Repository tag.
   */
  public function __construct(
    SearchAnalyticsRepository $searchRepository,
    CategoryRepository $categoryRepository,
    TagRepository $tagRepository
  ) {
    $this->searchRepository = $searchRepository;
    $this->categoryRepository = $categoryRepository;
    $this->tagRepository = $tagRepository;
  }

  /**
   * Mengambil rekomendasi search.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array{data: array<int, array<string, mixed>>, meta: array<string, mixed>}
   */
  public function execute(array $query): array {
    $keyword = trim((string) ($query['q'] ?? ''));
    $sourceSlug = $this->slugify((string) ($query['source_slug'] ?? self::INTERNAL_SOURCE));
    $limit = isset($query['limit']) ? (int) $query['limit'] : 10;
    $includeSystem = $this->boolValue($query['include_system'] ?? true);
    $systemMatchFirst = $this->boolValue($query['system_match_first'] ?? true);
    $tagMatchFirst = $this->boolValue($query['tag_match_first'] ?? true);

    $errors = [];
    if ($limit < 1 || $limit > 50) {
      $errors['limit'][] = 'The limit field must be between 1 and 50.';
    }

    if (strlen($keyword) > 255) {
      $errors['q'][] = 'The q field must not exceed 255 characters.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    $internalMetadataEnabled = $sourceSlug === self::INTERNAL_SOURCE;
    $isTagMode = str_starts_with($keyword, '#');
    $normalKeyword = $this->normalizeKeyword($keyword);
    $items = [];
    $seen = [];

    if ($internalMetadataEnabled && $isTagMode && $tagMatchFirst) {
      foreach ($this->tagRepository->findAllAsArray($keyword, 'prefix', $limit) as $tag) {
        $this->appendItem($items, $seen, [
          'type' => 'tag',
          'label' => '#' . (string) $tag['slug'],
          'value' => (string) $tag['slug'],
          'score' => 100.0,
          'match_reason' => 'prefix_match',
        ]);
      }
    }

    if (
      $internalMetadataEnabled
      && !$isTagMode
      && $systemMatchFirst
      && $normalKeyword !== ''
    ) {
      foreach ($this->categoryRepository->findMatchingAsArray($keyword, $limit) as $category) {
        $score = isset($category['match_score'])
          ? (float) $category['match_score']
          : 90.0;
        $this->appendItem($items, $seen, [
          'type' => 'system_category',
          'label' => (string) $category['name'],
          'value' => (string) $category['slug'],
          'score' => $score,
          'match_reason' => $score >= 100.0 ? 'exact_match' : 'prefix_match',
        ]);
      }
    }

    $recommendationKeyword = $normalKeyword !== '' ? $normalKeyword : null;
    foreach (
      $this->searchRepository->listUserKeywordRecommendations(
        $recommendationKeyword,
        $limit
      ) as $recommendation
    ) {
      $this->appendItem($items, $seen, $recommendation);
    }

    if (
      $internalMetadataEnabled
      && $includeSystem
      && $normalKeyword === ''
    ) {
      foreach ($this->categoryRepository->findAllAsArray() as $category) {
        $this->appendItem($items, $seen, [
          'type' => 'system_category',
          'label' => (string) $category['name'],
          'value' => (string) $category['slug'],
          'score' => 0.0,
          'match_reason' => 'fallback',
        ]);
      }
    }

    return [
      'data' => $items,
      'meta' => [
        'strategy' => $internalMetadataEnabled
          ? 'system_match_then_daily_trending'
          : 'daily_trending_only',
        'generated_from' => $this->searchRepository->latestTrendDate(),
        'source_slug' => $sourceSlug,
        'internal_metadata_enabled' => $internalMetadataEnabled,
        'system_match_first' => $systemMatchFirst,
        'tag_match_first' => $tagMatchFirst,
        'user_keyword_limit' => $limit,
      ],
    ];
  }

  /**
   * Menambahkan item unik ke daftar rekomendasi.
   *
   * @param array<int, array<string, mixed>> $items Daftar item.
   * @param array<string, bool> $seen Key item yang sudah masuk.
   * @param array<string, mixed> $item Item baru.
   *
   * @return void
   */
  private function appendItem(array &$items, array &$seen, array $item): void {
    $key = (string) $item['type'] . ':' . (string) $item['value'];
    if (isset($seen[$key])) {
      return;
    }

    $seen[$key] = true;
    $items[] = $item;
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

  /**
   * Normalisasi keyword input.
   *
   * @param string $keyword Keyword mentah.
   *
   * @return string Keyword normal.
   */
  private function normalizeKeyword(string $keyword): string {
    $keyword = strtolower(trim($keyword));
    $keyword = ltrim($keyword, '#');
    $keyword = preg_replace('/[^a-z0-9\s-]+/', '', $keyword) ?? '';
    $keyword = preg_replace('/[\s-]+/', '-', $keyword) ?? '';

    return trim($keyword, '-');
  }

  /**
   * Membentuk slug.
   *
   * @param string $value Teks mentah.
   *
   * @return string Slug.
   */
  private function slugify(string $value): string {
    return $this->normalizeKeyword($value);
  }
}
