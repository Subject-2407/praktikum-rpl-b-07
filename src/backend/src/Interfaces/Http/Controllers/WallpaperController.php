<?php

/**
 * Controller wallpaper.
 *
 * Controller ini menangani endpoint publik dan contributor untuk resource
 * wallpaper sesuai api-contract.md.
 *
 * @package Scapes\Interfaces\Http\Controllers
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetPublicWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\ListContributorWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\ListPublicWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\UpdateWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Core\Exceptions\AuthorizationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Interfaces\Http\Request;
use Scapes\Interfaces\Http\Resources\WallpaperResource;
use Scapes\Interfaces\Http\Response;

/**
 * Kelas WallpaperController - Endpoint wallpaper publik dan contributor.
 */
class WallpaperController {

  /**
   * Use case daftar publik.
   *
   * @var ListPublicWallpapersUseCase
   */
  private ListPublicWallpapersUseCase $listPublicUseCase;

  /**
   * Use case detail publik.
   *
   * @var GetPublicWallpaperUseCase
   */
  private GetPublicWallpaperUseCase $getPublicUseCase;

  /**
   * Use case daftar contributor.
   *
   * @var ListContributorWallpapersUseCase
   */
  private ListContributorWallpapersUseCase $listContributorUseCase;

  /**
   * Use case upload.
   *
   * @var UploadWallpaperUseCase
   */
  private UploadWallpaperUseCase $uploadUseCase;

  /**
   * Use case update.
   *
   * @var UpdateWallpaperUseCase
   */
  private UpdateWallpaperUseCase $updateUseCase;

  /**
   * Use case delete.
   *
   * @var DeleteWallpaperUseCase
   */
  private DeleteWallpaperUseCase $deleteUseCase;

  /**
   * Konstruktor WallpaperController.
   *
   * @param ListPublicWallpapersUseCase $listPublicUseCase Use case publik.
   * @param GetPublicWallpaperUseCase $getPublicUseCase Use case detail.
   * @param ListContributorWallpapersUseCase $listContributorUseCase Use case mine.
   * @param UploadWallpaperUseCase $uploadUseCase Use case upload.
   * @param UpdateWallpaperUseCase $updateUseCase Use case update.
   * @param DeleteWallpaperUseCase $deleteUseCase Use case delete.
   */
  public function __construct(
    ListPublicWallpapersUseCase $listPublicUseCase,
    GetPublicWallpaperUseCase $getPublicUseCase,
    ListContributorWallpapersUseCase $listContributorUseCase,
    UploadWallpaperUseCase $uploadUseCase,
    UpdateWallpaperUseCase $updateUseCase,
    DeleteWallpaperUseCase $deleteUseCase
  ) {
    $this->listPublicUseCase = $listPublicUseCase;
    $this->getPublicUseCase = $getPublicUseCase;
    $this->listContributorUseCase = $listContributorUseCase;
    $this->uploadUseCase = $uploadUseCase;
    $this->updateUseCase = $updateUseCase;
    $this->deleteUseCase = $deleteUseCase;
  }

  /**
   * GET /wallpapers.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  public function index(array $query): array {
    try {
      $result = $this->listPublicUseCase->execute($query);
      $baseUrl = Request::baseUrl();
      $data = array_map(
        fn (array $wallpaper): array => WallpaperResource::public(
          $wallpaper,
          $baseUrl
        ),
        $result['data']
      );

      return Response::success(
        'Wallpapers retrieved successfully.',
        $data,
        200,
        $result['meta']
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * GET /wallpapers/{id}.
   *
   * @param string $id ID wallpaper.
   *
   * @return array<string, mixed>
   */
  public function show(string $id): array {
    try {
      $wallpaper = $this->getPublicUseCase->execute($id);

      return Response::success(
        'Wallpaper retrieved successfully.',
        WallpaperResource::public($wallpaper, Request::baseUrl(), true)
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * GET /me/wallpapers.
   *
   * @param array<string, mixed> $query Query string.
   * @param array<string, mixed> $authUser User login.
   *
   * @return array<string, mixed>
   */
  public function mine(array $query, array $authUser): array {
    try {
      $result = $this->listContributorUseCase->execute(
        (int) $authUser['user_id'],
        $query
      );
      $baseUrl = Request::baseUrl();
      $data = array_map(
        fn (array $wallpaper): array => WallpaperResource::contributor(
          $wallpaper,
          $baseUrl
        ),
        $result['data']
      );

      return Response::success(
        'Your wallpapers retrieved successfully.',
        $data,
        200,
        $result['meta']
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * POST /me/wallpapers.
   *
   * @param array<string, mixed> $data Form request.
   * @param array<string, mixed> $file File upload.
   * @param array<string, mixed> $authUser User login.
   *
   * @return array<string, mixed>
   */
  public function store(array $data, array $file, array $authUser): array {
    try {
      $wallpaper = $this->uploadUseCase->execute(
        (int) $authUser['user_id'],
        $data,
        $file
      );

      if (!is_array($wallpaper)) {
        throw new \RuntimeException('Upload result tidak valid.');
      }

      return Response::success(
        'Wallpaper submitted for review.',
        WallpaperResource::uploaded($wallpaper, Request::baseUrl()),
        201
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * PATCH /me/wallpapers/{id}.
   *
   * @param string $id ID wallpaper.
   * @param array<string, mixed> $data Body JSON.
   * @param array<string, mixed> $authUser User login.
   *
   * @return array<string, mixed>
   */
  public function update(string $id, array $data, array $authUser): array {
    try {
      $wallpaper = $this->updateUseCase->execute(
        $id,
        (int) $authUser['user_id'],
        $data
      );

      return Response::success(
        'Wallpaper updated successfully.',
        WallpaperResource::updated($wallpaper, Request::baseUrl())
      );
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * DELETE /me/wallpapers/{id}.
   *
   * @param string $id ID wallpaper.
   * @param array<string, mixed> $authUser User login.
   *
   * @return array<string, mixed>
   */
  public function destroy(string $id, array $authUser): array {
    try {
      $this->deleteUseCase->execute(
        $id,
        (int) $authUser['user_id'],
        (string) $authUser['role']
      );

      return Response::success('Wallpaper deleted successfully.', null);
    } catch (\Throwable $e) {
      return $this->handleException($e);
    }
  }

  /**
   * Mengubah exception menjadi response HTTP.
   *
   * @param \Throwable $e Exception dari use case.
   *
   * @return array<string, mixed>
   */
  private function handleException(\Throwable $e): array {
    if ($e instanceof ValidationException) {
      return Response::error('Validation failed.', 400, $e->getErrors());
    }

    if ($e instanceof NotFoundException) {
      return Response::error('Resource not found.', 404);
    }

    if ($e instanceof AuthorizationException) {
      return Response::error($e->getMessage(), 403);
    }

    return Response::error('Internal server error.', 500);
  }
}
