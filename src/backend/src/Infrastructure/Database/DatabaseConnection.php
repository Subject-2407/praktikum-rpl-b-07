<?php

/**
 * Kelas untuk Manajemen Koneksi Database
 *
 * Kelas ini bertanggung jawab untuk membuat dan mengelola koneksi ke database.
 * Menggunakan pola Singleton untuk memastikan hanya ada satu instance koneksi aktif.
 *
 * Mendukung beberapa tipe database:
 * - MySQL
 * - PostgreSQL
 * - SQLite
 *
 * @package Scapes\Infrastructure\Database
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Database;

use PDO;
use PDOException;

/**
 * Kelas DatabaseConnection - Mengelola koneksi database.
 *
 * Kelas ini mengimplementasikan pola Singleton untuk memastikan hanya satu
 * instance koneksi database yang digunakan di seluruh aplikasi.
 *
 * @class DatabaseConnection
 */
class DatabaseConnection {

  /**
   * Instance singleton dari DatabaseConnection.
   *
   * @var self|null
   */
  private static ?self $instance = null;

  /**
   * Instance PDO untuk koneksi database.
   *
   * @var PDO|null
   */
  private ?PDO $pdo;

  /**
   * Konstruktor private untuk mencegah instantiasi dari luar.
   *
   * @throws PDOException Jika koneksi gagal.
   */
  private function __construct() {
    $this->pdo = $this->createConnection();
  }

  /**
   * Mencegah cloning instance singleton.
   *
   * @return void
   */
  private function __clone() {
  }

  /**
   * Mencegah unserialize instance singleton.
   *
   * @return void
   */
  public function __wakeup() {
  }

  /**
   * Mendapatkan instance singleton DatabaseConnection.
   *
   * Jika instance belum ada, akan membuat instance baru. Jika sudah ada,
   * mengembalikan instance yang sudah ada.
   *
   * @return self Instance singleton DatabaseConnection.
   * @throws PDOException Jika koneksi database gagal.
   */
  public static function getInstance(): self {
    if (self::$instance === null) {
      self::$instance = new self();
    }

    return self::$instance;
  }

  /**
   * Membuat koneksi database baru berdasarkan konfigurasi environment.
   *
   * Membaca tipe database dari variabel DB_CONNECTION dan membuat
   * DSN serta koneksi PDO yang sesuai.
   *
   * @return PDO Instance PDO untuk koneksi database.
   * @throws PDOException Jika koneksi gagal.
   */
  private function createConnection(): PDO {
    $connection = $_ENV['DB_CONNECTION'] ?? $_SERVER['DB_CONNECTION'] ?? 'mysql';
    $dsn = '';

    switch ($connection) {
      case 'mysql':
        $dsn = $this->createMysqlDsn();
        break;

      case 'pgsql':
        $dsn = $this->createPgsqlDsn();
        break;

      case 'sqlite':
        $dsn = $this->createSqliteDsn();
        break;

      default:
        throw new PDOException(
          "Tipe database '{$connection}' tidak didukung."
        );
    }

    try {
      $pdo = new PDO(
        $dsn,
        $_ENV['DB_USERNAME'] ?? $_SERVER['DB_USERNAME'] ?? '',
        $_ENV['DB_PASSWORD'] ?? $_SERVER['DB_PASSWORD'] ?? '',
        [
          PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
          PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
          PDO::ATTR_EMULATE_PREPARES => false,
        ]
      );

      return $pdo;
    } catch (PDOException $e) {
      throw new PDOException(
        'Gagal terhubung ke database: ' . $e->getMessage(),
        (int) $e->getCode(),
        $e
      );
    }
  }

  /**
   * Membuat DSN (Data Source Name) untuk koneksi MySQL.
   *
   * @return string DSN untuk koneksi MySQL.
   */
  private function createMysqlDsn(): string {
    $host = $_ENV['DB_HOST'] ?? $_SERVER['DB_HOST'] ?? 'localhost';
    $port = $_ENV['DB_PORT'] ?? $_SERVER['DB_PORT'] ?? '3306';
    $database = $_ENV['DB_DATABASE'] ?? $_SERVER['DB_DATABASE'] ?? null;
    $charset = $_ENV['DB_CHARSET'] ?? $_SERVER['DB_CHARSET'] ?? 'utf8mb4';

    if (!$database) {
      throw new PDOException('DB_DATABASE tidak dikonfigurasi.');
    }

    return "mysql:host={$host};port={$port};dbname={$database};charset={$charset}";
  }

  /**
   * Membuat DSN (Data Source Name) untuk koneksi PostgreSQL.
   *
   * @return string DSN untuk koneksi PostgreSQL.
   */
  private function createPgsqlDsn(): string {
    $host = $_ENV['DB_HOST'] ?? $_SERVER['DB_HOST'] ?? 'localhost';
    $port = $_ENV['DB_PORT'] ?? $_SERVER['DB_PORT'] ?? '5432';
    $database = $_ENV['DB_DATABASE'] ?? $_SERVER['DB_DATABASE'] ?? null;
    $username = $_ENV['DB_USERNAME'] ?? $_SERVER['DB_USERNAME'] ?? '';

    if (!$database) {
      throw new PDOException('DB_DATABASE tidak dikonfigurasi.');
    }

    return "pgsql:host={$host};port={$port};dbname={$database};user={$username}";
  }

  /**
   * Membuat DSN (Data Source Name) untuk koneksi SQLite.
   *
   * @return string DSN untuk koneksi SQLite.
   */
  private function createSqliteDsn(): string {
    $database = $_ENV['DB_DATABASE'] ?? $_SERVER['DB_DATABASE'] ?? null;

    if (!$database) {
      throw new PDOException('DB_DATABASE tidak dikonfigurasi.');
    }

    return "sqlite:{$database}";
  }

  /**
   * Mendapatkan instance PDO dari koneksi.
   *
   * @return PDO Instance PDO untuk melakukan query.
   */
  public function getPdo(): PDO {
    if ($this->pdo === null) {
      throw new PDOException('Koneksi database sudah ditutup.');
    }

    return $this->pdo;
  }

  /**
   * Menjalankan query dengan prepared statement.
   *
   * Metode ini membantu menjalankan query dengan parameter yang aman
   * terhadap SQL injection.
   *
   * @param string $query Query SQL yang akan dijalankan.
   * @param array<int, mixed> $params Parameter untuk query.
   *
   * @return \PDOStatement Statement yang sudah dieksekusi.
   * @throws PDOException Jika query gagal atau koneksi sudah ditutup.
   */
  public function query(string $query, array $params = []) {
    if ($this->pdo === null) {
      throw new PDOException('Koneksi database sudah ditutup.');
    }

    try {
      $statement = $this->pdo->prepare($query);
      $statement->execute($params);

      return $statement;
    } catch (PDOException $e) {
      throw new PDOException(
        'Query gagal: ' . $e->getMessage(),
        (int) $e->getCode(),
        $e
      );
    }
  }

  /**
   * Memulai transaksi database.
   *
   * @return bool True jika transaksi berhasil dimulai.
   * @throws PDOException Jika koneksi sudah ditutup.
   */
  public function beginTransaction(): bool {
    if ($this->pdo === null) {
      throw new PDOException('Koneksi database sudah ditutup.');
    }

    return $this->pdo->beginTransaction();
  }

  /**
   * Melakukan commit pada transaksi database.
   *
   * @return bool True jika commit berhasil.
   * @throws PDOException Jika koneksi sudah ditutup.
   */
  public function commit(): bool {
    if ($this->pdo === null) {
      throw new PDOException('Koneksi database sudah ditutup.');
    }

    return $this->pdo->commit();
  }

  /**
   * Melakukan rollback pada transaksi database.
   *
   * @return bool True jika rollback berhasil.
   * @throws PDOException Jika koneksi sudah ditutup.
   */
  public function rollback(): bool {
    if ($this->pdo === null) {
      throw new PDOException('Koneksi database sudah ditutup.');
    }

    return $this->pdo->rollBack();
  }

  /**
   * Menutup koneksi database.
   *
   * @return void
   */
  public function close(): void {
    if ($this->pdo !== null) {
      $this->pdo = null;
    }
  }
}
