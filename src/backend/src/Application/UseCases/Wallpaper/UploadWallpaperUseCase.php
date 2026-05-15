<?php

/**
 * Upload Wallpaper Use Case
 *
 * Menangani upload wallpaper dari contributor.
 * Melakukan validasi file, menyimpan ke filesystem, dan mencatat metadata di database.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\TagRepository;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Infrastructure\Storage\FileStorage;

/**
 * Kelas UploadWallpaperUseCase - Menangani upload wallpaper.
 *
 * @class UploadWallpaperUseCase
 */
class UploadWallpaperUseCase {

  /**
   * Repository untuk akses wallpaper data.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository untuk akses kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Repository untuk akses tag.
   *
   * @var TagRepository
   */
  private TagRepository $tagRepository;

  /**
   * Service untuk penyimpanan file.
   *
   * @var FileStorage
   */
  private FileStorage $storage;

  /**
   * Instance koneksi database untuk transaksi.
   *
   * @var DatabaseConnection
   */
  private DatabaseConnection $db;

  /**
   * Konstanta untuk maksimal ukuran file (10 MB).
   *
   * @var int
   */
  private const MAX_FILE_SIZE_KB = 10240;

  /**
   * Konstanta untuk minimum lebar gambar (1920 px).
   *
   * @var int
   */
  private const MIN_WIDTH = 1920;

  /**
   * Konstanta untuk minimum tinggi gambar (1080 px).
   *
   * @var int
   */
  private const MIN_HEIGHT = 1080;

  /**
   * Array dari MIME type yang diizinkan.
   *
   * @var array
   */
  private const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

  /**
   * Konstruktor UploadWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository untuk wallpaper.
   * @param CategoryRepository $categoryRepository Repository untuk kategori.
   * @param TagRepository $tagRepository Repository untuk tag.
   * @param FileStorage $storage Service storage.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    CategoryRepository $categoryRepository,
    TagRepository $tagRepository,
    FileStorage $storage
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->categoryRepository = $categoryRepository;
    $this->tagRepository = $tagRepository;
    $this->storage = $storage;
    $this->db = DatabaseConnection::getInstance();
  }

  /**
   * Melakukan upload wallpaper.
   *
   * @param int $contributorId ID contributor yang upload.
   * @param int $categoryId ID kategori wallpaper.
   * @param string $title Judul wallpaper.
   * @param string $tmpFilePath Path lengkap file temporary (PHP tmp).
   * @param string $originalName Nama asli file.
   * @param int $fileSizeKb Ukuran file dalam KB.
   * @param string $mimeType MIME type file.
   * @param int $width Lebar gambar dalam px.
   * @param int $height Tinggi gambar dalam px.
   * @param array $tagIds Daftar ID tag.
   * @param string|null $description Deskripsi wallpaper.
   *
   * @return Wallpaper Wallpaper yang baru dibuat.
   * @throws ValidationException Jika validasi gagal.
   */
  public function execute(
    int $contributorId,
    int $categoryId,
    string $title,
    string $tmpFilePath,
    string $originalName,
    int $fileSizeKb,
    string $mimeType,
    int $width,
    int $height,
    array $tagIds = [],
    ?string $description = null
  ): Wallpaper {
    // Validasi title tidak kosong
    if (empty(trim($title))) {
      throw new ValidationException('Judul wallpaper tidak boleh kosong');
    }

    // Validasi ukuran file
    if ($fileSizeKb > self::MAX_FILE_SIZE_KB) {
      throw new ValidationException('Ukuran file maksimal 10 MB');
    }

    // Validasi MIME type
    if (!in_array($mimeType, self::ALLOWED_MIME_TYPES, true)) {
      throw new ValidationException('Format file hanya mendukung JPEG, PNG, atau WebP');
    }

    // Validasi dimensi gambar
    if ($width < self::MIN_WIDTH || $height < self::MIN_HEIGHT) {
      throw new ValidationException(
        'Dimensi gambar minimal ' . self::MIN_WIDTH . 'x' . self::MIN_HEIGHT . ' px'
      );
    }

    // Validasi kategori ada
    $category = $this->categoryRepository->findByIdEntity($categoryId);
    if ($category === null) {
      throw new ValidationException('Kategori tidak ditemukan');
    }

    // Validasi tags (jika ada)
    foreach ($tagIds as $tagId) {
      $tag = $this->tagRepository->findByIdEntity((int) $tagId);
      if ($tag === null) {
        throw new ValidationException("Tag dengan ID {$tagId} tidak ditemukan");
      }
    }

    // Generate unique filename: {timestamp}_{rand}.{ext}
    $extension = pathinfo($originalName, PATHINFO_EXTENSION);
    $newFileName = time() . '_' . bin2hex(random_bytes(4)) . '.' . $extension;
    
    // Tentukan subfolder: pending/{category_slug}
    $subFolder = 'pending' . DIRECTORY_SEPARATOR . $category->getSlug();

    // Simpan file ke storage
    $persistentRelativePath = $this->storage->store($tmpFilePath, $subFolder, $newFileName);

    // Mulai transaksi database
    $this->db->beginTransaction();

    try {
      // Buat wallpaper baru dengan status pending
      $wallpaper = new Wallpaper(
        0,  // ID akan di-assign oleh database
        $contributorId,
        $categoryId,
        $title,
        $persistentRelativePath, // Path ke storage
        $newFileName,
        $fileSizeKb,
        $mimeType,
        $width,
        $height,
        'pending',  // Status default pending untuk moderasi
        $description,
        null,  // scheduled_at
        null,  // published_at
        date('Y-m-d H:i:s'),
        date('Y-m-d H:i:s')
      );

      // Simpan ke repository
      $savedWallpaper = $this->wallpaperRepository->save($wallpaper);

      // Tambahkan tags
      foreach ($tagIds as $tagId) {
        $this->tagRepository->addTagToWallpaper($savedWallpaper->getId(), (int) $tagId);
      }

      // Commit transaksi
      $this->db->commit();

      return $savedWallpaper;
    } catch (\Exception $e) {
      // Rollback jika terjadi kesalahan
      $this->db->rollback();
      
      // Hapus file yang sudah terlanjur dipindah jika transaksi gagal
      $this->storage->delete($persistentRelativePath);
      
      throw $e;
    }
  }
}