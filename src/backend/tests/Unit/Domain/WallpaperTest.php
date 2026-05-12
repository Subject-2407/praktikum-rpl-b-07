<?php

/**
 * Unit Tests untuk Wallpaper Entity
 *
 * Menguji Wallpaper domain entity dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Domain
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Domain;

use PHPUnit\Framework\TestCase;
use Scapes\Core\Domain\Wallpaper;

class WallpaperTest extends TestCase {

  /**
   * Test: Wallpaper entity created successfully
   * Arrange: Initialize wallpaper dengan data
   * Act: Create wallpaper entity
   * Assert: Semua property tersimpan dengan benar
   */
  public function test_wallpaper_entity_created_successfully(): void {
    // Arrange
    $id = 1;
    $contributorId = 1;
    $categoryId = 1;
    $title = 'Mountain View';
    $filePath = '/storage/wallpapers/mountain.jpg';
    $fileName = 'mountain.jpg';
    $fileSizeKb = 512;
    $mimeType = 'image/jpeg';
    $width = 1920;
    $height = 1080;
    $status = 'pending';

    // Act
    $wallpaper = new Wallpaper(
      $id,
      $contributorId,
      $categoryId,
      $title,
      $filePath,
      $fileName,
      $fileSizeKb,
      $mimeType,
      $width,
      $height,
      $status
    );

    // Assert
    $this->assertEquals($id, $wallpaper->getId());
    $this->assertEquals($title, $wallpaper->getTitle());
    $this->assertEquals($status, $wallpaper->getStatus());
    $this->assertTrue($wallpaper->isPending());
    $this->assertFalse($wallpaper->isApproved());
  }

  /**
   * Test: Wallpaper status checking methods work
   * Arrange: Create wallpaper dengan status berbeda
   * Act: Check status
   * Assert: Status methods return correct values
   */
  public function test_wallpaper_status_checking_methods(): void {
    // Arrange
    $pendingWallpaper = new Wallpaper(1, 1, 1, 'T', '/p', 'f', 512, 'image/jpeg', 1920, 1080, 'pending');
    $approvedWallpaper = new Wallpaper(2, 1, 1, 'T', '/p', 'f', 512, 'image/jpeg', 1920, 1080, 'approved');
    $rejectedWallpaper = new Wallpaper(3, 1, 1, 'T', '/p', 'f', 512, 'image/jpeg', 1920, 1080, 'rejected');
    $scheduledWallpaper = new Wallpaper(4, 1, 1, 'T', '/p', 'f', 512, 'image/jpeg', 1920, 1080, 'scheduled');

    // Act & Assert
    $this->assertTrue($pendingWallpaper->isPending());
    $this->assertFalse($pendingWallpaper->isApproved());

    $this->assertTrue($approvedWallpaper->isApproved());
    $this->assertFalse($approvedWallpaper->isPending());

    $this->assertTrue($rejectedWallpaper->isRejected());
    $this->assertFalse($rejectedWallpaper->isApproved());

    $this->assertTrue($scheduledWallpaper->isScheduled());
  }

  /**
   * Test: Wallpaper status can be updated
   * Arrange: Create wallpaper dengan status pending
   * Act: Set status to approved
   * Assert: getStatus returns approved dan isApproved returns true
   */
  public function test_wallpaper_status_can_be_updated(): void {
    // Arrange
    $wallpaper = new Wallpaper(1, 1, 1, 'T', '/p', 'f', 512, 'image/jpeg', 1920, 1080, 'pending');
    $this->assertTrue($wallpaper->isPending());

    // Act
    $wallpaper->setStatus('approved');

    // Assert
    $this->assertEquals('approved', $wallpaper->getStatus());
    $this->assertTrue($wallpaper->isApproved());
    $this->assertFalse($wallpaper->isPending());
  }

  /**
   * Test: Wallpaper with optional fields
   * Arrange: Create wallpaper dengan description dan scheduled_at
   * Act: Access optional fields
   * Assert: Optional fields tersimpan dengan benar
   */
  public function test_wallpaper_with_optional_fields(): void {
    // Arrange
    $description = 'Beautiful mountain landscape';
    $scheduledAt = '2026-05-20 10:00:00';

    // Act
    $wallpaper = new Wallpaper(
      1,
      1,
      1,
      'Mountain View',
      '/p',
      'f',
      512,
      'image/jpeg',
      1920,
      1080,
      'scheduled',
      $description,
      $scheduledAt
    );

    // Assert
    $this->assertEquals($description, $wallpaper->getDescription());
    $this->assertEquals($scheduledAt, $wallpaper->getScheduledAt());
  }
}
