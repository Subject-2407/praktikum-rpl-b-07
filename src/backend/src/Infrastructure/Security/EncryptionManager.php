<?php

/**
 * Manager untuk Enkripsi dan Dekripsi Data
 *
 * Kelas ini menyediakan utilitas untuk mengenkripsi dan mendekripsi data
 * menggunakan algoritma AES-256-CBC. Digunakan untuk mengamankan data sensitif
 * di database seperti token session atau API keys.
 *
 * @package Scapes\Infrastructure\Security
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Infrastructure\Security;

/**
 * Kelas EncryptionManager - Menangani keamanan data dengan enkripsi.
 *
 * @class EncryptionManager
 */
class EncryptionManager {

  /**
   * Key rahasia untuk enkripsi.
   *
   * @var string
   */
  private string $key;

  /**
   * Method enkripsi yang digunakan.
   *
   * @var string
   */
  private const METHOD = 'AES-256-CBC';

  /**
   * Konstruktor EncryptionManager.
   *
   * @param string $key Key rahasia (minimal 32 karakter untuk AES-256).
   */
  public function __construct(string $key) {
    // Pastikan key cukup panjang, jika tidak pad dengan hash
    if (strlen($key) < 32) {
      $this->key = hash('sha256', $key, true);
    } else {
      $this->key = substr($key, 0, 32);
    }
  }

  /**
   * Mengenkripsi data string.
   *
   * @param string $data Data plaintext.
   * @return string Data terenkripsi dalam format base64 (iv:data).
   */
  public function encrypt(string $data): string {
    $ivLength = openssl_cipher_iv_length(self::METHOD);
    $iv = openssl_random_pseudo_bytes($ivLength);
    
    $encrypted = openssl_encrypt(
      $data,
      self::METHOD,
      $this->key,
      0,
      $iv
    );

    return base64_encode($iv . ':' . $encrypted);
  }

  /**
   * Mendekripsi data terenkripsi.
   *
   * @param string $encryptedData Data base64 (iv:data).
   * @return string|null Data plaintext atau null jika gagal.
   */
  public function decrypt(string $encryptedData): ?string {
    $decoded = base64_decode($encryptedData);
    if ($decoded === false) {
      return null;
    }

    $parts = explode(':', $decoded, 2);
    if (count($parts) !== 2) {
      return null;
    }

    [$iv, $data] = $parts;
    $ivLength = openssl_cipher_iv_length(self::METHOD);

    if (strlen($iv) !== $ivLength) {
      return null;
    }

    $decrypted = openssl_decrypt(
      $data,
      self::METHOD,
      $this->key,
      0,
      $iv
    );

    return $decrypted !== false ? $decrypted : null;
  }

  /**
   * Membuat hash deterministik dari data untuk keperluan pencarian di DB.
   *
   * @param string $data Data asli.
   * @return string Hash data.
   */
  public function hash(string $data): string {
    return hash('sha256', $data);
  }
}
