<?php

/**
 * Use case daftar kategori.
 *
 * Mengambil semua kategori wallpaper yang tersedia.
 *
 * @package Scapes\Application\UseCases\Metadata
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Metadata;

use Scapes\Infrastructure\Repository\CategoryRepository;

/**
 * Kelas ListCategoriesUseCase - Daftar kategori.
 */
class ListCategoriesUseCase {

  /**
   * Repository kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Konstruktor ListCategoriesUseCase.
   *
   * @param CategoryRepository $categoryRepository Repository kategori.
   */
  public function __construct(CategoryRepository $categoryRepository) {
    $this->categoryRepository = $categoryRepository;
  }

  /**
   * Mengambil semua kategori.
   *
   * @return array<int, array<string, mixed>>
   */
  public function execute(): array {
    return $this->categoryRepository->findAllAsArray();
  }
}
