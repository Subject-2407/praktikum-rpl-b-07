<?php

/**
 * Use case update metadata wallpaper.
 *
 * Contributor hanya dapat mengubah wallpaper miliknya selama wallpaper
 * belum berstatus approved.
 *
 * @package Scapes\Application\UseCases\Wallpaper
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\UseCases\Wallpaper;

use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Repository\CategoryRepository;
use Scapes\Infrastructure\Repository\TagRepository;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Storage\FileStorage;

/**
 * Kelas UpdateWallpaperUseCase - Update metadata wallpaper.
 */
class UpdateWallpaperUseCase {

  /**
   * Repository wallpaper.
   *
   * @var WallpaperRepository
   */
  private WallpaperRepository $wallpaperRepository;

  /**
   * Repository kategori.
   *
   * @var CategoryRepository
   */
  private CategoryRepository $categoryRepository;

  /**
   * Repository tag.
   *
   * @var TagRepository
   */
  private TagRepository $tagRepository;

  /**
   * Storage file.
   *
   * @var FileStorage|null
   */
  private ?FileStorage $storage;

  /**
   * Konstruktor UpdateWallpaperUseCase.
   *
   * @param WallpaperRepository $wallpaperRepository Repository wallpaper.
   * @param CategoryRepository $categoryRepository Repository kategori.
   * @param TagRepository $tagRepository Repository tag.
   * @param FileStorage|null $storage Storage file.
   */
  public function __construct(
    WallpaperRepository $wallpaperRepository,
    CategoryRepository $categoryRepository,
    TagRepository $tagRepository,
    ?FileStorage $storage = null
  ) {
    $this->wallpaperRepository = $wallpaperRepository;
    $this->categoryRepository = $categoryRepository;
    $this->tagRepository = $tagRepository;
    $this->storage = $storage;
  }

  /**
   * Memperbarui metadata wallpaper.
   *
   * @param int|string $wallpaperId ID wallpaper.
   * @param int $contributorId ID contributor.
   * @param array<string, mixed> $data Data request.
   *
   * @return array<string, mixed> Detail wallpaper terbaru.
   */
  public function execute(
    int|string $wallpaperId,
    int $contributorId,
    array $data
  ): array {
    $wallpaper = $this->wallpaperRepository->findDetailedById($wallpaperId);
    if ($wallpaper === null) {
      throw new NotFoundException('Resource not found.');
    }

    if ((int) $wallpaper['contributor_id'] !== $contributorId) {
      throw new AuthorizationException(
        'Forbidden. You do not have access to this resource.'
      );
    }

    if ((string) $wallpaper['status'] === 'approved') {
      throw new AuthorizationException(
        'Approved wallpapers cannot be updated.'
      );
    }

    [$fields, $tagIds, $replaceTags, $tagTexts, $replaceProposals] =
      $this->validateData($data);
    $oldPath = (string) $wallpaper['file_path'];
    $oldThumbnailPath = (string) $wallpaper['thumbnail_path'];
    $newPath = null;
    $newThumbnailPath = null;

    if (
      isset($fields['category_id'])
      && (int) $fields['category_id'] !== (int) $wallpaper['category_id']
      && $this->storage !== null
    ) {
      $targetFolder = $this->storageFolderForStatus(
        (string) $wallpaper['status'],
        (int) $fields['category_id']
      );
      $newPath = $this->storage->move($oldPath, $targetFolder);
      $newThumbnailPath = $this->storage->move(
        $oldThumbnailPath,
        $this->thumbnailFolderForStatus(
          (string) $wallpaper['status'],
          (int) $fields['category_id']
        )
      );
    }

    try {
      $this->wallpaperRepository->transaction(
        function () use (
          $wallpaperId,
          $fields,
          $tagIds,
          $replaceTags,
          $tagTexts,
          $replaceProposals
        ): void {
          $this->wallpaperRepository->updateMetadata($wallpaperId, $fields);
          if ($replaceTags) {
            $this->tagRepository->replaceWallpaperTags($wallpaperId, $tagIds);
          }
          if ($replaceProposals) {
            $this->tagRepository->replaceWallpaperTagProposals(
              $wallpaperId,
              $tagTexts
            );
          }
        }
      );
    } catch (\Throwable $e) {
      if ($newPath !== null && $this->storage !== null) {
        $this->storage->move(
          $newPath,
          $this->storageFolderForStatus(
            (string) $wallpaper['status'],
            (int) $wallpaper['category_id']
          )
        );
      }
      if ($newThumbnailPath !== null && $this->storage !== null) {
        $this->storage->move(
          $newThumbnailPath,
          $this->thumbnailFolderForStatus(
            (string) $wallpaper['status'],
            (int) $wallpaper['category_id']
          )
        );
      }
      throw $e;
    }

    return $this->wallpaperRepository->findDetailedById($wallpaperId) ?? [];
  }

  /**
   * Validasi data update.
   *
   * @param array<string, mixed> $data Data request.
   *
   * @return array{
   *   0: array<string, mixed>,
   *   1: array<int, int>,
   *   2: bool,
   *   3: array<int, string>,
   *   4: bool
   * }
   */
  private function validateData(array $data): array {
    $errors = [];
    $fields = [];
    $replaceTags = array_key_exists('tags', $data);
    $replaceProposals = array_key_exists('tag_text', $data);
    $tagIds = [];
    $tagTexts = [];

    if (array_key_exists('title', $data)) {
      $title = trim((string) $data['title']);
      if ($title === '') {
        $errors['title'][] = 'The title field must not be empty.';
      } elseif (strlen($title) > 255) {
        $errors['title'][] = 'The title field must not exceed 255 characters.';
      } else {
        $fields['title'] = $title;
      }
    }

    if (array_key_exists('description', $data)) {
      $description = trim((string) $data['description']);
      $fields['description'] = $description !== '' ? $description : null;
    }

    if (array_key_exists('category_id', $data)) {
      $categoryId = (int) $data['category_id'];
      if ($categoryId <= 0 || $this->categoryRepository->findByIdEntity($categoryId) === null) {
        $errors['category_id'][] = 'The selected category_id is invalid.';
      } else {
        $fields['category_id'] = $categoryId;
      }
    }

    if ($replaceTags) {
      $tagIds = $this->normalizeTagIds($data['tags']);
      if (!$this->tagRepository->allIdsExist($tagIds)) {
        $errors['tags'][] = 'One or more selected tags are invalid.';
      }
    }

    if ($replaceProposals) {
      $tagTexts = $this->parseTagText($data['tag_text'], $errors);
    }

    if ($fields === [] && !$replaceTags && !$replaceProposals) {
      $errors['body'][] = 'At least one field must be provided.';
    }

    if ($errors !== []) {
      throw new ValidationException('Validation failed.', 0, $errors);
    }

    return [$fields, $tagIds, $replaceTags, $tagTexts, $replaceProposals];
  }

  /**
   * Normalisasi daftar ID tag.
   *
   * @param mixed $rawTags Input tag.
   *
   * @return array<int, int>
   */
  private function normalizeTagIds(mixed $rawTags): array {
    if (is_string($rawTags)) {
      $decoded = json_decode($rawTags, true);
      $rawTags = is_array($decoded) ? $decoded : explode(',', $rawTags);
    }

    if (!is_array($rawTags)) {
      return [];
    }

    return array_values(array_unique(array_map('intval', $rawTags)));
  }

  /**
   * Memecah input tag bebas contributor.
   *
   * @param mixed $rawTagText Input tag_text.
   * @param array<string, array<int, string>> $errors Error validasi.
   *
   * @return array<int, string>
   */
  private function parseTagText(mixed $rawTagText, array &$errors): array {
    if ($rawTagText === null || trim((string) $rawTagText) === '') {
      return [];
    }

    $tokens = preg_split('/\s+/', trim((string) $rawTagText)) ?: [];
    $tags = [];

    foreach ($tokens as $token) {
      if (!str_starts_with($token, '#')) {
        $errors['tag_text'][] = 'Each tag must start with #.';
        continue;
      }

      $slug = $this->tagRepository->normalizeTagSlug($token);
      if ($slug === '') {
        $errors['tag_text'][] = 'Each tag must contain letters or numbers.';
        continue;
      }

      if (strlen($slug) > 100) {
        $errors['tag_text'][] = 'Each tag must not exceed 100 characters.';
        continue;
      }

      $tags[$slug] = $slug;
    }

    if (count($tags) > 20) {
      $errors['tag_text'][] = 'The tag_text field must not contain more than 20 tags.';
    }

    return array_values($tags);
  }

  /**
   * Folder storage file asli berdasarkan status.
   *
   * @param string $status Status wallpaper.
   * @param int $categoryId ID kategori.
   *
   * @return string Folder relatif di bawah wallpapers.
   */
  private function storageFolderForStatus(string $status, int $categoryId): string {
    if ($status === 'approved') {
      return (string) $categoryId;
    }

    return 'pending' . DIRECTORY_SEPARATOR . (string) $categoryId;
  }

  /**
   * Folder storage thumbnail berdasarkan status.
   *
   * @param string $status Status wallpaper.
   * @param int $categoryId ID kategori.
   *
   * @return string Folder relatif thumbnail di bawah wallpapers.
   */
  private function thumbnailFolderForStatus(string $status, int $categoryId): string {
    if ($status === 'approved') {
      return (string) $categoryId . DIRECTORY_SEPARATOR . 'thumbnails';
    }

    return 'pending'
      . DIRECTORY_SEPARATOR
      . (string) $categoryId
      . DIRECTORY_SEPARATOR
      . 'thumbnails';
  }
}
