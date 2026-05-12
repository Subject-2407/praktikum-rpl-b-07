<?php

/**
 * Unit Tests untuk UploadWallpaperUseCase
 *
 * Menguji fungsi upload wallpaper dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Core\Domain\Category;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\CategoryRepository;

class UploadWallpaperUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private CategoryRepository $categoryRepository;
  private UploadWallpaperUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->categoryRepository = $this->createMock(CategoryRepository::class);
    $this->useCase = new UploadWallpaperUseCase(
      $this->wallpaperRepository,
      $this->categoryRepository
    );
  }

  /**
   * Test: Upload wallpaper dengan data valid berhasil
   * Arrange: Data wallpaper lengkap dan valid
   * Act: Execute use case
   * Assert: Wallpaper disimpan dengan status pending
   */
  public function test_upload_wallpaper_dengan_data_valid_berhasil(): void {
    // Arrange
    $contributorId = 1;
    $categoryId = 1;
    $category = new Category($categoryId, 'Minimalist', 'minimalist');
    $title = 'Mountain View';
    $filePath = '/storage/wallpapers/mountain.jpg';
    $fileName = 'mountain.jpg';
    $fileSizeKb = 512;
    $mimeType = 'image/jpeg';
    $width = 1920;
    $height = 1080;
    $description = 'Beautiful mountain landscape';

    $expectedWallpaper = new Wallpaper(
      1,
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
      $description
    );

    $this->categoryRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($categoryId)
      ->willReturn($category);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('save')
      ->willReturn($expectedWallpaper);

    // Act
    $result = $this->useCase->execute(
      $contributorId,
      $categoryId,
      $title,
      $filePath,
      $fileName,
      $fileSizeKb,
      $mimeType,
      $width,
      $height,
      $description
    );

    // Assert
    $this->assertInstanceOf(Wallpaper::class, $result);
    $this->assertEquals($title, $result->getTitle());
    $this->assertEquals('pending', $result->getStatus());
    $this->assertTrue($result->isPending());
  }

  /**
   * Test: Upload gagal karena title kosong
   * Arrange: Title tidak diisi
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_upload_dengan_title_kosong_gagal(): void {
    // Arrange
    $title = '';  // kosong

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Judul wallpaper tidak boleh kosong');

    // Act
    $this->useCase->execute(1, 1, $title, '/path', 'file.jpg', 512, 'image/jpeg', 1920, 1080);
  }

  /**
   * Test: Upload gagal karena ukuran file terlalu besar
   * Arrange: File size melebihi 10 MB
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_upload_dengan_ukuran_file_terlalu_besar_gagal(): void {
    // Arrange
    $fileSizeKb = 10241;  // 10 MB + 1 KB

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Ukuran file maksimal 10 MB');

    // Act
    $this->useCase->execute(1, 1, 'Title', '/path', 'file.jpg', $fileSizeKb, 'image/jpeg', 1920, 1080);
  }

  /**
   * Test: Upload gagal karena format file tidak didukung
   * Arrange: MIME type bukan image/jpeg, image/png, atau image/webp
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_upload_dengan_format_file_tidak_didukung_gagal(): void {
    // Arrange
    $mimeType = 'image/gif';  // tidak didukung

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Format file hanya mendukung JPEG, PNG, atau WebP');

    // Act
    $this->useCase->execute(1, 1, 'Title', '/path', 'file.gif', 512, $mimeType, 1920, 1080);
  }

  /**
   * Test: Upload gagal karena dimensi gambar terlalu kecil
   * Arrange: Width atau height kurang dari minimum
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_upload_dengan_dimensi_terlalu_kecil_gagal(): void {
    // Arrange

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Dimensi gambar minimal 1920x1080 px');

    // Act
    $this->useCase->execute(1, 1, 'Title', '/path', 'file.jpg', 512, 'image/jpeg', 1000, 800);
  }

  /**
   * Test: Upload gagal karena kategori tidak ditemukan
   * Arrange: Category ID tidak ada
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_upload_dengan_kategori_tidak_ada_gagal(): void {
    // Arrange
    $this->categoryRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->willReturn(null);

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Kategori tidak ditemukan');

    // Act
    $this->useCase->execute(1, 999, 'Title', '/path', 'file.jpg', 512, 'image/jpeg', 1920, 1080);
  }
}
