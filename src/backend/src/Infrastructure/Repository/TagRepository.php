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
  public function findAllAsArray(?string $keyword = null): array {
    try {
      $params = [];
      $where = '';

      if ($keyword !== null && trim($keyword) !== '') {
        $where = ' WHERE name LIKE ? OR slug LIKE ?';
        $like = '%' . trim($keyword) . '%';
        $params = [$like, $like];
      }

      $stmt = $this->db->query(
        "SELECT id, name, slug FROM {$this->table}{$where} ORDER BY name ASC",
        $params
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
      $query = "INSERT INTO wallpaper_tags (wallpaper_id, tag_id) VALUES (?, ?)";
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
          'INSERT INTO wallpaper_tags (wallpaper_id, tag_id) VALUES (?, ?)',
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
