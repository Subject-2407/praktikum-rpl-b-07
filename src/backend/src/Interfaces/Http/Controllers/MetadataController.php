<?php

/**
 * Controller metadata.
 *
 * Controller ini melayani endpoint publik untuk sources, categories, dan
 * tags yang dipakai client Scapes.
 *
 * @package Scapes\Interfaces\Http\Controllers
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Metadata\ListCategoriesUseCase;
use Scapes\Application\UseCases\Metadata\ListSourcesUseCase;
use Scapes\Application\UseCases\Metadata\ListTagsUseCase;
use Scapes\Interfaces\Http\Response;

/**
 * Kelas MetadataController - Endpoint metadata publik.
 */
class MetadataController {

  /**
   * Use case daftar source.
   *
   * @var ListSourcesUseCase
   */
  private ListSourcesUseCase $sourcesUseCase;

  /**
   * Use case daftar kategori.
   *
   * @var ListCategoriesUseCase
   */
  private ListCategoriesUseCase $categoriesUseCase;

  /**
   * Use case daftar tag.
   *
   * @var ListTagsUseCase
   */
  private ListTagsUseCase $tagsUseCase;

  /**
   * Konstruktor MetadataController.
   *
   * @param ListSourcesUseCase $sourcesUseCase Use case source.
   * @param ListCategoriesUseCase $categoriesUseCase Use case kategori.
   * @param ListTagsUseCase $tagsUseCase Use case tag.
   */
  public function __construct(
    ListSourcesUseCase $sourcesUseCase,
    ListCategoriesUseCase $categoriesUseCase,
    ListTagsUseCase $tagsUseCase
  ) {
    $this->sourcesUseCase = $sourcesUseCase;
    $this->categoriesUseCase = $categoriesUseCase;
    $this->tagsUseCase = $tagsUseCase;
  }

  /**
   * GET /sources.
   *
   * @return array<string, mixed>
   */
  public function sources(): array {
    try {
      return Response::success(
        'Sources retrieved successfully.',
        $this->sourcesUseCase->execute()
      );
    } catch (\Throwable $e) {
      return Response::error('Internal server error.', 500);
    }
  }

  /**
   * GET /categories.
   *
   * @return array<string, mixed>
   */
  public function categories(): array {
    try {
      return Response::success(
        'Categories retrieved successfully.',
        $this->categoriesUseCase->execute()
      );
    } catch (\Throwable $e) {
      return Response::error('Internal server error.', 500);
    }
  }

  /**
   * GET /tags.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  public function tags(array $query): array {
    try {
      return Response::success(
        'Tags retrieved successfully.',
        $this->tagsUseCase->execute(
          isset($query['q']) ? (string) $query['q'] : null
        )
      );
    } catch (\Throwable $e) {
      return Response::error('Internal server error.', 500);
    }
  }
}
