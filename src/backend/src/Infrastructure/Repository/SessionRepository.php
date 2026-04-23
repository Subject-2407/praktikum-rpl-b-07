<?php

/**
 * Repository untuk tabel sessions
 *
 * Menyimpan dan mencabut sesi (token) pengguna.
 *
 * @package Scapes\Infrastructure\Repository
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use Scapes\Core\Exceptions\DatabaseException;

class SessionRepository extends BaseRepository {

  protected string $table = 'sessions';

  /**
   * Membuat sesi baru dengan token JWT.
   *
   * @param int $userId ID user
   * @param string $token JWT token
   * @param string|null $ipAddress IP address (opsional)
   * @param string $expiresAt Datetime expiry (Y-m-d H:i:s)
   * @return int ID session baru
   * @throws DatabaseException
   */
  public function create(int $userId, string $token, ?string $ipAddress, string $expiresAt): int {
    try {
      $query = "INSERT INTO {$this->table} (user_id, token, ip_address, expires_at, created_at) VALUES (?, ?, ?, ?, NOW())";
      $this->db->query($query, [$userId, $token, $ipAddress, $expiresAt]);

      $pdo = $this->db->getPdo();
      return (int) $pdo->lastInsertId();
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat session: ' . $e->getMessage());
    }
  }

  /**
   * Mencabut session berdasarkan token (set revoked_at = NOW()).
   *
   * @param string $token JWT token
   * @return bool True jika berhasil
   * @throws DatabaseException
   */
  public function revokeByToken(string $token): bool {
    try {
      $query = "UPDATE {$this->table} SET revoked_at = NOW() WHERE token = ? AND revoked_at IS NULL";
      $this->db->query($query, [$token]);

      return true;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencabut session: ' . $e->getMessage());
    }
  }

  /**
   * Cek apakah token sudah dicabut.
   *
   * @param string $token
   * @return bool True jika dicabut
   */
  public function isRevoked(string $token): bool {
    try {
      $query = "SELECT revoked_at FROM {$this->table} WHERE token = ? LIMIT 1";
      $stmt = $this->db->query($query, [$token]);
      $data = $stmt->fetch();

      return !empty($data['revoked_at']);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mengecek session: ' . $e->getMessage());
    }
  }
}
