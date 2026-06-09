<?php

/**
 * Implementasi no-op untuk notifikasi email.
 *
 * @package Scapes\Infrastructure\Notification
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Notification;

use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Core\Domain\User;

/**
 * Kelas NullEmailNotification - Tidak melakukan pengiriman email.
 */
class NullEmailNotification implements EmailNotificationInterface {

  /**
   * @inheritDoc
   */
  public function sendEmailVerification(User $user, string $token): void {
  }

  /**
   * @inheritDoc
   */
  public function sendPasswordReset(User $user, string $token): void {
  }

  /**
   * @inheritDoc
   */
  public function sendWallpaperModerationDecision(array $wallpaper): void {
  }
}
