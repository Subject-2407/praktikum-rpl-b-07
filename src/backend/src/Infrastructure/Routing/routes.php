<?php

/**
 * Definisi route API Scapes.
 *
 * File ini melakukan wiring controller, use case, repository, dan middleware
 * untuk seluruh endpoint yang didefinisikan di docs/api-contract.md.
 *
 * @package Scapes\Infrastructure\Routing
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Routing;

use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Application\UseCases\Auth\RequestPasswordResetUseCase;
use Scapes\Application\UseCases\Auth\ResetPasswordUseCase;
use Scapes\Application\UseCases\Auth\VerifyEmailUseCase;
use Scapes\Application\UseCases\Metadata\ListCategoriesUseCase;
use Scapes\Application\UseCases\Metadata\ListSourcesUseCase;
use Scapes\Application\UseCases\Metadata\ListTagsUseCase;
use Scapes\Application\UseCases\Moderation\ListModerationWallpapersUseCase;
use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetPublicWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\ListContributorWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\ListPublicWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\UpdateWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Infrastructure\Auth\AuthMiddleware;
use Scapes\Infrastructure\Auth\OptionalAuthMiddleware;
use Scapes\Interfaces\Http\Controllers\AuthController;
use Scapes\Interfaces\Http\Controllers\MetadataController;
use Scapes\Interfaces\Http\Controllers\ModerationController;
use Scapes\Interfaces\Http\Controllers\WallpaperController;
use Scapes\Interfaces\Http\Request;

/**
 * Daftarkan semua route API.
 *
 * @param Router $router Instance router.
 * @param array<string, mixed> $services Service container sederhana.
 *
 * @return Router Router dengan route terdaftar.
 */
function registerMVPRoutes(Router $router, array $services): Router
{
  $authController = buildAuthController($services);
  $wallpaperController = buildWallpaperController($services);
  $moderationController = buildModerationController($services);
  $metadataController = buildMetadataController($services);

  $authMiddleware = new AuthMiddleware(
    $services['jwtManager'],
    $services['tokenDenylist']
  );
  $optionalAuthMiddleware = new OptionalAuthMiddleware(
    $services['jwtManager'],
    $services['tokenDenylist']
  );
  $contributorMiddleware = new AuthMiddleware(
    $services['jwtManager'],
    $services['tokenDenylist'],
    'contributor'
  );
  $adminMiddleware = new AuthMiddleware(
    $services['jwtManager'],
    $services['tokenDenylist'],
    'admin'
  );

  $router->post(
    '/registrations',
    fn (array $params): array => $authController->register(Request::json())
  );

  $router->post(
    '/email-verifications',
    fn (array $params): array => $authController->verifyEmail(Request::json())
  );

  $router->post(
    '/sessions',
    fn (array $params): array => $authController->login(Request::json())
  );

  $router->get(
    '/sessions/current',
    fn (array $params): array => $authController->currentSession(
      $params['auth_user']
    ),
    [$authMiddleware]
  );

  $router->delete(
    '/sessions/current',
    fn (array $params): array => $authController->logout($params['auth_user']),
    [$authMiddleware]
  );

  $router->post(
    '/password-resets',
    fn (array $params): array => $authController->requestPasswordReset(
      Request::json()
    )
  );

  $router->put(
    '/password-resets/{token}',
    fn (array $params): array => $authController->resetPassword(
      (string) $params['token'],
      Request::json()
    )
  );

  $router->get(
    '/wallpapers',
    fn (array $params): array => $wallpaperController->index(Request::query())
  );

  $router->get(
    '/wallpapers/{id}',
    fn (array $params): array => $wallpaperController->show((int) $params['id'])
  );

  $router->get(
    '/me/wallpapers',
    fn (array $params): array => $wallpaperController->mine(
      Request::query(),
      $params['auth_user']
    ),
    [$contributorMiddleware]
  );

  $router->post(
    '/me/wallpapers',
    fn (array $params): array => $wallpaperController->store(
      Request::form(),
      Request::file('file') ?? [],
      $params['auth_user']
    ),
    [$contributorMiddleware]
  );

  $router->patch(
    '/me/wallpapers/{id}',
    fn (array $params): array => $wallpaperController->update(
      (int) $params['id'],
      Request::json(),
      $params['auth_user']
    ),
    [$contributorMiddleware]
  );

  $router->delete(
    '/me/wallpapers/{id}',
    fn (array $params): array => $wallpaperController->destroy(
      (int) $params['id'],
      $params['auth_user']
    ),
    [$contributorMiddleware]
  );

  $router->get(
    '/moderation/wallpapers',
    fn (array $params): array => $moderationController->index(Request::query()),
    [$adminMiddleware]
  );

  $router->patch(
    '/moderation/wallpapers/{id}',
    fn (array $params): array => $moderationController->update(
      (int) $params['id'],
      Request::json(),
      $params['auth_user']
    ),
    [$adminMiddleware]
  );

  $router->get(
    '/sources',
    fn (array $params): array => $metadataController->sources()
  );

  $router->get(
    '/categories',
    fn (array $params): array => $metadataController->categories()
  );

  $router->get(
    '/tags',
    fn (array $params): array => $metadataController->tags(Request::query())
  );

  $router->get(
    '/wallpapers/{path}',
    function (array $params) use ($services): array {
      return serveWallpaperFile($params, $services);
    },
    [$optionalAuthMiddleware]
  );

  return $router;
}

/**
 * Membuat AuthController.
 *
 * @param array<string, mixed> $services Service container.
 *
 * @return AuthController Controller auth.
 */
function buildAuthController(array $services): AuthController
{
  return new AuthController(
    new RegisterContributorUseCase($services['userRepository']),
    new VerifyEmailUseCase($services['userRepository']),
    new LoginUserUseCase($services['userRepository'], $services['jwtManager']),
    new LogoutUserUseCase($services['tokenDenylist']),
    new RequestPasswordResetUseCase($services['userRepository']),
    new ResetPasswordUseCase($services['userRepository'])
  );
}

/**
 * Membuat WallpaperController.
 *
 * @param array<string, mixed> $services Service container.
 *
 * @return WallpaperController Controller wallpaper.
 */
function buildWallpaperController(array $services): WallpaperController
{
  return new WallpaperController(
    new ListPublicWallpapersUseCase($services['wallpaperRepository']),
    new GetPublicWallpaperUseCase($services['wallpaperRepository']),
    new ListContributorWallpapersUseCase($services['wallpaperRepository']),
    new UploadWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository'],
      $services['tagRepository'],
      $services['storage']
    ),
    new UpdateWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository'],
      $services['tagRepository']
    ),
    new DeleteWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['storage']
    )
  );
}

/**
 * Membuat ModerationController.
 *
 * @param array<string, mixed> $services Service container.
 *
 * @return ModerationController Controller moderasi.
 */
function buildModerationController(array $services): ModerationController
{
  return new ModerationController(
    new ListModerationWallpapersUseCase($services['wallpaperRepository']),
    new ModerateWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['moderationReviewRepository'],
      $services['storage']
    )
  );
}

/**
 * Membuat MetadataController.
 *
 * @param array<string, mixed> $services Service container.
 *
 * @return MetadataController Controller metadata.
 */
function buildMetadataController(array $services): MetadataController
{
  return new MetadataController(
    new ListSourcesUseCase($services['apiSourceRepository']),
    new ListCategoriesUseCase($services['categoryRepository']),
    new ListTagsUseCase($services['tagRepository'])
  );
}

/**
 * Melayani file wallpaper dari storage.
 *
 * @param array<string, mixed> $params Parameter route.
 * @param array<string, mixed> $services Service container.
 *
 * @return array<string, mixed> Response error jika file tidak dapat dilayani.
 */
function serveWallpaperFile(array $params, array $services): array
{
  $path = str_replace(['\\', '/'], DIRECTORY_SEPARATOR, (string) $params['path']);
  $relativePath = 'wallpapers' . DIRECTORY_SEPARATOR . $path;
  $isPending = str_starts_with(str_replace('\\', '/', $path), 'pending/');

  if ($isPending && empty($params['auth_user'])) {
    return [
      'success' => false,
      'status_code' => 401,
      'message' => 'Unauthorized. Please log in.',
      'errors' => null,
    ];
  }

  try {
    $absolutePath = $services['storage']->getAbsolutePath($relativePath);
  } catch (\Throwable $e) {
    return [
      'success' => false,
      'status_code' => 404,
      'message' => 'Resource not found.',
      'errors' => null,
    ];
  }

  if (!is_file($absolutePath)) {
    return [
      'success' => false,
      'status_code' => 404,
      'message' => 'Resource not found.',
      'errors' => null,
    ];
  }

  $mimeType = mime_content_type($absolutePath) ?: 'application/octet-stream';
  header('Content-Type: ' . $mimeType);
  header('Content-Length: ' . (string) filesize($absolutePath));
  readfile($absolutePath);
  exit;
}
