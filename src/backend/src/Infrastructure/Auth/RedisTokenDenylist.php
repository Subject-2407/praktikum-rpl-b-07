<?php

/**
 * Implementasi denylist JWT berbasis Redis.
 *
 * Kelas ini memakai Predis untuk menyimpan JTI token yang sudah logout
 * sampai masa kedaluwarsa token tersebut berakhir.
 *
 * @package Scapes\Infrastructure\Auth
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Auth;

use Predis\Client;
use RuntimeException;
use Scapes\Application\Contracts\Auth\TokenDenylistInterface;

/**
 * Kelas RedisTokenDenylist - Penyimpanan denylist JWT di Redis.
 */
class RedisTokenDenylist implements TokenDenylistInterface {

  /**
   * Prefix key Redis untuk denylist JWT.
   *
   * @var string
   */
  private const KEY_PREFIX = 'jwt:denylist:';

  /**
   * Client Predis.
   *
   * @var Client
   */
  private Client $client;

  /**
   * Prefix aplikasi untuk namespace key Redis.
   *
   * @var string
   */
  private string $prefix;

  /**
   * Konstruktor RedisTokenDenylist.
   *
   * @param array<string, mixed> $config Konfigurasi koneksi Redis.
   */
  public function __construct(array $config) {
    if (!class_exists(Client::class)) {
      throw new RuntimeException('Library predis/predis belum terpasang.');
    }

    $this->prefix = (string) ($config['prefix'] ?? 'scapes:');
    unset($config['prefix']);

    $this->client = new Client($config);
  }

  /**
   * Memasukkan JTI token ke denylist sampai token kedaluwarsa.
   *
   * @param string $jti Identitas unik token JWT.
   * @param int $expiresAt Unix timestamp kedaluwarsa token.
   *
   * @return void
   */
  public function deny(string $jti, int $expiresAt): void {
    $ttl = max(1, $expiresAt - time());
    $this->client->setex($this->buildKey($jti), $ttl, '1');
  }

  /**
   * Mengecek apakah JTI token sudah dicabut.
   *
   * @param string $jti Identitas unik token JWT.
   *
   * @return bool True jika token sudah masuk denylist.
   */
  public function isDenied(string $jti): bool {
    return (bool) $this->client->exists($this->buildKey($jti));
  }

  /**
   * Membuat key Redis untuk JTI.
   *
   * @param string $jti Identitas unik token JWT.
   *
   * @return string Key Redis final.
   */
  private function buildKey(string $jti): string {
    return $this->prefix . self::KEY_PREFIX . $jti;
  }
}
