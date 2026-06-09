<?php

/**
 * Unit Tests untuk ListModerationWallpapersUseCase
 *
 * Menguji validasi pagination pada queue moderasi wallpaper.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Moderation\ListModerationWallpapersUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

class ListModerationWallpapersUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private ListModerationWallpapersUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->useCase = new ListModerationWallpapersUseCase($this->wallpaperRepository);
  }

  public function test_per_page_di_atas_100_ditolak(): void {
    $this->wallpaperRepository
      ->expects($this->never())
      ->method('listForModeration');

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
