<?php

/**
 * Controller moderasi wallpaper.
 *
 * Controller ini menangani endpoint admin untuk queue dan keputusan
 * moderasi wallpaper.
 *
 * @package Scapes\Interfaces\Http\Controllers
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Moderation\ListModerationWallpapersUseCase;
use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Core\Exceptions\UnprocessableEntityException;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Logging\AppLogger;
use Scapes\Interfaces\Http\Request;
use Scapes\Interfaces\Http\Resources\WallpaperResource;
use Scapes\Interfaces\Http\Response;

/**
 * Kelas ModerationController - Endpoint moderasi admin.
 */
class ModerationController {

  /**
   * Use case daftar queue moderasi.
   *
   * @var ListModerationWallpapersUseCase
   */
  private ListModerationWallpapersUseCase $listUseCase;

  /**
   * Use case keputusan moderasi.
   *
   * @var ModerateWallpaperUseCase
   */
  private ModerateWallpaperUseCase $moderateUseCase;

  /**
   * Konstruktor ModerationController.
   *
   * @param ListModerationWallpapersUseCase $listUseCase Use case daftar.
   * @param ModerateWallpaperUseCase $moderateUseCase Use case keputusan.
   */
  public function __construct(
    ListModerationWallpapersUseCase $listUseCase,
    ModerateWallpaperUseCase $moderateUseCase
  ) {
    $this->listUseCase = $listUseCase;
    $this->moderateUseCase = $moderateUseCase;
  }

  /**
   * GET /moderation/wallpapers.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  public function index(array $query): array {
    try {
      $result = $this->listUseCase->execute($query);
      $baseUrl = Request::baseUrl();
      $data = array_map(
        fn (array $wallpaper): array => WallpaperResource::adminQueue(
          $wallpaper,
          $baseUrl
        ),
        $result['data']
      );

      return Response::success(
        'Wallpapers for moderation retrieved successfully.',
        $data,
        200,
        $result['meta']
      );
    } catch (\Throwable $e) {
      return $this->handleException($e, 'index', [
        'query' => $query,
      ]);
    }
  }

  /**
   * PATCH /moderation/wallpapers/{id}.
   *
   * @param string $id ID wallpaper.
   * @param array<string, mixed> $data Body JSON.
   * @param array<string, mixed> $authUser User admin.
   *
   * @return array<string, mixed>
   */
  public function update(string $id, array $data, array $authUser): array {
    try {
      $wallpaper = $this->moderateUseCase->execute(
        $id,
        (int) $authUser['user_id'],
        $data
      );

      if (!is_array($wallpaper)) {
        throw new \RuntimeException('Moderation result tidak valid.');
      }

      $message = (string) $wallpaper['status'] === 'approved'
        ? 'Wallpaper approved and is now publicly visible.'
        : 'Wallpaper rejected. The contributor has been notified.';

      return Response::success(
        $message,
        WallpaperResource::moderated($wallpaper, Request::baseUrl())
      );
    } catch (\Throwable $e) {
      return $this->handleException($e, 'update', [
        'wallpaper_id' => $id,
        'payload' => $data,
        'auth_user' => $authUser,
      ]);
    }
  }

  /**
   * Mengubah exception menjadi response HTTP.
   *
   * @param \Throwable $e Exception dari use case.
   *
   * @return array<string, mixed>
   */
  private function handleException(
    \Throwable $e,
    string $action = 'unknown',
    array $context = []
  ): array {
    if ($e instanceof ValidationException) {
      return Response::error('Validation failed.', 400, $e->getErrors());
    }

    if ($e instanceof NotFoundException) {
      return Response::error('Resource not found.', 404);
    }

    if ($e instanceof UnprocessableEntityException) {
      return Response::error($e->getMessage(), 422);
    }

    AppLogger::logThrowable('moderation_controller_exception', $e, array_merge(
      [
        'controller' => self::class,
        'action' => $action,
      ],
      $context
    ));

    return Response::error('Internal server error.', 500);
  }
}
