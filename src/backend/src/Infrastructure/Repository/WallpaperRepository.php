<?php

/**
 * Repository wallpaper.
 *
 * Repository ini menangani query untuk daftar publik, area contributor,
 * moderasi admin, dan operasi tulis pada tabel wallpapers.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas WallpaperRepository - Repository untuk data wallpaper.
 */
class WallpaperRepository extends BaseRepository {

  /**
   * Nama tabel wallpaper.
   *
   * @var string
   */
  protected string $table = 'wallpapers';

  /**
   * Daftar field sort publik yang diizinkan.
   *
   * @var array<string, string>
   */
  private const PUBLIC_SORTS = [
    'published_at' => 'w.published_at',
    'title' => 'w.title',
  ];

  /**
   * Daftar field sort moderasi yang diizinkan.
   *
   * @var array<string, string>
   */
  private const MODERATION_SORTS = [
    'created_at' => 'w.created_at',
    'title' => 'w.title',
  ];

  /**
   * Mencari wallpaper berdasarkan ID sebagai entity lama.
   *
   * @param int|string $id ID wallpaper.
   *
   * @return Wallpaper|null Entity wallpaper jika ditemukan.
   */
  public function findByIdEntity(int $id): ?Wallpaper {
    $row = $this->findRawById($id);
    return $row === null ? null : $this->mapToWallpaper($row);
  }

  /**
   * Mencari row wallpaper mentah berdasarkan ID.
   *
   * @param int $id ID wallpaper.
   *
   * @return array<string, mixed>|null Row wallpaper jika ditemukan.
   */
  public function findRawById(int|string $id): ?array {
    try {
      $stmt = $this->db->query(
        "SELECT * FROM {$this->table} WHERE id = ? LIMIT 1",
        [$id]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return $data ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari detail wallpaper publik yang sudah approved dan published.
   *
   * @param int|string $id ID wallpaper.
   *
   * @return array<string, mixed>|null Detail wallpaper.
   */
  public function findPublicById(int|string $id): ?array {
    try {
      $stmt = $this->db->query(
        $this->baseSelect()
          . ' WHERE w.id = ?
            AND w.status = ?
            AND w.published_at IS NOT NULL
          LIMIT 1',
        [$id, 'approved']
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
      $items = $this->hydrateRows($rows);

      return $items[0] ?? null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari wallpaper publik: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari detail wallpaper internal tanpa filter status.
   *
   * @param int|string $id ID wallpaper.
   *
   * @return array<string, mixed>|null Detail wallpaper.
   */
  public function findDetailedById(int|string $id): ?array {
    try {
      $stmt = $this->db->query(
        $this->baseSelect() . ' WHERE w.id = ? LIMIT 1',
        [$id]
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
      $items = $this->hydrateRows($rows);

      return $items[0] ?? null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari detail wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mendapatkan daftar wallpaper publik dengan filter dan pagination.
   *
   * @param array<string, mixed> $filters Filter query.
   *
   * @return array{items: array<int, array<string, mixed>>, total: int}
   */
  public function listPublic(array $filters): array {
    [$whereSql, $params] = $this->buildPublicFilters($filters);
    $sortBy = (string) ($filters['sort_by'] ?? 'published_at');
    $order = strtolower((string) ($filters['order'] ?? 'desc')) === 'asc'
      ? 'ASC'
      : 'DESC';
    $sortColumn = self::PUBLIC_SORTS[$sortBy] ?? self::PUBLIC_SORTS['published_at'];
    $limit = (int) $filters['per_page'];
    $offset = ((int) $filters['page'] - 1) * $limit;

    try {
      $countStmt = $this->db->query(
        "SELECT COUNT(*) AS total
          FROM {$this->table} w
          JOIN categories c ON c.id = w.category_id
          JOIN users u ON u.id = w.contributor_id
          {$whereSql}",
        $params
      );
      $countData = $countStmt->fetch(PDO::FETCH_ASSOC);
      $total = (int) ($countData['total'] ?? 0);

      $stmt = $this->db->query(
        $this->baseSelect()
          . " {$whereSql}
            ORDER BY {$sortColumn} {$order}
            LIMIT ? OFFSET ?",
        array_merge($params, [$limit, $offset])
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];

      return [
        'items' => $this->hydrateRows($rows),
        'total' => $total,
      ];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengambil wallpaper publik: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mendapatkan wallpaper milik contributor.
   *
   * @param int $contributorId ID contributor.
   * @param string|null $status Filter status.
   * @param int $page Halaman.
   * @param int $perPage Jumlah item per halaman.
   *
   * @return array{items: array<int, array<string, mixed>>, total: int}
   */
  public function listByContributor(
    int $contributorId,
    ?string $status,
    int $page,
    int $perPage
  ): array {
    $where = ['w.contributor_id = ?'];
    $params = [$contributorId];

    if ($status !== null) {
      $where[] = 'w.status = ?';
      $params[] = $status;
    }

    $whereSql = 'WHERE ' . implode(' AND ', $where);
    $offset = ($page - 1) * $perPage;

    try {
      $countStmt = $this->db->query(
        "SELECT COUNT(*) AS total
          FROM {$this->table} w
          {$whereSql}",
        $params
      );
      $countData = $countStmt->fetch(PDO::FETCH_ASSOC);

      $stmt = $this->db->query(
        $this->baseSelect()
          . " {$whereSql}
            ORDER BY w.created_at DESC
            LIMIT ? OFFSET ?",
        array_merge($params, [$perPage, $offset])
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];

      return [
        'items' => $this->hydrateRows($rows),
        'total' => (int) ($countData['total'] ?? 0),
      ];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengambil wallpaper contributor: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mendapatkan queue moderasi admin.
   *
   * @param array<string, mixed> $filters Filter query.
   *
   * @return array{items: array<int, array<string, mixed>>, total: int}
   */
  public function listForModeration(array $filters): array {
    $where = ['w.status = ?'];
    $params = [(string) ($filters['status'] ?? 'pending')];

    if (!empty($filters['contributor_id'])) {
      $where[] = 'w.contributor_id = ?';
      $params[] = (int) $filters['contributor_id'];
    }

    $whereSql = 'WHERE ' . implode(' AND ', $where);
    $sortBy = (string) ($filters['sort_by'] ?? 'created_at');
    $order = strtolower((string) ($filters['order'] ?? 'asc')) === 'desc'
      ? 'DESC'
      : 'ASC';
    $sortColumn = self::MODERATION_SORTS[$sortBy]
      ?? self::MODERATION_SORTS['created_at'];
    $limit = (int) $filters['per_page'];
    $offset = ((int) $filters['page'] - 1) * $limit;

    try {
      $countStmt = $this->db->query(
        "SELECT COUNT(*) AS total
          FROM {$this->table} w
          {$whereSql}",
        $params
      );
      $countData = $countStmt->fetch(PDO::FETCH_ASSOC);

      $stmt = $this->db->query(
        $this->baseSelect()
          . " {$whereSql}
            ORDER BY {$sortColumn} {$order}
            LIMIT ? OFFSET ?",
        array_merge($params, [$limit, $offset])
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];

      return [
        'items' => $this->hydrateRows($rows),
        'total' => (int) ($countData['total'] ?? 0),
      ];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengambil queue moderasi: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menyimpan entity wallpaper untuk kompatibilitas use case lama.
   *
   * @param Wallpaper $wallpaper Entity wallpaper.
   *
   * @return Wallpaper Entity tersimpan.
   */
  public function save(Wallpaper $wallpaper): Wallpaper {
    if ($wallpaper->getId() === 0) {
      $id = $this->create([
        'contributor_id' => $wallpaper->getContributorId(),
        'category_id' => $wallpaper->getCategoryId(),
        'title' => $wallpaper->getTitle(),
        'description' => $wallpaper->getDescription(),
        'file_size_kb' => $wallpaper->getFileSizeKb(),
        'mime_type' => $wallpaper->getMimeType(),
        'width' => $wallpaper->getWidth(),
        'height' => $wallpaper->getHeight(),
        'target_device' => $wallpaper->getTargetDevice(),
        'status' => $wallpaper->getStatus(),
        'published_at' => $wallpaper->getPublishedAt(),
      ]);

      return new Wallpaper(
        $id,
        $wallpaper->getContributorId(),
        $wallpaper->getCategoryId(),
        $wallpaper->getTitle(),
        $wallpaper->getFilePath(),
        $wallpaper->getFileName(),
        $wallpaper->getFileSizeKb(),
        $wallpaper->getMimeType(),
        $wallpaper->getWidth(),
        $wallpaper->getHeight(),
        $wallpaper->getStatus(),
        $wallpaper->getDescription(),
        null,
        $wallpaper->getPublishedAt(),
        $wallpaper->getCreatedAt(),
        $wallpaper->getUpdatedAt(),
        $wallpaper->getTargetDevice()
      );
    }

    $this->updateMetadata($wallpaper->getId(), [
      'title' => $wallpaper->getTitle(),
      'description' => $wallpaper->getDescription(),
    ]);
    $this->updateModerationState(
      $wallpaper->getId(),
      $wallpaper->getStatus(),
      $wallpaper->getPublishedAt()
    );

    return $wallpaper;
  }

  /**
   * Membuat wallpaper baru.
   *
   * @param array<string, mixed> $data Data wallpaper.
   *
   * @return int|string ID wallpaper baru.
   */
  public function create(array $data): int|string {
    $id = $data['id'] ?? null;
    try {
      if ($id !== null) {
        $this->db->query(
          "INSERT INTO {$this->table}
            (id, contributor_id, category_id, title, description,
              file_size_kb, mime_type, width, height,
              target_device, status, published_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
          [
            $id,
            $data['contributor_id'],
            $data['category_id'],
            $data['title'],
            $data['description'],
            $data['file_size_kb'],
            $data['mime_type'],
            $data['width'],
            $data['height'],
            $data['target_device'],
            $data['status'],
            $data['published_at'],
          ]
        );

        return $id;
      }

      $this->db->query(
        "INSERT INTO {$this->table}
          (contributor_id, category_id, title, description,
            file_size_kb, mime_type, width, height,
            target_device, status, published_at, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
        [
          $data['contributor_id'],
          $data['category_id'],
          $data['title'],
          $data['description'],
          $data['file_size_kb'],
          $data['mime_type'],
          $data['width'],
          $data['height'],
          $data['target_device'],
          $data['status'],
          $data['published_at'],
        ]
      );

      return (int) $this->db->getPdo()->lastInsertId();
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal membuat wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Memperbarui metadata wallpaper.
   *
   * @param int|string $id ID wallpaper.
   * @param array<string, mixed> $fields Field yang diubah.
   *
   * @return void
   */
  public function updateMetadata(int|string $id, array $fields): void {
    if ($fields === []) {
      return;
    }

    $sets = [];
    $params = [];

    foreach ($fields as $field => $value) {
      $sets[] = "{$field} = ?";
      $params[] = $value;
    }

    $sets[] = 'updated_at = NOW()';
    $params[] = $id;

    try {
      $this->db->query(
        "UPDATE {$this->table} SET " . implode(', ', $sets) . ' WHERE id = ?',
        $params
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memperbarui wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Memperbarui status dan publikasi wallpaper.
   *
   * @param int|string $id ID wallpaper.
   * @param string $status Status baru.
   * @param string|null $filePath Path baru jika file dipindahkan.
   * @param string|null $publishedAt Datetime publikasi.
   *
   * @return void
   */
  public function updateModerationState(
    int|string $id,
    string $status,
    ?string $publishedAt
  ): void {
    $sets = [
      'status = ?',
      'published_at = ?',
      'updated_at = NOW()',
    ];
    $params = [$status, $publishedAt];

    $params[] = $id;

    try {
      $this->db->query(
        "UPDATE {$this->table} SET " . implode(', ', $sets) . ' WHERE id = ?',
        $params
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memperbarui status wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menghapus wallpaper dari database.
   *
   * @param int|string $id ID wallpaper.
   *
   * @return void
   */
  public function delete(int|string $id): void {
    try {
      $this->db->query("DELETE FROM {$this->table} WHERE id = ?", [$id]);
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal menghapus wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari wallpaper dari path storage asli atau thumbnail.
   *
   * @param string $relativePath Path relatif dari storage.
   *
   * @return array<string, mixed>|null Detail wallpaper jika path dikenali.
   */
  public function findDetailedByStoragePath(string $relativePath): ?array {
    $normalizedPath = str_replace('\\', '/', $relativePath);

    try {
      $stmt = $this->db->query($this->baseSelect());
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
      $items = $this->hydrateRows($rows);

      foreach ($items as $item) {
        $filePath = str_replace('\\', '/', (string) $item['file_path']);
        $thumbnailPath = str_replace('\\', '/', (string) $item['thumbnail_path']);

        if ($normalizedPath === $filePath || $normalizedPath === $thumbnailPath) {
          return $item;
        }
      }

      return null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari wallpaper berdasarkan path: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari semua wallpaper dari contributor tertentu sebagai entity lama.
   *
   * @param int $contributorId ID contributor.
   *
   * @return array<int, Wallpaper>
   */
  public function findByContributorId(int $contributorId): array {
    $result = $this->listByContributor($contributorId, null, 1, 1000);

    return array_map(
      fn (array $row): Wallpaper => $this->mapToWallpaper($row),
      $result['items']
    );
  }

  /**
   * Mencari wallpaper berdasarkan status sebagai entity lama.
   *
   * @param string $status Status wallpaper.
   * @param int $limit Limit item.
   * @param int $offset Offset pagination.
   *
   * @return array<int, Wallpaper>
   */
  public function findByStatus(
    string $status,
    int $limit = 10,
    int $offset = 0
  ): array {
    $page = (int) floor($offset / max(1, $limit)) + 1;
    $result = $this->listForModeration([
      'status' => $status,
      'page' => $page,
      'per_page' => $limit,
      'sort_by' => 'created_at',
      'order' => 'desc',
    ]);

    return array_map(
      fn (array $row): Wallpaper => $this->mapToWallpaper($row),
      $result['items']
    );
  }

  /**
   * Mencari wallpaper berdasarkan kategori dan status sebagai entity lama.
   *
   * @param int $categoryId ID kategori.
   * @param string $status Status wallpaper.
   * @param int $limit Limit item.
   * @param int $offset Offset pagination.
   *
   * @return array<int, Wallpaper>
   */
  public function findByCategoryAndStatus(
    int $categoryId,
    string $status,
    int $limit = 10,
    int $offset = 0
  ): array {
    try {
      $stmt = $this->db->query(
        "SELECT * FROM {$this->table}
          WHERE category_id = ? AND status = ?
          ORDER BY created_at DESC
          LIMIT ? OFFSET ?",
        [$categoryId, $status, $limit, $offset]
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];

      return array_map([$this, 'mapToWallpaper'], $rows);
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari wallpaper kategori: ' . $e->getMessage()
      );
    }
  }

  /**
   * Query dasar untuk detail wallpaper.
   *
   * @return string SQL SELECT dasar.
   */
  private function baseSelect(): string {
    return "SELECT
        w.*,
        c.name AS category_name,
        c.slug AS category_slug,
        u.display_name AS contributor_display_name,
        u.email AS contributor_email
      FROM {$this->table} w
      JOIN categories c ON c.id = w.category_id
      JOIN users u ON u.id = w.contributor_id";
  }

  /**
   * Membuat filter SQL untuk daftar publik.
   *
   * @param array<string, mixed> $filters Filter request.
   *
   * @return array{0: string, 1: array<int, mixed>}
   */
  private function buildPublicFilters(array $filters): array {
    $where = ['w.status = ?', 'w.published_at IS NOT NULL'];
    $params = ['approved'];

    if (!empty($filters['q'])) {
      $like = '%' . (string) $filters['q'] . '%';
      $where[] = '(w.title LIKE ?
        OR w.description LIKE ?
        OR EXISTS (
          SELECT 1
          FROM wallpaper_tags wts
          JOIN tags ts ON ts.id = wts.tag_id
          WHERE wts.wallpaper_id = w.id
            AND (ts.name LIKE ? OR ts.slug LIKE ?)
        ))';
      array_push($params, $like, $like, $like, $like);
    }

    if (!empty($filters['category'])) {
      $where[] = 'c.slug = ?';
      $params[] = (string) $filters['category'];
    }

    foreach ($filters['tags'] ?? [] as $tag) {
      $where[] = 'EXISTS (
        SELECT 1
        FROM wallpaper_tags wt_filter
        JOIN tags t_filter ON t_filter.id = wt_filter.tag_id
        WHERE wt_filter.wallpaper_id = w.id
          AND t_filter.slug = ?
      )';
      $params[] = (string) $tag;
    }

    if (!empty($filters['target_device'])) {
      $where[] = 'w.target_device = ?';
      $params[] = (string) $filters['target_device'];
    }

    return ['WHERE ' . implode(' AND ', $where), $params];
  }

  /**
   * Melengkapi row wallpaper dengan category, contributor, tag, dan review.
   *
   * @param array<int, array<string, mixed>> $rows Row database.
   *
   * @return array<int, array<string, mixed>>
   */
  private function hydrateRows(array $rows): array {
    if ($rows === []) {
      return [];
    }

    $ids = array_map(
      fn (array $row): int|string => $row['id'],
      $rows
    );
    $tagsByWallpaper = $this->loadTags($ids);
    $reviewsByWallpaper = $this->loadLatestReviews($ids);

    foreach ($rows as &$row) {
      $id = (string) $row['id'];
      $row = $this->normalizeRow($row);
      $row['tags'] = $tagsByWallpaper[$id] ?? [];
      $row['moderation'] = $reviewsByWallpaper[$id] ?? null;
    }

    return $rows;
  }

  /**
   * Memuat tag untuk banyak wallpaper.
   *
   * @param array<int, int|string> $wallpaperIds Daftar ID wallpaper.
   *
   * @return array<string, array<int, array<string, mixed>>>
   */
  private function loadTags(array $wallpaperIds): array {
    $placeholders = implode(',', array_fill(0, count($wallpaperIds), '?'));

    try {
      $stmt = $this->db->query(
        "SELECT wt.wallpaper_id, t.id, t.name, t.slug
          FROM wallpaper_tags wt
          JOIN tags t ON t.id = wt.tag_id
          WHERE wt.wallpaper_id IN ({$placeholders})
          ORDER BY t.name ASC",
        $wallpaperIds
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memuat tag wallpaper: ' . $e->getMessage()
      );
    }

    $grouped = [];
    foreach ($rows as $row) {
      $wallpaperId = (string) $row['wallpaper_id'];
      $grouped[$wallpaperId][] = [
        'id' => (int) $row['id'],
        'name' => (string) $row['name'],
        'slug' => (string) $row['slug'],
      ];
    }

    return $grouped;
  }

  /**
   * Memuat review moderasi terbaru untuk banyak wallpaper.
   *
   * @param array<int, int|string> $wallpaperIds Daftar ID wallpaper.
   *
   * @return array<string, array<string, mixed>>
   */
  private function loadLatestReviews(array $wallpaperIds): array {
    $placeholders = implode(',', array_fill(0, count($wallpaperIds), '?'));

    try {
      $stmt = $this->db->query(
        "SELECT mr.*
          FROM moderation_reviews mr
          JOIN (
            SELECT wallpaper_id, MAX(reviewed_at) AS reviewed_at
            FROM moderation_reviews
            WHERE wallpaper_id IN ({$placeholders})
            GROUP BY wallpaper_id
          ) latest ON latest.wallpaper_id = mr.wallpaper_id
            AND latest.reviewed_at = mr.reviewed_at",
        $wallpaperIds
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memuat review moderasi: ' . $e->getMessage()
      );
    }

    $grouped = [];
    foreach ($rows as $row) {
      $grouped[(string) $row['wallpaper_id']] = [
        'decision' => (string) $row['decision'],
        'reason' => $row['reason'] !== null ? (string) $row['reason'] : null,
        'reviewed_at' => (string) $row['reviewed_at'],
        'admin_id' => (int) $row['admin_id'],
      ];
    }

    return $grouped;
  }

  /**
   * Menormalisasi tipe data row wallpaper.
   *
   * @param array<string, mixed> $row Row database.
   *
   * @return array<string, mixed>
   */
  private function normalizeRow(array $row): array {
    $row['id'] = (string) $row['id'];
    $row['contributor_id'] = (int) $row['contributor_id'];
    $row['category_id'] = (int) $row['category_id'];
    $row['file_size_kb'] = (int) $row['file_size_kb'];
    $row['width'] = (int) $row['width'];
    $row['height'] = (int) $row['height'];
    $row['file_name'] = $this->buildFileName(
      (string) $row['id'],
      (string) $row['mime_type']
    );
    $row['file_path'] = $this->buildFilePath(
      (string) $row['id'],
      (int) $row['category_id'],
      (string) $row['status'],
      (string) $row['mime_type']
    );
    $row['thumbnail_path'] = $this->buildThumbnailPath(
      (string) $row['id'],
      (int) $row['category_id'],
      (string) $row['status']
    );
    $row['category'] = [
      'id' => (int) $row['category_id'],
      'name' => (string) $row['category_name'],
      'slug' => (string) $row['category_slug'],
    ];
    $row['contributor'] = [
      'id' => (int) $row['contributor_id'],
      'display_name' => (string) $row['contributor_display_name'],
      'email' => (string) $row['contributor_email'],
    ];

    unset($row['category_name'], $row['category_slug'], $row['contributor_display_name'], $row['contributor_email']);

    return $row;
  }

  /**
   * Membentuk nama file asli dari UUID dan MIME type.
   *
   * @param string $id UUID wallpaper.
   * @param string $mimeType MIME type wallpaper.
   *
   * @return string Nama file.
   */
  private function buildFileName(string $id, string $mimeType): string {
    $extension = match ($mimeType) {
      'image/jpeg' => 'jpeg',
      'image/png' => 'png',
      'image/webp' => 'webp',
      default => 'bin',
    };

    return $id . '.' . $extension;
  }

  /**
   * Membentuk path file asli dari UUID, kategori, dan status.
   *
   * @param string $id UUID wallpaper.
   * @param int $categoryId ID kategori.
   * @param string $status Status wallpaper.
   * @param string $mimeType MIME type wallpaper.
   *
   * @return string Path relatif storage.
   */
  private function buildFilePath(
    string $id,
    int $categoryId,
    string $status,
    string $mimeType
  ): string {
    $parts = ['wallpapers'];

    if ($status !== 'approved') {
      $parts[] = 'pending';
    }

    $parts[] = (string) $categoryId;
    $parts[] = $this->buildFileName($id, $mimeType);

    return implode(DIRECTORY_SEPARATOR, $parts);
  }

  /**
   * Membentuk path thumbnail dari UUID wallpaper dan kategori.
   *
   * @param string $id UUID wallpaper.
   * @param int $categoryId ID kategori.
   * @param string $status Status wallpaper.
   *
   * @return string Path thumbnail relatif storage.
   */
  private function buildThumbnailPath(
    string $id,
    int $categoryId,
    string $status
  ): string {
    $parts = ['wallpapers'];

    if ($status !== 'approved') {
      $parts[] = 'pending';
    }

    $parts[] = (string) $categoryId;
    $parts[] = 'thumbnails';
    $parts[] = $id . '.webp';

    return implode(DIRECTORY_SEPARATOR, $parts);
  }

  /**
   * Mengubah row database menjadi entity Wallpaper lama.
   *
   * @param array<string, mixed> $data Data wallpaper.
   *
   * @return Wallpaper Entity wallpaper.
   */
  private function mapToWallpaper(array $data): Wallpaper {
    $normalized = isset($data['file_path'], $data['file_name'])
      ? $data
      : $this->normalizeRow($data);

    return new Wallpaper(
      (int) $normalized['id'],
      (int) $normalized['contributor_id'],
      (int) $normalized['category_id'],
      (string) $normalized['title'],
      (string) $normalized['file_path'],
      (string) $normalized['file_name'],
      (int) $normalized['file_size_kb'],
      (string) $normalized['mime_type'],
      (int) $normalized['width'],
      (int) $normalized['height'],
      (string) $normalized['status'],
      $normalized['description'] !== null ? (string) $normalized['description'] : null,
      null,
      $normalized['published_at'] !== null ? (string) $normalized['published_at'] : null,
      (string) $normalized['created_at'],
      (string) $normalized['updated_at'],
      (string) ($normalized['target_device'] ?? 'desktop')
    );
  }
}
