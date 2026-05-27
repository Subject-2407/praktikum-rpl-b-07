<?php

/**
 * Use case daftar sumber wallpaper.
 *
 * Mengambil metadata source aktif dari database tanpa menyimpan API key
 * provider eksternal di backend.
 *
 * @package Scapes\Application\UseCases\Metadata
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Metadata;

use Scapes\Infrastructure\Repository\ApiSourceRepository;

/**
 * Kelas ListSourcesUseCase - Daftar source aktif.
 */
class ListSourcesUseCase {

  /**
   * Repository sumber API.
   *
   * @var ApiSourceRepository
   */
  private ApiSourceRepository $sourceRepository;

  /**
   * Konstruktor ListSourcesUseCase.
   *
   * @param ApiSourceRepository $sourceRepository Repository source.
   */
  public function __construct(ApiSourceRepository $sourceRepository) {
    $this->sourceRepository = $sourceRepository;
  }

  /**
   * Mengambil semua source aktif.
   *
   * @return array<int, array<string, mixed>>
   */
  public function execute(): array {
    return $this->sourceRepository->findActive();
  }
}
