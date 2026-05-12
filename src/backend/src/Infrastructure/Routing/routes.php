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
use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;

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
  // ============================================================
  // AUTH ROUTES
  // ============================================================

  $router->post('/auth/register', function (array $params) use ($services) {
    $registerUseCase = new RegisterContributorUseCase(
      $services['userRepository']
    );
    $loginUseCase = new LoginUserUseCase(
      $services['userRepository'],
      $services['sessionRepository']
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
      $services['sessionRepository']
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
      $services['sessionRepository']
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
      $services['categoryRepository']
    );
    $deleteUseCase = new DeleteWallpaperUseCase(
      $services['wallpaperRepository']
    );
    $getStatusUseCase = new GetWallpaperStatusUseCase(
      $services['wallpaperRepository']
    );

    $controller = new WallpaperController(
      $uploadUseCase,
      $deleteUseCase,
      $getStatusUseCase
    );

    return $controller->upload(
      getJsonInput(),
      $_FILES['file'] ?? []
    );
  });

  $router->delete(
    '/wallpaper/{id}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase
      );

      $data = getJsonInput();
      $data['wallpaper_id'] = (int)$params['id'];

      return $controller->delete($data);
    }
  );

  $router->get('/wallpaper/{id}', function (array $params) use ($services) {
    $uploadUseCase = new UploadWallpaperUseCase(
      $services['wallpaperRepository'],
      $services['categoryRepository']
    );
    $deleteUseCase = new DeleteWallpaperUseCase(
      $services['wallpaperRepository']
    );
    $getStatusUseCase = new GetWallpaperStatusUseCase(
      $services['wallpaperRepository']
    );

    $controller = new WallpaperController(
      $uploadUseCase,
      $deleteUseCase,
      $getStatusUseCase
    );

    return $controller->getStatus(['wallpaper_id' => (int)$params['id']]);
  });

  $router->get(
    '/wallpaper/contributor/{contributor_id}',
    function (array $params) use ($services) {
      $uploadUseCase = new UploadWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['categoryRepository']
      );
      $deleteUseCase = new DeleteWallpaperUseCase(
        $services['wallpaperRepository']
      );
      $getStatusUseCase = new GetWallpaperStatusUseCase(
        $services['wallpaperRepository']
      );

      $controller = new WallpaperController(
        $uploadUseCase,
        $deleteUseCase,
        $getStatusUseCase
      );

      return $controller->getAllByContributor([
        'contributor_id' => (int)$params['contributor_id'],
      ]);
    }
  );

  // ============================================================
  // MODERATION ROUTES (Admin only)
  // ============================================================

  $router->post(
    '/moderation/moderate',
    function (array $params) use ($services) {
      $moderateUseCase = new ModerateWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['moderationReviewRepository']
      );

      $controller = new ModerationController($moderateUseCase);

      return $controller->moderate(getJsonInput());
    }
  );

  $router->get(
    '/moderation/pending',
    function (array $params) use ($services) {
      $moderateUseCase = new ModerateWallpaperUseCase(
        $services['wallpaperRepository'],
        $services['moderationReviewRepository']
      );

      $controller = new ModerationController($moderateUseCase);

      return $controller->getPending(getJsonInput());
    }
  );

  return $router;
}
