-- Update wallpaper resolution checks to match target_device-specific standards.
-- Run this on databases created before the target-specific validation change.

ALTER TABLE wallpapers DROP CONSTRAINT chk_wallpapers_resolution IF EXISTS;

ALTER TABLE wallpapers
  ADD CONSTRAINT chk_wallpapers_resolution CHECK (
    (target_device = 'desktop' AND width >= 1920 AND height >= 1080)
    OR (target_device = 'mobile' AND width >= 360 AND height >= 800)
    OR (target_device = 'tablet' AND width >= 768 AND height >= 1024)
  );
