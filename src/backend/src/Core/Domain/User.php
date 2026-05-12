<?php

/**
 * Domain Entity: User
 *
 * Merepresentasikan user (Contributor atau Admin) dalam sistem Scapes.
 * Mengandung data identitas pengguna dan role-nya.
 *
 * @package Scapes\Core\Domain
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Domain;

/**
 * Kelas User - Entitas domain untuk pengguna sistem.
 *
 * @class User
 */
class User {

  /**
   * ID unik pengguna.
   *
   * @var int
   */
  private int $id;

  /**
   * Email pengguna.
   *
   * @var string
   */
  private string $email;

  /**
   * Hash password pengguna (bcrypt).
   *
   * @var string
   */
  private string $passwordHash;

  /**
   * Role pengguna (contributor atau admin).
   *
   * @var string
   */
  private string $role;

  /**
   * Status verifikasi email pengguna.
   *
   * @var bool
   */
  private bool $isVerified;

  /**
   * Waktu akun dibuat.
   *
   * @var string
   */
  private string $createdAt;

  /**
   * Waktu akun terakhir diperbarui.
   *
   * @var string
   */
  private string $updatedAt;

  /**
   * Konstruktor User.
   *
   * @param int $id ID unik pengguna.
   * @param string $email Email pengguna.
   * @param string $passwordHash Hash password bcrypt.
   * @param string $role Role pengguna (contributor atau admin).
   * @param bool $isVerified Status verifikasi email.
   * @param string $createdAt Waktu akun dibuat.
   * @param string $updatedAt Waktu akun diperbarui.
   */
  public function __construct(
    int $id,
    string $email,
    string $passwordHash,
    string $role,
    bool $isVerified = false,
    string $createdAt = '',
    string $updatedAt = ''
  ) {
    $this->id = $id;
    $this->email = $email;
    $this->passwordHash = $passwordHash;
    $this->role = $role;
    $this->isVerified = $isVerified;
    $this->createdAt = $createdAt ?: date('Y-m-d H:i:s');
    $this->updatedAt = $updatedAt ?: date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan ID pengguna.
   *
   * @return int
   */
  public function getId(): int {
    return $this->id;
  }

  /**
   * Mendapatkan email pengguna.
   *
   * @return string
   */
  public function getEmail(): string {
    return $this->email;
  }

  /**
   * Mendapatkan hash password pengguna.
   *
   * @return string
   */
  public function getPasswordHash(): string {
    return $this->passwordHash;
  }

  /**
   * Mendapatkan role pengguna.
   *
   * @return string
   */
  public function getRole(): string {
    return $this->role;
  }

  /**
   * Mendapatkan status verifikasi email.
   *
   * @return bool
   */
  public function isVerified(): bool {
    return $this->isVerified;
  }

  /**
   * Mengatur status verifikasi email.
   *
   * @param bool $isVerified Status verifikasi.
   *
   * @return void
   */
  public function setVerified(bool $isVerified): void {
    $this->isVerified = $isVerified;
    $this->updatedAt = date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan waktu akun dibuat.
   *
   * @return string
   */
  public function getCreatedAt(): string {
    return $this->createdAt;
  }

  /**
   * Mendapatkan waktu akun terakhir diperbarui.
   *
   * @return string
   */
  public function getUpdatedAt(): string {
    return $this->updatedAt;
  }

  /**
   * Mengecek apakah password yang diberikan cocok dengan hash.
   *
   * @param string $password Password plaintext.
   *
   * @return bool
   */
  public function verifyPassword(string $password): bool {
    return password_verify($password, $this->passwordHash);
  }

  /**
   * Mengecek apakah pengguna adalah admin.
   *
   * @return bool
   */
  public function isAdmin(): bool {
    return $this->role === 'admin';
  }

  /**
   * Mengecek apakah pengguna adalah contributor.
   *
   * @return bool
   */
  public function isContributor(): bool {
    return $this->role === 'contributor';
  }
}
