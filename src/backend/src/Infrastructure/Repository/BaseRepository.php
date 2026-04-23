<?php

/**
 * Base Repository Class
 *
 * Kelas abstrak yang menjadi base untuk semua repository. Repository
 * bertanggung jawab untuk mengakses dan memanipulasi data dari database.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas BaseRepository - Base class untuk semua repository.
 *
 * Kelas ini menyediakan metode-metode umum yang digunakan oleh semua
 * repository untuk mengakses database.
 *
 * @abstract
 * @class BaseRepository
 */
abstract class BaseRepository {

  /**
   * Instance DatabaseConnection.
   *
   * @var DatabaseConnection
   */
  protected DatabaseConnection $db;

  /**
   * Nama tabel yang digunakan repository.
   *
   * @var string
   */
  protected string $table = '';

  /**
   * Konstruktor BaseRepository.
   *
   * @param DatabaseConnection|null $db Instance database yang diinjeksikan.
   *                                    Jika null, akan menggunakan singleton.
   */
  public function __construct(?DatabaseConnection $db = null) {
    $this->db = $db ?? DatabaseConnection::getInstance();
  }

  /**
   * Mencari record berdasarkan ID.
   *
   * @param int $id ID record yang dicari.
   *
   * @return array|null Array data jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findById(int $id): ?array {
    if (empty($this->table)) {
      throw new DatabaseException('Nama tabel tidak didefinisikan di repository.');
    }

    try {
      $query = "SELECT * FROM {$this->table} WHERE id = ?";
      $result = $this->db->query($query, [$id]);

      return $result->fetch(PDO::FETCH_ASSOC) ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari record: ' . $e->getMessage());
    }
  }

  /**
   * Mendapatkan semua record dari tabel.
   *
   * @return array Array berisi semua record dari tabel.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findAll(): array {
    if (empty($this->table)) {
      throw new DatabaseException('Nama tabel tidak didefinisikan di repository.');
    }

    try {
      $query = "SELECT * FROM {$this->table}";
      $result = $this->db->query($query);

      return $result->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mendapatkan semua record: ' . $e->getMessage());
    }
  }

  /**
   * Menghitung total record di tabel.
   *
   * @return int Total record di tabel.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function count(): int {
    if (empty($this->table)) {
      throw new DatabaseException('Nama tabel tidak didefinisikan di repository.');
    }

    try {
      $query = "SELECT COUNT(*) as total FROM {$this->table}";
      $result = $this->db->query($query);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      return (int) ($data['total'] ?? 0);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menghitung record: ' . $e->getMessage());
    }
  }
}
