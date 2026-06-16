<?php

/**
 * Tag Repository
 *
 * Menangani akses data tag dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel tags.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\Tag;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas TagRepository - Repository untuk akses data tag.
 *
 * @class TagRepository
 */
class TagRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'tags';

  /**
   * Mencari tag berdasarkan ID dan return sebagai entity.
   *
   * @param int $id ID tag.
   *
   * @return Tag|null Tag jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByIdEntity(int $id): ?Tag {
    $data = parent::findById($id);

    if ($data === null) {
      return null;
    }

    return $this->mapToTag($data);
  }

  /**
   * Mencari tag berdasarkan slug.
   *
   * @param string $slug Slug tag.
   *
   * @return Tag|null Tag jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findBySlug(string $slug): ?Tag {
    try {
      $query = "SELECT * FROM {$this->table} WHERE slug = ? LIMIT 1";
      $result = $this->db->query($query, [$slug]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToTag($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari tag: ' . $e->getMessage());
    }
  }

  /**
   * Mendapatkan semua tag.
   *
   * @return array<int, Tag> Array dari Tag.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findAll(): array {
    try {
      $query = "SELECT * FROM {$this->table} ORDER BY name ASC";
      $result = $this->db->query($query);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToTag'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mendapatkan tag: ' . $e->getMessage());
    }
  }

  /**
   * Mendapatkan semua tag sebagai array response.
   *
   * @param string|null $keyword Keyword pencarian tag.
   *
   * @return array<int, array<string, mixed>>
   */
  public function findAllAsArray(
    ?string $keyword = null,
    string $match = 'contains',
    int $limit = 100
  ): array {
    try {
      $params = [];
      $where = '';
      $limit = max(1, min(100, $limit));

      if ($keyword !== null && trim($keyword) !== '') {
        $keyword = $this->normalizeTagSlug($keyword);
        $where = ' WHERE name LIKE ? OR slug LIKE ?';
        $like = $match === 'prefix'
          ? $keyword . '%'
          : '%' . $keyword . '%';
        $params = [$like, $like];
      }

      $stmt = $this->db->query(
        "SELECT id, name, slug FROM {$this->table}{$where}
          ORDER BY name ASC
          LIMIT ?",
        array_merge($params, [$limit])
      );

      return $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mendapatkan tag: ' . $e->getMessage());
    }
  }

  /**
   * Mengecek seluruh ID tag valid.
   *
   * @param array<int, int> $tagIds Daftar ID tag.
   *
   * @return bool True jika semua tag ditemukan.
   */
  public function allIdsExist(array $tagIds): bool {
    $uniqueIds = array_values(array_unique(array_filter($tagIds)));
    if ($uniqueIds === []) {
      return true;
    }

    $placeholders = implode(',', array_fill(0, count($uniqueIds), '?'));

    try {
      $stmt = $this->db->query(
        "SELECT COUNT(*) AS total FROM {$this->table}
          WHERE id IN ({$placeholders})",
        $uniqueIds
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return (int) ($data['total'] ?? 0) === count($uniqueIds);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mengecek tag: ' . $e->getMessage());
    }
  }

  /**
   * Menambahkan relasi tag ke wallpaper.
   *
   * @param int|string $wallpaperId ID wallpaper.
   * @param int $tagId ID tag.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function addTagToWallpaper(int|string $wallpaperId, int $tagId): void {
    try {
      $query = "INSERT IGNORE INTO wallpaper_tags (wallpaper_id, tag_id)
        VALUES (?, ?)";
      $this->db->query($query, [$wallpaperId, $tagId]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menambahkan tag ke wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mengganti semua tag pada wallpaper.
   *
   * @param int|string $wallpaperId ID wallpaper.
   * @param array<int, int> $tagIds Daftar ID tag baru.
   *
   * @return void
   */
  public function replaceWallpaperTags(int|string $wallpaperId, array $tagIds): void {
    try {
      $this->db->query(
        'DELETE FROM wallpaper_tags WHERE wallpaper_id = ?',
        [$wallpaperId]
      );

      foreach (array_values(array_unique($tagIds)) as $tagId) {
        $this->db->query(
          'INSERT IGNORE INTO wallpaper_tags (wallpaper_id, tag_id) VALUES (?, ?)',
          [$wallpaperId, (int) $tagId]
        );
      }
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengganti tag wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengganti proposal tag pending untuk wallpaper.
   *
   * @param int|string $wallpaperId ID wallpaper.
   * @param array<int, string> $tagTexts Daftar tag mentah atau slug.
   *
   * @return void
   */
  public function replaceWallpaperTagProposals(
    int|string $wallpaperId,
    array $tagTexts
  ): void {
    try {
      $this->db->query(
        "DELETE FROM wallpaper_tag_proposals
          WHERE wallpaper_id = ? AND status = 'pending'",
        [$wallpaperId]
      );

      foreach ($this->normalizeTagTexts($tagTexts) as $tagText) {
        $slug = $this->normalizeTagSlug($tagText);
        $existing = $this->findBySlug($slug);
        $this->db->query(
          "INSERT INTO wallpaper_tag_proposals
            (wallpaper_id, tag_text, tag_slug, existing_tag_id, status,
              created_at)
            VALUES (?, ?, ?, ?, 'pending', NOW())
            ON DUPLICATE KEY UPDATE
              tag_text = VALUES(tag_text),
              existing_tag_id = VALUES(existing_tag_id),
              status = 'pending',
              resolved_at = NULL",
          [
            $wallpaperId,
            $tagText,
            $slug,
            $existing?->getId(),
          ]
        );
      }
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mengganti proposal tag wallpaper: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menyetujui proposal tag pending dan membuat relasi final.
   *
   * @param int|string $wallpaperId ID wallpaper.
   *
   * @return void
   */
  public function resolvePendingProposals(int|string $wallpaperId): void {
    try {
      $stmt = $this->db->query(
        "SELECT id, tag_text, tag_slug, existing_tag_id
          FROM wallpaper_tag_proposals
          WHERE wallpaper_id = ? AND status = 'pending'
          ORDER BY id ASC",
        [$wallpaperId]
      );
      $proposals = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];

      foreach ($proposals as $proposal) {
        $tagId = $proposal['existing_tag_id'] !== null
          ? (int) $proposal['existing_tag_id']
          : $this->upsertBySlug(
            (string) $proposal['tag_text'],
            (string) $proposal['tag_slug']
          );

        $this->addTagToWallpaper($wallpaperId, $tagId);
        $this->db->query(
          "UPDATE wallpaper_tag_proposals
            SET existing_tag_id = ?, status = 'approved', resolved_at = NOW()
            WHERE id = ?",
          [$tagId, (int) $proposal['id']]
        );
      }
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal menyetujui proposal tag: ' . $e->getMessage()
      );
    }
  }

  /**
   * Membuang proposal tag pending ketika wallpaper ditolak.
   *
   * @param int|string $wallpaperId ID wallpaper.
   *
   * @return void
   */
  public function discardPendingProposals(int|string $wallpaperId): void {
    try {
      $this->db->query(
        "UPDATE wallpaper_tag_proposals
          SET status = 'discarded', resolved_at = NOW()
          WHERE wallpaper_id = ? AND status = 'pending'",
        [$wallpaperId]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal membuang proposal tag: ' . $e->getMessage()
      );
    }
  }

  /**
   * Memuat proposal tag untuk beberapa wallpaper.
   *
   * @param array<int, int|string> $wallpaperIds Daftar ID wallpaper.
   *
   * @return array<string, array<int, array<string, mixed>>>
   */
  public function findProposalsByWallpaperIds(array $wallpaperIds): array {
    if ($wallpaperIds === []) {
      return [];
    }

    $placeholders = implode(',', array_fill(0, count($wallpaperIds), '?'));

    try {
      $stmt = $this->db->query(
        "SELECT id, wallpaper_id, tag_text, tag_slug, existing_tag_id, status
          FROM wallpaper_tag_proposals
          WHERE wallpaper_id IN ({$placeholders})
          ORDER BY tag_text ASC",
        $wallpaperIds
      );
      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memuat proposal tag wallpaper: ' . $e->getMessage()
      );
    }

    $grouped = [];
    foreach ($rows as $row) {
      $wallpaperId = (string) $row['wallpaper_id'];
      $grouped[$wallpaperId][] = [
        'id' => (int) $row['id'],
        'tag_text' => (string) $row['tag_text'],
        'tag_slug' => (string) $row['tag_slug'],
        'existing_tag_id' => $row['existing_tag_id'] !== null
          ? (int) $row['existing_tag_id']
          : null,
        'status' => (string) $row['status'],
      ];
    }

    return $grouped;
  }

  /**
   * Membuat tag baru jika slug belum tersedia.
   *
   * @param string $tagText Nama tag.
   * @param string $slug Slug tag.
   *
   * @return int ID tag.
   */
  public function upsertBySlug(string $tagText, string $slug): int {
    $slug = $this->normalizeTagSlug($slug);
    $name = $this->normalizeTagName($tagText);

    $existing = $this->findBySlug($slug);
    if ($existing !== null) {
      return $existing->getId();
    }

    try {
      $this->db->query(
        "INSERT IGNORE INTO {$this->table} (name, slug, created_at)
          VALUES (?, ?, NOW())",
        [$name, $slug]
      );

      $tag = $this->findBySlug($slug);
      if ($tag === null) {
        throw new DatabaseException('Tag gagal dibuat.');
      }

      return $tag->getId();
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat tag: ' . $e->getMessage());
    }
  }

  /**
   * Normalisasi slug tag.
   *
   * @param string $tagText Tag mentah.
   *
   * @return string Slug tag.
   */
  public function normalizeTagSlug(string $tagText): string {
    $tagText = strtolower(trim($tagText));
    $tagText = ltrim($tagText, '#');
    $tagText = preg_replace('/[^a-z0-9\s-]+/', '', $tagText) ?? '';
    $tagText = preg_replace('/[\s-]+/', '-', $tagText) ?? '';

    return trim($tagText, '-');
  }

  /**
   * Normalisasi nama tag untuk disimpan.
   *
   * @param string $tagText Tag mentah.
   *
   * @return string Nama tag.
   */
  private function normalizeTagName(string $tagText): string {
    $slug = $this->normalizeTagSlug($tagText);

    return $slug;
  }

  /**
   * Normalisasi dan deduplikasi daftar tag.
   *
   * @param array<int, string> $tagTexts Daftar tag.
   *
   * @return array<int, string>
   */
  private function normalizeTagTexts(array $tagTexts): array {
    $normalized = [];
    foreach ($tagTexts as $tagText) {
      $slug = $this->normalizeTagSlug((string) $tagText);
      if ($slug !== '') {
        $normalized[$slug] = $slug;
      }
    }

    return array_values($normalized);
  }

  /**
   * Mendapatkan semua tag untuk wallpaper tertentu.
   *
   * @param int|string $wallpaperId ID wallpaper.
   *
   * @return array<int, Tag> Array dari Tag.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByWallpaperId(int|string $wallpaperId): array {
    try {
      $query = "SELECT t.* FROM {$this->table} t 
                JOIN wallpaper_tags wt ON t.id = wt.tag_id 
                WHERE wt.wallpaper_id = ? 
                ORDER BY t.name ASC";
      $result = $this->db->query($query, [$wallpaperId]);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToTag'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mendapatkan tag wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mengkonversi data dari database menjadi Tag entity.
   *
   * @param array<string, mixed> $data Data dari database.
   *
   * @return Tag Tag entity.
   */
  private function mapToTag(array $data): Tag {
    return new Tag(
      (int) $data['id'],
      $data['name'],
      $data['slug'],
      $data['created_at']
    );
  }
}
