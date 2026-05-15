<?php

/**
 * Wallpaper Repository
 *
 * Menangani akses data wallpaper dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel wallpapers.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas WallpaperRepository - Repository untuk akses data wallpaper.
 *
 * @class WallpaperRepository
 * @extends BaseRepository
 */
class WallpaperRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'wallpapers';

  /**
   * Mencari wallpaper berdasarkan ID dan return sebagai entity.
   *
   * @param int $id ID wallpaper.
   *
   * @return Wallpaper|null Wallpaper jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByIdEntity(int $id): ?Wallpaper {
    $data = parent::findById($id);

    if ($data === null) {
      return null;
    }

    return $this->mapToWallpaper($data);
  }

  /**
   * Mencari semua wallpaper dari contributor tertentu.
   *
   * @param int $contributorId ID contributor.
   *
   * @return array Array dari Wallpaper.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByContributorId(int $contributorId): array {
    try {
      $query = "SELECT * FROM {$this->table} WHERE contributor_id = ? ORDER BY created_at DESC";
      $result = $this->db->query($query, [$contributorId]);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToWallpaper'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mencari wallpaper berdasarkan status dengan pagination.
   *
   * @param string $status Status wallpaper (pending, approved, rejected, scheduled).
   * @param int $limit Limit per page.
   * @param int $offset Offset pagination.
   *
   * @return array Array dari Wallpaper.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByStatus(string $status, int $limit = 10, int $offset = 0): array {
    try {
      $query = "SELECT * FROM {$this->table} 
                WHERE status = ? 
                ORDER BY created_at DESC 
                LIMIT ? OFFSET ?";
      $result = $this->db->query($query, [$status, $limit, $offset]);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToWallpaper'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Menyimpan wallpaper baru ke database.
   *
   * @param Wallpaper $wallpaper Wallpaper yang akan disimpan.
   *
   * @return Wallpaper Wallpaper dengan ID yang sudah di-assign.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function save(Wallpaper $wallpaper): Wallpaper {
    try {
      if ($wallpaper->getId() === 0) {
        // Insert wallpaper baru
        $query = "INSERT INTO {$this->table} 
                  (contributor_id, category_id, title, description, file_path, file_name, 
                   file_size_kb, mime_type, width, height, status, scheduled_at, 
                   published_at, created_at, updated_at) 
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        $this->db->query($query, [
          $wallpaper->getContributorId(),
          $wallpaper->getCategoryId(),
          $wallpaper->getTitle(),
          $wallpaper->getDescription(),
          $wallpaper->getFilePath(),
          $wallpaper->getFileName(),
          $wallpaper->getFileSizeKb(),
          $wallpaper->getMimeType(),
          $wallpaper->getWidth(),
          $wallpaper->getHeight(),
          $wallpaper->getStatus(),
          $wallpaper->getScheduledAt(),
          $wallpaper->getPublishedAt(),
          $wallpaper->getCreatedAt(),
          $wallpaper->getUpdatedAt(),
        ]);

        // Ambil ID yang baru di-generate
        $lastId = (int) $this->db->getPdo()->lastInsertId();

        return new Wallpaper(
          $lastId,
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
          $wallpaper->getScheduledAt(),
          $wallpaper->getPublishedAt(),
          $wallpaper->getCreatedAt(),
          $wallpaper->getUpdatedAt()
        );
      } else {
        // Update wallpaper yang sudah ada
        $query = "UPDATE {$this->table} 
                  SET title = ?, description = ?, status = ?, scheduled_at = ?, 
                      published_at = ?, updated_at = ? 
                  WHERE id = ?";
        $this->db->query($query, [
          $wallpaper->getTitle(),
          $wallpaper->getDescription(),
          $wallpaper->getStatus(),
          $wallpaper->getScheduledAt(),
          $wallpaper->getPublishedAt(),
          $wallpaper->getUpdatedAt(),
          $wallpaper->getId(),
        ]);

        return $wallpaper;
      }
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menyimpan wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Menghapus wallpaper dari database.
   *
   * @param int $id ID wallpaper yang akan dihapus.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function delete(int $id): void {
    try {
      $query = "DELETE FROM {$this->table} WHERE id = ?";
      $this->db->query($query, [$id]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menghapus wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mencari wallpaper berdasarkan kategori dan status dengan pagination.
   *
   * @param int $categoryId ID kategori.
   * @param string $status Status wallpaper.
   * @param int $limit Limit per page.
   * @param int $offset Offset pagination.
   *
   * @return array Array dari Wallpaper.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByCategoryAndStatus(int $categoryId, string $status, int $limit = 10, int $offset = 0): array {
    try {
      $query = "SELECT * FROM {$this->table} 
                WHERE category_id = ? AND status = ? 
                ORDER BY created_at DESC 
                LIMIT ? OFFSET ?";
      $result = $this->db->query($query, [$categoryId, $status, $limit, $offset]);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToWallpaper'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari wallpaper: ' . $e->getMessage());
    }
  }

  /**
   * Mengkonversi data dari database menjadi Wallpaper entity.
   *
   * @param array $data Data dari database.
   *
   * @return Wallpaper Wallpaper entity.
   */
  private function mapToWallpaper(array $data): Wallpaper {
    return new Wallpaper(
      (int) $data['id'],
      (int) $data['contributor_id'],
      (int) $data['category_id'],
      $data['title'],
      $data['file_path'],
      $data['file_name'],
      (int) $data['file_size_kb'],
      $data['mime_type'],
      (int) $data['width'],
      (int) $data['height'],
      $data['status'],
      $data['description'],
      $data['scheduled_at'],
      $data['published_at'],
      $data['created_at'],
      $data['updated_at']
    );
  }
}
