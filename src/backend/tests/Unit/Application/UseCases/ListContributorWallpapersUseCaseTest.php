<?php

/**
 * Unit Tests untuk ListContributorWallpapersUseCase
 *
 * Menguji validasi pagination pada daftar wallpaper contributor.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Wallpaper\ListContributorWallpapersUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\WallpaperRepository;

class ListContributorWallpapersUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private ListContributorWallpapersUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->useCase = new ListContributorWallpapersUseCase($this->wallpaperRepository);
  }

  public function test_per_page_di_atas_100_ditolak(): void {
    $this->wallpaperRepository
      ->expects($this->never())
      ->method('listByContributor');

    try {
      $this->useCase->execute(1, ['per_page' => 101]);
      $this->fail('Expected ValidationException was not thrown.');
    } catch (ValidationException $e) {
      $this->assertSame('Validation failed.', $e->getMessage());
      $this->assertSame([
        'per_page' => ['The selected per_page is invalid.'],
      ], $e->getErrors());
    }
  }
}
