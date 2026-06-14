-- Sync wallpaper constraints with target_device-specific resolution standards.
-- Safe to rerun on MariaDB 10.6+ and MySQL 8.x.

SET @schema_name = DATABASE();
SET @drop_check_clause = IF(
  VERSION() LIKE '%MariaDB%',
  'DROP CONSTRAINT',
  'DROP CHECK'
);

SET @drop_chk_wallpapers_width = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = @schema_name
      AND TABLE_NAME = 'wallpapers'
      AND CONSTRAINT_NAME = 'chk_wallpapers_width'
  ),
  CONCAT('ALTER TABLE wallpapers ', @drop_check_clause, ' chk_wallpapers_width'),
  'SELECT ''chk_wallpapers_width not present'''
);
PREPARE stmt FROM @drop_chk_wallpapers_width;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_chk_wallpapers_height = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = @schema_name
      AND TABLE_NAME = 'wallpapers'
      AND CONSTRAINT_NAME = 'chk_wallpapers_height'
  ),
  CONCAT('ALTER TABLE wallpapers ', @drop_check_clause, ' chk_wallpapers_height'),
  'SELECT ''chk_wallpapers_height not present'''
);
PREPARE stmt FROM @drop_chk_wallpapers_height;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_chk_wallpapers_resolution = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = @schema_name
      AND TABLE_NAME = 'wallpapers'
      AND CONSTRAINT_NAME = 'chk_wallpapers_resolution'
  ),
  CONCAT(
    'ALTER TABLE wallpapers ',
    @drop_check_clause,
    ' chk_wallpapers_resolution'
  ),
  'SELECT ''chk_wallpapers_resolution not present'''
);
PREPARE stmt FROM @drop_chk_wallpapers_resolution;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE wallpapers
  ADD CONSTRAINT chk_wallpapers_resolution CHECK (
    (target_device = 'desktop' AND width >= 1920 AND height >= 1080)
    OR (target_device = 'mobile' AND width >= 360 AND height >= 800)
    OR (target_device = 'tablet' AND width >= 768 AND height >= 1024)
  );
