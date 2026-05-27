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
 */
class CategoryRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'categories';

  /**
   * Mencari kategori berdasarkan ID dan return sebagai entity.
   *
   * @param int $id ID kategori.
   *
   * @return Category|null Kategori jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByIdEntity(int $id): ?Category {
    $data = parent::findById($id);

    if ($data === null) {
      return null;
    }

    return $this->mapToCategory($data);
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
   * @return array<int, Category> Array dari Category.
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
   * @param array<string, mixed> $data Data dari database.
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

  /**
   * Mendapatkan semua kategori sebagai array response.
   *
   * @return array<int, array<string, mixed>>
   */
  public function findAllAsArray(): array {
    try {
      $stmt = $this->db->query(
        "SELECT id, name, slug FROM {$this->table} ORDER BY name ASC"
      );

      return $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mendapatkan kategori: ' . $e->getMessage()
      );
    }
  }
}
