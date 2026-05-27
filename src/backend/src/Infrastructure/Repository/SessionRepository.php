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
use Scapes\Infrastructure\Security\EncryptionManager;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas SessionRepository - Repository untuk akses data session.
 *
 * @class SessionRepository
 */
class SessionRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'sessions';

  /**
   * Manager enkripsi.
   *
   * @var EncryptionManager
   */
  private EncryptionManager $encryption;

  /**
   * Konstruktor SessionRepository.
   *
   * @param DatabaseConnection|null $db
   * @param EncryptionManager|null $encryption
   */
  public function __construct(?DatabaseConnection $db = null, ?EncryptionManager $encryption = null) {
    parent::__construct($db);
    $this->encryption = $encryption ?? new EncryptionManager($_ENV['APP_KEY'] ?? 'default_key');
  }

  /**
   * Membuat session baru untuk user.
   *
   * @param int $userId ID user.
   * @param string $token Token session plaintext (JWT).
   * @param string $ipAddress IP address user.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function createSession(int $userId, string $token, string $ipAddress = ''): void {
    try {
      // Session berlaku 30 menit dari sekarang
      $expiresAt = date('Y-m-d H:i:s', time() + (30 * 60));
      
      // Enkripsi token dan buat hash
      $encryptedToken = $this->encryption->encrypt($token);
      $tokenHash = $this->encryption->hash($token);

      $query = "INSERT INTO {$this->table} 
                (user_id, token, token_hash, ip_address, expires_at, created_at) 
                VALUES (?, ?, ?, ?, ?, ?)";
      $this->db->query($query, [
        $userId,
        $encryptedToken,
        $tokenHash,
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
   * @param string $token Token session plaintext.
   *
   * @return array<string, mixed>|null Data session jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByToken(string $token): ?array {
    try {
      $tokenHash = $this->encryption->hash($token);
      
      $query = "SELECT * FROM {$this->table} 
                WHERE token_hash = ? 
                AND revoked_at IS NULL 
                AND expires_at > NOW() 
                LIMIT 1";
      $result = $this->db->query($query, [$tokenHash]);
      $data = $result->fetch(PDO::FETCH_ASSOC);
      
      if (!$data) {
        return null;
      }

      // Dekripsi token asli (opsional, tapi untuk validasi payload jika perlu)
      $data['token_plaintext'] = $this->encryption->decrypt($data['token']);
      
      return $data;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari session: ' . $e->getMessage());
    }
  }

  /**
   * Mencabut (revoke) session berdasarkan token.
   *
   * @param string $token Token session plaintext.
   *
   * @return void
   * @throws DatabaseException Jika terjadi error database.
   */
  public function revokeSession(string $token): void {
    try {
      $tokenHash = $this->encryption->hash($token);
      
      $query = "UPDATE {$this->table} SET revoked_at = ? WHERE token_hash = ?";
      $this->db->query($query, [date('Y-m-d H:i:s'), $tokenHash]);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencabut session: ' . $e->getMessage());
    }
  }

  /**
   * Mencari semua session aktif dari user.
   *
   * @param int $userId ID user.
   *
   * @return array<int, array<string, mixed>> Array dari session yang aktif.
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
   * @param string $token JWT token plaintext
   * @param string|null $ipAddress IP address (opsional)
   * @param string $expiresAt Datetime expiry (Y-m-d H:i:s)
   *
   * @return int ID session baru
   * @throws DatabaseException
   */
  public function create(int $userId, string $token, ?string $ipAddress, string $expiresAt): int {
    try {
      $encryptedToken = $this->encryption->encrypt($token);
      $tokenHash = $this->encryption->hash($token);

      $query = "INSERT INTO {$this->table} 
                (user_id, token, token_hash, ip_address, expires_at, created_at) 
                VALUES (?, ?, ?, ?, ?, NOW())";
      $this->db->query($query, [$userId, $encryptedToken, $tokenHash, $ipAddress, $expiresAt]);

      $pdo = $this->db->getPdo();
      return (int) $pdo->lastInsertId();
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat session: ' . $e->getMessage());
    }
  }

  /**
   * Mencabut session berdasarkan token (untuk backward compatibility).
   *
   * @param string $token JWT token plaintext
   *
   * @return bool True jika berhasil
   * @throws DatabaseException
   */
  public function revokeByToken(string $token): bool {
    try {
      $tokenHash = $this->encryption->hash($token);
      
      $query = "UPDATE {$this->table} SET revoked_at = NOW() WHERE token_hash = ? AND revoked_at IS NULL";
      $this->db->query($query, [$tokenHash]);

      return true;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencabut session: ' . $e->getMessage());
    }
  }

  /**
   * Cek apakah token sudah dicabut.
   *
   * @param string $token Token session plaintext.
   *
   * @return bool True jika dicabut.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function isRevoked(string $token): bool {
    try {
      $tokenHash = $this->encryption->hash($token);
      
      $query = "SELECT revoked_at FROM {$this->table} WHERE token_hash = ? LIMIT 1";
      $stmt = $this->db->query($query, [$tokenHash]);
      $data = $stmt->fetch();

      // Jika data tidak ditemukan, anggap dicabut atau invalid
      if (!$data) {
        return true;
      }

      return !empty($data['revoked_at']);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mengecek session: ' . $e->getMessage());
    }
  }
}

