<?php

/**
 * Unit Tests untuk WallpaperResource.
 *
 * Menguji presenter response wallpaper agar field penting tetap konsisten
 * dengan kontrak API.
 *
 * @package Scapes\Tests\Unit\Interfaces\Http\Resources
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Interfaces\Http\Resources;

use PHPUnit\Framework\TestCase;
use Scapes\Interfaces\Http\Resources\WallpaperResource;

class WallpaperResourceTest extends TestCase {

  public function test_contributor_mengikutkan_resolusi_dan_tipe_file(): void {
    $wallpaper = [
      'id' => '650e8400-e29b-41d4-a716-446655440001',
      'title' => 'Neon City Lights',
      'description' => 'Cyberpunk skyline with glowing neon reflections.',
      'file_path' => 'storage/wallpapers/pending/8/file.jpg',
      'thumbnail_path' => 'storage/wallpapers/pending/8/thumbnails/file.webp',
      'width' => 3840,
      'height' => 2160,
      'mime_type' => 'image/jpeg',
      'status' => 'pending',
      'target_device' => 'desktop',
      'category' => ['id' => 8, 'name' => 'Technology', 'slug' => 'technology'],
      'tags' => [['id' => 3, 'name' => 'neon', 'slug' => 'neon']],
      'proposed_tags' => [],
      'moderation' => null,
      'created_at' => '2026-04-19T12:00:00Z',
      'updated_at' => '2026-04-19T12:00:00Z',
    ];

    $response = WallpaperResource::contributor(
      $wallpaper,
      'https://scapes.my.id'
    );

    $this->assertSame(3840, $response['width']);
    $this->assertSame(2160, $response['height']);
    $this->assertSame('image/jpeg', $response['mime_type']);
    $this->assertSame(
      'https://scapes.my.id/storage/wallpapers/pending/8/file.jpg',
      $response['file_path']
    );
    $this->assertSame(
      'https://scapes.my.id/storage/wallpapers/pending/8/thumbnails/file.webp',
      $response['thumbnail_path']
    );
  }
}
