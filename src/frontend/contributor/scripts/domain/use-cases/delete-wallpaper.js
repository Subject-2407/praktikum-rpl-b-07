export async function deleteWallpaper(wallpaperRepository, id) {
  if (!id) {
    throw new Error('Wallpaper tidak valid.');
  }

  return wallpaperRepository.delete(id);
}
