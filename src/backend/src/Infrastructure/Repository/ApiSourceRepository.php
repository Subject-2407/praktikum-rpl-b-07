<?php

/**
 * Repository sumber API wallpaper.
 *
 * Repository ini membaca tabel api_sources untuk metadata provider eksternal
 * dan sumber internal Scapes.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas ApiSourceRepository - Repository untuk tabel api_sources.
 */
class ApiSourceRepository extends BaseRepository {

  /**
   * Nama tabel sumber API.
   *
   * @var string
   */
  protected string $table = 'api_sources';

  /**
   * Mendapatkan semua sumber API aktif.
   *
   * @return array<int, array<string, mixed>>
   */
  public function findActive(): array {
    try {
      $stmt = $this->db->query(
        "SELECT id, name, slug, base_url, is_default
          FROM {$this->table}
          WHERE is_active = 1
          ORDER BY id ASC"
      );

      $rows = $stmt->fetchAll(PDO::FETCH_ASSOC) ?: [];
      foreach ($rows as &$row) {
        $row['id'] = (int) $row['id'];
        $row['is_default'] = (bool) $row['is_default'];
      }

      return $rows;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mendapatkan sumber API: ' . $e->getMessage()
      );
    }
  }
}
