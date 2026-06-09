<?php

/**
 * Repository pengguna.
 *
 * Repository ini menangani akses tabel users, email_verifications,
 * password_resets, dan login_attempts sesuai skema database Scapes.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas UserRepository - Repository untuk data pengguna dan token auth.
 */
class UserRepository extends BaseRepository {

  /**
   * Nama tabel pengguna.
   *
   * @var string
   */
  protected string $table = 'users';

  /**
   * Mencari pengguna berdasarkan email.
   *
   * @param string $email Email pengguna.
   *
   * @return User|null Entity pengguna jika ditemukan.
   */
  public function findByEmail(string $email): ?User {
    try {
      $stmt = $this->db->query(
        "SELECT * FROM {$this->table} WHERE email = ? LIMIT 1",
        [$email]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return $data ? $this->mapToUser($data) : null;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari user: ' . $e->getMessage());
    }
  }

  /**
   * Mencari pengguna berdasarkan ID.
   *
   * @param int $id ID pengguna.
   *
   * @return User|null Entity pengguna jika ditemukan.
   */
  public function findByIdEntity(int $id): ?User {
    try {
      $stmt = $this->db->query(
        "SELECT * FROM {$this->table} WHERE id = ? LIMIT 1",
        [$id]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return $data ? $this->mapToUser($data) : null;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari user: ' . $e->getMessage());
    }
  }

  /**
   * Menyimpan pengguna baru atau memperbarui pengguna lama.
   *
   * @param User $user Entity pengguna.
   *
   * @return User Entity pengguna yang sudah tersimpan.
   */
  public function save(User $user): User {
    try {
      if ($user->getId() === 0) {
        $this->db->query(
          "INSERT INTO {$this->table}
            (display_name, email, password_hash, role, is_verified, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)",
          [
            $user->getDisplayName(),
            $user->getEmail(),
            $user->getPasswordHash(),
            $user->getRole(),
            $user->isVerified() ? 1 : 0,
            $user->getCreatedAt(),
            $user->getUpdatedAt(),
          ]
        );

        $id = (int) $this->db->getPdo()->lastInsertId();
        return new User(
          $id,
          $user->getDisplayName(),
          $user->getEmail(),
          $user->getPasswordHash(),
          $user->getRole(),
          $user->isVerified(),
          $user->getCreatedAt(),
          $user->getUpdatedAt()
        );
      }

      $this->db->query(
        "UPDATE {$this->table}
          SET display_name = ?, email = ?, password_hash = ?, role = ?, is_verified = ?,
            updated_at = ?
          WHERE id = ?",
        [
          $user->getDisplayName(),
          $user->getEmail(),
          $user->getPasswordHash(),
          $user->getRole(),
          $user->isVerified() ? 1 : 0,
          $user->getUpdatedAt(),
          $user->getId(),
        ]
      );

      return $user;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menyimpan user: ' . $e->getMessage());
    }
  }

  /**
   * Membuat token verifikasi email.
   *
   * @param int $userId ID pengguna.
   * @param string $token Token acak.
   * @param string $expiresAt Datetime kedaluwarsa.
   *
   * @return void
   */
  public function createEmailVerification(
    int $userId,
    string $token,
    string $expiresAt
  ): void {
    try {
      $this->db->query(
        'INSERT INTO email_verifications
          (user_id, token, expires_at, created_at)
          VALUES (?, ?, ?, NOW())',
        [$userId, $token, $expiresAt]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal membuat token verifikasi: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari token verifikasi email.
   *
   * @param string $token Token verifikasi.
   *
   * @return array<string, mixed>|null Data token jika ditemukan.
   */
  public function findEmailVerification(string $token): ?array {
    try {
      $stmt = $this->db->query(
        'SELECT * FROM email_verifications WHERE token = ? LIMIT 1',
        [$token]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return $data ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari token verifikasi: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menandai token verifikasi sudah dipakai.
   *
   * @param int $verificationId ID record verifikasi.
   *
   * @return void
   */
  public function markEmailVerificationUsed(int $verificationId): void {
    try {
      $this->db->query(
        'UPDATE email_verifications SET used_at = NOW() WHERE id = ?',
        [$verificationId]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memakai token verifikasi: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menandai user sudah terverifikasi.
   *
   * @param int $userId ID pengguna.
   *
   * @return void
   */
  public function markVerified(int $userId): void {
    try {
      $this->db->query(
        "UPDATE {$this->table}
          SET is_verified = 1, updated_at = NOW()
          WHERE id = ?",
        [$userId]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memverifikasi user: ' . $e->getMessage()
      );
    }
  }

  /**
   * Membuat token reset password.
   *
   * @param int $userId ID pengguna.
   * @param string $token Token reset.
   * @param string $expiresAt Datetime kedaluwarsa.
   *
   * @return void
   */
  public function createPasswordReset(
    int $userId,
    string $token,
    string $expiresAt
  ): void {
    try {
      $this->db->query(
        'INSERT INTO password_resets
          (user_id, token, expires_at, created_at)
          VALUES (?, ?, ?, NOW())',
        [$userId, $token, $expiresAt]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal membuat token reset password: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencari token reset password.
   *
   * @param string $token Token reset.
   *
   * @return array<string, mixed>|null Data token jika ditemukan.
   */
  public function findPasswordReset(string $token): ?array {
    try {
      $stmt = $this->db->query(
        'SELECT * FROM password_resets WHERE token = ? LIMIT 1',
        [$token]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return $data ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencari token reset password: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menandai token reset password sudah dipakai.
   *
   * @param int $resetId ID record reset.
   *
   * @return void
   */
  public function markPasswordResetUsed(int $resetId): void {
    try {
      $this->db->query(
        'UPDATE password_resets SET used_at = NOW() WHERE id = ?',
        [$resetId]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memakai token reset password: ' . $e->getMessage()
      );
    }
  }

  /**
   * Memperbarui password hash pengguna.
   *
   * @param int $userId ID pengguna.
   * @param string $passwordHash Password hash baru.
   *
   * @return void
   */
  public function updatePasswordHash(int $userId, string $passwordHash): void {
    try {
      $this->db->query(
        "UPDATE {$this->table}
          SET password_hash = ?, updated_at = NOW()
          WHERE id = ?",
        [$passwordHash, $userId]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal memperbarui password: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mencatat percobaan login.
   *
   * @param string $identifier Email atau identifier login.
   * @param string $ipAddress IP address client.
   * @param bool $isSuccess Status keberhasilan login.
   *
   * @return void
   */
  public function recordLoginAttempt(
    string $identifier,
    string $ipAddress,
    bool $isSuccess
  ): void {
    try {
      $this->db->query(
        'INSERT INTO login_attempts
          (identifier, ip_address, is_success, attempted_at)
          VALUES (?, ?, ?, NOW())',
        [$identifier, $ipAddress, $isSuccess ? 1 : 0]
      );
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal mencatat percobaan login: ' . $e->getMessage()
      );
    }
  }

  /**
   * Menghitung percobaan login gagal terbaru.
   *
   * @param string $identifier Email atau identifier login.
   * @param string $ipAddress IP address client.
   * @param int $windowMinutes Rentang waktu pengecekan dalam menit.
   *
   * @return int Jumlah percobaan gagal.
   */
  public function countRecentFailedLoginAttempts(
    string $identifier,
    string $ipAddress,
    int $windowMinutes
  ): int {
    try {
      $safeWindowMinutes = max(1, $windowMinutes);
      $stmt = $this->db->query(
        'SELECT COUNT(*) AS total
          FROM login_attempts
          WHERE is_success = 0
            AND attempted_at >= DATE_SUB(NOW(), INTERVAL '
            . $safeWindowMinutes . ' MINUTE)
            AND (identifier = ? OR ip_address = ?)',
        [$identifier, $ipAddress]
      );
      $data = $stmt->fetch(PDO::FETCH_ASSOC);

      return (int) ($data['total'] ?? 0);
    } catch (\PDOException $e) {
      throw new DatabaseException(
        'Gagal menghitung percobaan login: ' . $e->getMessage()
      );
    }
  }

  /**
   * Mengubah array database menjadi entity User.
   *
   * @param array<string, mixed> $data Data database.
   *
   * @return User Entity pengguna.
   */
  private function mapToUser(array $data): User {
    return new User(
      (int) $data['id'],
      (string) $data['display_name'],
      (string) $data['email'],
      (string) $data['password_hash'],
      (string) $data['role'],
      (bool) $data['is_verified'],
      (string) $data['created_at'],
      (string) $data['updated_at']
    );
  }
}
