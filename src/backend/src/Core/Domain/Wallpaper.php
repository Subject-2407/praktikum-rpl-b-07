<?php

/**
 * Domain Entity: Wallpaper
 *
 * Merepresentasikan wallpaper yang diunggah oleh contributor.
 * Mengandung metadata wallpaper dan status moderasi.
 *
 * @package Scapes\Core\Domain
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Domain;

/**
 * Kelas Wallpaper - Entitas domain untuk wallpaper.
 *
 * @class Wallpaper
 */
class Wallpaper {

  /**
   * ID unik wallpaper.
   *
   * @var int
   */
  private int $id;

  /**
   * ID contributor yang mengunggah wallpaper.
   *
   * @var int
   */
  private int $contributorId;

  /**
   * ID kategori wallpaper.
   *
   * @var int
   */
  private int $categoryId;

  /**
   * Judul wallpaper.
   *
   * @var string
   */
  private string $title;

  /**
   * Deskripsi wallpaper.
   *
   * @var string|null
   */
  private ?string $description;

  /**
   * Path lengkap file wallpaper.
   *
   * @var string
   */
  private string $filePath;

  /**
   * Nama file wallpaper.
   *
   * @var string
   */
  private string $fileName;

  /**
   * Ukuran file dalam KB.
   *
   * @var int
   */
  private int $fileSizeKb;

  /**
   * MIME type file.
   *
   * @var string
   */
  private string $mimeType;

  /**
   * Lebar gambar dalam piksel.
   *
   * @var int
   */
  private int $width;

  /**
   * Tinggi gambar dalam piksel.
   *
   * @var int
   */
  private int $height;

  /**
   * Status wallpaper (pending, approved, rejected, scheduled).
   *
   * @var string
   */
  private string $status;

  /**
   * Target perangkat wallpaper.
   *
   * @var string
   */
  private string $targetDevice;

  /**
   * Waktu publikasi terjadwal.
   *
   * @var string|null
   */
  private ?string $scheduledAt;

  /**
   * Waktu wallpaper dipublikasikan.
   *
   * @var string|null
   */
  private ?string $publishedAt;

  /**
   * Waktu wallpaper dibuat.
   *
   * @var string
   */
  private string $createdAt;

  /**
   * Waktu wallpaper terakhir diperbarui.
   *
   * @var string
   */
  private string $updatedAt;

  /**
   * Konstruktor Wallpaper.
   *
   * @param int $id ID unik wallpaper.
   * @param int $contributorId ID contributor.
   * @param int $categoryId ID kategori.
   * @param string $title Judul wallpaper.
   * @param string $filePath Path file.
   * @param string $fileName Nama file.
   * @param int $fileSizeKb Ukuran file.
   * @param string $mimeType MIME type.
   * @param int $width Lebar gambar.
   * @param int $height Tinggi gambar.
   * @param string $status Status moderasi.
   * @param string|null $description Deskripsi.
   * @param string|null $scheduledAt Waktu publikasi.
   * @param string|null $publishedAt Waktu dipublikasikan.
   * @param string $createdAt Waktu dibuat.
   * @param string $updatedAt Waktu diperbarui.
   * @param string $targetDevice Target perangkat.
   */
  public function __construct(
    int $id,
    int $contributorId,
    int $categoryId,
    string $title,
    string $filePath,
    string $fileName,
    int $fileSizeKb,
    string $mimeType,
    int $width,
    int $height,
    string $status = 'pending',
    ?string $description = null,
    ?string $scheduledAt = null,
    ?string $publishedAt = null,
    string $createdAt = '',
    string $updatedAt = '',
    string $targetDevice = 'desktop'
  ) {
    $this->id = $id;
    $this->contributorId = $contributorId;
    $this->categoryId = $categoryId;
    $this->title = $title;
    $this->filePath = $filePath;
    $this->fileName = $fileName;
    $this->fileSizeKb = $fileSizeKb;
    $this->mimeType = $mimeType;
    $this->width = $width;
    $this->height = $height;
    $this->status = $status;
    $this->targetDevice = $targetDevice;
    $this->description = $description;
    $this->scheduledAt = $scheduledAt;
    $this->publishedAt = $publishedAt;
    $this->createdAt = $createdAt ?: date('Y-m-d H:i:s');
    $this->updatedAt = $updatedAt ?: date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan ID wallpaper.
   *
   * @return int
   */
  public function getId(): int {
    return $this->id;
  }

  /**
   * Mendapatkan ID contributor.
   *
   * @return int
   */
  public function getContributorId(): int {
    return $this->contributorId;
  }

  /**
   * Mendapatkan ID kategori.
   *
   * @return int
   */
  public function getCategoryId(): int {
    return $this->categoryId;
  }

  /**
   * Mendapatkan judul wallpaper.
   *
   * @return string
   */
  public function getTitle(): string {
    return $this->title;
  }

  /**
   * Mendapatkan deskripsi wallpaper.
   *
   * @return string|null
   */
  public function getDescription(): ?string {
    return $this->description;
  }

  /**
   * Mendapatkan path file wallpaper.
   *
   * @return string
   */
  public function getFilePath(): string {
    return $this->filePath;
  }

  /**
   * Mendapatkan nama file wallpaper.
   *
   * @return string
   */
  public function getFileName(): string {
    return $this->fileName;
  }

  /**
   * Mendapatkan ukuran file dalam KB.
   *
   * @return int
   */
  public function getFileSizeKb(): int {
    return $this->fileSizeKb;
  }

  /**
   * Mendapatkan MIME type file.
   *
   * @return string
   */
  public function getMimeType(): string {
    return $this->mimeType;
  }

  /**
   * Mendapatkan lebar gambar.
   *
   * @return int
   */
  public function getWidth(): int {
    return $this->width;
  }

  /**
   * Mendapatkan tinggi gambar.
   *
   * @return int
   */
  public function getHeight(): int {
    return $this->height;
  }

  /**
   * Mendapatkan status wallpaper.
   *
   * @return string
   */
  public function getStatus(): string {
    return $this->status;
  }

  /**
   * Mendapatkan target perangkat wallpaper.
   *
   * @return string
   */
  public function getTargetDevice(): string {
    return $this->targetDevice;
  }

  /**
   * Mengatur status wallpaper.
   *
   * @param string $status Status baru.
   *
   * @return void
   */
  public function setStatus(string $status): void {
    $this->status = $status;
    $this->updatedAt = date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan waktu publikasi terjadwal.
   *
   * @return string|null
   */
  public function getScheduledAt(): ?string {
    return $this->scheduledAt;
  }

  /**
   * Mendapatkan waktu dipublikasikan.
   *
   * @return string|null
   */
  public function getPublishedAt(): ?string {
    return $this->publishedAt;
  }

  /**
   * Mendapatkan waktu dibuat.
   *
   * @return string
   */
  public function getCreatedAt(): string {
    return $this->createdAt;
  }

  /**
   * Mendapatkan waktu diperbarui.
   *
   * @return string
   */
  public function getUpdatedAt(): string {
    return $this->updatedAt;
  }

  /**
   * Mengecek apakah wallpaper belum di-approve.
   *
   * @return bool
   */
  public function isPending(): bool {
    return $this->status === 'pending';
  }

  /**
   * Mengecek apakah wallpaper sudah di-approve.
   *
   * @return bool
   */
  public function isApproved(): bool {
    return $this->status === 'approved';
  }

  /**
   * Mengecek apakah wallpaper ditolak.
   *
   * @return bool
   */
  public function isRejected(): bool {
    return $this->status === 'rejected';
  }

  /**
   * Mengecek apakah wallpaper terjadwal publikasi.
   *
   * @return bool
   */
  public function isScheduled(): bool {
    return $this->status === 'scheduled';
  }
}
