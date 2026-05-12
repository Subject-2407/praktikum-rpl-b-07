<?php

/**
 * User Repository
 *
 * Menangani akses data pengguna dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel users.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\User;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas UserRepository - Repository untuk akses data pengguna.
 *
 * @class UserRepository
 * @extends BaseRepository
 */
class UserRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'users';

  /**
   * Mencari user berdasarkan email.
   *
   * @param string $email Email pengguna.
   *
   * @return User|null User jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByEmail(string $email): ?User {
    try {
      $query = "SELECT * FROM {$this->table} WHERE email = ? LIMIT 1";
      $result = $this->db->query($query, [$email]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToUser($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari user: ' . $e->getMessage());
    }
  }

  /**
   * Mencari user berdasarkan ID.
   *
   * @param int $id ID pengguna.
   *
   * @return User|null User jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByIdEntity(int $id): ?User {
    try {
      $query = "SELECT * FROM {$this->table} WHERE id = ? LIMIT 1";
      $result = $this->db->query($query, [$id]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToUser($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari user: ' . $e->getMessage());
    }
  }

  /**
   * Menyimpan user baru ke database.
   *
   * @param User $user User yang akan disimpan.
   *
   * @return User User dengan ID yang sudah di-assign oleh database.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function save(User $user): User {
    try {
      if ($user->getId() === 0) {
        // Insert user baru
        $query = "INSERT INTO {$this->table} 
                  (email, password_hash, role, is_verified, created_at, updated_at) 
                  VALUES (?, ?, ?, ?, ?, ?)";
        $this->db->query($query, [
          $user->getEmail(),
          $user->getPasswordHash(),
          $user->getRole(),
          $user->isVerified() ? 1 : 0,
          $user->getCreatedAt(),
          $user->getUpdatedAt(),
        ]);

        // Ambil ID yang baru di-generate
        $lastId = (int) $this->db->getPdo()->lastInsertId();

        // Buat user baru dengan ID
        return new User(
          $lastId,
          $user->getEmail(),
          $user->getPasswordHash(),
          $user->getRole(),
          $user->isVerified(),
          $user->getCreatedAt(),
          $user->getUpdatedAt()
        );
      } else {
        // Update user yang sudah ada
        $query = "UPDATE {$this->table} 
                  SET email = ?, password_hash = ?, role = ?, is_verified = ?, updated_at = ? 
                  WHERE id = ?";
        $this->db->query($query, [
          $user->getEmail(),
          $user->getPasswordHash(),
          $user->getRole(),
          $user->isVerified() ? 1 : 0,
          $user->getUpdatedAt(),
          $user->getId(),
        ]);

        return $user;
      }
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menyimpan user: ' . $e->getMessage());
    }
  }

  /**
   * Mengkonversi data dari database menjadi User entity.
   *
   * @param array $data Data dari database.
   *
   * @return User User entity.
   */
  private function mapToUser(array $data): User {
    return new User(
      (int) $data['id'],
      $data['email'],
      $data['password_hash'],
      $data['role'],
      (bool) $data['is_verified'],
      $data['created_at'],
      $data['updated_at']
    );
  }

  /**
   * Simpan user baru (untuk backward compatibility).
   * Mengembalikan ID user yang baru dibuat.
   *
   * @param array $data Data user (email, password_hash, role)
   *
   * @return int ID user baru
   * @throws DatabaseException
   */
  public function create(array $data): int {
    try {
      $query = "INSERT INTO {$this->table} 
                (email, password_hash, role, is_verified, created_at, updated_at) 
                VALUES (?, ?, ?, ?, NOW(), NOW())";
      $this->db->query($query, [
        $data['email'],
        $data['password_hash'],
        $data['role'] ?? 'contributor',
        $data['is_verified'] ?? 0,
      ]);

      $pdo = $this->db->getPdo();
      return (int) $pdo->lastInsertId();
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal membuat user: ' . $e->getMessage());
    }
  }
}

