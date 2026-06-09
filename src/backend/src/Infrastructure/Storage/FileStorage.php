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
   * Membuat thumbnail WebP dari file gambar.
   *
   * @param string $sourcePath Path file sumber.
   * @param string $subFolder Sub-folder tujuan.
   * @param string $fileName Nama file thumbnail.
   * @param int $maxWidth Lebar maksimum thumbnail.
   * @param int $maxHeight Tinggi maksimum thumbnail.
   *
   * @return string Path relatif thumbnail yang tersimpan.
   */
  public function storeThumbnailWebp(
    string $sourcePath,
    string $subFolder,
    string $fileName,
    int $maxWidth = 480,
    int $maxHeight = 270
  ): string {
    $this->assertSafeRelativePath($subFolder);
    $this->assertSafeFileName($fileName);

    $imageInfo = @getimagesize($sourcePath);
    if ($imageInfo === false) {
      throw new \RuntimeException('File sumber thumbnail bukan gambar valid');
    }

    if (!extension_loaded('gd')) {
      return $this->storeThumbnailWebpWithImagick(
        $sourcePath,
        $subFolder,
        $fileName,
        $maxWidth,
        $maxHeight
      );
    }

    $source = $this->createImageResource($sourcePath, (string) $imageInfo['mime']);
    if (!$source instanceof \GdImage) {
      throw new \RuntimeException('Gagal membaca gambar untuk thumbnail');
    }

    $width = (int) $imageInfo[0];
    $height = (int) $imageInfo[1];
    $ratio = min($maxWidth / $width, $maxHeight / $height, 1);
    $thumbnailWidth = max(1, (int) round($width * $ratio));
    $thumbnailHeight = max(1, (int) round($height * $ratio));

    $thumbnail = imagecreatetruecolor($thumbnailWidth, $thumbnailHeight);
    if (!$thumbnail instanceof \GdImage) {
      imagedestroy($source);
      throw new \RuntimeException('Gagal membuat kanvas thumbnail');
    }

    imagecopyresampled(
      $thumbnail,
      $source,
      0,
      0,
      0,
      0,
      $thumbnailWidth,
      $thumbnailHeight,
      $width,
      $height
    );

    $relativeFolder = 'wallpapers' . DIRECTORY_SEPARATOR . trim($subFolder, DIRECTORY_SEPARATOR);
    $targetDir = $this->storagePath . DIRECTORY_SEPARATOR . $relativeFolder;

    if (!is_dir($targetDir)) {
      mkdir($targetDir, 0755, true);
    }

    $targetPath = $targetDir . DIRECTORY_SEPARATOR . $fileName;
    $stored = imagewebp($thumbnail, $targetPath, 82);

    imagedestroy($source);
    imagedestroy($thumbnail);

    if (!$stored) {
      throw new \RuntimeException('Gagal menyimpan thumbnail');
    }

    return $relativeFolder . DIRECTORY_SEPARATOR . $fileName;
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
   * Membuat resource GD dari gambar sesuai MIME type.
   *
   * @param string $path Path gambar.
   * @param string $mimeType MIME type gambar.
   *
   * @return \GdImage|null Resource gambar.
   */
  private function createImageResource(string $path, string $mimeType): ?\GdImage {
    return match ($mimeType) {
      'image/jpeg' => imagecreatefromjpeg($path) ?: null,
      'image/png' => imagecreatefrompng($path) ?: null,
      'image/webp' => imagecreatefromwebp($path) ?: null,
      default => null,
    };
  }

  /**
   * Membuat thumbnail WebP memakai Imagick saat GD tidak tersedia.
   *
   * @param string $sourcePath Path file sumber.
   * @param string $subFolder Sub-folder tujuan.
   * @param string $fileName Nama file thumbnail.
   * @param int $maxWidth Lebar maksimum thumbnail.
   * @param int $maxHeight Tinggi maksimum thumbnail.
   *
   * @return string Path relatif thumbnail yang tersimpan.
   */
  private function storeThumbnailWebpWithImagick(
    string $sourcePath,
    string $subFolder,
    string $fileName,
    int $maxWidth,
    int $maxHeight
  ): string {
    if (!class_exists('\Imagick')) {
      throw new \RuntimeException(
        'GD atau Imagick diperlukan untuk membuat thumbnail WebP'
      );
    }

    $relativeFolder = 'wallpapers' . DIRECTORY_SEPARATOR . trim($subFolder, DIRECTORY_SEPARATOR);
    $targetDir = $this->storagePath . DIRECTORY_SEPARATOR . $relativeFolder;

    if (!is_dir($targetDir)) {
      mkdir($targetDir, 0755, true);
    }

    $targetPath = $targetDir . DIRECTORY_SEPARATOR . $fileName;
    $image = new \Imagick($sourcePath);
    $image->thumbnailImage($maxWidth, $maxHeight, true, true);
    $image->setImageFormat('webp');
    $image->setImageCompressionQuality(82);
    $image->writeImage($targetPath);
    $image->clear();
    $image->destroy();

    return $relativeFolder . DIRECTORY_SEPARATOR . $fileName;
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
