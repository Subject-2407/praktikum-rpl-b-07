<?php

declare(strict_types=1);

namespace Scapes\Tests\Unit\Infrastructure\Auth;

use PHPUnit\Framework\TestCase;
use Scapes\Infrastructure\Auth\JWTManager;

class JWTManagerTest extends TestCase {

  public function test_create_token_menggunakan_ttl_kustom_dalam_menit(): void {
    $jwtManager = new JWTManager('test-secret', 45);

    $issuedAtBefore = time();
    $token = $jwtManager->createToken([
      'sub' => 1,
      'role' => 'contributor',
    ]);
    $issuedAtAfter = time();

    $this->assertGreaterThanOrEqual($issuedAtBefore + (45 * 60), $token['exp']);
    $this->assertLessThanOrEqual($issuedAtAfter + (45 * 60), $token['exp']);
  }
}
