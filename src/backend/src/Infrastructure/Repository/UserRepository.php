<?php

/**
 * Repository untuk entity User
 *
 * Berisi operasi akses data untuk tabel `users`.
 * Dokumentasi dan komentar menggunakan bahasa Indonesia.
 *
 * @package Scapes\Infrastructure\Repository
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use Scapes\Core\Exceptions\DatabaseException;

class UserRepository extends BaseRepository {

  /**
   * Nama tabel yang akan digunakan oleh repository ini.
   *
   * @var string
   */
  protected string $table = 'users';

  /**
   * Mencari user berdasarkan email.
   *
   * @param string $email Email user yang dicari.
   * @return array|null Data user sebagai array assoc, atau null jika tidak ditemukan.
   * @throws DatabaseException Jika terjadi kesalahan database.
   */
  public function findByEmail(string $email): ?array {
    try {
      $query = "SELECT * FROM {$this->table} WHERE email = ? LIMIT 1";
      $stmt = $this->db->query($query, [$email]);

      $data = $stmt->fetch();
      return $data ?: null;
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mengambil user: ' . $e->getMessage());
    }
  }

  /**
   * Simpan user baru (minimal implementasi insert untuk kebutuhan auth).
   * Mengembalikan ID user yang baru dibuat.
   *
   * @param array $data Data user (email, password_hash, role)
   * @return int ID user baru
   * @throws DatabaseException
   */
  public function create(array $data): int {
    try {
      $query = "INSERT INTO {$this->table} (email, password_hash, role, is_verified, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
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
