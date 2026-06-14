export async function updateWallpaperMetadata(wallpaperRepository, id, payload) {
  if (!id) {
    throw new Error('Wallpaper tidak valid.');
  }

  return wallpaperRepository.updateMetadata(id, payload);
}
