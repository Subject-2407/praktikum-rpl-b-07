<?php

/**
 * Moderation Review Repository
 *
 * Menangani akses data review moderasi dari database.
 * Repository ini menyediakan metode-metode untuk CRUD operations pada tabel moderation_reviews.
 *
 * @package Scapes\Infrastructure\Repository
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Repository;

use PDO;
use Scapes\Core\Domain\ModerationReview;
use Scapes\Infrastructure\Database\DatabaseConnection;
use Scapes\Core\Exceptions\DatabaseException;

/**
 * Kelas ModerationReviewRepository - Repository untuk akses data moderation review.
 *
 * @class ModerationReviewRepository
 */
class ModerationReviewRepository extends BaseRepository {

  /**
   * Nama tabel database yang digunakan repository.
   *
   * @var string
   */
  protected string $table = 'moderation_reviews';

  /**
   * Menyimpan review moderasi baru ke database.
   *
   * @param ModerationReview $review Review yang akan disimpan.
   *
   * @return ModerationReview Review dengan ID yang sudah di-assign.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function save(ModerationReview $review): ModerationReview {
    try {
      $query = "INSERT INTO {$this->table} 
                (wallpaper_id, admin_id, decision, reason, reviewed_at) 
                VALUES (?, ?, ?, ?, ?)";
      $this->db->query($query, [
        $review->getWallpaperId(),
        $review->getAdminId(),
        $review->getDecision(),
        $review->getReason(),
        $review->getReviewedAt(),
      ]);

      // Ambil ID yang baru di-generate
      $lastId = (int) $this->db->getPdo()->lastInsertId();

      return new ModerationReview(
        $lastId,
        $review->getWallpaperId(),
        $review->getAdminId(),
        $review->getDecision(),
        $review->getReason(),
        $review->getReviewedAt()
      );
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal menyimpan moderation review: ' . $e->getMessage());
    }
  }

  /**
   * Mencari review moderasi berdasarkan wallpaper ID.
   *
   * @param int|string $wallpaperId ID wallpaper.
   *
   * @return ModerationReview|null Review jika ditemukan, null jika tidak.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByWallpaperId(int|string $wallpaperId): ?ModerationReview {
    try {
      $query = "SELECT * FROM {$this->table} WHERE wallpaper_id = ? ORDER BY reviewed_at DESC LIMIT 1";
      $result = $this->db->query($query, [$wallpaperId]);
      $data = $result->fetch(PDO::FETCH_ASSOC);

      if (!$data) {
        return null;
      }

      return $this->mapToModerationReview($data);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari moderation review: ' . $e->getMessage());
    }
  }

  /**
   * Mencari semua review moderasi dari admin tertentu.
   *
   * @param int $adminId ID admin.
   *
   * @return array<int, ModerationReview> Array dari ModerationReview.
   * @throws DatabaseException Jika terjadi error database.
   */
  public function findByAdminId(int $adminId): array {
    try {
      $query = "SELECT * FROM {$this->table} WHERE admin_id = ? ORDER BY reviewed_at DESC";
      $result = $this->db->query($query, [$adminId]);
      $dataArray = $result->fetchAll(PDO::FETCH_ASSOC);

      return array_map([$this, 'mapToModerationReview'], $dataArray);
    } catch (\PDOException $e) {
      throw new DatabaseException('Gagal mencari moderation review: ' . $e->getMessage());
    }
  }

  /**
   * Mengkonversi data dari database menjadi ModerationReview entity.
   *
   * @param array<string, mixed> $data Data dari database.
   *
   * @return ModerationReview ModerationReview entity.
   */
  private function mapToModerationReview(array $data): ModerationReview {
    return new ModerationReview(
      (int) $data['id'],
      $data['wallpaper_id'],
      (int) $data['admin_id'],
      $data['decision'],
      $data['reason'],
      $data['reviewed_at']
    );
  }
}
