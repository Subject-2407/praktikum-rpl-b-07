<?php

/**
 * Unit Tests untuk ModerationReview Entity
 *
 * Menguji ModerationReview domain entity dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Domain
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Domain;

use PHPUnit\Framework\TestCase;
use Scapes\Core\Domain\ModerationReview;

class ModerationReviewTest extends TestCase {

  /**
   * Test: ModerationReview entity created successfully
   * Arrange: Initialize review dengan data approval
   * Act: Create review entity
   * Assert: Semua property tersimpan dengan benar
   */
  public function test_moderation_review_entity_created_successfully(): void {
    // Arrange
    $id = 1;
    $wallpaperId = 1;
    $adminId = 100;
    $decision = 'approved';

    // Act
    $review = new ModerationReview($id, $wallpaperId, $adminId, $decision);

    // Assert
    $this->assertEquals($id, $review->getId());
    $this->assertEquals($wallpaperId, $review->getWallpaperId());
    $this->assertEquals($adminId, $review->getAdminId());
    $this->assertEquals($decision, $review->getDecision());
    $this->assertTrue($review->isApproved());
  }

  /**
   * Test: ModerationReview dengan reason untuk rejection
   * Arrange: Create review dengan decision rejected dan reason
   * Act: Access decision dan reason
   * Assert: Decision dan reason tersimpan dengan benar
   */
  public function test_moderation_review_with_rejection_reason(): void {
    // Arrange
    $reason = 'Konten tidak sesuai dengan panduan komunitas';
    $decision = 'rejected';

    // Act
    $review = new ModerationReview(1, 1, 100, $decision, $reason);

    // Assert
    $this->assertEquals($reason, $review->getReason());
    $this->assertTrue($review->isRejected());
    $this->assertFalse($review->isApproved());
  }

  /**
   * Test: ModerationReview decision checking methods
   * Arrange: Create approved dan rejected reviews
   * Act: Check decision methods
   * Assert: isApproved dan isRejected return correct values
   */
  public function test_moderation_review_decision_checking_methods(): void {
    // Arrange
    $approvedReview = new ModerationReview(1, 1, 100, 'approved');
    $rejectedReview = new ModerationReview(2, 2, 100, 'rejected', 'Invalid content');

    // Act & Assert
    $this->assertTrue($approvedReview->isApproved());
    $this->assertFalse($approvedReview->isRejected());

    $this->assertTrue($rejectedReview->isRejected());
    $this->assertFalse($rejectedReview->isApproved());
  }
}
