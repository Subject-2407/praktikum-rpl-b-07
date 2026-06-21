<?php

/**
 * Unit test rekomendasi search.
 *
 * Menguji prioritas rekomendasi berdasarkan source, kategori bawaan, dan tag
 * resmi internal Scapes.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\UseCases\Search\ListSearchRecommendationsUseCase;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\SearchAnalyticsRepository;
use Scapes\Infrastructure\Repository\TagRepository;

/**
 * Kelas ListSearchRecommendationsUseCaseTest.
 */
class ListSearchRecommendationsUseCaseTest extends TestCase {

  /**
   * Test: kategori bawaan yang match muncul sebelum rekomendasi user-keyword.
   */
  public function test_scapes_memprioritaskan_kategori_bawaan_yang_match(): void {
    $searchRepository = $this->createMock(SearchAnalyticsRepository::class);
    $categoryRepository = $this->createMock(CategoryRepository::class);
    $tagRepository = $this->createMock(TagRepository::class);

    $categoryRepository
      ->expects($this->once())
      ->method('findMatchingAsArray')
      ->with('minimal', 10)
      ->willReturn([
        [
          'id' => 1,
          'name' => 'Minimalist',
          'slug' => 'minimalist',
          'match_score' => 90,
        ],
      ]);

    $searchRepository
      ->expects($this->once())
      ->method('listUserKeywordRecommendations')
      ->with('minimal', 10)
      ->willReturn([
        [
          'type' => 'user_keyword_category',
          'label' => 'Minimal Desk',
          'value' => 'minimal-desk',
          'score' => 42.0,
          'match_reason' => 'trending',
        ],
      ]);

    $searchRepository
      ->expects($this->once())
      ->method('latestTrendDate')
      ->willReturn('2026-06-16');

    $useCase = new ListSearchRecommendationsUseCase(
      $searchRepository,
      $categoryRepository,
      $tagRepository
    );

    $result = $useCase->execute(['q' => 'minimal', 'source_slug' => 'scapes']);

    $this->assertSame('system_category', $result['data'][0]['type']);
    $this->assertSame('minimalist', $result['data'][0]['value']);
    $this->assertSame('user_keyword_category', $result['data'][1]['type']);
  }

  /**
   * Test: source external tidak memakai kategori/tag internal.
   */
  public function test_external_source_fallback_ke_rekomendasi_normal(): void {
    $searchRepository = $this->createMock(SearchAnalyticsRepository::class);
    $categoryRepository = $this->createMock(CategoryRepository::class);
    $tagRepository = $this->createMock(TagRepository::class);

    $categoryRepository
      ->expects($this->never())
      ->method('findMatchingAsArray');
    $tagRepository
      ->expects($this->never())
      ->method('findAllAsArray');

    $searchRepository
      ->expects($this->once())
      ->method('listUserKeywordRecommendations')
      ->with('minimal', 10)
      ->willReturn([
        [
          'type' => 'user_keyword_category',
          'label' => 'Minimal Desk',
          'value' => 'minimal-desk',
          'score' => 42.0,
          'match_reason' => 'trending',
        ],
      ]);

    $searchRepository
      ->expects($this->once())
      ->method('latestTrendDate')
      ->willReturn('2026-06-16');

    $useCase = new ListSearchRecommendationsUseCase(
      $searchRepository,
      $categoryRepository,
      $tagRepository
    );

    $result = $useCase->execute(['q' => 'minimal', 'source_slug' => 'unsplash']);

    $this->assertFalse($result['meta']['internal_metadata_enabled']);
    $this->assertSame('user_keyword_category', $result['data'][0]['type']);
  }

  /**
   * Test: tag resmi diprioritaskan saat query diawali #.
   */
  public function test_scapes_memprioritaskan_tag_resmi_untuk_hash_query(): void {
    $searchRepository = $this->createMock(SearchAnalyticsRepository::class);
    $categoryRepository = $this->createMock(CategoryRepository::class);
    $tagRepository = $this->createMock(TagRepository::class);

    $tagRepository
      ->expects($this->once())
      ->method('findAllAsArray')
      ->with('#color', 'prefix', 10)
      ->willReturn([
        ['id' => 7, 'name' => 'colorful', 'slug' => 'colorful'],
      ]);

    $searchRepository
      ->expects($this->once())
      ->method('listUserKeywordRecommendations')
      ->with('color', 10)
      ->willReturn([]);

    $searchRepository
      ->expects($this->once())
      ->method('latestTrendDate')
      ->willReturn('2026-06-16');

    $useCase = new ListSearchRecommendationsUseCase(
      $searchRepository,
      $categoryRepository,
      $tagRepository
    );

    $result = $useCase->execute(['q' => '#color', 'source_slug' => 'scapes']);

    $this->assertSame('tag', $result['data'][0]['type']);
    $this->assertSame('#colorful', $result['data'][0]['label']);
  }
}
