<?php

/**
 * Controller search analytics.
 *
 * Controller ini melayani pencatatan search event, kategori trending, dan
 * rekomendasi search untuk frontend user.
 *
 * @package Scapes\Interfaces\Http\Controllers
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Search\ListSearchRecommendationsUseCase;
use Scapes\Application\UseCases\Search\ListTrendingCategoriesUseCase;
use Scapes\Application\UseCases\Search\LogSearchEventUseCase;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Infrastructure\Logging\AppLogger;
use Scapes\Interfaces\Http\Response;

/**
 * Kelas SearchAnalyticsController - Endpoint search analytics.
 */
class SearchAnalyticsController {

  /**
   * Use case log search.
   *
   * @var LogSearchEventUseCase
   */
  private LogSearchEventUseCase $logUseCase;

  /**
   * Use case kategori trending.
   *
   * @var ListTrendingCategoriesUseCase
   */
  private ListTrendingCategoriesUseCase $trendingUseCase;

  /**
   * Use case rekomendasi search.
   *
   * @var ListSearchRecommendationsUseCase
   */
  private ListSearchRecommendationsUseCase $recommendationsUseCase;

  /**
   * Konstruktor SearchAnalyticsController.
   *
   * @param LogSearchEventUseCase $logUseCase Use case log.
   * @param ListTrendingCategoriesUseCase $trendingUseCase Use case trending.
   * @param ListSearchRecommendationsUseCase $recommendationsUseCase Use case rekomendasi.
   */
  public function __construct(
    LogSearchEventUseCase $logUseCase,
    ListTrendingCategoriesUseCase $trendingUseCase,
    ListSearchRecommendationsUseCase $recommendationsUseCase
  ) {
    $this->logUseCase = $logUseCase;
    $this->trendingUseCase = $trendingUseCase;
    $this->recommendationsUseCase = $recommendationsUseCase;
  }

  /**
   * POST /search-logs.
   *
   * @param array<string, mixed> $data Payload request.
   *
   * @return array<string, mixed>
   */
  public function log(array $data): array {
    try {
      return Response::success(
        'Search event accepted.',
        $this->logUseCase->execute($data),
        202
      );
    } catch (\Throwable $e) {
      return $this->handleException($e, 'log', ['payload' => $data]);
    }
  }

  /**
   * GET /categories/trending.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  public function trending(array $query): array {
    try {
      return Response::success(
        'Trending categories retrieved successfully.',
        $this->trendingUseCase->execute($query)
      );
    } catch (\Throwable $e) {
      return $this->handleException($e, 'trending', ['query' => $query]);
    }
  }

  /**
   * GET /recommendations/search.
   *
   * @param array<string, mixed> $query Query string.
   *
   * @return array<string, mixed>
   */
  public function recommendations(array $query): array {
    try {
      $result = $this->recommendationsUseCase->execute($query);

      return Response::success(
        'Search recommendations retrieved successfully.',
        $result['data'],
        200,
        $result['meta']
      );
    } catch (\Throwable $e) {
      return $this->handleException($e, 'recommendations', ['query' => $query]);
    }
  }

  /**
   * Mengubah exception menjadi response HTTP.
   *
   * @param \Throwable $e Exception.
   * @param string $action Nama action.
   * @param array<string, mixed> $context Context log.
   *
   * @return array<string, mixed>
   */
  private function handleException(
    \Throwable $e,
    string $action,
    array $context = []
  ): array {
    if ($e instanceof ValidationException) {
      return Response::error('Validation failed.', 400, $e->getErrors());
    }

    AppLogger::logThrowable('search_analytics_controller_exception', $e, array_merge(
      [
        'controller' => self::class,
        'action' => $action,
      ],
      $context
    ));

    return Response::internalErrorFromThrowable($e);
  }
}
