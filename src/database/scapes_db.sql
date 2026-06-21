-- ============================================================
-- DATABASE: Scapes
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS scapes
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE scapes;

-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE users (
  id            INT           NOT NULL AUTO_INCREMENT,
  display_name  VARCHAR(100)  NOT NULL,
  email         VARCHAR(255)  NOT NULL,
  password_hash VARCHAR(255)  NOT NULL,
  role          ENUM('contributor', 'admin') NOT NULL,
  is_verified   TINYINT(1)    NOT NULL DEFAULT 0,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Akun terdaftar; mencakup contributor dan admin yang dibedakan via kolom role.';

-- ============================================================
-- 2. email_verifications
-- ============================================================
CREATE TABLE email_verifications (
  id         INT          NOT NULL AUTO_INCREMENT,
  user_id    INT          NOT NULL,
  token      VARCHAR(255) NOT NULL,
  expires_at DATETIME     NOT NULL,
  used_at    DATETIME     NULL DEFAULT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_email_verifications PRIMARY KEY (id),
  CONSTRAINT uq_email_verifications_token UNIQUE (token),
  CONSTRAINT fk_email_verifications_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Token verifikasi email saat registrasi contributor baru.';

-- ============================================================
-- 3. password_resets
-- ============================================================
CREATE TABLE password_resets (
  id         INT          NOT NULL AUTO_INCREMENT,
  user_id    INT          NOT NULL,
  token      VARCHAR(255) NOT NULL,
  expires_at DATETIME     NOT NULL,
  used_at    DATETIME     NULL DEFAULT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_password_resets PRIMARY KEY (id),
  CONSTRAINT uq_password_resets_token UNIQUE (token),
  CONSTRAINT fk_password_resets_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Token reset password; berlaku 24 jam dan hanya bisa dipakai sekali.';

-- ============================================================
-- 4. login_attempts
-- ============================================================
CREATE TABLE login_attempts (
  id           INT          NOT NULL AUTO_INCREMENT,
  identifier   VARCHAR(255) NOT NULL,
  ip_address   VARCHAR(45)  NOT NULL,
  is_success   TINYINT(1)   NOT NULL DEFAULT 0,
  attempted_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_login_attempts PRIMARY KEY (id),

  INDEX idx_login_attempts_identifier (identifier),
  INDEX idx_login_attempts_ip (ip_address),
  INDEX idx_login_attempts_attempted_at (attempted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Log percobaan login untuk deteksi brute-force (maks 5 gagal berturut-turut).';

-- ============================================================
-- 5. categories
-- ============================================================
CREATE TABLE categories (
  id         INT          NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_categories PRIMARY KEY (id),
  CONSTRAINT uq_categories_name UNIQUE (name),
  CONSTRAINT uq_categories_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Kategori wallpaper; slug digunakan sebagai nama sub-folder penyimpanan lokal.';

-- ============================================================
-- 6. tags
-- ============================================================
CREATE TABLE tags (
  id         INT          NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_tags PRIMARY KEY (id),
  CONSTRAINT uq_tags_name UNIQUE (name),
  CONSTRAINT uq_tags_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tag bebas untuk pencarian keyword wallpaper.';

-- ============================================================
-- 7. wallpapers
-- ============================================================
CREATE TABLE wallpapers (
  id             CHAR(36)     NOT NULL,
  contributor_id INT          NOT NULL,
  category_id    INT          NOT NULL,
  title          VARCHAR(255) NOT NULL,
  description    TEXT         NULL DEFAULT NULL,
  file_size_kb   INT          NOT NULL,
  mime_type      VARCHAR(50)  NOT NULL,
  width          INT          NOT NULL,
  height         INT          NOT NULL,
  target_device  ENUM('desktop', 'mobile', 'tablet') NOT NULL,
  status         ENUM('pending', 'approved', 'rejected')
                              NOT NULL DEFAULT 'pending',
  published_at   DATETIME     NULL DEFAULT NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_wallpapers PRIMARY KEY (id),
  CONSTRAINT fk_wallpapers_contributor
    FOREIGN KEY (contributor_id) REFERENCES users (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_wallpapers_category
    FOREIGN KEY (category_id) REFERENCES categories (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_wallpapers_file_size  CHECK (file_size_kb > 0 AND file_size_kb <= 10240),
  CONSTRAINT chk_wallpapers_mime_type  CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/webp')),
  CONSTRAINT chk_wallpapers_resolution CHECK (
    (target_device = 'desktop' AND width >= 1920 AND height >= 1080)
    OR (target_device = 'mobile' AND width >= 360 AND height >= 800)
    OR (target_device = 'tablet' AND width >= 768 AND height >= 1024)
  ),

  INDEX idx_wallpapers_contributor_id (contributor_id),
  INDEX idx_wallpapers_category_id (category_id),
  INDEX idx_wallpapers_target_device (target_device),
  INDEX idx_wallpapers_status (status),
  INDEX idx_wallpapers_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Entitas utama konten; menyimpan metadata wallpaper, target perangkat, dan status moderasi.';

-- ============================================================
-- 8. wallpaper_tags  (junction table - relasi m:n)
-- ============================================================
CREATE TABLE wallpaper_tags (
  wallpaper_id CHAR(36) NOT NULL,
  tag_id       INT      NOT NULL,

  CONSTRAINT pk_wallpaper_tags PRIMARY KEY (wallpaper_id, tag_id),
  CONSTRAINT fk_wallpaper_tags_wallpaper
    FOREIGN KEY (wallpaper_id) REFERENCES wallpapers (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_wallpaper_tags_tag
    FOREIGN KEY (tag_id) REFERENCES tags (id)
    ON DELETE CASCADE ON UPDATE CASCADE,

  INDEX idx_wallpaper_tags_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabel jembatan many-to-many antara wallpapers dan tags.';

-- ============================================================
-- 9. moderation_reviews
-- ============================================================
CREATE TABLE moderation_reviews (
  id           INT          NOT NULL AUTO_INCREMENT,
  wallpaper_id CHAR(36)     NOT NULL,
  admin_id     INT          NOT NULL,
  decision     ENUM('approved', 'rejected') NOT NULL,
  reason       TEXT         NULL DEFAULT NULL,
  reviewed_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_moderation_reviews PRIMARY KEY (id),
  CONSTRAINT fk_moderation_reviews_wallpaper
    FOREIGN KEY (wallpaper_id) REFERENCES wallpapers (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_moderation_reviews_admin
    FOREIGN KEY (admin_id) REFERENCES users (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  INDEX idx_moderation_reviews_wallpaper_id (wallpaper_id),
  INDEX idx_moderation_reviews_admin_id (admin_id),
  INDEX idx_moderation_reviews_decision (decision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Riwayat keputusan moderasi; reason wajib diisi di application layer saat decision=rejected.';

-- ============================================================
-- 10. api_sources
-- ============================================================
CREATE TABLE api_sources (
  id         INT          NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  base_url   VARCHAR(500) NOT NULL,
  is_default TINYINT(1)   NOT NULL DEFAULT 0,
  is_active  TINYINT(1)   NOT NULL DEFAULT 1,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_api_sources PRIMARY KEY (id),
  CONSTRAINT uq_api_sources_name UNIQUE (name),
  CONSTRAINT uq_api_sources_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Master sumber wallpaper eksternal: Scapes (default), Pexels, Unsplash, Pixabay.';

-- ============================================================
-- 11. audit_logs
-- ============================================================
CREATE TABLE audit_logs (
  id          INT          NOT NULL AUTO_INCREMENT,
  user_id     INT          NULL DEFAULT NULL,
  action      VARCHAR(100) NOT NULL,
  entity_type VARCHAR(100) NULL DEFAULT NULL,
  entity_id   VARCHAR(36)  NULL DEFAULT NULL,
  meta        JSON         NULL DEFAULT NULL,
  ip_address  VARCHAR(45)  NULL DEFAULT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_audit_logs PRIMARY KEY (id),
  CONSTRAINT fk_audit_logs_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_audit_logs_user_id (user_id),
  INDEX idx_audit_logs_action (action),
  INDEX idx_audit_logs_entity (entity_type, entity_id),
  INDEX idx_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Jejak audit aktivitas penting; user_id NULL untuk aksi sistem otomatis.';

-- ============================================================
-- 12. search_logs
-- ============================================================
CREATE TABLE search_logs (
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

-- ============================================================
-- 13. daily_trending_categories
-- ============================================================
CREATE TABLE daily_trending_categories (
  id                  BIGINT        NOT NULL AUTO_INCREMENT,
  trend_date          DATE          NOT NULL,
  category_id         INT           NULL DEFAULT NULL,
  label               VARCHAR(100)  NOT NULL,
  slug                VARCHAR(100)  NOT NULL,
  origin              ENUM('user_keyword','system')
                                  NOT NULL DEFAULT 'user_keyword',
  search_count        INT           NOT NULL DEFAULT 0,
  unique_client_count INT           NOT NULL DEFAULT 0,
  score               DECIMAL(10,4) NOT NULL DEFAULT 0,
  top_keywords        JSON          NULL DEFAULT NULL,
  computed_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

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

-- ============================================================
-- 14. wallpaper_tag_proposals
-- ============================================================
CREATE TABLE wallpaper_tag_proposals (
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

-- ============================================================
-- SEED DATA
-- ============================================================

-- Sumber wallpaper (api_sources)
INSERT INTO api_sources (name, slug, base_url, is_default, is_active) VALUES
  ('Scapes',   'scapes',   'https://api.scapes.app/v1',          1, 1),
  ('Unsplash', 'unsplash', 'https://api.unsplash.com',           0, 1),
  ('Pexels',   'pexels',   'https://api.pexels.com/v1',          0, 1),
  ('Pixabay',  'pixabay',  'https://pixabay.com/api',            0, 1);

-- Kategori dasar
INSERT INTO categories (name, slug) VALUES
  ('Minimalist',  'minimalist'),
  ('Nature',      'nature'),
  ('Abstract',    'abstract'),
  ('Architecture','architecture'),
  ('Dark',        'dark'),
  ('Space',       'space'),
  ('Anime',       'anime'),
  ('Technology',  'technology');

-- Tag dasar
INSERT INTO tags (name, slug) VALUES
  ('dark',       'dark'),
  ('light',      'light'),
  ('neon',       'neon'),
  ('pastel',     'pastel'),
  ('4k',         '4k'),
  ('monochrome', 'monochrome'),
  ('colorful',   'colorful'),
  ('gradient',   'gradient'),
  ('retro',      'retro'),
  ('futuristic', 'futuristic');

