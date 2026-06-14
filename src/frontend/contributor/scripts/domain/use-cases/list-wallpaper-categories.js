export async function listWallpaperCategories(wallpaperRepository) {
  return wallpaperRepository.getCategories();
}
