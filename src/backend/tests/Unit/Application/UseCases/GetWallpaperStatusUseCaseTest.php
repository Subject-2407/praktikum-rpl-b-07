<?php

/**
 * Unit Tests untuk GetWallpaperStatusUseCase
 *
 * Menguji fungsi get status wallpaper dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Wallpaper\GetWallpaperStatusUseCase;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

class GetWallpaperStatusUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private GetWallpaperStatusUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->useCase = new GetWallpaperStatusUseCase($this->wallpaperRepository);
  }

  /**
   * Test: Get status wallpaper berhasil
   * Arrange: Wallpaper ada di database
   * Act: Execute use case
   * Assert: Wallpaper entity dikembalikan
   */
  public function test_get_wallpaper_status_berhasil(): void {
    // Arrange
    $wallpaperId = 1;
    $wallpaper = new Wallpaper(
      $wallpaperId,
      1,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080,
      'approved'
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    // Act
    $result = $this->useCase->execute($wallpaperId);

    // Assert
    $this->assertInstanceOf(Wallpaper::class, $result);
    $this->assertEquals('Mountain View', $result->getTitle());
    $this->assertEquals('approved', $result->getStatus());
  }

  /**
   * Test: Get status gagal karena wallpaper tidak ditemukan
   * Arrange: Wallpaper ID tidak ada
   * Act: Execute use case
   * Assert: NotFoundException dilempar
   */
  public function test_get_wallpaper_status_tidak_ada_gagal(): void {
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
    $this->useCase->execute($wallpaperId);
  }

  /**
   * Test: Get semua wallpaper dari contributor berhasil
   * Arrange: Contributor memiliki beberapa wallpaper
   * Act: Execute use case
   * Assert: Array dari Wallpaper dikembalikan
   */
  public function test_get_all_wallpaper_by_contributor_berhasil(): void {
    // Arrange
    $contributorId = 1;
    $wallpapers = [
      new Wallpaper(1, $contributorId, 1, 'Mountain', '/p1', 'f1.jpg', 512, 'image/jpeg', 1920, 1080, 'pending'),
      new Wallpaper(2, $contributorId, 1, 'Forest', '/p2', 'f2.jpg', 512, 'image/jpeg', 1920, 1080, 'approved'),
    ];

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByContributorId')
      ->with($contributorId)
      ->willReturn($wallpapers);

    // Act
    $results = $this->useCase->getAllByContributor($contributorId);

    // Assert
    $this->assertIsArray($results);
    $this->assertCount(2, $results);
    $this->assertEquals('Mountain', $results[0]->getTitle());
    $this->assertEquals('Forest', $results[1]->getTitle());
  }
}
