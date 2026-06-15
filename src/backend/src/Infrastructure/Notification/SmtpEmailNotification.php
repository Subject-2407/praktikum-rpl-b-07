<?php

/**
 * Implementasi notifikasi email aplikasi berbasis SMTP.
 *
 * @package Scapes\Infrastructure\Notification
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Notification;

use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Core\Domain\User;

/**
 * Kelas SmtpEmailNotification - Template email transaksional Scapes.
 */
class SmtpEmailNotification implements EmailNotificationInterface {

  /**
   * Mailer SMTP.
   *
   * @var SmtpMailer
   */
  private SmtpMailer $mailer;

  /**
   * Nama aplikasi.
   *
   * @var string
   */
  private string $appName;

  /**
   * URL dasar frontend.
   *
   * @var string
   */
  private string $frontendUrl;

  /**
   * URL verifikasi email.
   *
   * @var string
   */
  private string $emailVerificationUrl;

  /**
   * URL reset password.
   *
   * @var string
   */
  private string $passwordResetUrl;

  /**
   * Konstruktor SmtpEmailNotification.
   *
   * @param SmtpMailer $mailer Mailer SMTP.
   * @param string $appName Nama aplikasi.
   * @param string $frontendUrl URL frontend.
   * @param string $emailVerificationUrl URL halaman verifikasi.
   * @param string $passwordResetUrl URL halaman reset password.
   */
  public function __construct(
    SmtpMailer $mailer,
    string $appName = 'Scapes',
    string $frontendUrl = '',
    string $emailVerificationUrl = '',
    string $passwordResetUrl = ''
  ) {
    $this->mailer = $mailer;
    $this->appName = trim($appName) !== '' ? trim($appName) : 'Scapes';
    $this->frontendUrl = trim($frontendUrl);
    $this->emailVerificationUrl = trim($emailVerificationUrl);
    $this->passwordResetUrl = trim($passwordResetUrl);
  }

  /**
   * @inheritDoc
   */
  public function sendEmailVerification(User $user, string $token): void {
    $actionUrl = $this->buildTokenUrl(
      $this->emailVerificationUrl,
      $token,
      'token'
    );
    $body = implode("\n", [
      'Hello ' . $this->resolveGreetingName($user) . ',',
      '',
      'Thank you for registering at ' . $this->appName . '.',
      'Please verify your account using the link below:',
      $actionUrl,
      '',
      'If the link does not work, you can use this token manually:',
      $token,
      '',
      'This verification link expires in 24 hours.',
      '',
      'Regards,',
      $this->appName . ' Team',
    ]);

    $this->mailer->send(
      $user->getEmail(),
      'Verify your ' . $this->appName . ' account',
      $body
    );
  }

  /**
   * @inheritDoc
   */
  public function sendPasswordReset(User $user, string $token): void {
    $actionUrl = $this->buildTokenUrl(
      $this->passwordResetUrl,
      $token,
      'token'
    );
    $body = implode("\n", [
      'Hello ' . $this->resolveGreetingName($user) . ',',
      '',
      'We received a request to reset your ' . $this->appName . ' password.',
      'Use the link below to set a new password:',
      $actionUrl,
      '',
      'If the link does not work, you can use this token manually:',
      $token,
      '',
      'This reset link expires in 24 hours.',
      'If you did not request this, you can ignore this email.',
      '',
      'Regards,',
      $this->appName . ' Team',
    ]);

    $this->mailer->send(
      $user->getEmail(),
      'Reset your ' . $this->appName . ' password',
      $body
    );
  }

  /**
   * @inheritDoc
   */
  public function sendWallpaperModerationDecision(array $wallpaper): void {
    $contributor = $wallpaper['contributor'] ?? [];
    $email = is_array($contributor) ? (string) ($contributor['email'] ?? '') : '';
    if ($email === '') {
      return;
    }

    $displayName = is_array($contributor)
      ? (string) ($contributor['display_name'] ?? '')
      : '';
    $status = (string) ($wallpaper['status'] ?? '');
    $title = (string) ($wallpaper['title'] ?? 'Your wallpaper');
    $moderation = is_array($wallpaper['moderation'] ?? null)
      ? $wallpaper['moderation']
      : [];
    $reason = isset($moderation['reason']) && $moderation['reason'] !== null
      ? trim((string) $moderation['reason'])
      : '';

    if ($status === 'approved') {
      $body = implode("\n", [
        'Hello ' . $this->resolveName($displayName, $email) . ',',
        '',
        'Good news. Your wallpaper "' . $title . '" has been approved and is now publicly visible.',
        $this->frontendUrl !== ''
          ? 'You can open ' . $this->frontendUrl . ' to review it.'
          : 'You can open the application to review it.',
        '',
        'Regards,',
        $this->appName . ' Team',
      ]);

      $this->mailer->send(
        $email,
        'Your wallpaper has been approved',
        $body
      );
      return;
    }

    if ($status === 'rejected') {
      $bodyLines = [
        'Hello ' . $this->resolveName($displayName, $email) . ',',
        '',
        'Your wallpaper "' . $title . '" was rejected during moderation.',
      ];

      if ($reason !== '') {
        $bodyLines[] = 'Reason: ' . $reason;
      }

      $bodyLines[] = $this->frontendUrl !== ''
        ? 'Please review it in ' . $this->frontendUrl . ' and submit a revised version if needed.'
        : 'Please review it in the application and submit a revised version if needed.';
      $bodyLines[] = '';
      $bodyLines[] = 'Regards,';
      $bodyLines[] = $this->appName . ' Team';

      $this->mailer->send(
        $email,
        'Your wallpaper needs revision',
        implode("\n", $bodyLines)
      );
    }
  }

  /**
   * Membentuk URL aksi dengan token.
   *
   * @param string $configuredUrl URL dasar/templated.
   * @param string $token Token aksi.
   * @param string $queryKey Nama query string token.
   *
   * @return string
   */
  private function buildTokenUrl(string $configuredUrl, string $token, string $queryKey): string {
    $configuredUrl = trim($configuredUrl);
    if ($configuredUrl === '') {
      return $token;
    }

    if (str_contains($configuredUrl, '{token}')) {
      return str_replace('{token}', rawurlencode($token), $configuredUrl);
    }

    $separator = str_contains($configuredUrl, '?') ? '&' : '?';
    return $configuredUrl . $separator . rawurlencode($queryKey) . '=' . rawurlencode($token);
  }

  /**
   * Menentukan nama sapaan pengguna.
   *
   * @param User $user Pengguna.
   *
   * @return string
   */
  private function resolveGreetingName(User $user): string {
    return $this->resolveName($user->getDisplayName(), $user->getEmail());
  }

  /**
   * Menentukan nama tampil fallback dari email.
   *
   * @param string $displayName Nama tampilan.
   * @param string $email Email pengguna.
   *
   * @return string
   */
  private function resolveName(string $displayName, string $email): string {
    $displayName = trim($displayName);
    if ($displayName !== '' && !filter_var($displayName, FILTER_VALIDATE_EMAIL)) {
      return $displayName;
    }

    $localPart = explode('@', $email)[0] ?? 'there';
    return $localPart !== '' ? $localPart : 'there';
  }
}
