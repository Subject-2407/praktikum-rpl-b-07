export async function listContributorWallpapers(wallpaperRepository, filters = {}) {
  return wallpaperRepository.listContributorWallpapers(filters);
}

export async function listContributorWallpaperPage(wallpaperRepository, filters = {}) {
  return wallpaperRepository.listContributorWallpaperPage(filters);
}
