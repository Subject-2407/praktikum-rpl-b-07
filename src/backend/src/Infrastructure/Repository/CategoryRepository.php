<?php

/**
 * Category Repository
 *
 * Menangani akses data kategori dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel categories.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\Category;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas CategoryRepository - Repository untuk akses data kategori.
 *
 * @class CategoryRepository
 * @extends BaseRepository
 */
class CategoryRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'categories';

  /**
   * Mencari kategori berdasarkan ID.
   *
   * @param int $id ID kategori.
   *
   * @return Category|null Category jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findById(int $id): ?Category {
    try {
      $query = "SELECT * FROM {$this->table} WHERE id = ? LIMIT 1";
      $result = $this->db->query($query, [$id]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToCategory($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari kategori: ' . $e->getMessage());
    }
  }

  /**
   * Mencari kategori berdasarkan slug.
   *
   * @param string $slug Slug kategori.
   *
   * @return Category|null Category jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findBySlug(string $slug): ?Category {
    try {
      $query = "SELECT * FROM {$this->table} WHERE slug = ? LIMIT 1";
      $result = $this->db->query($query, [$slug]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToCategory($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari kategori: ' . $e->getMessage());
    }
  }

  /**
   * Mendapatkan semua kategori.
   *
   * @return array Array dari Category.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findAll(): array {
    try {
      $query = "SELECT * FROM {$this->table} ORDER BY name ASC";
      $result = $this->db->query($query);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToCategory'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mendapatkan kategori: ' . $e->getMessage());
    }
  }

  /**
   * Mengkonversi data dari database menjadi Category entity.
   *
   * @param array $data Data dari database.
   *
   * @return Category Category entity.
   */
  private function mapToCategory(array $data): Category {
    return new Category(
      (int) $data['id'],
      $data['name'],
      $data['slug'],
      $data['created_at']
    );
  }
}
