<?php

/**
 * Service untuk Manajemen File Storage
 *
 * Bertanggung jawab untuk menyimpan, memindahkan, dan menghapus file
 * di dalam filesystem aplikasi.
 *
 * @package Scapes\Infrastructure\Storage
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Storage;

/**
 * Kelas FileStorage - Menangani operasi file.
 *
 * @class FileStorage
 */
class FileStorage {

  /**
   * Root path untuk penyimpanan file.
   *
   * @var string
   */
  private string $storagePath;

  /**
   * Konstruktor FileStorage.
   *
   * @param string $storagePath Path root folder storage.
   */
  public function __construct(string $storagePath) {
    $this->storagePath = rtrim($storagePath, DIRECTORY_SEPARATOR);
  }

  /**
   * Menyimpan file yang diupload.
   *
   * @param string $tmpPath Path temporary file (dari $_FILES).
   * @param string $subFolder Sub-folder tujuan (misal: pending/nature).
   * @param string $fileName Nama file tujuan.
   * @return string Path relatif file yang tersimpan.
   */
  public function store(string $tmpPath, string $subFolder, string $fileName): string {
    $this->assertSafeRelativePath($subFolder);
    $this->assertSafeFileName($fileName);

    $relativeFolder = 'wallpapers' . DIRECTORY_SEPARATOR . trim($subFolder, DIRECTORY_SEPARATOR);
    $targetDir = $this->storagePath . DIRECTORY_SEPARATOR . $relativeFolder;

    if (!is_dir($targetDir)) {
      mkdir($targetDir, 0755, true);
    }

    $targetPath = $targetDir . DIRECTORY_SEPARATOR . $fileName;
    
    if (move_uploaded_file($tmpPath, $targetPath)) {
      return $relativeFolder . DIRECTORY_SEPARATOR . $fileName;
    }

    if (
      defined('ENVIRONMENT')
      && ENVIRONMENT === 'testing'
      && rename($tmpPath, $targetPath)
    ) {
      return $relativeFolder . DIRECTORY_SEPARATOR . $fileName;
    }

    throw new \RuntimeException('Gagal memindahkan file ke folder storage');
  }

  /**
   * Memindahkan file antar folder (misal dari pending ke approved).
   *
   * @param string $currentRelativePath Path relatif file saat ini.
   * @param string $newSubFolder Sub-folder tujuan baru.
   * @return string Path relatif file yang baru.
   */
  public function move(string $currentRelativePath, string $newSubFolder): string {
    $this->assertSafeRelativePath($currentRelativePath);
    $this->assertSafeRelativePath($newSubFolder);

    $oldPath = $this->storagePath . DIRECTORY_SEPARATOR . $currentRelativePath;
    
    if (!file_exists($oldPath)) {
      throw new \RuntimeException('File asal tidak ditemukan: ' . $currentRelativePath);
    }

    $fileName = basename($currentRelativePath);
    // Kita asumsikan struktur folder adalah wallpapers/{status}/{category}
    // Jika kita ingin memindahkan dari pending ke approved, kita ganti bagian status-nya
    $relativeFolder = 'wallpapers' . DIRECTORY_SEPARATOR . trim($newSubFolder, DIRECTORY_SEPARATOR);
    $targetDir = $this->storagePath . DIRECTORY_SEPARATOR . $relativeFolder;

    if (!is_dir($targetDir)) {
      mkdir($targetDir, 0755, true);
    }

    $newPath = $targetDir . DIRECTORY_SEPARATOR . $fileName;

    if (rename($oldPath, $newPath)) {
      return $relativeFolder . DIRECTORY_SEPARATOR . $fileName;
    }

    throw new \RuntimeException('Gagal memindahkan file di storage');
  }

  /**
   * Menghapus file dari storage.
   *
   * @param string $relativePath Path relatif file.
   * @return bool True jika berhasil dihapus.
   */
  public function delete(string $relativePath): bool {
    $this->assertSafeRelativePath($relativePath);

    $fullPath = $this->storagePath . DIRECTORY_SEPARATOR . $relativePath;
    if (file_exists($fullPath)) {
      return unlink($fullPath);
    }
    return false;
  }

  /**
   * Mendapatkan path absolut dari path relatif.
   *
   * @param string $relativePath
   * @return string
   */
  public function getAbsolutePath(string $relativePath): string {
    $this->assertSafeRelativePath($relativePath);

    return $this->storagePath . DIRECTORY_SEPARATOR . $relativePath;
  }

  /**
   * Memastikan path relatif tidak mengandung traversal.
   *
   * @param string $path Path relatif.
   *
   * @return void
   */
  private function assertSafeRelativePath(string $path): void {
    $normalized = str_replace('\\', '/', $path);
    if (
      str_contains($normalized, '../')
      || str_contains($normalized, '/..')
      || str_starts_with($normalized, '..')
      || str_starts_with($normalized, '/')
    ) {
      throw new \InvalidArgumentException('Path storage tidak valid');
    }
  }

  /**
   * Memastikan nama file aman.
   *
   * @param string $fileName Nama file.
   *
   * @return void
   */
  private function assertSafeFileName(string $fileName): void {
    if ($fileName !== basename($fileName)) {
      throw new \InvalidArgumentException('Nama file tidak valid');
    }
  }
}
