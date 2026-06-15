<?php

/**
 * Use case upload wallpaper.
 *
 * Use case ini memvalidasi metadata, file gambar, kategori, tag, lalu
 * menyimpan file ke storage dan metadata ke database.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\ValidationException;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\TagRepository;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Storage\FileStorage;

/**
 * Kelas UploadWallpaperUseCase - Upload wallpaper contributor.
 */
class UploadWallpaperUseCase {

  /**
   * Maksimal ukuran file dalam byte.
   *
   * @var int
   */
  private const MAX_FILE_SIZE_BYTES = 10485760;

  /**
   * Resolusi minimal berdasarkan target perangkat.
   *
   * @var array<string, array{width: int, height: int}>
   */
  private const MIN_RESOLUTION_BY_TARGET_DEVICE = [
    'desktop' => ['width' => 1920, 'height' => 1080],
    'mobile' => ['width' => 360, 'height' => 800],
    'tablet' => ['width' => 768, 'height' => 1024],
  ];

  /**
   * MIME type yang diterima.
   *
   * @var array<string, string>
   */
  private const ALLOWED_MIME_TYPES = [
    'image/jpeg' => 'jpeg',
    'image/png' => 'png',
    'image/webp' => 'webp',
  ];

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Repository tag.
   *
   * @var TagRepository
   */
  private ?TagRepository $tagRepository;

  /**
   * Storage file.
   *
   * @var FileStorage
   */
  private ?FileStorage $storage;

  /**
   * Konstruktor UploadWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   * @param CategoryRepository $categoryRepository Repository kategori.
   * @param TagRepository $tagRepository Repository tag.
   * @param FileStorage $storage Storage file.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    CategoryRepository $categoryRepository,
    ?TagRepository $tagRepository = null,
    ?FileStorage $storage = null
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->categoryRepository = $categoryRepository;
    $this->tagRepository = $tagRepository;
    $this->storage = $storage;
  }

  /**
   * Mengunggah wallpaper baru.
   *
   * @param int $contributorId ID contributor.
   * @param mixed $data Metadata request atau category_id legacy.
   * @param mixed $file Data file upload atau title legacy.
   *
   * @return array<string, mixed> Detail wallpaper baru.
   */
  public function execute(
    int $contributorId,
    mixed $data,
    mixed $file,
    mixed ...$legacyArgs
  ): array|Wallpaper {
    if (!is_array($data) || !is_array($file)) {
      return $this->executeLegacy(
        $contributorId,
        $data,
        $file,
        array_values($legacyArgs)
      );
    }

    $tagRepository = $this->tagRepository;
    $storage = $this->storage;
    if ($tagRepository === null || $storage === null) {
      throw new \LogicException('Dependency upload wallpaper belum lengkap.');
    }

    $title = trim((string) ($data['title'] ?? ''));
    $description = isset($data['description'])
      ? trim((string) $data['description'])
      : null;
    $categoryId = (int) ($data['category_id'] ?? 0);
    $tagIds = $this->normalizeTagIds($data['tags'] ?? []);

    $errors = [];
    if ($title === '') {
      $errors['title'][] = 'The title field is required.';
    } elseif (strlen($title) > 255) {
      $errors['title'][] = 'The title field must not exceed 255 characters.';
    }

    if ($categoryId <= 0) {
      $errors['category_id'][] = 'The category_id field is required.';
    }

    if (($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
      $errors['file'][] = 'The file field is required.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    $category = $this->categoryRepository->findByIdEntity($categoryId);
    if ($category === null) {
      throw new ValidationException('Validation failed.', 0, [
        'category_id' => ['The selected category_id is invalid.'],
      ]);
    }

    if (!$tagRepository->allIdsExist($tagIds)) {
      throw new ValidationException('Validation failed.', 0, [
        'tags' => ['One or more selected tags are invalid.'],
      ]);
    }

    $tmpPath = (string) $file['tmp_name'];
    $fileSizeBytes = (int) $file['size'];
    $imageInfo = @getimagesize($tmpPath);
    $mimeType = $this->detectMimeType($tmpPath);

    $this->validateFile($fileSizeBytes, $mimeType, $imageInfo);

    if ($imageInfo === false) {
      throw new ValidationException('Validation failed.', 0, [
        'file' => ['The uploaded file must be a valid image.'],
      ]);
    }

    $width = (int) $imageInfo[0];
    $height = (int) $imageInfo[1];
    $targetDevice = $this->detectTargetDevice($width, $height);
    $extension = self::ALLOWED_MIME_TYPES[$mimeType];
    $wallpaperId = $this->uuidV4();
    $fileName = $wallpaperId . '.' . $extension;
    $thumbnailName = $wallpaperId . '.webp';
    $subFolder = 'pending' . DIRECTORY_SEPARATOR . (string) $categoryId;
    $relativePath = $storage->store($tmpPath, $subFolder, $fileName);
    $thumbnailPath = null;

    try {
      $thumbnailPath = $storage->storeThumbnailWebp(
        $storage->getAbsolutePath($relativePath),
        'pending'
          . DIRECTORY_SEPARATOR
          . (string) $categoryId
          . DIRECTORY_SEPARATOR
          . 'thumbnails',
        $thumbnailName
      );
    } catch (\Throwable $e) {
      $storage->delete($relativePath);
      throw $e;
    }

    try {
      $createdWallpaperId = $this->wallpaperRepository->transaction(
        function () use (
          $wallpaperId,
          $contributorId,
          $categoryId,
          $title,
          $description,
          $relativePath,
          $thumbnailPath,
          $fileName,
          $fileSizeBytes,
          $mimeType,
          $width,
          $height,
          $tagIds,
          $tagRepository,
          $targetDevice
        ): int|string {
          $id = $this->wallpaperRepository->create([
            'contributor_id' => $contributorId,
            'category_id' => $categoryId,
            'title' => $title,
            'description' => $description !== '' ? $description : null,
            'id' => $wallpaperId,
            'file_size_kb' => (int) ceil($fileSizeBytes / 1024),
            'mime_type' => $mimeType,
            'width' => $width,
            'height' => $height,
            'target_device' => $targetDevice,
            'status' => 'pending',
            'published_at' => null,
          ]);

          $tagRepository->replaceWallpaperTags($id, $tagIds);
          return $id;
        }
      );
    } catch (\Throwable $e) {
      $storage->delete($relativePath);
      if ($thumbnailPath !== null) {
        $storage->delete($thumbnailPath);
      }
      throw $e;
    }

    return $this->wallpaperRepository->findDetailedById($createdWallpaperId) ?? [];
  }

  /**
   * Menjalankan alur upload lama untuk kompatibilitas unit test MVP.
   *
   * @param int $contributorId ID contributor.
   * @param mixed $categoryId ID kategori.
   * @param mixed $title Judul wallpaper.
   * @param array<int, mixed> $args Argumen lama berikutnya.
   *
   * @return Wallpaper Entity wallpaper.
   */
  private function executeLegacy(
    int $contributorId,
    mixed $categoryId,
    mixed $title,
    array $args
  ): Wallpaper {
    $categoryId = (int) $categoryId;
    $title = (string) $title;
    $filePath = (string) ($args[0] ?? '');
    $fileName = (string) ($args[1] ?? '');
    $fileSizeKb = (int) ($args[2] ?? 0);
    $mimeType = (string) ($args[3] ?? '');
    $width = (int) ($args[4] ?? 0);
    $height = (int) ($args[5] ?? 0);
    $description = isset($args[6]) ? (string) $args[6] : null;

    if (trim($title) === '') {
      throw new ValidationException('Judul wallpaper tidak boleh kosong');
    }

    if ($fileSizeKb > 10240) {
      throw new ValidationException('Ukuran file maksimal 10 MB');
    }

    if (!array_key_exists($mimeType, self::ALLOWED_MIME_TYPES)) {
      throw new ValidationException(
        'Format file hanya mendukung JPEG, PNG, atau WebP'
      );
    }

    $targetDevice = $this->detectTargetDevice($width, $height);
    $minimumResolution = $this->minimumResolutionFor($targetDevice);
    if (
      $width < $minimumResolution['width']
      || $height < $minimumResolution['height']
    ) {
      throw new ValidationException(sprintf(
        'Dimensi gambar minimal untuk %s adalah %dx%d px',
        $targetDevice,
        $minimumResolution['width'],
        $minimumResolution['height']
      ));
    }

    if ($this->categoryRepository->findByIdEntity($categoryId) === null) {
      throw new ValidationException('Kategori tidak ditemukan');
    }

    $wallpaper = new Wallpaper(
      0,
      $contributorId,
      $categoryId,
      $title,
      $filePath,
      $fileName,
      $fileSizeKb,
      $mimeType,
      $width,
      $height,
      'pending',
      $description,
      null,
      null,
      '',
      '',
      $targetDevice
    );

    return $this->wallpaperRepository->save($wallpaper);
  }

  /**
   * Validasi detail file gambar.
   *
   * @param int $fileSizeBytes Ukuran file byte.
   * @param string $mimeType MIME type file.
   * @param array<int|string, mixed>|false $imageInfo Info gambar.
   *
   * @return void
   */
  private function validateFile(
    int $fileSizeBytes,
    string $mimeType,
    array|false $imageInfo
  ): void {
    $errors = [];

    if ($fileSizeBytes <= 0 || $fileSizeBytes > self::MAX_FILE_SIZE_BYTES) {
      $errors['file'][] =
        'File size must not exceed 10 MB.';
    }

    if (!array_key_exists($mimeType, self::ALLOWED_MIME_TYPES)) {
      $errors['file'][] =
        'Invalid format. Allowed formats are JPG, PNG, and WebP.';
    }

    if ($imageInfo === false) {
      $errors['file'][] = 'The uploaded file must be a valid image.';
    } else {
      $width = (int) $imageInfo[0];
      $height = (int) $imageInfo[1];
      $targetDevice = $this->detectTargetDevice($width, $height);
      $minimumResolution = $this->minimumResolutionFor($targetDevice);
      if (
        $width < $minimumResolution['width']
        || $height < $minimumResolution['height']
      ) {
        $errors['file'][] = sprintf(
          'Image resolution for %s must be at least %dx%d.',
          $targetDevice,
          $minimumResolution['width'],
          $minimumResolution['height']
        );
      }
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }
  }

  /**
   * Deteksi MIME type dari file temporary.
   *
   * @param string $tmpPath Path file temporary.
   *
   * @return string MIME type.
   */
  private function detectMimeType(string $tmpPath): string {
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    if ($finfo === false) {
      return '';
    }

    $mimeType = finfo_file($finfo, $tmpPath);
    finfo_close($finfo);

    return is_string($mimeType) ? $mimeType : '';
  }

  /**
   * Menentukan target perangkat dari rasio gambar.
   *
   * @param int $width Lebar gambar.
   * @param int $height Tinggi gambar.
   *
   * @return string Target perangkat.
   */
  private function detectTargetDevice(int $width, int $height): string {
    $ratio = $height === 0 ? 1.0 : round($width / $height, 2);

    if ($ratio >= 1.5) {
      return 'desktop';
    }

    if ($ratio <= 0.6) {
      return 'mobile';
    }

    return 'tablet';
  }

  /**
   * Mengambil resolusi minimal untuk target perangkat.
   *
   * @param string $targetDevice Target perangkat.
   *
   * @return array{width: int, height: int} Resolusi minimal.
   */
  private function minimumResolutionFor(string $targetDevice): array {
    return self::MIN_RESOLUTION_BY_TARGET_DEVICE[$targetDevice]
      ?? self::MIN_RESOLUTION_BY_TARGET_DEVICE['desktop'];
  }

  /**
   * Normalisasi daftar ID tag.
   *
   * @param mixed $rawTags Input tag dari request.
   *
   * @return array<int, int>
   */
  private function normalizeTagIds(mixed $rawTags): array {
    if (is_string($rawTags)) {
      $decoded = json_decode($rawTags, true);
      $rawTags = is_array($decoded) ? $decoded : explode(',', $rawTags);
    }

    if (!is_array($rawTags)) {
      return [];
    }

    return array_values(array_unique(array_map('intval', $rawTags)));
  }

  /**
   * Membuat UUID v4.
   *
   * @return string UUID v4.
   */
  private function uuidV4(): string {
    $bytes = random_bytes(16);
    $bytes[6] = chr((ord($bytes[6]) & 0x0f) | 0x40);
    $bytes[8] = chr((ord($bytes[8]) & 0x3f) | 0x80);

    return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($bytes), 4));
  }
}
