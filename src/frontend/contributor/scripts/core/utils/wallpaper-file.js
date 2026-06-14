export function validateWallpaperFile(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const maxSize = 10 * 1024 * 1024;

  if (!file) {
    return 'Pilih file wallpaper terlebih dahulu.';
  }

  if (!allowedTypes.includes(file.type)) {
    return 'Format file harus JPG, PNG, atau WebP.';
  }

  if (file.size > maxSize) {
    return 'Ukuran file maksimal 10MB.';
  }

  return '';
}
