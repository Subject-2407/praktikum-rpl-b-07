<?php

/**
 * Interface untuk Koneksi Database
 *
 * Interface ini mendefinisikan kontrak yang harus diimplementasikan
 * oleh kelas-kelas yang menangani koneksi ke database.
 *
 * @package Scapes\Infrastructure\Database
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Database;

use PDO;
use PDOStatement;

/**
 * Interface DatabaseConnectionInterface.
 *
 * Mendefinisikan metode-metode yang harus disediakan oleh kelas
 * yang mengimplementasikan koneksi ke database.
 *
 * @interface DatabaseConnectionInterface
 */
interface DatabaseConnectionInterface {

  /**
   * Mendapatkan instance PDO dari koneksi.
   *
   * @return PDO Instance PDO untuk melakukan query.
   */
  public function getPdo(): PDO;

  /**
   * Menjalankan query dengan prepared statement.
   *
   * @param string $query Query SQL yang akan dijalankan.
   * @param array<int, mixed> $params Parameter untuk query.
   *
   * @return PDOStatement Statement yang sudah dieksekusi.
   */
  public function query(string $query, array $params = []): PDOStatement;

  /**
   * Memulai transaksi database.
   *
   * @return bool True jika transaksi berhasil dimulai.
   */
  public function beginTransaction(): bool;

  /**
   * Melakukan commit pada transaksi database.
   *
   * @return bool True jika commit berhasil.
   */
  public function commit(): bool;

  /**
   * Melakukan rollback pada transaksi database.
   *
   * @return bool True jika rollback berhasil.
   */
  public function rollback(): bool;

  /**
   * Menutup koneksi database.
   *
   * @return void
   */
  public function close(): void;
}
