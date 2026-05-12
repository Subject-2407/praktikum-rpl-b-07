<?php

/**
 * Unit Tests untuk User Entity
 *
 * Menguji User domain entity dengan pattern AAA.
 *
 * @package Scapes\Tests\Unit\Domain
 */

declare(strict_types=1);

namespace Scapes\Tests\Unit\Domain;

use PHPUnit\Framework\TestCase;
use Scapes\Core\Domain\User;

class UserTest extends TestCase {

  /**
   * Test: User entity created successfully
   * Arrange: Initialize user dengan data
   * Act: Create user entity
   * Assert: Semua property tersimpan dengan benar
   */
  public function test_user_entity_created_successfully(): void {
    // Arrange
    $id = 1;
    $email = 'user@example.com';
    $passwordHash = password_hash('password123', PASSWORD_BCRYPT, ['cost' => 12]);
    $role = 'contributor';

    // Act
    $user = new User($id, $email, $passwordHash, $role);

    // Assert
    $this->assertEquals($id, $user->getId());
    $this->assertEquals($email, $user->getEmail());
    $this->assertEquals($passwordHash, $user->getPasswordHash());
    $this->assertEquals($role, $user->getRole());
    $this->assertFalse($user->isVerified());
  }

  /**
   * Test: Password verification works correctly
   * Arrange: User dengan password hash
   * Act: Verify correct password
   * Assert: Password verification success
   */
  public function test_password_verification_success(): void {
    // Arrange
    $password = 'password123';
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $user = new User(1, 'user@example.com', $passwordHash, 'contributor');

    // Act & Assert
    $this->assertTrue($user->verifyPassword($password));
  }

  /**
   * Test: Password verification fails with wrong password
   * Arrange: User dengan password hash
   * Act: Verify wrong password
   * Assert: Password verification fails
   */
  public function test_password_verification_fails_with_wrong_password(): void {
    // Arrange
    $password = 'password123';
    $wrongPassword = 'wrongpassword';
    $passwordHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $user = new User(1, 'user@example.com', $passwordHash, 'contributor');

    // Act & Assert
    $this->assertFalse($user->verifyPassword($wrongPassword));
  }

  /**
   * Test: User role checking methods work
   * Arrange: User dengan role admin
   * Act: Check role
   * Assert: isAdmin returns true, isContributor returns false
   */
  public function test_user_role_checking_methods(): void {
    // Arrange
    $admin = new User(1, 'admin@example.com', 'hash', 'admin');
    $contributor = new User(2, 'user@example.com', 'hash', 'contributor');

    // Act & Assert
    $this->assertTrue($admin->isAdmin());
    $this->assertFalse($admin->isContributor());
    $this->assertTrue($contributor->isContributor());
    $this->assertFalse($contributor->isAdmin());
  }

  /**
   * Test: Email verification status can be set
   * Arrange: User baru dengan is_verified = false
   * Act: Set verified to true
   * Assert: isVerified returns true
   */
  public function test_email_verification_status_can_be_set(): void {
    // Arrange
    $user = new User(1, 'user@example.com', 'hash', 'contributor', false);
    $this->assertFalse($user->isVerified());

    // Act
    $user->setVerified(true);

    // Assert
    $this->assertTrue($user->isVerified());
  }
}
