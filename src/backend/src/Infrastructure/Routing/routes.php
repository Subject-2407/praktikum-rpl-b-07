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
use Scapes\Application\UseCases\Search\ListSearchRecommendationsUseCase;
use Scapes\Application\UseCases\Search\ListTrendingCategoriesUseCase;
use Scapes\Application\UseCases\Search\LogSearchEventUseCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetPublicWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\ListContributorWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\ListPublicWallpapersUseCase;
use Scapes\Application\UseCases\Wallpaper\UpdateWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Infrastructure\Auth\AuthMiddleware;
use Scapes\Infrastructure\Auth\OptionalAuthMiddleware;
use Scapes\Infrastructure\Security\CsrfMiddleware;
use Scapes\Interfaces\Http\Controllers\AuthController;
use Scapes\Interfaces\Http\Controllers\MetadataController;
use Scapes\Interfaces\Http\Controllers\ModerationController;
use Scapes\Interfaces\Http\Controllers\SearchAnalyticsController;
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
  $searchAnalyticsController = buildSearchAnalyticsController($services);

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

  $csrfMiddleware = new CsrfMiddleware();

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
    [$authMiddleware, $csrfMiddleware]
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
    fn (array $params): array => $wallpaperController->show((string) $params['id'])
  );

  $router->post(
    '/search-logs',
    fn (array $params): array => $searchAnalyticsController->log(Request::json())
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
    [$contributorMiddleware, $csrfMiddleware]
  );

  $router->patch(
    '/me/wallpapers/{id}',
    fn (array $params): array => $wallpaperController->update(
      (string) $params['id'],
      Request::json(),
      $params['auth_user']
    ),
    [$contributorMiddleware, $csrfMiddleware]
  );

  $router->delete(
    '/me/wallpapers/{id}',
    fn (array $params): array => $wallpaperController->destroy(
      (string) $params['id'],
      $params['auth_user']
    ),
    [$contributorMiddleware, $csrfMiddleware]
  );

  $router->get(
    '/moderation/wallpapers',
    fn (array $params): array => $moderationController->index(Request::query()),
    [$adminMiddleware]
  );

  $router->patch(
    '/moderation/wallpapers/{id}',
    fn (array $params): array => $moderationController->update(
      (string) $params['id'],
      Request::json(),
      $params['auth_user']
    ),
    [$adminMiddleware, $csrfMiddleware]
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
    '/categories/trending',
    fn (array $params): array => $searchAnalyticsController->trending(
      Request::query()
    )
  );

  $router->get(
    '/tags',
    fn (array $params): array => $metadataController->tags(Request::query())
  );

  $router->get(
    '/recommendations/search',
    fn (array $params): array => $searchAnalyticsController->recommendations(
      Request::query()
    )
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
    new RegisterContributorUseCase(
      $services['userRepository'],
      $services['emailNotification']
    ),
    new VerifyEmailUseCase($services['userRepository']),
    new LoginUserUseCase($services['userRepository'], $services['jwtManager']),
    new LogoutUserUseCase($services['tokenDenylist']),
    new RequestPasswordResetUseCase(
      $services['userRepository'],
      $services['emailNotification']
    ),
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
      $services['tagRepository'],
      $services['storage']
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
      $services['storage'],
      $services['emailNotification'],
      $services['tagRepository']
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
 * Membuat SearchAnalyticsController.
 *
 * @param array<string, mixed> $services Service container.
 *
 * @return SearchAnalyticsController Controller search analytics.
 */
function buildSearchAnalyticsController(array $services): SearchAnalyticsController
{
  return new SearchAnalyticsController(
    new LogSearchEventUseCase($services['searchAnalyticsRepository']),
    new ListTrendingCategoriesUseCase($services['searchAnalyticsRepository']),
    new ListSearchRecommendationsUseCase(
      $services['searchAnalyticsRepository'],
      $services['categoryRepository'],
      $services['tagRepository']
    )
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
  $normalizedRelativePath = str_replace('\\', '/', $relativePath);
  $isPendingPath = str_starts_with(str_replace('\\', '/', $path), 'pending/');
  $wallpaper = $services['wallpaperRepository']->findDetailedByStoragePath(
    $normalizedRelativePath
  );

  if ($wallpaper !== null && (string) $wallpaper['status'] !== 'approved') {
    $authUser = $params['auth_user'] ?? null;
    $isAdmin = is_array($authUser) && (string) ($authUser['role'] ?? '') === 'admin';
    $isOwner = is_array($authUser)
      && (int) ($authUser['user_id'] ?? 0) === (int) $wallpaper['contributor_id'];

    if (!$isAdmin && !$isOwner) {
      return [
        'success' => false,
        'status_code' => empty($authUser) ? 401 : 403,
        'message' => empty($authUser)
          ? 'Unauthorized. Please log in.'
          : 'Forbidden. You do not have access to this resource.',
        'errors' => null,
      ];
    }
  }

  if ($wallpaper === null && $isPendingPath) {
    $authUser = $params['auth_user'] ?? null;
    if (empty($authUser) || (string) ($authUser['role'] ?? '') !== 'admin') {
      return [
        'success' => false,
        'status_code' => empty($authUser) ? 401 : 403,
        'message' => empty($authUser)
          ? 'Unauthorized. Please log in.'
          : 'Forbidden. You do not have access to this resource.',
        'errors' => null,
      ];
    }
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
