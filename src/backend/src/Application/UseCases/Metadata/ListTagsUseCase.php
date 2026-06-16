<?php

/**
 * Use case daftar tag.
 *
 * Mengambil semua tag yang tersedia dengan opsi filter keyword.
 *
 * @package Scapes\Application\UseCases\Metadata
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Metadata;

use Scapes\Infrastructure\Repository\TagRepository;

/**
 * Kelas ListTagsUseCase - Daftar tag.
 */
class ListTagsUseCase {

  /**
   * Repository tag.
   *
   * @var TagRepository
   */
  private TagRepository $tagRepository;

  /**
   * Konstruktor ListTagsUseCase.
   *
   * @param TagRepository $tagRepository Repository tag.
   */
  public function __construct(TagRepository $tagRepository) {
    $this->tagRepository = $tagRepository;
  }

  /**
   * Mengambil semua tag.
   *
   * @param string|null $keyword Keyword pencarian.
   * @param string $match Mode match.
   * @param int $limit Jumlah maksimal.
   *
   * @return array<int, array<string, mixed>>
   */
  public function execute(
    ?string $keyword = null,
    string $match = 'contains',
    int $limit = 100
  ): array {
    return $this->tagRepository->findAllAsArray($keyword, $match, $limit);
  }
}
