-- ============================================================
-- Migration: Search analytics, trending categories, tag proposals
-- Date: 2026-06-16
-- ============================================================

USE scapes;

CREATE TABLE IF NOT EXISTS search_logs (
  id                  BIGINT       NOT NULL AUTO_INCREMENT,
  keyword_raw         VARCHAR(255) NOT NULL,
  keyword_normalized  VARCHAR(255) NOT NULL,
  keyword_slug        VARCHAR(255) NOT NULL,
  source_slug         VARCHAR(100) NULL DEFAULT NULL,
  matched_category_id INT          NULL DEFAULT NULL,
  matched_tag_id      INT          NULL DEFAULT NULL,
  client_hash         CHAR(64)     NULL DEFAULT NULL,
  locale              VARCHAR(20)  NULL DEFAULT NULL,
  result_count        INT          NULL DEFAULT NULL,
  searched_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_search_logs PRIMARY KEY (id),
  CONSTRAINT fk_search_logs_category
    FOREIGN KEY (matched_category_id) REFERENCES categories (id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_search_logs_tag
    FOREIGN KEY (matched_tag_id) REFERENCES tags (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_search_logs_keyword_normalized (keyword_normalized),
  INDEX idx_search_logs_keyword_slug (keyword_slug),
  INDEX idx_search_logs_source_slug (source_slug),
  INDEX idx_search_logs_client_hash (client_hash),
  INDEX idx_search_logs_searched_at (searched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Log pencarian user yang privacy-safe untuk rekomendasi dan trending.';

CREATE TABLE IF NOT EXISTS daily_trending_categories (
  id                  BIGINT       NOT NULL AUTO_INCREMENT,
  trend_date          DATE         NOT NULL,
  category_id         INT          NULL DEFAULT NULL,
  label               VARCHAR(100) NOT NULL,
  slug                VARCHAR(100) NOT NULL,
  origin              ENUM('user_keyword','system')
                                 NOT NULL DEFAULT 'user_keyword',
  search_count        INT          NOT NULL DEFAULT 0,
  unique_client_count INT          NOT NULL DEFAULT 0,
  score               DECIMAL(10,4) NOT NULL DEFAULT 0,
  top_keywords        JSON         NULL DEFAULT NULL,
  computed_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_daily_trending_categories PRIMARY KEY (id),
  CONSTRAINT uq_daily_trending_categories_date_origin_slug
    UNIQUE (trend_date, origin, slug),
  CONSTRAINT fk_daily_trending_categories_category
    FOREIGN KEY (category_id) REFERENCES categories (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_daily_trending_categories_date (trend_date),
  INDEX idx_daily_trending_categories_slug (slug),
  INDEX idx_daily_trending_categories_origin_score (origin, score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Agregasi kategori trending harian dari keyword pencarian user.';

CREATE TABLE IF NOT EXISTS wallpaper_tag_proposals (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  wallpaper_id    CHAR(36)     NOT NULL,
  tag_text        VARCHAR(100) NOT NULL,
  tag_slug        VARCHAR(100) NOT NULL,
  existing_tag_id INT          NULL DEFAULT NULL,
  status          ENUM('pending','approved','discarded')
                                NOT NULL DEFAULT 'pending',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at     DATETIME     NULL DEFAULT NULL,

  CONSTRAINT pk_wallpaper_tag_proposals PRIMARY KEY (id),
  CONSTRAINT uq_wallpaper_tag_proposals_wallpaper_slug
    UNIQUE (wallpaper_id, tag_slug),
  CONSTRAINT fk_wallpaper_tag_proposals_wallpaper
    FOREIGN KEY (wallpaper_id) REFERENCES wallpapers (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_wallpaper_tag_proposals_existing_tag
    FOREIGN KEY (existing_tag_id) REFERENCES tags (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_wallpaper_tag_proposals_wallpaper_id (wallpaper_id),
  INDEX idx_wallpaper_tag_proposals_tag_slug (tag_slug),
  INDEX idx_wallpaper_tag_proposals_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Proposal tag contributor yang diproses saat wallpaper dimoderasi.';
