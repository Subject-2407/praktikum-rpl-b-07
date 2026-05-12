<?php

/**
 * Unit Tests untuk DeleteWallpaperUseCase
 *
 * Menguji fungsi delete wallpaper dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

class DeleteWallpaperUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private DeleteWallpaperUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->useCase = new DeleteWallpaperUseCase($this->wallpaperRepository);
  }

  /**
   * Test: Delete wallpaper berhasil oleh pemilik
   * Arrange: Contributor menghapus wallpaper miliknya
   * Act: Execute use case
   * Assert: Wallpaper berhasil dihapus
   */
  public function test_delete_wallpaper_oleh_pemilik_berhasil(): void {
    // Arrange
    $wallpaperId = 1;
    $contributorId = 1;
    $wallpaper = new Wallpaper(
      $wallpaperId,
      $contributorId,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('delete')
      ->with($wallpaperId);

    // Act & Assert
    $this->useCase->execute($wallpaperId, $contributorId, 'contributor');
  }

  /**
   * Test: Delete wallpaper berhasil oleh admin
   * Arrange: Admin menghapus wallpaper milik contributor lain
   * Act: Execute use case
   * Assert: Wallpaper berhasil dihapus
   */
  public function test_delete_wallpaper_oleh_admin_berhasil(): void {
    // Arrange
    $wallpaperId = 1;
    $adminId = 100;
    $contributorId = 1;  // beda contributor
    $wallpaper = new Wallpaper(
      $wallpaperId,
      $contributorId,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('delete')
      ->with($wallpaperId);

    // Act & Assert
    $this->useCase->execute($wallpaperId, $adminId, 'admin');
  }

  /**
   * Test: Delete gagal karena wallpaper tidak ditemukan
   * Arrange: Wallpaper ID tidak ada
   * Act: Execute use case
   * Assert: NotFoundException dilempar
   */
  public function test_delete_wallpaper_tidak_ada_gagal(): void {
    // Arrange
    $wallpaperId = 999;

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn(null);

    // Assert
    $this->expectException(NotFoundException::class);
    $this->expectExceptionMessage('Wallpaper tidak ditemukan');

    // Act
    $this->useCase->execute($wallpaperId, 1, 'contributor');
  }

  /**
   * Test: Delete gagal karena tidak punya permission
   * Arrange: Contributor mencoba hapus wallpaper orang lain
   * Act: Execute use case
   * Assert: AuthorizationException dilempar
   */
  public function test_delete_wallpaper_tanpa_permission_gagal(): void {
    // Arrange
    $wallpaperId = 1;
    $requesterId = 2;  // beda user
    $ownerId = 1;
    $wallpaper = new Wallpaper(
      $wallpaperId,
      $ownerId,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    // Assert
    $this->expectException(AuthorizationException::class);
    $this->expectExceptionMessage('Anda tidak memiliki permission untuk menghapus wallpaper ini');

    // Act
    $this->useCase->execute($wallpaperId, $requesterId, 'contributor');
  }
}
