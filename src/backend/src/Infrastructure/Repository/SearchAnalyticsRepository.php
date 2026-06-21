<?php

/**
 * Repository search analytics.
 *
 * Repository ini menyimpan event pencarian dan membaca agregasi kategori
 * trending harian untuk rekomendasi Search tab.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas SearchAnalyticsRepository - Akses data search analytics.
 */
class SearchAnalyticsRepository extends BaseRepository {

  /**
   * Nama tabel search log.
   *
   * @var string
   */
  protected string $table = 'search_logs';

  /**
   * Menyimpan event pencarian dan memperbarui agregasi harian sederhana.
   *
   * @param array<string, mixed> $data Payload search event.
   *
   * @return array<string, mixed> Status penyimpanan.
   */
  public function logSearchEvent(array $data): array {
    $keywordRaw = trim((string) $data['keyword']);
    $keywordNormalized = $this->normalizeKeyword($keywordRaw);
    $keywordSlug = $this->slugify($keywordNormalized);
    $sourceSlug = $data['source_slug'] !== null
      ? $this->slugify((string) $data['source_slug'])
      : null;
    $clientHash = $data['client_hash'] !== null
      ? strtolower((string) $data['client_hash'])
      : null;

    if (
      $clientHash !== null
      && $this->isDuplicateEvent($keywordSlug, $sourceSlug, $clientHash)
    ) {
      return ['accepted' => true, 'deduplicated' => true];
    }

    $matchedCategoryId = $this->findCategoryIdBySlug($keywordSlug);
    $matchedTagId = $this->findTagIdBySlug($keywordSlug);

    try {
      $this->db->query(
        "INSERT INTO {$this->table}
          (keyword_raw, keyword_normalized, keyword_slug, source_slug,
            matched_category_id, matched_tag_id, client_hash, locale,
            result_count, searched_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
        [
          $keywordRaw,
          $keywordNormalized,
          $keywordSlug,
          $sourceSlug,
          $matchedCategoryId,
          $matchedTagId,
          $clientHash,
          $data['locale'],
          $data['result_count'],
        ]
      );

      $this->upsertDailyTrendingCategory(
        $keywordNormalized,
        $keywordSlug,
        $matchedCategoryId,
        $clientHash
      );

      return ['accepted' => true, 'deduplicated' => false];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal menyimpan search log: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengambil kategori trending harian.
   *
   * @param string|null $date Tanggal trending.
   * @param int $limit Jumlah kategori user-keyword.
   * @param bool $includeSystem Sertakan kategori bawaan.
   *
   * @return array<int, array<string, mixed>>
   */
  public function listTrendingCategories(
    ?string $date,
    int $limit,
    bool $includeSystem
  ): array {
    $trendDate = $date ?? date('Y-m-d');
    $limit = max(1, min(50, $limit));
    $items = [];

    try {
      $stmt = $this->db->query(
        "SELECT dtc.*, c.id AS system_category_id,
            c.name AS system_category_name,
            c.slug AS system_category_slug
          FROM daily_trending_categories dtc
          LEFT JOIN categories c ON c.id = dtc.category_id
          WHERE dtc.trend_date = ? AND dtc.origin = 'user_keyword'
          ORDER BY dtc.score DESC, dtc.search_count DESC, dtc.label ASC
          LIMIT ?",
        [$trendDate, $limit]
      );

      foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) ?: [] as $row) {
        $items[] = $this->formatTrendingRow($row);
      }

      if ($includeSystem) {
        $existingSlugs = array_fill_keys(
          array_map(
            fn (array $item): string => (string) $item['slug'],
            $items
          ),
          true
        );
        $items = array_merge(
          $items,
          $this->listSystemCategoryFallbacks($trendDate, $existingSlugs)
        );
      }

      return $items;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengambil kategori trending: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengambil rekomendasi user-keyword dari agregasi terbaru.
   *
   * @param string|null $keyword Keyword prefix.
   * @param int $limit Jumlah maksimal.
   *
   * @return array<int, array<string, mixed>>
   */
  public function listUserKeywordRecommendations(
    ?string $keyword,
    int $limit
  ): array {
    $trendDate = $this->latestTrendDate();
    if ($trendDate === null) {
      return [];
    }

    $keyword = $keyword !== null ? $this->slugify($keyword) : '';
    $params = [$trendDate];
    $where = "WHERE trend_date = ? AND origin = 'user_keyword'";

    if ($keyword !== '') {
      $where .= ' AND (slug LIKE ? OR LOWER(label) LIKE ?)';
      $like = $keyword . '%';
      $params[] = $like;
      $params[] = $like;
    }

    $limit = max(1, min(50, $limit));
    $params[] = $limit;

    try {
      $stmt = $this->db->query(
        "SELECT *
          FROM daily_trending_categories
          {$where}
          ORDER BY score DESC, search_count DESC, label ASC
          LIMIT ?",
        $params
      );

      $items = [];
      foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) ?: [] as $row) {
        $items[] = [
          'type' => 'user_keyword_category',
          'label' => (string) $row['label'],
          'value' => (string) $row['slug'],
          'score' => (float) $row['score'],
          'match_reason' => 'trending',
        ];
      }

      return $items;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengambil rekomendasi keyword: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengambil tanggal agregasi terbaru.
   *
   * @return string|null Tanggal terbaru.
   */
  public function latestTrendDate(): ?string {
    try {
      $stmt = $this->db->query(
        "SELECT MAX(trend_date) AS trend_date
          FROM daily_trending_categories
          WHERE origin = 'user_keyword'"
      );
      $row = $stmt->fetch(PDO::FETCH_ASSOC) ?: [];
      $date = $row['trend_date'] ?? null;

      return is_string($date) && $date !== '' ? $date : null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal membaca tanggal trending: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengecek duplikasi event singkat untuk client anonim yang sama.
   *
   * @param string $keywordSlug Slug keyword.
   * @param string|null $sourceSlug Slug source.
   * @param string $clientHash Hash anonim.
   *
   * @return bool True jika duplikat.
   */
  private function isDuplicateEvent(
    string $keywordSlug,
    ?string $sourceSlug,
    string $clientHash
  ): bool {
    $stmt = $this->db->query(
      "SELECT id
        FROM {$this->table}
        WHERE keyword_slug = ?
          AND (source_slug <=> ?)
          AND client_hash = ?
          AND searched_at >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)
        LIMIT 1",
      [$keywordSlug, $sourceSlug, $clientHash]
    );

    return (bool) $stmt->fetch(PDO::FETCH_ASSOC);
  }

  /**
   * Mencari kategori exact berdasarkan slug.
   *
   * @param string $slug Slug kategori.
   *
   * @return int|null ID kategori.
   */
  private function findCategoryIdBySlug(string $slug): ?int {
    $stmt = $this->db->query(
      'SELECT id FROM categories WHERE slug = ? LIMIT 1',
      [$slug]
    );
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    return $row ? (int) $row['id'] : null;
  }

  /**
   * Mencari tag exact berdasarkan slug.
   *
   * @param string $slug Slug tag.
   *
   * @return int|null ID tag.
   */
  private function findTagIdBySlug(string $slug): ?int {
    $stmt = $this->db->query(
      'SELECT id FROM tags WHERE slug = ? LIMIT 1',
      [$slug]
    );
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    return $row ? (int) $row['id'] : null;
  }

  /**
   * Memperbarui agregasi kategori trending harian.
   *
   * @param string $keyword Keyword normal.
   * @param string $slug Slug keyword.
   * @param int|null $categoryId ID kategori sistem jika match.
   * @param string|null $clientHash Hash anonim.
   *
   * @return void
   */
  private function upsertDailyTrendingCategory(
    string $keyword,
    string $slug,
    ?int $categoryId,
    ?string $clientHash
  ): void {
    $uniqueIncrement = $clientHash !== null ? 1 : 0;
    $scoreIncrement = 1.0 + ($uniqueIncrement * 0.5);
    $label = $this->labelFromSlug($slug);
    $topKeywords = json_encode([$keyword], JSON_UNESCAPED_SLASHES);

    $this->db->query(
      "INSERT INTO daily_trending_categories
        (trend_date, category_id, label, slug, origin, search_count,
          unique_client_count, score, top_keywords, computed_at)
        VALUES (CURDATE(), ?, ?, ?, 'user_keyword', 1, ?, ?, ?, NOW())
        ON DUPLICATE KEY UPDATE
          category_id = COALESCE(VALUES(category_id), category_id),
          search_count = search_count + 1,
          unique_client_count = unique_client_count + VALUES(unique_client_count),
          score = score + VALUES(score),
          top_keywords = VALUES(top_keywords),
          computed_at = NOW()",
      [
        $categoryId,
        $label,
        $slug,
        $uniqueIncrement,
        $scoreIncrement,
        $topKeywords,
      ]
    );
  }

  /**
   * Mengambil kategori bawaan sebagai fallback response.
   *
   * @param string $trendDate Tanggal response.
   * @param array<string, bool> $existingSlugs Slug yang sudah ada.
   *
   * @return array<int, array<string, mixed>>
   */
  private function listSystemCategoryFallbacks(
    string $trendDate,
    array $existingSlugs
  ): array {
    $stmt = $this->db->query(
      'SELECT id, name, slug FROM categories ORDER BY name ASC'
    );
    $items = [];

    foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) ?: [] as $row) {
      $slug = (string) $row['slug'];
      if (isset($existingSlugs[$slug])) {
        continue;
      }

      $items[] = [
        'origin' => 'system',
        'category' => [
          'id' => (int) $row['id'],
          'name' => (string) $row['name'],
          'slug' => $slug,
        ],
        'label' => (string) $row['name'],
        'slug' => $slug,
        'trend_date' => $trendDate,
        'search_count' => 0,
        'score' => 0.0,
        'top_keywords' => [],
      ];
    }

    return $items;
  }

  /**
   * Format row trending menjadi response.
   *
   * @param array<string, mixed> $row Row database.
   *
   * @return array<string, mixed>
   */
  private function formatTrendingRow(array $row): array {
    $topKeywords = json_decode((string) ($row['top_keywords'] ?? '[]'), true);

    return [
      'origin' => (string) $row['origin'],
      'category' => $row['system_category_id'] !== null
        ? [
          'id' => (int) $row['system_category_id'],
          'name' => (string) $row['system_category_name'],
          'slug' => (string) $row['system_category_slug'],
        ]
        : null,
      'label' => (string) $row['label'],
      'slug' => (string) $row['slug'],
      'trend_date' => (string) $row['trend_date'],
      'search_count' => (int) $row['search_count'],
      'score' => (float) $row['score'],
      'top_keywords' => is_array($topKeywords) ? $topKeywords : [],
    ];
  }

  /**
   * Normalisasi keyword display.
   *
   * @param string $keyword Keyword mentah.
   *
   * @return string Keyword normal.
   */
  private function normalizeKeyword(string $keyword): string {
    $keyword = strtolower(trim($keyword));
    $keyword = ltrim($keyword, '#');
    $keyword = preg_replace('/[^a-z0-9\s-]+/', ' ', $keyword) ?? '';
    $keyword = preg_replace('/\s+/', ' ', $keyword) ?? '';

    return trim($keyword);
  }

  /**
   * Membentuk slug dari teks.
   *
   * @param string $value Teks mentah.
   *
   * @return string Slug.
   */
  private function slugify(string $value): string {
    $value = strtolower(trim($value));
    $value = ltrim($value, '#');
    $value = preg_replace('/[^a-z0-9\s-]+/', '', $value) ?? '';
    $value = preg_replace('/[\s-]+/', '-', $value) ?? '';

    return trim($value, '-');
  }

  /**
   * Membentuk label dari slug.
   *
   * @param string $slug Slug kategori.
   *
   * @return string Label.
   */
  private function labelFromSlug(string $slug): string {
    return ucwords(str_replace('-', ' ', $slug));
  }
}
