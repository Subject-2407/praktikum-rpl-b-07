<?php

/**
 * Unit Tests untuk ListPublicWallpapersUseCase
 *
 * Menguji validasi pagination pada daftar wallpaper publik.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Wallpaper\ListPublicWallpapersUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

class ListPublicWallpapersUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private ListPublicWallpapersUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->useCase = new ListPublicWallpapersUseCase($this->wallpaperRepository);
  }

  public function test_per_page_di_atas_100_ditolak(): void {
    $this->wallpaperRepository
      ->expects($this->never())
      ->method('listPublic');

    try {
      $this->useCase->execute(['per_page' => 101]);
      $this->fail('Expected ValidationException was not thrown.');
    } catch (ValidationException $e) {
      $this->assertSame('Validation failed.', $e->getMessage());
      $this->assertSame([
        'per_page' => ['The selected per_page is invalid.'],
      ], $e->getErrors());
    }
  }
}
