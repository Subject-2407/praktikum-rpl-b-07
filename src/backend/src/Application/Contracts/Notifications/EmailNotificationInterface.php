<?php

/**
 * Kontrak notifikasi email aplikasi.
 *
 * @package Scapes\Application\Contracts\Notifications
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Application\Contracts\Notifications;

use Scapes\Core\Domain\User;

/**
 * Interface EmailNotificationInterface - Pengiriman email transaksional.
 */
interface EmailNotificationInterface {

  /**
   * Mengirim email verifikasi akun contributor.
   *
   * @param User $user Pengguna yang baru terdaftar.
   * @param string $token Token verifikasi email.
   *
   * @return void
   */
  public function sendEmailVerification(User $user, string $token): void;

  /**
   * Mengirim email reset password.
   *
   * @param User $user Pengguna pemilik akun.
   * @param string $token Token reset password.
   *
   * @return void
   */
  public function sendPasswordReset(User $user, string $token): void;

  /**
   * Mengirim notifikasi hasil moderasi wallpaper.
   *
   * @param array<string, mixed> $wallpaper Detail wallpaper hasil moderasi.
   *
   * @return void
   */
  public function sendWallpaperModerationDecision(array $wallpaper): void;
}
