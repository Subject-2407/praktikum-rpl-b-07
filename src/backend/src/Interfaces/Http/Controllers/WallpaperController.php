<?php

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetWallpaperStatusUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;

/**
 * Pengontrol untuk menangani operasi wallpaper.
 * Mengelola upload, penghapusan, dan status tracking wallpaper.
 */
class WallpaperController
{
  private UploadWallpaperUseCase $uploadUseCase;
  private DeleteWallpaperUseCase $deleteUseCase;
  private GetWallpaperStatusUseCase $getStatusUseCase;

  /**
   * Inisialisasi pengontrol wallpaper dengan use case.
   *
   * @param UploadWallpaperUseCase $uploadUseCase Use case upload
   * @param DeleteWallpaperUseCase $deleteUseCase Use case delete
   * @param GetWallpaperStatusUseCase $getStatusUseCase Use case get status
   */
  public function __construct(
    UploadWallpaperUseCase $uploadUseCase,
    DeleteWallpaperUseCase $deleteUseCase,
    GetWallpaperStatusUseCase $getStatusUseCase
  ) {
    $this->uploadUseCase = $uploadUseCase;
    $this->deleteUseCase = $deleteUseCase;
    $this->getStatusUseCase = $getStatusUseCase;
  }

  /**
   * Upload wallpaper baru.
   * POST /wallpaper/upload
   * Body: FormData dengan file dan metadata
   *
   * @param array<string, mixed> $data Data permintaan
   * @param array<string, mixed> $file Data file dari $_FILES
   * @return array<string, mixed> Respons JSON
   */
  public function upload(array $data, array $file): array
  {
    try {
      // Validasi input dasar
      if (empty($data['title'])) {
        return $this->errorResponse('Wallpaper title is required', 400);
      }

      if (empty($data['category_id'])) {
        return $this->errorResponse('Wallpaper category is required', 400);
      }

      if (empty($data['contributor_id'])) {
        return $this->errorResponse(
          'Contributor ID not found',
          401
        );
      }

      if (empty($file) || !isset($file['tmp_name'])) {
        return $this->errorResponse('Wallpaper file must be uploaded', 400);
      }

      $title = trim($data['title']);
      $description = !empty($data['description']) ?
        trim($data['description']) : null;
      $categoryId = (int)$data['category_id'];
      $contributorId = (int)$data['contributor_id'];
      $tmpFile = $file['tmp_name'];

      // Jalankan use case
      $wallpaper = $this->uploadUseCase->execute(
        $contributorId,
        $title,
        $tmpFile,
        $categoryId,
        $description
      );

      return $this->successResponse(
        [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'category_id' => $wallpaper->getCategoryId(),
          'contributor_id' => $wallpaper->getContributorId(),
          'uploaded_at' => $wallpaper->getUploadedAt(),
        ],
        'Wallpaper uploaded successfully. Waiting for moderation.',
        201
      );
    } catch (ValidationException $e) {
      return $this->errorResponse($e->getMessage(), 400);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Hapus wallpaper.
   * DELETE /wallpaper/{id}
   * Body: {"wallpaper_id": 1, "user_id": 1}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function delete(array $data): array
  {
    try {
      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('Wallpaper ID is required', 400);
      }

      if (empty($data['user_id'])) {
        return $this->errorResponse('User not authenticated', 401);
      }

      $wallpaperId = (int)$data['wallpaper_id'];
      $userId = (int)$data['user_id'];

      // Jalankan use case
      $this->deleteUseCase->execute($wallpaperId, $userId);

      return $this->successResponse(
        [],
        'Wallpaper deleted successfully',
        200
      );
    } catch (NotFoundException $e) {
      return $this->errorResponse($e->getMessage(), 404);
    } catch (AuthorizationException $e) {
      return $this->errorResponse($e->getMessage(), 403);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Dapatkan status dan detail wallpaper.
   * GET /wallpaper/{id}
   * Body: {"wallpaper_id": 1}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function getStatus(array $data): array
  {
    try {
      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('Wallpaper ID is required', 400);
      }

      $wallpaperId = (int)$data['wallpaper_id'];

      // Jalankan use case
      $wallpaper = $this->getStatusUseCase->execute($wallpaperId);

      return $this->successResponse(
        [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'width' => $wallpaper->getWidth(),
          'height' => $wallpaper->getHeight(),
          'size_kb' => $wallpaper->getSizeKb(),
          'category_id' => $wallpaper->getCategoryId(),
          'contributor_id' => $wallpaper->getContributorId(),
          'description' => $wallpaper->getDescription(),
          'uploaded_at' => $wallpaper->getUploadedAt(),
          'updated_at' => $wallpaper->getUpdatedAt(),
        ],
        'Wallpaper details retrieved successfully',
        200
      );
    } catch (NotFoundException $e) {
      return $this->errorResponse($e->getMessage(), 404);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Dapatkan semua wallpaper kontributor.
   * GET /wallpaper/contributor/{contributor_id}
   * Body: {"contributor_id": 1}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function getAllByContributor(array $data): array
  {
    try {
      if (empty($data['contributor_id'])) {
        return $this->errorResponse(
          'Contributor ID is required',
          400
        );
      }

      $contributorId = (int)$data['contributor_id'];

      // Jalankan use case
      $wallpapers = $this->getStatusUseCase->executeGetAll(
        $contributorId
      );

      $wallpaperData = [];
      foreach ($wallpapers as $wallpaper) {
        $wallpaperData[] = [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'category_id' => $wallpaper->getCategoryId(),
          'uploaded_at' => $wallpaper->getUploadedAt(),
        ];
      }

      return $this->successResponse(
        ['wallpapers' => $wallpaperData],
        'Contributor wallpapers retrieved successfully',
        200
      );
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Format respons sukses.
   *
   * @param array<string, mixed> $data Data respons
   * @param string $message Pesan sukses
   * @param int $statusCode Kode HTTP
   * @return array<string, mixed> Respons JSON
   */
  private function successResponse(
    array $data,
    string $message,
    int $statusCode
  ): array {
    return [
      'success' => true,
      'status_code' => $statusCode,
      'message' => $message,
      'data' => $data,
    ];
  }

  /**
   * Format respons kesalahan.
   *
   * @param string $message Pesan kesalahan
   * @param int $statusCode Kode HTTP
   * @return array<string, mixed> Respons JSON
   */
  private function errorResponse(string $message, int $statusCode): array
  {
    return [
      'success' => false,
      'status_code' => $statusCode,
      'message' => $message,
      'data' => [],
    ];
  }
}
