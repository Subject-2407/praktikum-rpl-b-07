<?php

/**
 * Domain Entity: Category
 *
 * Merepresentasikan kategori wallpaper (Minimalist, Nature, Abstract, dll).
 * Digunakan sebagai basis penamaan folder penyimpanan lokal.
 *
 * @package Scapes\Core\Domain
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Domain;

/**
 * Kelas Category - Entitas domain untuk kategori wallpaper.
 *
 * @class Category
 */
class Category {

  /**
   * ID unik kategori.
   *
   * @var int
   */
  private int $id;

  /**
   * Nama kategori.
   *
   * @var string
   */
  private string $name;

  /**
   * Slug kategori (URL-friendly).
   *
   * @var string
   */
  private string $slug;

  /**
   * Waktu kategori dibuat.
   *
   * @var string
   */
  private string $createdAt;

  /**
   * Konstruktor Category.
   *
   * @param int $id ID unik kategori.
   * @param string $name Nama kategori.
   * @param string $slug Slug kategori.
   * @param string $createdAt Waktu dibuat.
   */
  public function __construct(
    int $id,
    string $name,
    string $slug,
    string $createdAt = ''
  ) {
    $this->id = $id;
    $this->name = $name;
    $this->slug = $slug;
    $this->createdAt = $createdAt ?: date('Y-m-d H:i:s');
  }

  /**
   * Mendapatkan ID kategori.
   *
   * @return int
   */
  public function getId(): int {
    return $this->id;
  }

  /**
   * Mendapatkan nama kategori.
   *
   * @return string
   */
  public function getName(): string {
    return $this->name;
  }

  /**
   * Mendapatkan slug kategori.
   *
   * @return string
   */
  public function getSlug(): string {
    return $this->slug;
  }

  /**
   * Mendapatkan waktu dibuat.
   *
   * @return string
   */
  public function getCreatedAt(): string {
    return $this->createdAt;
  }
}
