<?php

/**
 * Session Repository
 *
 * Menangani akses data session dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel sessions.
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
 * Kelas SessionRepository - Repository untuk akses data session.
 *
 * @class SessionRepository
 * @extends BaseRepository
 */
class SessionRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'sessions';

  /**
   * Membuat session baru untuk user.
   *
   * @param int $userId ID user.
   * @param string $token Token session.
   * @param string $ipAddress IP address user.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function createSession(int $userId, string $token, string $ipAddress = ''): void {
    try {
      // Session berlaku 30 menit dari sekarang
      $expiresAt = date('Y-m-d H:i:s', time() + (30 * 60));

      $query = "INSERT INTO {$this->table} 
                (user_id, token, ip_address, expires_at, created_at) 
                VALUES (?, ?, ?, ?, ?)";
      $this->db->query($query, [
        $userId,
        $token,
        $ipAddress,
        $expiresAt,
        date('Y-m-d H:i:s'),
      ]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat session: ' . $e->getMessage());
    }
  }

  /**
   * Mencari session berdasarkan token.
   *
   * @param string $token Token session.
   *
   * @return array|null Data session jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByToken(string $token): ?array {
    try {
      $query = "SELECT * FROM {$this->table} 
                WHERE token = ? 
                AND revoked_at IS NULL 
                AND expires_at > NOW() 
                LIMIT 1";
      $result = $this->db->query($query, [$token]);
      return $result->fetch(PDO::FETCH_ASSOC) ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari session: ' . $e->getMessage());
    }
  }

  /**
   * Mencabut (revoke) session berdasarkan token.
   *
   * @param string $token Token session.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function revokeSession(string $token): void {
    try {
      $query = "UPDATE {$this->table} SET revoked_at = ? WHERE token = ?";
      $this->db->query($query, [date('Y-m-d H:i:s'), $token]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencabut session: ' . $e->getMessage());
    }
  }

  /**
   * Mencari semua session aktif dari user.
   *
   * @param int $userId ID user.
   *
   * @return array Array dari session yang aktif.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findActiveSessionsByUser(int $userId): array {
    try {
      $query = "SELECT * FROM {$this->table} 
                WHERE user_id = ? 
                AND revoked_at IS NULL 
                AND expires_at > NOW() 
                ORDER BY created_at DESC";
      $result = $this->db->query($query, [$userId]);
      return $result->fetchAll(PDO::FETCH_ASSOC) ?: [];
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari session: ' . $e->getMessage());
    }
  }

  /**
   * Membuat sesi baru dengan token (untuk backward compatibility).
   *
   * @param int $userId ID user
   * @param string $token JWT token
   * @param string|null $ipAddress IP address (opsional)
   * @param string $expiresAt Datetime expiry (Y-m-d H:i:s)
   *
   * @return int ID session baru
   * @throws DatabaseException
   */
  public function create(int $userId, string $token, ?string $ipAddress, string $expiresAt): int {
    try {
      $query = "INSERT INTO {$this->table} 
                (user_id, token, ip_address, expires_at, created_at) 
                VALUES (?, ?, ?, ?, NOW())";
      $this->db->query($query, [$userId, $token, $ipAddress, $expiresAt]);

      $pdo = $this->db->getPdo();
      return (int) $pdo->lastInsertId();
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat session: ' . $e->getMessage());
    }
  }

  /**
   * Mencabut session berdasarkan token (untuk backward compatibility).
   *
   * @param string $token JWT token
   *
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
   * @param string $token Token session.
   *
   * @return bool True jika dicabut.
   * @throws DatabaseException Jika terjadi error database.
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

