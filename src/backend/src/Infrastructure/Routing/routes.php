<?php

declare(strict_types=1);

namespace Scapes\Infrastructure\Routing;

use Scapes\Interfaces\Http\Controllers\AuthController;
use Scapes\Interfaces\Http\Controllers\WallpaperController;
use Scapes\Interfaces\Http\Controllers\ModerationController;
use Scapes\Application\UseCases\Auth\RegisterContributorUseCase;
use Scapes\Application\UseCases\Auth\LoginUserUseCase;
use Scapes\Application\UseCases\Auth\LogoutUserUseCase;
use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetWallpaperStatusUseCase;
use Scapes\Application\UseCases\Wallpaper\GetApprovedWallpapersByCategoryUseCase;
use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;

use Scapes\Infrastructure\Auth\AuthMiddleware;
use Scapes\Infrastructure\Auth\OptionalAuthMiddleware;

/**
 * Ambil JSON input dari request body.
 *
 * @return array<string, mixed>
 */
function getJsonInput(): array
{
  $input = file_get_contents('php://input');

  if (empty($input)) {
    // Jika tidak ada raw input, coba dari $_POST (form-urlencoded)
    return $_POST;
  }

  $decoded = json_decode($input, true);
  return is_array($decoded) ? $decoded : [];
}

/**
 * Daftarkan semua routes untuk MVP API.
 * Routes dikelompokkan berdasarkan resource: auth, wallpaper, moderation.
 *
 * @param Router $router Instance router
 * @param array<string, mixed> $services Service container dengan dependencies
 * @return Router Router dengan semua routes terdaftar
 */
function registerMVPRoutes(Router $router, array $services): Router
{
  // Middlewares
  $authMiddleware = new AuthMiddleware($services['jwtManager'], $services['sessionRepository']);
  $optionalAuthMiddleware = new OptionalAuthMiddleware($services['jwtManager'], $services['sessionRepository']);
  $contributorMiddleware = new AuthMiddleware($services['jwtManager'], $services['sessionRepository'], 'contributor');
  $adminMiddleware = new AuthMiddleware($services['jwtManager'], $services['sessionRepository'], 'admin');

  // ============================================================
  // AUTH ROUTES
  // ============================================================

  $router->post('/auth/register', function (array $params) use ($services) {
    $registerUseCase = new RegisterContributorUseCase(
      $services['userRepository']
    );
    $loginUseCase = new LoginUserUseCase(
      $services['userRepository'],
      $services['sessionRepository'],
      $services['jwtManager']
    );
    $logoutUseCase = new LogoutUserUseCase(
      $services['sessionRepository']
    );

    $controller = new AuthController(
      $registerUseCase,
      $loginUseCase,
      $logoutUseCase
    );

    return $controller->register(getJsonInput());
  });

  $router->post('/auth/login', function (array $params) use ($services) {
    $registerUseCase = new RegisterContributorUseCase(
      $services['userRepository']
    );
    $loginUseCase = new LoginUserUseCase(
      $services['userRepository'],
      $services['sessionRepository'],
      $services['jwtManager']
    );
    $logoutUseCase = new LogoutUserUseCase(
      $services['sessionRepository']
    );

    $controller = new AuthController(
      $registerUseCase,
      $loginUseCase,
      $logoutUseCase
    );

    return $controller->login(getJsonInput());
  });

  $router->post('/auth/logout', function (array $params) use ($services) {
    $registerUseCase = new RegisterContributorUseCase(
      $services['userRepository']
    );
    $loginUseCase = new LoginUserUseCase(
      $services['userRepository'],
      $services['sessionRepository'],
      $services['jwtManager']
    );
    $logoutUseCase = new LogoutUserUseCase(
      $services['sessionRepository']
    );

    $controller = new AuthController(
      $registerUseCase,
      $loginUseCase,
      $logoutUseCase
    );

    return $controller->logout(getJsonInput());
  });

  // ============================================================
  // WALLPAPER ROUTES
  // ============================================================

  $router->post('/wallpaper/upload', function (array $params) use ($services) {
    $uploadUseCase = new UploadWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository'],
      $services['tagRepository'],
      $services['storage']
    );
    $deleteUseCase = new DeleteWallpaperUseCase(
      $services['wallpaperRepository']
    );
    $getStatusUseCase = new GetWallpaperStatusUseCase(
      $services['wallpaperRepository']
    );
    $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository']
    );

    $controller = new WallpaperController(
      $uploadUseCase,
      $deleteUseCase,
      $getStatusUseCase,
      $getApprovedByCategoryUseCase
    );

    return $controller->upload(
      getJsonInput(),
      $_FILES['file'] ?? [],
      $params['auth_user']
    );
  }, [$contributorMiddleware]);

  $router->delete(
    '/wallpaper/{id}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository'],
        $services['tagRepository'],
        $services['storage']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );
      $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase,
        $getApprovedByCategoryUseCase
      );

      $data = getJsonInput();
      $data['wallpaper_id'] = (int)$params['id'];

      return $controller->delete($data, $params['auth_user']);
    },
    [$authMiddleware]
  );

  $router->get('/wallpaper/{id}', function (array $params) use ($services) {
    $uploadUseCase = new UploadWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository'],
      $services['tagRepository'],
      $services['storage']
    );
    $deleteUseCase = new DeleteWallpaperUseCase(
      $services['wallpaperRepository']
    );
    $getStatusUseCase = new GetWallpaperStatusUseCase(
      $services['wallpaperRepository']
    );
    $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository']
    );

    $controller = new WallpaperController(
      $uploadUseCase,
      $deleteUseCase,
      $getStatusUseCase,
      $getApprovedByCategoryUseCase
    );

    return $controller->getStatus(['wallpaper_id' => (int)$params['id']], $params['auth_user']);
  }, [$optionalAuthMiddleware]);

  $router->get(
    '/wallpaper/contributor/{contributor_id}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository'],
        $services['tagRepository'],
        $services['storage']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );
      $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase,
        $getApprovedByCategoryUseCase
      );

      return $controller->getAllByContributor([
        'contributor_id' => (int)$params['contributor_id'],
      ], $params['auth_user']);
    },
    [$optionalAuthMiddleware]
  );

  $router->get(
    '/wallpaper/category/{category_id}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository'],
        $services['tagRepository'],
        $services['storage']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );
      $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase,
        $getApprovedByCategoryUseCase
      );

      return $controller->getApprovedByCategory([
        'category_id' => (int)$params['category_id'],
        'page' => (int)($_GET['page'] ?? 1),
        'limit' => (int)($_GET['limit'] ?? 20),
      ]);
    }
  );

  $router->get(
    '/wallpapers/{path}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository'],
        $services['tagRepository'],
        $services['storage']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );
      $getApprovedByCategoryUseCase = new GetApprovedWallpapersByCategoryUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase,
        $getApprovedByCategoryUseCase
      );

      return $controller->serveImage($params, $params['auth_user']);
    },
    [$optionalAuthMiddleware]
  );

  // ============================================================
  // MODERATION ROUTES (Admin only)
  // ============================================================

  $router->post(
    '/moderation/moderate',
    function (array $params) use ($services) {
      $moderateUseCase = new ModerateWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['moderationReviewRepository'],
        $services['categoryRepository'],
        $services['storage']
      );

      $controller = new ModerationController($moderateUseCase);

      return $controller->moderate(getJsonInput(), $params['auth_user']);
    },
    [$adminMiddleware]
  );

  $router->get(
    '/moderation/pending',
    function (array $params) use ($services) {
      $moderateUseCase = new ModerateWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['moderationReviewRepository'],
        $services['categoryRepository'],
        $services['storage']
      );

      $controller = new ModerationController($moderateUseCase);

      return $controller->getPending(getJsonInput(), $params['auth_user']);
    },
    [$adminMiddleware]
  );

  return $router;
}
