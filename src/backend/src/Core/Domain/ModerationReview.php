<?php

/**
 * Domain Entity: ModerationReview
 *
 * Merepresentasikan keputusan moderasi admin terhadap wallpaper.
 * Menyimpan riwayat review dengan alasan penolakan jika ada.
 *
 * @package Scapes\Core\Domain
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Domain;

/**
 * Kelas ModerationReview - Entitas domain untuk review moderasi.
 *
 * @class ModerationReview
 */
class ModerationReview {

  /**
   * ID unik review moderasi.
   *
   * @var int
   */
  private int $id;

  /**
   * ID wallpaper yang dimoderasi.
   *
   * @var int|string
   */
  private int|string $wallpaperId;

  /**
   * ID admin yang membuat keputusan.
   *
   * @var int
   */
  private int $adminId;

  /**
   * Keputusan moderasi (approved atau rejected).
   *
   * @var string
   */
  private string $decision;

  /**
   * Alasan keputusan, khususnya untuk penolakan.
   *
   * @var string|null
   */
  private ?string $reason;

  /**
   * Waktu keputusan dibuat.
   *
   * @var string
   */
  private string $reviewedAt;

  /**
   * Konstruktor ModerationReview.
   *
   * @param int $id ID unik review.
   * @param int|string $wallpaperId ID wallpaper.
   * @param int $adminId ID admin pembuat keputusan.
   * @param string $decision Keputusan (approved atau rejected).
   * @param string|null $reason Alasan keputusan.
   * @param string $reviewedAt Waktu review.
   */
  public function __construct(
    int $id,
    int|string $wallpaperId,
    int $adminId,
    string $decision,
    ?string $reason = null,
    string $reviewedAt = ''
  ) {
    $this->id = $id;
    $this->wallpaperId = $wallpaperId;
    $this->adminId = $adminId;
    $this->decision = $decision;
    $this->reason = $reason;
    $this->reviewedAt = $reviewedAt ?: date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan ID review.
   *
   * @return int
   */
  public function getId(): int {
    return $this->id;
  }

  /**
   * Mendapatkan ID wallpaper.
   *
   * @return int|string
   */
  public function getWallpaperId(): int|string {
    return $this->wallpaperId;
  }

  /**
   * Mendapatkan ID admin.
   *
   * @return int
   */
  public function getAdminId(): int {
    return $this->adminId;
  }

  /**
   * Mendapatkan keputusan moderasi.
   *
   * @return string
   */
  public function getDecision(): string {
    return $this->decision;
  }

  /**
   * Mendapatkan alasan keputusan.
   *
   * @return string|null
   */
  public function getReason(): ?string {
    return $this->reason;
  }

  /**
   * Mendapatkan waktu review.
   *
   * @return string
   */
  public function getReviewedAt(): string {
    return $this->reviewedAt;
  }

  /**
   * Mengecek apakah keputusan adalah approval.
   *
   * @return bool
   */
  public function isApproved(): bool {
    return $this->decision === 'approved';
  }

  /**
   * Mengecek apakah keputusan adalah rejection.
   *
   * @return bool
   */
  public function isRejected(): bool {
    return $this->decision === 'rejected';
  }
}
