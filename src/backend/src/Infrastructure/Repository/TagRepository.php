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
 * @extends BaseRepository
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
   * @return array Array dari Tag.
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
   * Menambahkan relasi tag ke wallpaper.
   *
   * @param int $wallpaperId ID wallpaper.
   * @param int $tagId ID tag.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function addTagToWallpaper(int $wallpaperId, int $tagId): void {
    try {
      $query = "INSERT INTO wallpaper_tags (wallpaper_id, tag_id) VALUES (?, ?)";
      $this->db->query($query, [$wallpaperId, $tagId]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menambahkan tag ke wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mendapatkan semua tag untuk wallpaper tertentu.
   *
   * @param int $wallpaperId ID wallpaper.
   *
   * @return array Array dari Tag.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByWallpaperId(int $wallpaperId): array {
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
   * @param array $data Data dari database.
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
