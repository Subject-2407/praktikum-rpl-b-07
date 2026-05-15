<?php

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Controllers;

use Scapes\Application\UseCases\Wallpaper\UploadWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\DeleteWallpaperUseCase;
use Scapes\Application\UseCases\Wallpaper\GetWallpaperStatusUseCase;
use Scapes\Application\UseCases\Wallpaper\GetApprovedWallpapersByCategoryUseCase;
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
  private GetApprovedWallpapersByCategoryUseCase $getApprovedByCategoryUseCase;

  /**
   * Inisialisasi pengontrol wallpaper dengan use case.
   *
   * @param UploadWallpaperUseCase $uploadUseCase Use case upload
   * @param DeleteWallpaperUseCase $deleteUseCase Use case delete
   * @param GetWallpaperStatusUseCase $getStatusUseCase Use case get status
   * @param GetApprovedWallpapersByCategoryUseCase $getApprovedByCategoryUseCase Use case get approved by category
   */
  public function __construct(
    UploadWallpaperUseCase $uploadUseCase,
    DeleteWallpaperUseCase $deleteUseCase,
    GetWallpaperStatusUseCase $getStatusUseCase,
    GetApprovedWallpapersByCategoryUseCase $getApprovedByCategoryUseCase
  ) {
    $this->uploadUseCase = $uploadUseCase;
    $this->deleteUseCase = $deleteUseCase;
    $this->getStatusUseCase = $getStatusUseCase;
    $this->getApprovedByCategoryUseCase = $getApprovedByCategoryUseCase;
  }

  /**
   * Upload wallpaper baru.
   * POST /wallpaper/upload
   * Body: FormData dengan file dan metadata
   *
   * @param array<string, mixed> $data Data permintaan
   * @param array<string, mixed> $file Data file dari $_FILES
   * @param array<string, mixed> $authUser Data user terautentikasi (dari middleware)
   * @return array<string, mixed> Respons JSON
   */
  public function upload(array $data, array $file, array $authUser): array
  {
    try {
      // Validasi input dasar
      if (empty($data['title'])) {
        return $this->errorResponse('Wallpaper title is required', 400);
      }

      if (empty($data['category_id'])) {
        return $this->errorResponse('Wallpaper category is required', 400);
      }

      if (empty($authUser['user_id'])) {
        return $this->errorResponse(
          'Contributor not authenticated',
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
      $contributorId = (int)$authUser['user_id'];
      $tmpFile = $file['tmp_name'];
      
      // Ambil tag_ids jika ada
      $tagIds = [];
      if (!empty($data['tag_ids'])) {
        $tagIds = is_array($data['tag_ids']) ? 
          $data['tag_ids'] : explode(',', (string)$data['tag_ids']);
      }

      // Jalankan use case
      $wallpaper = $this->uploadUseCase->execute(
        $contributorId,
        $categoryId,
        $title,
        $tmpFile,
        $file['name'],
        (int)($file['size'] / 1024), // Ukuran dalam KB
        $file['type'],
        getimagesize($tmpFile)[0] ?? 0, // Lebar gambar
        getimagesize($tmpFile)[1] ?? 0, // Tinggi gambar
        $tagIds,
        $description
      );

      return $this->successResponse(
        [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'category_id' => $wallpaper->getCategoryId(),
          'contributor_id' => $wallpaper->getContributorId(),
          'published_at' => $wallpaper->getPublishedAt(),
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
   * Body: {"wallpaper_id": 1}
   *
   * @param array<string, mixed> $data Data permintaan
   * @param array<string, mixed> $authUser Data user terautentikasi
   * @return array<string, mixed> Respons JSON
   */
  public function delete(array $data, array $authUser): array
  {
    try {
      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('Wallpaper ID is required', 400);
      }

      if (empty($authUser['user_id'])) {
        return $this->errorResponse('User not authenticated', 401);
      }

      $wallpaperId = (int)$data['wallpaper_id'];
      $userId = (int)$authUser['user_id'];
      $userRole = $authUser['role'] ?? 'contributor';

      // Jalankan use case
      $this->deleteUseCase->execute($wallpaperId, $userId, $userRole);

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
   * @param array<string, mixed> $authUser Data user terautentikasi (opsional)
   * @return array<string, mixed> Respons JSON
   */
  public function getStatus(array $data, ?array $authUser = null): array
  {
    try {
      if (empty($data['wallpaper_id'])) {
        return $this->errorResponse('Wallpaper ID is required', 400);
      }

      $wallpaperId = (int)$data['wallpaper_id'];

      // Jalankan use case
      $wallpaper = $this->getStatusUseCase->execute($wallpaperId);

      // Proteksi: Pending wallpaper hanya boleh dilihat admin atau pemilik
      if ($wallpaper->isPending() && !$this->canViewWallpaper($wallpaper, $authUser)) {
        return $this->errorResponse(
          'Anda tidak memiliki izin untuk melihat wallpaper ini',
          403
        );
      }

      $imagePath = $this->formatImagePath($wallpaper->getFilePath(), $wallpaper->getStatus());
      return $this->successResponse(
        [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'width' => $wallpaper->getWidth(),
          'height' => $wallpaper->getHeight(),
          'size_kb' => $wallpaper->getFileSizeKb(),
          'category_id' => $wallpaper->getCategoryId(),
          'contributor_id' => $wallpaper->getContributorId(),
          'description' => $wallpaper->getDescription(),
          'image_path' => $imagePath,
          'image_url' => $this->buildImageUrl($imagePath),
          'uploaded_at' => $wallpaper->getCreatedAt(),
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
   * @param array<string, mixed> $authUser Data user terautentikasi (opsional)
   * @return array<string, mixed> Respons JSON
   */
  public function getAllByContributor(array $data, ?array $authUser = null): array
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
      $wallpapers = $this->getStatusUseCase->getAllByContributor(
        $contributorId
      );

      $wallpaperData = [];
      foreach ($wallpapers as $wallpaper) {
        // Hanya tampilkan jika sudah approved, atau jika pengakses adalah pemilik/admin
        if ($wallpaper->isApproved() || $this->canViewWallpaper($wallpaper, $authUser)) {
          $imagePath = $this->formatImagePath($wallpaper->getFilePath(), $wallpaper->getStatus());
          $wallpaperData[] = [
            'id' => $wallpaper->getId(),
            'title' => $wallpaper->getTitle(),
            'status' => $wallpaper->getStatus(),
            'category_id' => $wallpaper->getCategoryId(),
            'image_path' => $imagePath,
            'image_url' => $this->buildImageUrl($imagePath),
            'uploaded_at' => $wallpaper->getCreatedAt(),
          ];
        }
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
   * Dapatkan wallpaper approved berdasarkan kategori.
   * GET /wallpaper/category/{category_id}
   * Body/Query: {"page": 1, "limit": 20}
   *
   * @param array<string, mixed> $data Data permintaan
   * @return array<string, mixed> Respons JSON
   */
  public function getApprovedByCategory(array $data): array
  {
    try {
      if (empty($data['category_id'])) {
        return $this->errorResponse('Category ID is required', 400);
      }

      $categoryId = (int)$data['category_id'];
      $page = !empty($data['page']) ? (int)$data['page'] : 1;
      $limit = !empty($data['limit']) ? (int)$data['limit'] : 20;

      // Jalankan use case
      $wallpapers = $this->getApprovedByCategoryUseCase->execute(
        $categoryId,
        $limit,
        $page
      );

      $wallpaperData = [];
      foreach ($wallpapers as $wallpaper) {
        $imagePath = $this->formatImagePath($wallpaper->getFilePath(), $wallpaper->getStatus());
        $wallpaperData[] = [
          'id' => $wallpaper->getId(),
          'title' => $wallpaper->getTitle(),
          'status' => $wallpaper->getStatus(),
          'category_id' => $wallpaper->getCategoryId(),
          'contributor_id' => $wallpaper->getContributorId(),
          'width' => $wallpaper->getWidth(),
          'height' => $wallpaper->getHeight(),
          'size_kb' => $wallpaper->getFileSizeKb(),
          'image_path' => $imagePath,
          'image_url' => $this->buildImageUrl($imagePath),
          'uploaded_at' => $wallpaper->getCreatedAt(),
        ];
      }

      return $this->successResponse(
        [
          'wallpapers' => $wallpaperData,
          'page' => $page,
          'limit' => $limit,
          'total' => count($wallpapers),
        ],
        'Approved wallpapers retrieved successfully',
        200
      );
    } catch (ValidationException $e) {
      return $this->errorResponse($e->getMessage(), 400);
    } catch (\Exception $e) {
      return $this->errorResponse($e->getMessage(), 500);
    }
  }

  /**
   * Mengecek apakah user memiliki izin untuk melihat wallpaper tertentu.
   * 
   * @param \Scapes\Core\Domain\Wallpaper $wallpaper
   * @param array|null $authUser
   * @return bool
   */
  private function canViewWallpaper(\Scapes\Core\Domain\Wallpaper $wallpaper, ?array $authUser): bool {
    if (!$authUser) return false;
    
    $isAdmin = ($authUser['role'] ?? '') === 'admin';
    $isOwner = (int)($authUser['user_id'] ?? 0) === $wallpaper->getContributorId();
    
    return $isAdmin || $isOwner;
  }

  /**
   * Memformat image path untuk kemudahan penggunaan di client side.
   * Untuk wallpaper approved, menghapus bagian 'approved/'.
   * 
   * @param string $relativePath
   * @param string $status
   * @return string
   */
  private function formatImagePath(string $relativePath, string $status): string {
    // Normalize to forward slashes for URL convenience
    $path = str_replace(DIRECTORY_SEPARATOR, '/', $relativePath);

    if ($status === 'approved') {
      // Ganti 'wallpapers/approved/' menjadi 'wallpapers/'
      return str_replace('wallpapers/approved/', 'wallpapers/', $path);
    }
    return $path;
  }
  /**
   * Build full image URL dari image path.
   * Contoh: 'minimalist/1234567_xyz.jpg' menjadi 'http://localhost:8000/wallpapers/minimalist/1234567_xyz.jpg'
   * 
   * @param string $imagePath Image path dari formatImagePath()
   * @return string Full URL ke image
   */
  private function buildImageUrl(string $imagePath): string {
    // Get base URL dari request
    $scheme = $_SERVER['REQUEST_SCHEME'] ?? 'http';
    $host = $_SERVER['HTTP_HOST'] ?? 'localhost:8000';
    
    return "{$scheme}://{$host}/{$imagePath}";
  }
  /**
   * Melayani file gambar wallpaper secara langsung.
   * GET /wallpapers/{path}
   * 
   * @param array $params
   * @param array|null $authUser
   * @return array|void Hanya return jika error
   */
  public function serveImage(array $params, ?array $authUser = null) {
    $path = $params['path'] ?? '';
    if (empty($path)) {
      return $this->errorResponse('File path required', 400);
    }

    // Normalize path separators from URL to OS standard
    $normalizedPath = str_replace(['/', '\\'], DIRECTORY_SEPARATOR, $path);

    // Tentukan root storage path
    $storageRoot = BASE_PATH . DIRECTORY_SEPARATOR . 'storage' . DIRECTORY_SEPARATOR . 'wallpapers';

    // Cek apakah ini request ke folder pending
    $isPending = strpos($normalizedPath, 'pending' . DIRECTORY_SEPARATOR) === 0;
    $physicalPath = '';

    if ($isPending) {
      // Security check untuk pending
      $physicalPath = $storageRoot . DIRECTORY_SEPARATOR . $normalizedPath;

      if (!$authUser || ($authUser['role'] !== 'admin' && !isset($authUser['user_id']))) {
        return $this->errorResponse('Unauthorized access to pending content', 401);
      }
    } else {
      // Approved content: Map ke folder 'approved'
      // Jika path tidak mengandung 'pending/', kita cari di folder 'approved'
      $physicalPath = $storageRoot . DIRECTORY_SEPARATOR . 'approved' . DIRECTORY_SEPARATOR . $normalizedPath;
    }

    if (!file_exists($physicalPath)) {
      return $this->errorResponse('Image not found: ' . $normalizedPath, 404);
    }

    // Dapatkan MIME type
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $physicalPath);
    finfo_close($finfo);

    // Stream file
    header("Content-Type: $mimeType");
    header("Content-Length: " . filesize($physicalPath));
    readfile($physicalPath);
    exit;
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
