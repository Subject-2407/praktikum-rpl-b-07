<?php

/**
 * Implementasi notifikasi untuk menandai konfigurasi email yang belum benar.
 *
 * @package Scapes\Infrastructure\Notification
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Notification;

use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Core\Domain\User;
use Scapes\Core\Exceptions\NotificationException;

/**
 * Kelas MisconfiguredEmailNotification - Gagal eksplisit saat email belum siap.
 */
class MisconfiguredEmailNotification implements EmailNotificationInterface {

  /**
   * Alasan konfigurasi email tidak valid.
   *
   * @var string
   */
  private string $reason;

  /**
   * Konstruktor MisconfiguredEmailNotification.
   *
   * @param string $reason Pesan error konfigurasi.
   */
  public function __construct(string $reason) {
    $this->reason = trim($reason) !== ''
      ? trim($reason)
      : 'Konfigurasi email belum lengkap.';
  }

  /**
   * @inheritDoc
   */
  public function sendEmailVerification(User $user, string $token): void {
    throw new NotificationException($this->reason);
  }

  /**
   * @inheritDoc
   */
  public function sendPasswordReset(User $user, string $token): void {
    throw new NotificationException($this->reason);
  }

  /**
   * @inheritDoc
   */
  public function sendWallpaperModerationDecision(array $wallpaper): void {
    throw new NotificationException($this->reason);
  }
}
