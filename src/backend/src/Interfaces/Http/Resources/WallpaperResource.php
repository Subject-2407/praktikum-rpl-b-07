<?php

/**
 * Presenter resource wallpaper untuk response HTTP.
 *
 * Kelas ini mengubah row database menjadi struktur JSON sesuai kontrak API
 * tanpa menaruh detail presentasi di use case.
 *
 * @package Scapes\Interfaces\Http\Resources
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Interfaces\Http\Resources;

/**
 * Kelas WallpaperResource - Formatter response wallpaper.
 */
class WallpaperResource {

  /**
   * Membentuk data wallpaper publik.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   * @param string $baseUrl Base URL request.
   * @param bool $detail True untuk detail lengkap.
   *
   * @return array<string, mixed>
   */
  public static function public(
    array $wallpaper,
    string $baseUrl,
    bool $detail = false
  ): array {
    $data = [
      'id' => (int) $wallpaper['id'],
      'title' => (string) $wallpaper['title'],
      'description' => $wallpaper['description'],
      'file_path' => self::fileUrl((string) $wallpaper['file_path'], $baseUrl),
      'width' => (int) $wallpaper['width'],
      'height' => (int) $wallpaper['height'],
      'target_device' => (string) $wallpaper['target_device'],
      'category' => $wallpaper['category'],
      'tags' => $wallpaper['tags'],
      'contributor' => $wallpaper['contributor'],
      'published_at' => self::date($wallpaper['published_at']),
    ];

    if ($detail) {
      $data['file_name'] = (string) $wallpaper['file_name'];
      $data['file_size_kb'] = (int) $wallpaper['file_size_kb'];
      $data['mime_type'] = (string) $wallpaper['mime_type'];
      $data['status'] = (string) $wallpaper['status'];
      $data['created_at'] = self::date($wallpaper['created_at']);
    }

    return $data;
  }

  /**
   * Membentuk data item wallpaper contributor.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   *
   * @return array<string, mixed>
   */
  public static function contributor(array $wallpaper): array {
    $data = [
      'id' => (int) $wallpaper['id'],
      'title' => (string) $wallpaper['title'],
      'status' => (string) $wallpaper['status'],
      'target_device' => (string) $wallpaper['target_device'],
      'category' => $wallpaper['category'],
      'tags' => $wallpaper['tags'],
      'moderation' => self::moderation($wallpaper['moderation']),
      'created_at' => self::date($wallpaper['created_at']),
      'updated_at' => self::date($wallpaper['updated_at']),
    ];

    if (
      (string) $wallpaper['status'] === 'pending'
      && strtotime((string) $wallpaper['created_at']) < strtotime('-3 days')
    ) {
      $data['is_review_overdue'] = true;
    }

    return $data;
  }

  /**
   * Membentuk response hasil upload.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   *
   * @return array<string, mixed>
   */
  public static function uploaded(array $wallpaper): array {
    return [
      'id' => (int) $wallpaper['id'],
      'title' => (string) $wallpaper['title'],
      'status' => (string) $wallpaper['status'],
      'file_name' => (string) $wallpaper['file_name'],
      'file_size_kb' => (int) $wallpaper['file_size_kb'],
      'width' => (int) $wallpaper['width'],
      'height' => (int) $wallpaper['height'],
      'target_device' => (string) $wallpaper['target_device'],
      'category' => $wallpaper['category'],
      'tags' => $wallpaper['tags'],
      'created_at' => self::date($wallpaper['created_at']),
    ];
  }

  /**
   * Membentuk response hasil update metadata.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   *
   * @return array<string, mixed>
   */
  public static function updated(array $wallpaper): array {
    return [
      'id' => (int) $wallpaper['id'],
      'title' => (string) $wallpaper['title'],
      'description' => $wallpaper['description'],
      'status' => (string) $wallpaper['status'],
      'target_device' => (string) $wallpaper['target_device'],
      'category' => $wallpaper['category'],
      'tags' => $wallpaper['tags'],
      'updated_at' => self::date($wallpaper['updated_at']),
    ];
  }

  /**
   * Membentuk item queue moderasi admin.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   * @param string $baseUrl Base URL request.
   *
   * @return array<string, mixed>
   */
  public static function adminQueue(array $wallpaper, string $baseUrl): array {
    $data = [
      'id' => (int) $wallpaper['id'],
      'title' => (string) $wallpaper['title'],
      'file_path' => self::fileUrl((string) $wallpaper['file_path'], $baseUrl),
      'width' => (int) $wallpaper['width'],
      'height' => (int) $wallpaper['height'],
      'target_device' => (string) $wallpaper['target_device'],
      'mime_type' => (string) $wallpaper['mime_type'],
      'file_size_kb' => (int) $wallpaper['file_size_kb'],
      'status' => (string) $wallpaper['status'],
      'category' => $wallpaper['category'],
      'tags' => $wallpaper['tags'],
      'contributor' => $wallpaper['contributor'],
      'moderation' => self::moderation($wallpaper['moderation']),
      'created_at' => self::date($wallpaper['created_at']),
      'updated_at' => self::date($wallpaper['updated_at']),
      'published_at' => self::date($wallpaper['published_at']),
    ];

    if (
      (string) $wallpaper['status'] === 'pending'
      && strtotime((string) $wallpaper['created_at']) < strtotime('-3 days')
    ) {
      $data['is_review_overdue'] = true;
    }

    return $data;
  }

  /**
   * Membentuk response hasil moderasi.
   *
   * @param array<string, mixed> $wallpaper Data wallpaper.
   *
   * @return array<string, mixed>
   */
  public static function moderated(array $wallpaper): array {
    $data = [
      'id' => (int) $wallpaper['id'],
      'status' => (string) $wallpaper['status'],
      'moderation' => self::moderation($wallpaper['moderation']),
    ];

    if ((string) $wallpaper['status'] === 'approved') {
      $data['published_at'] = self::date($wallpaper['published_at']);
    }

    return $data;
  }

  /**
   * Membentuk URL file dari path relatif.
   *
   * @param string $path Path relatif storage.
   * @param string $baseUrl Base URL request.
   *
   * @return string URL file.
   */
  private static function fileUrl(string $path, string $baseUrl): string {
    if (str_starts_with($path, 'http://') || str_starts_with($path, 'https://')) {
      return $path;
    }

    return rtrim($baseUrl, '/') . '/' . ltrim(str_replace('\\', '/', $path), '/');
  }

  /**
   * Format tanggal menjadi ISO 8601 UTC.
   *
   * @param mixed $value Nilai tanggal database.
   *
   * @return string|null Tanggal ISO atau null.
   */
  private static function date(mixed $value): ?string {
    if ($value === null || $value === '') {
      return null;
    }

    $timestamp = strtotime((string) $value);
    return $timestamp === false ? null : gmdate('Y-m-d\TH:i:s\Z', $timestamp);
  }

  /**
   * Format data moderasi.
   *
   * @param mixed $moderation Data moderasi dari repository.
   *
   * @return array<string, mixed>|null
   */
  private static function moderation(mixed $moderation): ?array {
    if (!is_array($moderation)) {
      return null;
    }

    return [
      'decision' => (string) $moderation['decision'],
      'reason' => $moderation['reason'],
      'reviewed_at' => self::date($moderation['reviewed_at']),
      'admin_id' => (int) $moderation['admin_id'],
    ];
  }
}
