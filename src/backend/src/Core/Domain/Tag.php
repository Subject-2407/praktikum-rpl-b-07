<?php

/**
 * Domain Entity: Tag
 *
 * Merepresentasikan tag untuk wallpaper (4k, Dark, Neon, dll).
 * Digunakan untuk pencarian keyword.
 *
 * @package Scapes\Core\Domain
 * @version 1.0
 */

declare(strict_types=1);

namespace Scapes\Core\Domain;

/**
 * Kelas Tag - Entitas domain untuk tag wallpaper.
 *
 * @class Tag
 */
class Tag {

  /**
   * ID unik tag.
   *
   * @var int
   */
  private int $id;

  /**
   * Nama tag.
   *
   * @var string
   */
  private string $name;

  /**
   * Slug tag (URL-friendly).
   *
   * @var string
   */
  private string $slug;

  /**
   * Waktu tag dibuat.
   *
   * @var string
   */
  private string $createdAt;

  /**
   * Konstruktor Tag.
   *
   * @param int $id ID unik tag.
   * @param string $name Nama tag.
   * @param string $slug Slug tag.
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
   * Mendapatkan ID tag.
   *
   * @return int
   */
  public function getId(): int {
    return $this->id;
  }

  /**
   * Mendapatkan nama tag.
   *
   * @return string
   */
  public function getName(): string {
    return $this->name;
  }

  /**
   * Mendapatkan slug tag.
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
