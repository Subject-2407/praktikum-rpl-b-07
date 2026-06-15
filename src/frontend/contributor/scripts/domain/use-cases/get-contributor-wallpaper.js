export async function getContributorWallpaper(wallpaperRepository, id) {
  if (!id) {
    return null;
  }

  return wallpaperRepository.getWallpaper(id);
}
