<?php

/**
 * Unit Tests untuk ModerateWallpaperUseCase
 *
 * Menguji fungsi moderasi wallpaper dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Application\UseCases
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Application\UseCases;

use PHPUnit\Framework\TestCase;
use Scapes\Application\Contracts\Notifications\EmailNotificationInterface;
use Scapes\Application\UseCases\Moderation\ModerateWallpaperUseCase;
use Scapes\Core\Domain\ModerationReview;
use Scapes\Core\Domain\Wallpaper;
use Scapes\Core\Exceptions\ValidationException;
use Scapes\Core\Exceptions\NotFoundException;
use Scapes\Infrastructure\Storage\FileStorage;
use Scapes\Infrastructure\Repository\WallpaperRepository;
use Scapes\Infrastructure\Repository\ModerationReviewRepository;

class ModerateWallpaperUseCaseTest extends TestCase {

  private WallpaperRepository $wallpaperRepository;
  private ModerationReviewRepository $moderationRepository;
  private EmailNotificationInterface $emailNotification;
  private ModerateWallpaperUseCase $useCase;

  protected function setUp(): void {
    $this->wallpaperRepository = $this->createMock(WallpaperRepository::class);
    $this->moderationRepository = $this->createMock(ModerationReviewRepository::class);
    $this->emailNotification = $this->createMock(EmailNotificationInterface::class);
    $this->useCase = new ModerateWallpaperUseCase(
      $this->wallpaperRepository,
      $this->moderationRepository,
      null,
      $this->emailNotification
    );
  }

  /**
   * Test: Approve wallpaper berhasil
   * Arrange: Wallpaper pending dan admin approval
   * Act: Execute use case
   * Assert: Review disimpan dan status diubah ke approved
   */
  public function test_moderate_approve_wallpaper_berhasil(): void {
    // Arrange
    $wallpaperId = 1;
    $adminId = 100;
    $wallpaper = new Wallpaper(
      $wallpaperId,
      1,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080,
      'pending'
    );

    $review = new ModerationReview(
      1,
      $wallpaperId,
      $adminId,
      'approved'
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    $this->moderationRepository
      ->expects($this->once())
      ->method('save')
      ->willReturn($review);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('save');

    // Act
    $result = $this->useCase->execute($wallpaperId, $adminId, 'approved');

    // Assert
    $this->assertInstanceOf(ModerationReview::class, $result);
    $this->assertTrue($result->isApproved());
  }

  /**
   * Test: Reject wallpaper dengan reason berhasil
   * Arrange: Wallpaper pending dan admin rejection
   * Act: Execute use case
   * Assert: Review disimpan dengan reason dan status diubah ke rejected
   */
  public function test_moderate_reject_wallpaper_berhasil(): void {
    // Arrange
    $wallpaperId = 1;
    $adminId = 100;
    $reason = 'Konten tidak sesuai dengan panduan komunitas';
    $wallpaper = new Wallpaper(
      $wallpaperId,
      1,
      1,
      'Mountain View',
      '/path/file.jpg',
      'file.jpg',
      512,
      'image/jpeg',
      1920,
      1080,
      'pending'
    );

    $review = new ModerationReview(
      1,
      $wallpaperId,
      $adminId,
      'rejected',
      $reason
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn($wallpaper);

    $this->moderationRepository
      ->expects($this->once())
      ->method('save')
      ->willReturn($review);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('save');

    // Act
    $result = $this->useCase->execute($wallpaperId, $adminId, 'rejected', $reason);

    // Assert
    $this->assertInstanceOf(ModerationReview::class, $result);
    $this->assertTrue($result->isRejected());
    $this->assertEquals($reason, $result->getReason());
  }

  /**
   * Test: Moderate gagal karena wallpaper tidak ditemukan
   * Arrange: Wallpaper ID tidak ada
   * Act: Execute use case
   * Assert: NotFoundException dilempar
   */
  public function test_moderate_wallpaper_tidak_ada_gagal(): void {
    // Arrange
    $wallpaperId = 999;

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->with($wallpaperId)
      ->willReturn(null);

    // Assert
    $this->expectException(NotFoundException::class);
    $this->expectExceptionMessage('Wallpaper tidak ditemukan');

    // Act
    $this->useCase->execute($wallpaperId, 100, 'approved');
  }

  /**
   * Test: Moderate gagal karena decision tidak valid
   * Arrange: Decision bukan 'approved' atau 'rejected'
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_moderate_dengan_decision_invalid_gagal(): void {
    // Arrange

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Keputusan harus approved atau rejected');

    // Act
    $this->useCase->execute(1, 100, 'invalid_decision');
  }

  /**
   * Test: Moderate gagal karena reason tidak ada saat reject
   * Arrange: Decision reject tapi reason kosong
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_moderate_reject_tanpa_reason_gagal(): void {
    // Arrange

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Alasan penolakan wajib diisi');

    // Act
    $this->useCase->execute(1, 100, 'rejected', '');
  }

  /**
   * Test: Moderate gagal karena wallpaper sudah dimoderasi
   * Arrange: Wallpaper sudah berstatus approved/rejected
   * Act: Execute use case
   * Assert: ValidationException dilempar
   */
  public function test_moderate_wallpaper_sudah_dimoderasi_gagal(): void {
    // Arrange
    $wallpaper = new Wallpaper(
      1, 1, 1, 'Title', '/path', 'file.jpg', 512, 'image/jpeg', 1920, 1080, 'approved'
    );

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('findByIdEntity')
      ->willReturn($wallpaper);

    // Assert
    $this->expectException(ValidationException::class);
    $this->expectExceptionMessage('Wallpaper sudah dimoderasi sebelumnya');

    // Act
    $this->useCase->execute(1, 100, 'approved');
  }

  public function test_moderate_api_flow_mengirim_notifikasi_email(): void {
    $storage = $this->createMock(FileStorage::class);
    $useCase = new ModerateWallpaperUseCase(
      $this->wallpaperRepository,
      $this->moderationRepository,
      $storage,
      $this->emailNotification
    );

    $pendingWallpaper = [
      'id' => '15',
      'title' => 'Aurora Dreams',
      'status' => 'pending',
      'file_path' => 'wallpapers\\pending\\1\\15.jpeg',
      'thumbnail_path' => 'wallpapers\\pending\\1\\thumbnails\\15.webp',
      'category' => ['id' => 1, 'name' => 'Nature', 'slug' => 'nature'],
      'contributor' => [
        'id' => 3,
        'display_name' => 'Creator Four',
        'email' => 'creator4@example.com',
      ],
    ];
    $moderatedWallpaper = [
      'id' => '15',
      'title' => 'Aurora Dreams',
      'status' => 'rejected',
      'file_path' => 'wallpapers\\pending\\1\\15.jpeg',
      'thumbnail_path' => 'wallpapers\\pending\\1\\thumbnails\\15.webp',
      'category' => ['id' => 1, 'name' => 'Nature', 'slug' => 'nature'],
      'contributor' => [
        'id' => 3,
        'display_name' => 'Creator Four',
        'email' => 'creator4@example.com',
      ],
      'moderation' => [
        'decision' => 'rejected',
        'reason' => 'Needs better contrast',
        'reviewed_at' => '2026-06-10 12:00:00',
        'admin_id' => 100,
      ],
    ];

    $this->wallpaperRepository
      ->expects($this->exactly(2))
      ->method('findDetailedById')
      ->with('15')
      ->willReturnOnConsecutiveCalls($pendingWallpaper, $moderatedWallpaper);

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('transaction')
      ->willReturnCallback(fn (callable $callback): mixed => $callback());

    $this->moderationRepository
      ->expects($this->once())
      ->method('save');

    $this->wallpaperRepository
      ->expects($this->once())
      ->method('updateModerationState')
      ->with('15', 'rejected', null);

    $this->emailNotification
      ->expects($this->once())
      ->method('sendWallpaperModerationDecision')
      ->with($moderatedWallpaper);

    $result = $useCase->execute('15', 100, [
      'decision' => 'rejected',
      'reason' => 'Needs better contrast',
    ]);

    $this->assertSame($moderatedWallpaper, $result);
  }
}
